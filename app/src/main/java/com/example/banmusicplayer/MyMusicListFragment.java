package com.example.banmusicplayer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.banmusicplayer.base.StaticInfo;
import com.example.banmusicplayer.tool.MusicSettingTool;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class MyMusicListFragment extends Fragment {
	private ListView musicList;
	private LinkedHashMap<String, String> listInfo;// 表名，列表名
	private ArrayList<String> myMusicList;// 列表名称
	private ArrayList<String> myMusicListKey;// 列表key
	private ArrayAdapter<String> adapter;
	private LinkedHashMap<String, ArrayList<String>> myMusicListPath;// 列表名，列表对应的音乐列表路径
	private ExecutorService executorService;
	private int readyToGo = 0, listLongClickPosition = 0;// 已读取到的的列表个数 , 长按的位置
	private String tempPath = "";// 暂存的文件路??
	private ProgressDialog proDialog;

	private MusicSettingTool tool;
	private AlertDialog.Builder builder;

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				proDialog.setMessage("正在将音乐加入列表中...");
				proDialog.setCancelable(false);
				proDialog.setIndeterminate(true);
				proDialog.show();

				break;

			case 2:
				proDialog.setMessage("正在删除此列表信??...");
				proDialog.setCancelable(false);
				proDialog.setIndeterminate(true);
				proDialog.show();

				break;
			case 3:
				proDialog.dismiss();
				setListViewContent();

				break;
			default:
				break;
			}
		}
	};

	private MainFragmentActivity activity;

	public void onAttach(android.app.Activity activity) {
		this.activity = (MainFragmentActivity) activity;
		super.onAttach(activity);
	};

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		proDialog = new ProgressDialog(getActivity());
		myMusicListPath = new LinkedHashMap<>();

		executorService = Executors.newFixedThreadPool(10);

		View view = inflater.inflate(R.layout.activity_main, container, false);
		view.findViewById(R.id.a).setVisibility(View.GONE);// 毕竟复用，不用的时候要隐藏

		tool = new MusicSettingTool(getActivity());
		listInfo = tool.getListInfo();

		musicList = view.findViewById(R.id.my_musiclist);
		this.registerForContextMenu(musicList);

		myMusicListKey = new ArrayList<>();
		myMusicList = new ArrayList<>();
		if (listInfo.size() != 0) {
			for (String s : listInfo.keySet()) {
				myMusicListKey.add(s);
			}
			for (String s : listInfo.values()) {
				myMusicList.add(s);
			}
			setListViewContent();
		} else {
			System.out.println("没有自定义列??");
		}

		return view;
	}

	/**
	 * 通过表名找到此列表对应的地址
	 */
	private void loadMusicListFromDB() {
		readyToGo = 0;

		for (final String s : myMusicList) {
			Log.i("keySet", s);
			executorService.execute(() -> {
				myMusicListPath.put(s,
						StaticInfo.db.getThisListMusicPath(s));
				System.out.println(myMusicListPath);
				readyToGo++;
			});
		}
	}

	@Override
	public void onResume() {
		if (StaticInfo.MY_MUSIC_LIST_PATH == null
				|| StaticInfo.MY_LIST_INFO_UPDATE) {
			loadMusicListFromDB();
			StaticInfo.MY_MUSIC_LIST_PATH = myMusicListPath;
			StaticInfo.MY_LIST_INFO_UPDATE = false;
		} else {
			myMusicListPath = StaticInfo.MY_MUSIC_LIST_PATH;
			readyToGo = myMusicListPath.size();
			System.out.println("已读取，不再读取自建目录");
		}
		setListViewContent();
		super.onResume();
	}

	/**
	 * 音乐列表的设??
	 */
	private void setListViewContent() {
		adapter = new ArrayAdapter<>(getActivity(),
				android.R.layout.simple_list_item_1, myMusicList);
		musicList.setAdapter(adapter);
		musicList.setOnItemClickListener((parent, view, position, id) -> {
			Log.i("readyToGo", readyToGo + "");
			Log.i("listInfo.size()", listInfo.size() + "");
			if (readyToGo == listInfo.size()) {
				Log.v("跳转position", position + "");
				Log.v("跳转content",
						myMusicListPath.get(myMusicList.get(position)) + "");
				Intent intent = new Intent(getActivity(),
						MusicListActivity.class);
				intent.putStringArrayListExtra("musicPath",
						myMusicListPath.get(myMusicList.get(position)));
				intent.putExtra("canAddMusic", true);
				intent.putExtra("listName", myMusicList.get(position));
				startActivity(intent);
			}
		});
		musicList.setOnItemLongClickListener((parent, view, position, id) -> {
			listLongClickPosition = position;
			return false;
		});
	}

	/**
	 * 添加自定义列表
	 *
	 * @param listName
	 */
	private boolean canClose = false;

	public void addList() {
		final AlertDialog dialog;
		canClose = false;

		LinearLayout layout = new LinearLayout(getActivity());
		layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		layout.setBackgroundColor(Color.WHITE);
		TextView textView = new TextView(getActivity());
		textView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		textView.setText("新列表名:");
		final EditText editText = new EditText(getActivity());
		editText.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		layout.addView(textView);
		layout.addView(editText);

		builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("请输入新的列表名：").setView(layout)
				.setIcon(android.R.drawable.ic_dialog_info)
				.setNegativeButton(R.string.Sys_no, null);
		builder.setPositiveButton(R.string.Sys_confirm, null);

		dialog = builder.show();
		dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(
				v -> {
					String newListName = editText.getText().toString();
					if (newListName.equals("")) {
						Toast.makeText(getActivity(), "请输入列表名",
								Toast.LENGTH_SHORT).show();
						return;
					}
					if (!myMusicListPath.containsKey(newListName)) {
						readyToGo++;
						myMusicListPath.put(newListName,
								new ArrayList<>());
						Toast.makeText(getActivity(), "成功添加新的列表",
								Toast.LENGTH_SHORT).show();
						int listIndex = tool.getListIndex();
						listInfo.put("T_" + listIndex, newListName);
						tool.saveListCount(listInfo.size());
						tool.saveListInfo(listInfo);
						canClose = true;

						myMusicList.add(newListName);
						myMusicListKey.add("T_" + listIndex);
						StaticInfo.MY_LIST_INFO_UPDATE = true;// 要重新加载我的列表
						setListViewContent();
					} else {
						Toast.makeText(getActivity(), "此列表名已存在，请重新输??",
								Toast.LENGTH_SHORT).show();
					}

					if (canClose) {
						dialog.cancel();
					}
				});
	}

	/**
	 * 选择文件夹之后的操作
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == -1) {
			Uri uri = data.getData();

			Cursor cursor = getActivity().getContentResolver().query(uri, null,
					null, null, Media.TITLE);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					tempPath = cursor.getString(cursor
							.getColumnIndex(Media.DATA));
				}
				cursor.close();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		menu.setHeaderTitle(R.string.main_operating);
		menu.add(0, 1, Menu.NONE, "删除该列");
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			AlertDialog.Builder builder = new Builder(getActivity());
			builder.setMessage("你是否将要从移除此列表？")
					.setIcon(android.R.drawable.ic_dialog_alert).setTitle(" ")
					.setNegativeButton(R.string.Sys_no, null)
					.setPositiveButton(R.string.Sys_confirm, (dialog, which) -> {

						Message.obtain(handler, 2).sendToTarget();
						new Thread() {
							public void run() {
								if (StaticInfo.db.deleteOneMusicPath(
										myMusicList
												.get(listLongClickPosition),
										null)) {
									listInfo.remove(myMusicListKey
											.get(listLongClickPosition));// 先删总表里的内容
									myMusicList
											.remove(listLongClickPosition);// 删显示的表名里的内容
									tool.deleteListInfo(myMusicListKey
											.get(listLongClickPosition),
											myMusicList.size());
									myMusicListKey
											.remove(listLongClickPosition);// 删表名里的内??
									readyToGo--;
									StaticInfo.MY_LIST_INFO_UPDATE = true;// ??要重新加载我的列??
									Message.obtain(handler, 3)
											.sendToTarget();
								}
							};
						}.start();
					}).show();
			break;

		default:
			return super.onContextItemSelected(item);
		}
		return true;
	}

	@Override
	public void onDestroy() {
		proDialog.dismiss();
		super.onDestroy();
	}
}
