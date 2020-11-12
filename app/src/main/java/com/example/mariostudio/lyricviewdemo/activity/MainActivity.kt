package com.example.mariostudio.lyricviewdemo.activityimport android.animation.Animatorimport android.animation.AnimatorListenerAdapterimport android.animation.ValueAnimatorimport android.annotation.TargetApiimport android.graphics.Colorimport android.media.MediaPlayerimport android.media.MediaPlayer.*import android.os.Buildimport android.os.Bundleimport android.os.Handlerimport android.os.Messageimport android.view.Viewimport android.view.ViewStubimport android.view.WindowManagerimport android.widget.ImageViewimport android.widget.SeekBarimport android.widget.SeekBar.OnSeekBarChangeListenerimport android.widget.TextViewimport android.widget.Toastimport androidx.appcompat.app.AppCompatActivityimport com.example.banmusicplayer.Rimport com.example.mariostudio.lyricviewdemo.LyricViewimport com.example.mariostudio.lyricviewdemo.LyricView.OnPlayerClickListenerimport com.example.mariostudio.lyricviewdemo.util.PreferenceUtilimport com.example.mariostudio.lyricviewdemo.view.CustomRelativeLayoutimport com.example.mariostudio.lyricviewdemo.view.CustomSettingViewimport com.example.mariostudio.lyricviewdemo.view.CustomSettingView.OnColorItemChangeListenerimport com.nineoldandroids.view.ViewHelperimport java.io.Fileimport java.io.IOExceptionimport java.text.DecimalFormatimport kotlin.random.Randomclass MainActivity : AppCompatActivity(), View.OnClickListener, OnBufferingUpdateListener, OnPreparedListener, OnCompletionListener, OnSeekBarChangeListener, OnPlayerClickListener {    private var lyricView: LyricView? = null    private var mediaPlayer: MediaPlayer? = null    private var statueBar: View? = null    private var display_seek: SeekBar? = null    private var display_total: TextView? = null    private var display_title: TextView? = null    private var display_position: TextView? = null    private var btnPre: ImageView? = null    private var btnPlay: ImageView? = null    private var btnNext: ImageView? = null    private var btnSetting: ImageView? = null    private var btnMode: ImageView? = null    private var song_urls: Array<String>? = null    private var song_names: Array<String?>? = null    private var position = 0    private var mode = 0 //0:顺序循环，1:随机循环,2：单曲循环    private var currentState = State.STATE_STOP    private var press_animator: ValueAnimator? = null    private var up_animator: ValueAnimator? = null    private var setting_layout: ViewStub? = null    private var customSettingView: CustomSettingView? = null    private var customRelativeLayout: CustomRelativeLayout? = null    private val MSG_REFRESH = 0x167    private val MSG_LOADING = 0x177    private val MSG_LYRIC_SHOW = 0x187    private val animatorDuration: Long = 120    override fun onCreate(savedInstanceState: Bundle?) {        super.onCreate(savedInstanceState)        setContentView(R.layout.activity_music_player_new)        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {            setTranslucentStatus()        }        initAllViews()        initAllDatum()    }    override fun onDestroy() {        stop()        super.onDestroy()    }    @TargetApi(19)    private fun setTranslucentStatus() {        val window = window        val params = window.attributes        val status = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS        params.flags = params.flags or status        window.attributes = params    }    val statusBarHeight: Int        get() {            var result = 0            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")            if (resourceId > 0) {                result = resources.getDimensionPixelSize(resourceId)            }            return result        }    private fun initAllViews() {        statueBar = findViewById(R.id.statue_bar)        statueBar!!.layoutParams.height = statusBarHeight        display_title = findViewById<View>(R.id.title_view) as TextView        display_position = findViewById<View>(android.R.id.text1) as TextView        display_total = findViewById<View>(android.R.id.text2) as TextView        display_seek = findViewById<View>(android.R.id.progress) as SeekBar        display_seek!!.setOnSeekBarChangeListener(this)        btnNext = findViewById<View>(android.R.id.button3) as ImageView        btnPlay = findViewById<View>(android.R.id.button2) as ImageView        btnPre = findViewById<View>(android.R.id.button1) as ImageView        btnSetting = findViewById<View>(R.id.action_setting) as ImageView        btnMode = findViewById<View>(R.id.action_mode) as ImageView        btnSetting!!.setOnClickListener(this)        btnPlay!!.setOnClickListener(this)        btnNext!!.setOnClickListener(this)        btnPre!!.setOnClickListener(this)        btnMode!!.setOnClickListener(this)        lyricView = findViewById<View>(R.id.lyric_view) as LyricView        lyricView!!.setOnPlayerClickListener(this)        lyricView!!.setLineSpace(PreferenceUtil.getInstance(this@MainActivity).getFloat(PreferenceUtil.KEY_TEXT_SIZE, 12.0f))        lyricView!!.setTextSize(PreferenceUtil.getInstance(this@MainActivity).getFloat(PreferenceUtil.KEY_TEXT_SIZE, 15.0f))        lyricView!!.setHighLightTextColor(PreferenceUtil.getInstance(this@MainActivity).getInt(PreferenceUtil.KEY_HIGHLIGHT_COLOR, Color.parseColor("#4FC5C7")))        setting_layout = findViewById<View>(R.id.main_setting_layout) as ViewStub    }    private fun initAllDatum() {//        song_lyrics = getResources().getStringArray(R.array.song_lyrics);//        song_names = getResources().getStringArray(R.array.song_names);//        song_urls = getResources().getStringArray(R.array.song_urls);        val i = intent        position = i.getIntExtra("position", 0)        val mp = i.getStringArrayListExtra("musicPath")        song_urls = mp.toTypedArray()        song_names = arrayOfNulls(mp.size)        val sb = StringBuilder()        for (j in mp.indices) {            sb.setLength(0) //清除？            val spl = song_urls!![j].split("/".toRegex()).toTypedArray()            val spl2 = spl.last().split("\\.".toRegex()).toTypedArray()            for (k in 0 until spl2.size - 1) {                sb.append(spl2[k])            }            song_names!![j] = sb.toString()        }        if (i.getBooleanExtra("isRandom", false)) {            mode = 1            btnMode!!.setImageResource(R.mipmap.ic_menu_share)            btnPre!!.visibility = View.INVISIBLE            btnNext!!.visibility = View.VISIBLE        }        song_urls?.run {            if (size == 1) {                mode = 2                btnMode!!.visibility = View.GONE                btnPre!!.visibility = View.INVISIBLE                btnNext!!.visibility = View.INVISIBLE            }        }        mediaPlayerSetup() // 准备    }    /**     * 准备     */    private fun mediaPlayerSetup() {        display_title!!.text = song_names!![position]        display_title!!.setSelected(true);//让其滚动起来        handler.removeMessages(MSG_LYRIC_SHOW)        handler.sendEmptyMessageDelayed(MSG_LYRIC_SHOW, 420)    }    /**     * 停止     */    private fun stop() {        if (null != mediaPlayer) {            mediaPlayer!!.stop()            mediaPlayer!!.release()            mediaPlayer = null        }        handler.removeMessages(MSG_REFRESH)        lyricView!!.reset("载入歌词ing...")        setCurrentState(State.STATE_STOP)    }    /**     * 暂停     */    private fun pause() {        if (mediaPlayer != null && currentState == State.STATE_PLAYING) {            setCurrentState(State.STATE_PAUSE)            mediaPlayer!!.pause()            handler.removeMessages(MSG_REFRESH)        }    }    /**     * 开始     */    private fun start() {        if (mediaPlayer != null && (currentState == State.STATE_PAUSE || currentState == State.STATE_PREPARE)) {            setCurrentState(State.STATE_PLAYING)            mediaPlayer!!.start()            handler.sendEmptyMessage(MSG_REFRESH)        }    }    /**     * 上一首     */    private fun previous() {        stop()        when (mode) {            0 -> {                position--                if (position < 0) {                    position = song_urls!!.size - 1                }            }        }        mediaPlayerSetup()    }    /**     * 下一首     */    private operator fun next() {        stop()        when (mode) {            0 -> {                position++                if (position >= song_urls!!.size) {                    position = 0                }            }            1 -> position = Random.nextInt(song_urls!!.size)        }        mediaPlayerSetup()    }    override fun onPrepared(mediaPlayer: MediaPlayer) {        setCurrentState(State.STATE_PREPARE)        val format = DecimalFormat("00")        display_seek!!.max = mediaPlayer.duration        display_total!!.text = format.format(mediaPlayer.duration / 1000 / 60.toLong()) + ":" + format.format(mediaPlayer.duration / 1000 % 60.toLong())        start()    }    override fun onBufferingUpdate(mediaPlayer: MediaPlayer, percent: Int) {        display_seek!!.secondaryProgress = (mediaPlayer.duration * 1.00f * percent / 100.0f).toInt()    }    override fun onCompletion(mediaPlayer: MediaPlayer) {        next()    }    /**     * 设置当前播放状态     */    private fun setCurrentState(state: State) {        if (state == currentState) {            return        }        currentState = state        when (state) {            State.STATE_PAUSE -> btnPlay!!.setImageResource(R.mipmap.m_icon_player_play_normal)            State.STATE_PLAYING -> btnPlay!!.setImageResource(R.mipmap.m_icon_player_pause_normal)            State.STATE_PREPARE -> {                if (lyricView != null) {                    lyricView!!.setPlayable(true)                }                setLoading(false)            }            State.STATE_STOP -> {                if (lyricView != null) {                    lyricView!!.setPlayable(false)                }                display_position!!.text = "--:--"                display_seek!!.secondaryProgress = 0                display_seek!!.progress = 0                display_seek!!.max = 100                btnPlay!!.setImageResource(R.mipmap.m_icon_player_play_normal)                setLoading(false)            }            State.STATE_SETUP -> {                //直接改音乐文件路径的后缀，默认歌词文件存在同目录下                val spl = song_urls!![position].split("\\.".toRegex()).toTypedArray()                val sb = StringBuilder()                var i = 0                while (i < spl.size - 1) {                    sb.append(spl[i])                    sb.append(".")                    i++                }                sb.append("lrc")                val file = File(sb.toString())                if (file.exists()) {                    lyricView!!.setLyricFile(file)                } else {                    lyricView!!.setLyricFile(null)                }                btnPlay!!.setImageResource(R.mipmap.m_icon_player_play_normal)                setLoading(true)            }            else -> {            }        }    }    var handler: Handler = object : Handler() {        override fun handleMessage(msg: Message) {            super.handleMessage(msg)            when (msg.what) {                MSG_REFRESH -> {                    if (mediaPlayer != null) {                        if (!display_seek!!.isPressed) {                            lyricView!!.setCurrentTimeMillis(mediaPlayer!!.currentPosition.toLong())                            val format = DecimalFormat("00")                            display_seek!!.progress = mediaPlayer!!.currentPosition                            display_position!!.text = format.format(mediaPlayer!!.currentPosition / 1000 / 60.toLong()) + ":" + format.format(mediaPlayer!!.currentPosition / 1000 % 60.toLong())                        }                    }                    sendEmptyMessageDelayed(MSG_REFRESH, 120)                }                MSG_LYRIC_SHOW -> try {                    setCurrentState(State.STATE_SETUP)                    mediaPlayer = MediaPlayer()                    mediaPlayer!!.setOnPreparedListener(this@MainActivity)                    mediaPlayer!!.setOnCompletionListener(this@MainActivity)                    mediaPlayer!!.setOnBufferingUpdateListener(this@MainActivity)                    mediaPlayer!!.setDataSource(song_urls!![position])                    mediaPlayer!!.prepareAsync()                } catch (e: IOException) {                    e.printStackTrace()                }                MSG_LOADING -> {                    val background = btnPlay!!.background                    var level = background.level                    level = level + 300                    if (level > 10000) {                        level = level - 10000                    }                    background.level = level                    sendEmptyMessageDelayed(MSG_LOADING, 50)                }                else -> {                }            }        }    }    private var mLoading = false    private fun setLoading(loading: Boolean) {        if (loading && !mLoading) {            btnPlay!!.setBackgroundResource(R.drawable.rotate_player_loading)            handler.sendEmptyMessageDelayed(MSG_LOADING, 200)            mLoading = true            return        }        if (!loading && mLoading) {            handler.removeMessages(MSG_LOADING)            btnPlay!!.setBackgroundColor(Color.TRANSPARENT)            mLoading = false            return        }    }    override fun onPlayerClicked(progress: Long, content: String) {        if (mediaPlayer != null && (currentState == State.STATE_PLAYING || currentState == State.STATE_PAUSE)) {            mediaPlayer!!.seekTo(progress.toInt())            if (currentState == State.STATE_PAUSE) {                start()            }        }    }    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {        if (fromUser) {            val format = DecimalFormat("00")            display_position!!.text = format.format(progress / 1000 / 60.toLong()) + ":" + format.format(progress / 1000 % 60.toLong())        }    }    override fun onStartTrackingTouch(seekBar: SeekBar) {        handler.removeMessages(MSG_REFRESH)    }    override fun onStopTrackingTouch(seekBar: SeekBar) {        mediaPlayer!!.seekTo(seekBar.progress)        handler.sendEmptyMessageDelayed(MSG_REFRESH, 120)    }    override fun onClick(view: View) {        if (press_animator != null && press_animator!!.isRunning) {            press_animator!!.cancel()        }        if (up_animator != null && up_animator!!.isRunning) {            up_animator!!.cancel()        }        when (view.id) {            android.R.id.button1 -> previous()            android.R.id.button2 -> {                if (currentState == State.STATE_PAUSE) {                    start()                    return                }                if (currentState == State.STATE_PLAYING) {                    pause()                }            }            android.R.id.button3 -> next()            R.id.action_setting -> {                if (customRelativeLayout == null) {                    customRelativeLayout = setting_layout!!.inflate() as CustomRelativeLayout                    initCustomSettingView()                }                customRelativeLayout!!.show()            }            R.id.action_mode -> {                mode = (mode + 1) % 3                when (mode) {                    0 -> {                        btnMode!!.setImageResource(R.mipmap.ic_menu_refresh)                        btnPre!!.visibility = View.VISIBLE                        btnNext!!.visibility = View.VISIBLE                        Toast.makeText(this, "播放模式切换为:顺序播放", Toast.LENGTH_SHORT).show()                    }                    1 -> {                        btnMode!!.setImageResource(R.mipmap.ic_menu_share)                        btnPre!!.visibility = View.INVISIBLE                        btnNext!!.visibility = View.VISIBLE                        Toast.makeText(this, "播放模式切换为:全部循环", Toast.LENGTH_SHORT).show()                    }                    2 -> {                        btnPre!!.visibility = View.INVISIBLE                        btnNext!!.visibility = View.INVISIBLE                        btnMode!!.setImageResource(R.mipmap.ic_menu_revert)                        Toast.makeText(this, "播放模式切换为:单曲循环", Toast.LENGTH_SHORT).show()                    }                }            }            else -> {            }        }        press_animator = pressAnimator(view)        press_animator!!.start()    }    private fun initCustomSettingView() {        customSettingView = customRelativeLayout!!.getChildAt(0) as CustomSettingView        customSettingView!!.setOnTextSizeChangeListener(TextSizeChangeListener())        customSettingView!!.setOnColorItemChangeListener(ColorItemClickListener())        customSettingView!!.setOnDismissBtnClickListener(DismissBtnClickListener())        customSettingView!!.setOnLineSpaceChangeListener(LineSpaceChangeListener())    }    private inner class TextSizeChangeListener : OnSeekBarChangeListener {        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {            if (fromUser) {                lyricView!!.setTextSize(15.0f + 3 * progress / 100.0f)            }        }        override fun onStartTrackingTouch(seekBar: SeekBar) {}        override fun onStopTrackingTouch(seekBar: SeekBar) {            PreferenceUtil.getInstance(this@MainActivity).putFloat(PreferenceUtil.KEY_TEXT_SIZE, 15.0f + 3 * seekBar.progress / 100.0f)        }    }    private inner class LineSpaceChangeListener : OnSeekBarChangeListener {        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {            if (fromUser) {                lyricView!!.setLineSpace(12.0f + 3 * progress / 100.0f)            }        }        override fun onStartTrackingTouch(seekBar: SeekBar) {}        override fun onStopTrackingTouch(seekBar: SeekBar) {            PreferenceUtil.getInstance(this@MainActivity).putFloat(PreferenceUtil.KEY_LINE_SPACE, 12.0f + 3 * seekBar.progress / 100.0f)        }    }    private inner class DismissBtnClickListener : View.OnClickListener {        override fun onClick(view: View) {            if (customRelativeLayout != null) {                customRelativeLayout!!.dismiss()            }        }    }    private inner class ColorItemClickListener : OnColorItemChangeListener {        override fun onColorChanged(color: Int) {            lyricView!!.setHighLightTextColor(color)            PreferenceUtil.getInstance(this@MainActivity).putInt(PreferenceUtil.KEY_HIGHLIGHT_COLOR, color)            if (customRelativeLayout != null) {                customRelativeLayout!!.dismiss()            }        }    }    fun pressAnimator(view: View): ValueAnimator {        val size = view.scaleX        val animator = ValueAnimator.ofFloat(size, size * 0.7f)        animator.addUpdateListener { animation ->            ViewHelper.setScaleX(view, (animation.animatedValue as Float))            ViewHelper.setScaleY(view, (animation.animatedValue as Float))        }        animator.addListener(object : AnimatorListenerAdapter() {            override fun onAnimationEnd(animation: Animator) {                super.onAnimationEnd(animation)                ViewHelper.setScaleX(view, size * 0.7f)                ViewHelper.setScaleY(view, size * 0.7f)                up_animator = upAnimator(view)                up_animator!!.start()            }            override fun onAnimationCancel(animation: Animator) {                super.onAnimationCancel(animation)                ViewHelper.setScaleX(view, size * 0.7f)                ViewHelper.setScaleY(view, size * 0.7f)            }        })        animator.duration = animatorDuration        return animator    }    fun upAnimator(view: View): ValueAnimator {        val size = view.scaleX        val animator = ValueAnimator.ofFloat(size, size * 10 / 7.00f)        animator.addUpdateListener { animation ->            ViewHelper.setScaleX(view, (animation.animatedValue as Float))            ViewHelper.setScaleY(view, (animation.animatedValue as Float))        }        animator.addListener(object : AnimatorListenerAdapter() {            override fun onAnimationStart(animation: Animator) {                super.onAnimationStart(animation)            }            override fun onAnimationEnd(animation: Animator) {                super.onAnimationEnd(animation)                ViewHelper.setScaleX(view, size * 10 / 7.00f)                ViewHelper.setScaleY(view, size * 10 / 7.00f)            }            override fun onAnimationCancel(animation: Animator) {                super.onAnimationCancel(animation)                ViewHelper.setScaleX(view, size * 10 / 7.00f)                ViewHelper.setScaleY(view, size * 10 / 7.00f)            }        })        animator.duration = animatorDuration        return animator    }    private enum class State {        STATE_STOP, STATE_SETUP, STATE_PREPARE, STATE_PLAYING, STATE_PAUSE    }}