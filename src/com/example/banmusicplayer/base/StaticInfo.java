package com.example.banmusicplayer.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.banmusicplayer.R;
import com.example.banmusicplayer.tool.MusicDB;
import com.example.banmusicplayer.tool.MusicInfo;

public class StaticInfo {
	private static int HASH_MAP_LIMIT = 150;// 限制hashMap的长度;

	public static MusicDB db;
	public static LinkedHashMap<String, ArrayList<String>> MY_MUSIC_LIST_PATH;// 我的音乐列表
	public static List<MusicInfo> MAIN_MUSIC_INFO;// 主页的音乐目录信息
	public static boolean MY_LIST_INFO_UPDATE = true;// 我的音乐列表 是否要更新
	@SuppressWarnings("serial")
	public static Map<String, String> SINGLE_MUSIC_INFO = new HashMap<String, String>() {
		private int currentIndex = 0;

		public String put(String key, String value) {
			if (this.size() >= 300) {
				if (currentIndex >= HASH_MAP_LIMIT) {
					currentIndex = 0;
				}
				Log.d("hashmap 删除", (String) this.keySet().toArray()[currentIndex++]);
				this.remove((String)this.keySet().toArray()[currentIndex++]);
			}

			return super.put(key, value);
		};
	};

	/**
	 * 取得关于 的文字信息view
	 * 
	 * @param c
	 * @return
	 */
	public static View getAboutContent(Context c) {
		String packageName = c.getPackageName();
		String versionname = "";
		try {
			versionname = c.getPackageManager().getPackageInfo(packageName, 0).versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ScrollView scrollView = new ScrollView(c);
		scrollView.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT));
		TextView message = new TextView(c);
		message.setBackgroundColor(Color.WHITE);
		message.setText(Html.fromHtml("<br/><b>班的音乐播放器</b><br/><br/>版本:"
				+ versionname + "<br/>作者:班克威<br/><br/>")
				+ c.getString(R.string.update_log));

		message.setTextSize(18);
		message.setGravity(Gravity.CENTER_HORIZONTAL);

		scrollView.addView(message);

		return scrollView;
	}
}
