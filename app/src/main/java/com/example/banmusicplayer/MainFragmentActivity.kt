package com.example.banmusicplayer

import android.app.AlertDialog
import android.app.Instrumentation
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RadioGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.example.banmusicplayer.base.StaticInfo
import com.example.banmusicplayer.server.MusicServer
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*


class MainFragmentActivity : MyBaseActivity() {
    private var menu: Menu? = null
    private var group: RadioGroup? = null
    private var fm: FragmentManager? = null
    private var ft: FragmentTransaction? = null
    private var proDialog: ProgressDialog? = null
    private var leftFrament: MainActivity? = null
    private var rightFragment: MyMusicListFragment? = null
    private var fragmentPos = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.fragementactivity_main)
        alertDialog = AlertDialog.Builder(this)

        inti()
        proDialog = ProgressDialog(this)
        leftFrament = MainActivity()
        rightFragment = MyMusicListFragment()
        fm = supportFragmentManager
        defaultCheckedFragment()
        group = findViewById<View>(R.id.my_files_menu) as RadioGroup
        group!!.setOnCheckedChangeListener { group, checkedId ->
            YoYo.with(Techniques.Swing).duration(1000)
                    .playOn(findViewById(checkedId))
            when (checkedId) {
                R.id.group1 -> {
                    group.findViewById<View>(R.id.group1).setBackgroundColor(
                            -0x11ff0012)
                    group.findViewById<View>(R.id.group2).setBackgroundColor(
                            -0x66220012)
                    defaultCheckedFragment()
                }
                R.id.group2 -> {
                    group.findViewById<View>(R.id.group2).setBackgroundColor(
                            -0x11ff0012)
                    group.findViewById<View>(R.id.group1).setBackgroundColor(
                            -0x66110023)
                    fragmentPos = 1
                    ft = fm!!.beginTransaction()
                    ft!!.replace(R.id.fragment_layout, rightFragment!!).commit()
                }
            }
            onCreateOptionsMenu(menu)
        }
        //初始化？
        onCreateOptionsMenu(menu)
    }

    private fun defaultCheckedFragment() {
        fragmentPos = 0
        ft = fm!!.beginTransaction()
        ft!!.replace(R.id.fragment_layout, leftFrament!!).commit()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu
        if (menu != null) {
            menu.clear()
        } else {
            return true
        }
        if (fragmentPos == 0) {
            menuInflater.inflate(R.menu.main, menu)
        } else {
            menuInflater.inflate(R.menu.main2, menu)
        }
        if (menu.javaClass.simpleName == "MenuBuilder") {
            try {
                val m = menu.javaClass.getDeclaredMethod(
                        "setOptionalIconsVisible", Boolean::class.javaPrimitiveType)
                m.isAccessible = true
                m.invoke(menu, true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_about) {
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("关于").setView(StaticInfo.getAboutContent(this))
            dialog.setNegativeButton(R.string.Sys_confirm) { dialog, which -> dialog.cancel() }.show()
            return super.onOptionsItemSelected(item)
        } else if (item.itemId == R.id.action_add_list) {
            rightFragment!!.addList()
            return super.onOptionsItemSelected(item)
        }
        proDialog!!.setMessage("正在载入音乐...")
        proDialog!!.setCancelable(false)
        proDialog!!.isIndeterminate = true
        proDialog!!.show()
        val musicPath = ArrayList<String>()
        if (fragmentPos == 0) {
            for (list in leftFrament!!.musicCatalog.values) {
                for (file in list) {
                    musicPath.add(file.absolutePath)
                }
            }
        } else {
        }
        val serviceIntent = Intent(this, MusicServer::class.java)
        serviceIntent.putStringArrayListExtra("musicPath", musicPath)
        val intent = Intent(this, MusicPlayerActivity::class.java)
        when (item.itemId) {
            R.id.action_play_all -> intent.putExtra("position", 0)
            R.id.action_random_play_all -> {
                val rd = (Math.random() * musicPath.size).toInt()
                intent.putExtra("position", rd)
                intent.putExtra("isRandom", true)
                serviceIntent.putExtra("isRandom", true)
            }
        }
        startService(serviceIntent)
        startActivity(intent)
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        if (proDialog!!.isShowing) {
            proDialog!!.dismiss()
        }
        super.onResume()
    }

    override fun onRestart() {
        if (fragmentPos == 0) {
            ft!!.show(leftFrament!!)
        } else {
            ft!!.show(rightFragment!!)
        }
        super.onRestart()
    }

    override fun onStop() {
        if (fragmentPos == 0) {
            ft!!.hide(leftFrament!!)
        } else {
            ft!!.hide(rightFragment!!)
        }
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        StaticInfo.db.close()
    }

    override fun initView() {
    }
    override fun initButton() {
        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            GlobalScope.launch {
                Instrumentation().sendKeyDownUpSync(82)
            }
        }
    }

    override fun initListView() {}
}