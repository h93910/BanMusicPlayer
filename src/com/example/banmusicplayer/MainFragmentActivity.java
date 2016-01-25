package com.example.banmusicplayer;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.example.banmusicplayer.base.StaticInfo;
import com.example.banmusicplayer.server.MusicServer;

public class MainFragmentActivity extends MyBaseActivity {
	private Menu menu;
	private RadioGroup group;
	private FragmentManager fm;
	private FragmentTransaction ft;
	private ProgressDialog proDialog;
	private MainActivity leftFrament;
	private MyMusicListFragment rightFragment;
	private int fragmentPos = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState,R.layout.fragementactivity_main);
		proDialog = new ProgressDialog(this);

		leftFrament = new MainActivity();
		rightFragment = new MyMusicListFragment();

		fm = getSupportFragmentManager();
		defaultCheckedFragment();

		group = (RadioGroup) findViewById(R.id.my_files_menu);
		group.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				YoYo.with(Techniques.Swing).duration(1000)
						.playOn(findViewById(checkedId));
				switch (checkedId) {
				case R.id.group1:
					// Toast.makeText(getApplicationContext(), "0",
					// Toast.LENGTH_SHORT).show();
					group.findViewById(R.id.group1).setBackgroundColor(
							0xee00ffee);
					group.findViewById(R.id.group2).setBackgroundColor(
							0x99ddffee);
					defaultCheckedFragment();
					break;

				case R.id.group2:
					// Toast.makeText(getApplicationContext(), "1",
					// Toast.LENGTH_SHORT).show();
					group.findViewById(R.id.group2).setBackgroundColor(
							0xee00ffee);
					group.findViewById(R.id.group1).setBackgroundColor(
							0x99eeffdd);
					fragmentPos = 1;
					ft = fm.beginTransaction();
					ft.replace(R.id.fragment_layout, rightFragment).commit();
					break;
				}
				onCreateOptionsMenu(menu);
			}
		});
	}

	private void defaultCheckedFragment() {
		fragmentPos = 0;
		ft = fm.beginTransaction();
		ft.replace(R.id.fragment_layout, leftFrament).commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		this.menu = menu;
		if (menu != null) {
			menu.clear();
		} else {
			return true;
		}
		if (fragmentPos == 0) {
			getMenuInflater().inflate(R.menu.main, menu);
		} else {
			getMenuInflater().inflate(R.menu.main2, menu);
		}
		if (menu != null) {
			if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
				try {
					Method m = menu.getClass().getDeclaredMethod(
							"setOptionalIconsVisible", boolean.class);
					m.setAccessible(true);
					m.invoke(menu, true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_about) {
			Builder dialog = new AlertDialog.Builder(this);
			dialog.setTitle("关于").setView(StaticInfo.getAboutContent(this));
			dialog.setNegativeButton("确定", new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			}).show();

			return super.onOptionsItemSelected(item);
		} else if (item.getItemId() == R.id.action_add_list) {
			rightFragment.addList();
			return super.onOptionsItemSelected(item);
		}

		proDialog.setMessage("正在载入音乐...");
		proDialog.setCancelable(false);
		proDialog.setIndeterminate(true);
		proDialog.show();

		ArrayList<String> musicPath = new ArrayList<String>();
		if (fragmentPos == 0) {
			for (ArrayList<File> list : leftFrament.getMusicCatalog().values()) {
				for (File file : list) {
					musicPath.add(file.getAbsolutePath());
				}
			}
		} else {

		}

		Intent serviceIntent = new Intent(this, MusicServer.class);
		serviceIntent.putStringArrayListExtra("musicPath", musicPath);

		Intent intent = new Intent(this, MusicPlayerActivity.class);

		switch (item.getItemId()) {
		case R.id.action_play_all:
			intent.putExtra("position", 0);
			break;

		case R.id.action_random_play_all:
			int rd = (int) (Math.random() * musicPath.size());
			intent.putExtra("position", rd);
			intent.putExtra("isRandom", true);
			serviceIntent.putExtra("isRandom", true);
			break;
		}
		startService(serviceIntent);
		startActivity(intent);

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		if (proDialog.isShowing()) {
			proDialog.dismiss();
		}
		super.onResume();
	}

	@Override
	protected void onRestart() {
		if (fragmentPos == 0) {
			ft.show(leftFrament);
		} else {
			ft.show(rightFragment);
		}
		super.onRestart();
	}

	@Override
	protected void onStop() {
		if (fragmentPos == 0) {
			ft.hide(leftFrament);
		} else {
			ft.hide(rightFragment);
		}
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		StaticInfo.db.close();
	}

	@Override
	public void initView() {
	}

	@Override
	public void initButton() {
	}

	@Override
	public void initListView() {
	}
}
