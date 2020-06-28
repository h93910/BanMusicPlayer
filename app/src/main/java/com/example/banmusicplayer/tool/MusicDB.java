package com.example.banmusicplayer.tool;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MusicDB extends SQLiteOpenHelper {
	SQLiteDatabase database;

	public MusicDB(Context context) {
		super(context, "ban.db", null, 1);
		database = getReadableDatabase();
		createTable();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	/**
	 * 通过列表名称得到这个列表内全部音乐的路径
	 * 
	 * @param listName
	 */
	public ArrayList<String> getThisListMusicPath(String listName) {
		ArrayList<String> musicPath = new ArrayList<String>();
		Cursor cursor = database.query("music_info",
				new String[] { "music_path" }, "list_name=?",
				new String[] { listName }, null, null, null);
		System.out.println("列表??:" + listName);
		if (cursor != null) {
			while (cursor.moveToNext()) {
				String oneMusicPath = cursor.getString(cursor
						.getColumnIndex("music_path"));
				musicPath.add(oneMusicPath);
				Log.i("oneMusicPath", oneMusicPath);
			}
		} else {
			Log.i("oneMusicPath", "cursor为空");
		}
		cursor.close();
		return musicPath;
	}

	/**
	 * 向数据库插入??条音乐信??
	 * 
	 * @param listName
	 * @param musicPath
	 * @return
	 */
	public boolean saveOneMusicPath(String listName, String musicPath) {
		// Log.v("listName", listName);
		// Log.v("tempPath", musicPath);

		ContentValues contentValues = new ContentValues();
		contentValues.put("list_name", listName);
		contentValues.put("music_path", musicPath);
		if (database.insert("music_info", null, contentValues) == -1) {
			// 没有表才失败的，??以要新建??
			createTable();
			return false;
		}
		Log.i("", "success insert");
		return true;
	}

	/**
	 * 删除??条音乐信??
	 * 
	 * @param listName
	 * @param musicPath
	 * @return
	 */
	public boolean deleteOneMusicPath(String listName, String musicPath) {
		if (musicPath != null) {
			if (database.delete("music_info", "list_name=? and music_path=?",
					new String[] { listName, musicPath }) == -1) {
				// 没有表才失败的，??以要新建??
				createTable();
				return false;
			}
		} else {
			if (database.delete("music_info", "list_name=?",
					new String[] { listName }) == -1) {
				// 没有表才失败的，??以要新建??
				createTable();
				return false;
			}
		}
		Log.i("", "success delete");
		return true;
	}

	/**
	 * 新建??张表
	 */
	public void createTable() {
		database.execSQL("CREATE TABLE IF NOT EXISTS music_info(list_name varchar(30),music_path nvarchar(500))");
	}
}
