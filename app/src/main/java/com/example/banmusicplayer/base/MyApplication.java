package com.example.banmusicplayer.base;

import android.app.Application;
import android.content.Context;

import com.example.banmusicplayer.tool.MusicDB;

public class MyApplication extends Application {
	private Context context;
	@Override
	public void onCreate() {
		super.onCreate();

		context=getApplicationContext();
		
		init();
	}

	private void init() {
		StaticInfo.db = new MusicDB(context);
	}
}
