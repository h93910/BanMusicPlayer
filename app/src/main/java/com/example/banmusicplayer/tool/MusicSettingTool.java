package com.example.banmusicplayer.tool;

import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class MusicSettingTool {
	private SharedPreferences sharedPreferences;
	private static int listCount = 0;

	public MusicSettingTool(Context context) {
		sharedPreferences = context.getSharedPreferences("ban.setting",
				Context.MODE_PRIVATE);
	}

	/**
	 * 取得??新表的数??
	 * 
	 * @return
	 */
	public int getListCount() {
		if (listCount == 0) {// 刚进来就??0
			listCount = sharedPreferences.getInt("listCount", 0);
		}
		return listCount;
	}

	/**
	 * 保存列表数量
	 * 
	 * @param count
	 */
	public void saveListCount(int count) {
		listCount = count;

		Editor editor = sharedPreferences.edit();
		editor.putInt("listCount", count);
		editor.commit();
	}

	/**
	 * 取得表的下标
	 * 
	 * @return
	 */
	public int getListIndex() {
		return sharedPreferences.getInt("listIndex", 1);
	}

	/**
	 * 保存表的下标
	 * 
	 * @param count
	 */
	public void updateListIndex() {
		Editor editor = sharedPreferences.edit();
		editor.putInt("listIndex", getListIndex() + 1);
		editor.commit();
	}

	/**
	 * 取列表名??
	 * 
	 * @return
	 */
	public LinkedHashMap<String, String> getListInfo() {
		LinkedHashMap<String, String> listInfo = new LinkedHashMap<String, String>();
		int conut = getListCount();
		int nowCount = 0;
		for (int i = 1; nowCount < conut; i++) {
			String tableName = sharedPreferences.getString("T_" + i, "");
			if (!tableName.equals("")) {
				listInfo.put("T_" + i, tableName);
				nowCount++;
			}
		}
		return listInfo;
	}

	/**
	 * 将列表信息（表名）存在设置文件中
	 * 
	 * @param listInfo
	 */
	public void saveListInfo(LinkedHashMap<String, String> listInfo) {
		updateListIndex();
		Set<Entry<String, String>> infoSet = listInfo.entrySet();
		Editor editor = sharedPreferences.edit();
		for (Entry<String, String> entry : infoSet) {
			editor.putString(entry.getKey(), entry.getValue());
		}
		editor.commit();
	}

	/**
	 * 删除某列信息
	 * 
	 * @param listInfo
	 */
	public void deleteListInfo(String listKey, int nowCount) {
		saveListCount(nowCount);

		Editor editor = sharedPreferences.edit();
		editor.remove(listKey);
		editor.commit();
	}
}
