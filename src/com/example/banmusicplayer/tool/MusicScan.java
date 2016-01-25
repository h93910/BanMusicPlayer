package com.example.banmusicplayer.tool;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.database.Cursor;
import android.provider.MediaStore.Audio.Media;

public class MusicScan {
	public static List<MusicInfo> scanImages(Activity activity) {
		List<MusicInfo> infos = new ArrayList<MusicInfo>();
		Cursor cursor = activity.getContentResolver().query(
				Media.EXTERNAL_CONTENT_URI, null, null, null, Media.TITLE);
		MusicInfo musicInfo = null;
		if (cursor != null) {
			while (cursor.moveToNext()) {
				String title = cursor.getString(cursor
						.getColumnIndex(Media.TITLE));
				String filePath = cursor.getString(cursor
						.getColumnIndex(Media.DATA));
				musicInfo = new MusicInfo(title, filePath);
				infos.add(musicInfo);
			}
		}
		cursor.close();
		return infos;
	}
}
