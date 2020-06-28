package com.example.banmusicplayer.tool;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore.Audio.Media;

public class MusicCatalogTool {
	public static LinkedHashMap<File, ArrayList<File>> getMusicCatalog(
			List<MusicInfo> musicInfo) {
		LinkedHashMap<File, ArrayList<File>> musicCatalog = new LinkedHashMap<File, ArrayList<File>>();

		File f;
		for (MusicInfo m : musicInfo) {
			f = new File(m.getMusicPath());
			File parent = f.getParentFile();
			if (musicCatalog.containsKey(parent)) {
				musicCatalog.get(parent).add(f);
			} else {
				ArrayList<File> files = new ArrayList<File>();
				files.add(f);
				musicCatalog.put(parent, files);
			}
		}
		return musicCatalog;
	}

	public static ArrayList<File> getCatalogFilePath(
			LinkedHashMap<File, ArrayList<File>> musicCatalog) {
		ArrayList<File> catalogFilePath = new ArrayList<File>();
		for (File f : musicCatalog.keySet()) {
			catalogFilePath.add(f);
		}
		return catalogFilePath;
	}

	public static Bitmap getAlbum(Context context, String musicPath) {
		Cursor cursor = context.getContentResolver().query(
				Media.EXTERNAL_CONTENT_URI, null, null, null, Media.TITLE);
		String tempPath = "";
		if (cursor.moveToFirst()) {
			do {
				tempPath = cursor.getString(cursor.getColumnIndex(Media.DATA));
				if (tempPath.equals(musicPath)) {
					break;
				}
			} while (cursor.moveToNext());
		}

		int albumID = cursor.getInt(cursor.getColumnIndex(Media.ALBUM_ID));

		String mUriAlbums = "content://media/external/audio/albums";
		cursor = context.getContentResolver().query(
				Uri.parse(mUriAlbums + "/" + albumID),
				new String[] { "album_art" }, null, null, null);
		String album_art = null;
		if (cursor.getCount() > 0 && cursor.getColumnCount() > 0) {
			cursor.moveToNext();
			album_art = cursor.getString(0);
		} else {
			return null;
		}
		cursor.close();

		return BitmapFactory.decodeFile(album_art);
	}

	public static String getArtist(Context context, String musicPath) {
		Cursor cursor = context.getContentResolver().query(
				Media.EXTERNAL_CONTENT_URI, null, null, null, Media.TITLE);
		String artist = "";
		if (cursor!=null&&cursor.moveToFirst()) {
			do {
				String path = cursor.getString(cursor
						.getColumnIndex(Media.DATA));
				if (path.equals(musicPath)) {
					artist = cursor.getString(cursor
							.getColumnIndex(Media.ARTIST));
					break;
				}
			} while (cursor.moveToNext());
		}
		cursor.close();

		return artist;
	}
}
