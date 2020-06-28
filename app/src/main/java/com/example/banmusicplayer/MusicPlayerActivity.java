package com.example.banmusicplayer;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.Html;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.example.banmusicplayer.server.MusicServer;
import com.example.banmusicplayer.server.MusicServer.MusicInfoReCall;
import com.example.banmusicplayer.server.MusicServer.MyBinder;

public class MusicPlayerActivity extends Activity implements
		OnCompletionListener, OnSeekBarChangeListener, OnTouchListener,
		MusicInfoReCall {
	private MediaPlayer mediaPlayer;
	private ImageView playButton, albumart, preMusicButton;
	private TextView currentTime;
	private TextView endTime;
	private TextView subtitle;
	private ScrollView subtitleScroller;
	private SeekBar bar;
	private CheckBox random, playmode;
	private Notification notification;
	private NotificationManager manager;
	private ProgressDialog proDialog;

	private int subtitlePosition, subtitleRowCount, subtitleRowMoveSize,
			subtitleSpaceCount;
	private int musicDuration;
	private boolean doUpdatePlayTime = true, initSubtitle;
	private ArrayList<String> subtitleContent;
	private ArrayList<Double> subtitleTime;

	private Handler handler = new Handler();
	private MyBinder binder;
	private MyServiceConnection connection = new MyServiceConnection();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// requestWindowFeature(Window.FEATURE_NO_TITLE); 隐藏标题
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_music_player);

		proDialog = new ProgressDialog(this);
		proDialog.setMessage("正在载入音乐...");
		proDialog.setCancelable(false);
		proDialog.setIndeterminate(true);
		proDialog.show();

		// 绑定service
		bindService(new Intent(this, MusicServer.class), connection,
				BIND_DEBUG_UNBIND);
	}

	class MyServiceConnection implements ServiceConnection {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			binder = (MyBinder) service;
			System.out.println(binder.toString());
			init();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {

		}
	}

	private void init() {
		Intent intent = this.getIntent();
		boolean isRandom = intent.getBooleanExtra("isRandom", false);

		mediaPlayer = binder.getMediaPlayer();
		manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		currentTime = (TextView) findViewById(R.id.my_music_start_time);
		endTime = (TextView) findViewById(R.id.my_music_end_time);
		albumart = (ImageView) findViewById(R.id.my_albumart);

		// 时间进度
		bar = (SeekBar) findViewById(R.id.my_music_seekbar);
		bar.setOnSeekBarChangeListener(this);

		playButton = (ImageView) findViewById(R.id.music_play_button);
		playButton.setImageResource(R.drawable.ic_media_pause);

		// 字幕设置
		subtitle = (TextView) findViewById(R.id.music_subtitle_textview);
		Log.v("subtitle.getTextSize();", subtitle.getTextSize()+"");
		Log.v("subtitle.getTextSize();", subtitle.getTextSize()+"");

		subtitleScroller = (ScrollView) findViewById(R.id.music_subtitle_scrollview);
		subtitleScroller.setOnTouchListener(this);
		subtitleSpaceCount = subtitleScroller.getHeight() / 2
				/ subtitle.getLineHeight();

		preMusicButton = (ImageView) findViewById(R.id.music_pre_button);
		playmode = (CheckBox) findViewById(R.id.my_button_playmode);
		random = (CheckBox) findViewById(R.id.my_button_random);
		random.setChecked(isRandom);
		if (isRandom) {
			preMusicButton.setVisibility(View.INVISIBLE);
		}

		handler.postDelayed(new UpdataCurrentTimeShow(), 300);
		binder.setCall(this);
		playNewMusic();
	}

	private void setSubtitle() {
		subtitlePosition = 0;

		subtitleTime = binder.getSubtitleTime();
		subtitleContent = binder.getSubtitleContent();
		subtitleRowCount = subtitleContent.size();

		setHighLight(0);
	}

	private void setHighLight(int subPosition) {
		String subtitleText = "";
		for (int i = 0; i < subtitleSpaceCount; i++) {
			subtitleText += "<br/>";
		}

		for (int i = 0; i < subtitleContent.size(); i++) {
			if (i != subPosition || subtitleContent.size() == 1) {
				subtitleText += subtitleContent.get(i) + "<br/>";
			} else {
				subtitleText += "<font color=\"#A671B1\"><B>"
						+ subtitleContent.get(i) + "</B></font><br/>";
			}
		}

		for (int i = 0; i < subtitleSpaceCount; i++) {
			subtitleText += "<br/>";
		}

		subtitle.setText(Html.fromHtml(subtitleText));
	}

	/**
	 * 设置音乐封面
	 */
	private void setAlbumart() {
		Bitmap album = binder.getAlbum();
		if (album != null) {
			albumart.setBackgroundColor(0x00000000);
			albumart.setImageBitmap(album);
		} else {
			albumart.setBackgroundResource(R.drawable.albumart_mp_unknown);
			albumart.setImageDrawable(null);
		}
		albumart.setAlpha(80);
	}

	/**
	 * 用于更新字幕
	 * 
	 * @author 班克威
	 * 
	 */
	class UpdataCurrentTimeShow implements Runnable {
		@Override
		public void run() {
			if (doUpdatePlayTime) {
				setSeekBarProgress();
			}
			System.out.println("updateTime" + subtitleContent.size());
			handler.postDelayed(this, 1000);
		}
	}

	@Override
	protected void onDestroy() {
		doUpdatePlayTime = false;
		handler.removeMessages(0);
		unbindService(connection);
		manager.cancel(1);
		manager.cancelAll();
		super.onDestroy();// 放置顺序有讲究，可来消除换歌立马返回时歌还在播放的BUG 2015年8月20日
	}

	private boolean playing = true;

	public void playOrPause(View v) {
		if (playing) {
			playing = false;
			mediaPlayer.pause();
			playButton.setImageResource(R.drawable.ic_media_play);
		} else {
			playing = true;
			mediaPlayer.start();
			playButton.setImageResource(R.drawable.ic_media_pause);
		}
	}

	public void next(View v) {
		binder.playNextMusic(false, random.isChecked());
	}

	public void previous(View v) {
		int currentTime = mediaPlayer.getCurrentPosition();
		if (currentTime < 5 * 1000) {
			binder.playPreviousMusic();
		} else {
			mediaPlayer.seekTo(0);
		}
	}

	private void playNextMusic() {
		binder.playNextMusic(playmode.isChecked(), random.isChecked());
	}

	/**
	 * 播放新的音乐,而且配置相关的设置
	 *
	 *            一个File文件
	 */
	private void playNewMusic() {
		handler.removeMessages(0);
		subtitleScroller.setEnabled(false);
		bar.setEnabled(false);

		String musicName = binder.getFileName();
		setTitle(musicName);
		mediaPlayer = binder.getMediaPlayer();
		mediaPlayer.setOnCompletionListener(this);
		notification = new Notification(R.drawable.icon, musicName,
				System.currentTimeMillis());
		//TODO
//		notification.setLatestEventInfo(this, "曲名", musicName, PendingIntent
//				.getActivity(this, 0, new Intent(this,
//						MusicPlayerActivity.class), 0));
		notification.flags |= Notification.FLAG_NO_CLEAR;

		manager.notify(1, notification);

		musicDuration = mediaPlayer.getDuration();

		String second = "";
		if ((musicDuration / 1000 % 60) < 10) {
			second += "0" + (musicDuration / 1000 % 60);
		} else {
			second += (musicDuration / 1000 % 60);
		}
		String endTimeString = (musicDuration / 60 / 1000) + ":" + second;
		endTime.setText(endTimeString);
		currentTime.setText("0:00");

		setAlbumart();// 专辑图片
		// 字幕
		subtitleScroller.scrollTo(0, 0);
		setSubtitle();
		initSubtitle = false;
		subtitleRowMoveSize = 0;
		subtitlePosition = 0;
		handler.postDelayed(new UpdataCurrentTimeShow(), 2000);

		proDialog.dismiss();

		if (playing) {
			mediaPlayer.start();
		}
	}

	public void random(View v) {
		if (random.isChecked()) {
			preMusicButton.setVisibility(View.INVISIBLE);
			Toast.makeText(this, "播放模式切换为:随机播放", Toast.LENGTH_SHORT).show();
		} else {
			preMusicButton.setVisibility(View.VISIBLE);
			Toast.makeText(this, "播放模式切换为:顺序播放", Toast.LENGTH_SHORT).show();
		}
	}

	public void playmode(View v) {
		if (playmode.isChecked()) {
			random.setVisibility(View.INVISIBLE);
			Toast.makeText(this, "播放模式切换为:单曲循环", Toast.LENGTH_SHORT).show();
		} else {
			random.setVisibility(View.VISIBLE);
			Toast.makeText(this, "播放模式切换为:全部循环", Toast.LENGTH_SHORT).show();
		}
	}

	public void test(View v) {
		onResume();
		onPause();
		onStop();
		System.out.println("hehe");
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		playNextMusic();
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		doUpdatePlayTime = false;
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		int barProgress = seekBar.getProgress();
		mediaPlayer
				.seekTo((int) (barProgress * 1.0f / bar.getMax() * musicDuration));
		double time = barProgress * 1.0f / bar.getMax() * musicDuration / 1000;

		// 字幕
		int i = 0;
		for (; i < subtitleContent.size(); i++) {
			if (!subtitleTime.isEmpty() && time < subtitleTime.get(i)) {
				subtitlePosition = i - 1;
				break;
			}
		}
		if (i == subtitleContent.size()) {
			subtitlePosition = i;
		}
		subtitleMove();

		doUpdatePlayTime = true;
		setSeekBarProgress();
	}

	private void setSeekBarProgress() {
		int musicCurrentTime = mediaPlayer.getCurrentPosition();
		bar.setProgress(bar.getMax() * musicCurrentTime / musicDuration);
		String second = "";
		if ((musicCurrentTime / 1000 % 60) < 10) {
			second += "0" + (musicCurrentTime / 1000 % 60);
		} else {
			second += (musicCurrentTime / 1000 % 60);
		}
		currentTime.setText((musicCurrentTime / 60 / 1000) + ":" + second);

		// 字幕的初始化
		if (!initSubtitle) {
			subtitleScroller.setEnabled(true);
			bar.setEnabled(true);

			subtitleRowCount = subtitle.getLineCount();
			subtitleRowMoveSize = subtitle.getLineHeight();
			subtitle.setHeight(subtitleRowCount * subtitle.getLineHeight());

			Log.i("行高", subtitleRowMoveSize + "");
			Log.i("行高", subtitle.getLineHeight() + "");
			Log.i("总可滑", subtitle.getHeight() - subtitleScroller.getHeight()
					+ "");
			subtitleScroller.scrollTo(0, 0);
			initSubtitle = true;
		}
		// 字幕的变化
		if (subtitleTime.size() != 0 && subtitleRowMoveSize != 0
				&& subtitlePosition < subtitleTime.size() - 1) {
			double nextSubtitleTime = subtitleTime.get(subtitlePosition + 1);
			if (nextSubtitleTime < musicCurrentTime / 1000) {
				subtitlePosition++;
				subtitleMove();
			}
		}
	}

	private void subtitleMove() {
		int scrollToY = subtitlePosition * subtitleRowMoveSize;
		Log.i("scrollToY", scrollToY + "");
		if (scrollToY <= subtitle.getHeight() - subtitleScroller.getHeight()) {
			subtitleScroller.smoothScrollTo(0, scrollToY);
		} else {
			subtitleScroller.smoothScrollBy(0, subtitle.getHeight());
		}
		setHighLight(subtitlePosition);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (subtitleContent.size() == 1) {
			return true;
		}
		doUpdatePlayTime = false;

		switch (event.getAction()) {
		case MotionEvent.ACTION_MOVE:
			Log.i("getScrollY()", subtitleScroller.getScrollY() + "");
			if (!initSubtitle) {
				break;
			}
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					subtitlePosition = subtitleScroller.getScrollY()
							/ subtitleRowMoveSize;
					setHighLight(subtitlePosition);
				}
			}, 100);

			break;
		case MotionEvent.ACTION_UP:
			Log.i("UPUPUP getScrollY()", subtitleScroller.getScrollY() + "");
			if (!initSubtitle) {
				break;
			}
			subtitlePosition = subtitleScroller.getScrollY()
					/ subtitleRowMoveSize;
			if (subtitlePosition >= subtitleContent.size()) {
				subtitlePosition = subtitleContent.size() - 1;
			}
			setHighLight(subtitlePosition);

			int goTime = 1000 * subtitleTime.get(subtitlePosition).intValue();
			if (goTime >= mediaPlayer.getDuration()) {
				goTime = mediaPlayer.getDuration();
			}// 防歌词不对的时拖动越界问题
			mediaPlayer.seekTo(goTime);
			setSeekBarProgress();
			doUpdatePlayTime = true;
			return true;// true把事件消化掉，不让其惯性甩出=.=
		}

		return false;
	}

	@Override
	public void updateInfo() {
		playNewMusic();
	}
}
