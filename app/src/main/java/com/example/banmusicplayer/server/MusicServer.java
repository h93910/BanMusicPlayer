package com.example.banmusicplayer.server;

import java.io.File;
import java.util.ArrayList;

import com.example.banmusicplayer.tool.MusicCatalogTool;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class MusicServer extends Service {
	private MediaPlayer mediaPlayer;
	private int position;
	private ArrayList<String> musicPath;
	private ArrayList<String> subtitleContent;
	private ArrayList<Double> subtitleTime;
	private Bitmap album;
	private MyBinder binder;
	protected Context context = this;
	

	@Override
	public IBinder onBind(Intent intent) {
		Log.i("service", "onBind");
		return binder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		binder = new MyBinder();
		Log.i("service", binder.toString());
		musicPath = intent.getStringArrayListExtra("musicPath");
		if (intent.getBooleanExtra("isRandom", false)) {
			int rd = (int) (Math.random() * musicPath.size());
			position = rd;
		} else {
			position = intent.getIntExtra("position", 0);
		}

		binder.playNewMusic(new File(musicPath.get(position)));

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		Log.i("service", "onDestroy");
		super.onDestroy();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.i("service", "onUnbind");
		mediaPlayer.release();
		stopSelf();
		return super.onUnbind(intent);
	}

	public class MyBinder extends Binder {
		private MusicInfoReCall call;

		public void playNewMusic(File f) {
			mediaPlayer = MediaPlayer.create(context, Uri.fromFile(f));

			getSubtitleInfo();
			getAlbumart(f);
			if (call != null) {
				call.updateInfo();
			}
		}

		public void playNextMusic(boolean playmode, boolean random) {
			mediaPlayer.release();
			if (!playmode) {
				if (random) {
					int rd = (int) (Math.random() * musicPath.size());
					position = rd;
				} else {
					position = (position + 1) % musicPath.size();
				}
			}
			playNewMusic(new File(musicPath.get(position)));
		}

		public void playPreviousMusic() {
			mediaPlayer.release();
			if (--position < 0) {
				position = musicPath.size() + position;
			}
			playNewMusic(new File(musicPath.get(position % musicPath.size())));

		}

		private void getAlbumart(File f) {
			album = MusicCatalogTool.getAlbum(context, f.getPath());
		}

		private void getSubtitleInfo() {
			File f = new File(musicPath.get(position % musicPath.size()));
			String lrcPath = f.getPath().subSequence(0,
					f.getPath().indexOf("."))
					+ ".lrc";
			System.out.println(lrcPath);

			MusicSubtitle ms = new MusicSubtitle(lrcPath);
			subtitleTime = ms.getSubtitleTime();
			subtitleContent = ms.getSubtitle();
		}

		public MediaPlayer getMediaPlayer() {
			return mediaPlayer;
		}

		public ArrayList<String> getSubtitleContent() {
			return subtitleContent;
		}

		public ArrayList<Double> getSubtitleTime() {
			return subtitleTime;
		}

		public Bitmap getAlbum() {
			return album;
		}

		public String getFileName() {
			File f = new File(musicPath.get(position));
			return f.getName().substring(0, f.getName().indexOf("."));
		}

		public void setCall(MusicInfoReCall call) {
			this.call = call;
		}

	}

	public interface MusicInfoReCall {
		public void updateInfo();
	}
}
