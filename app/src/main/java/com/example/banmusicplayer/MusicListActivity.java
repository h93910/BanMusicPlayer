package com.example.banmusicplayer;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Images.Media;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.banmusicplayer.base.StaticInfo;
import com.example.banmusicplayer.server.MusicServer;
import com.example.banmusicplayer.tool.MusicCatalogTool;
import com.example.banmusicplayer.tool.MusicSettingTool;
import com.example.mariostudio.lyricviewdemo.activity.MainActivity;

public class MusicListActivity extends MyBaseActivity {
    private MusicListActivity activity = this;

    private ListView musicList;
    private ProgressDialog proDialog;
    private ArrayList<String> musicPath;
    private TextView textView;
    private boolean canAddMusic;
    private String listName = "";
    private int listLongClickPosition = 0;

    private ArrayList<String> musicListNameMap;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    proDialog = new ProgressDialog(activity);
                    proDialog.setMessage(getString(R.string.main_music_join));
                    proDialog.setCancelable(false);
                    proDialog.setIndeterminate(true);
                    proDialog.show();
                    break;
                case 2:
                    Toast.makeText(getApplicationContext(), R.string.main_music_join_success,
                            Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_main);

        final Intent intent = this.getIntent();
        musicPath = intent.getStringArrayListExtra("musicPath");
        canAddMusic = intent.getBooleanExtra("canAddMusic", false);
        listName = intent.getStringExtra("listName");
        if (listName != null && !listName.equals("")) {
            findViewById(R.id.back).setVisibility(View.VISIBLE);// 显示返回按钮
            textView = (TextView) findViewById(R.id.list_name);
            textView.setVisibility(View.VISIBLE);
            textView.setText(listName);
        } else {
            findViewById(R.id.a).setVisibility(View.GONE);// 毕竟复用，不用的时???要隐藏
        }

        proDialog = new ProgressDialog(this);
        musicList = (ListView) findViewById(R.id.my_musiclist);
        musicList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                proDialog.setMessage("正在载入音乐...");
                proDialog.setCancelable(false);
                proDialog.setIndeterminate(true);
                proDialog.show();

//				Intent intent2 = new Intent(MusicListActivity.this,
//						MusicServer.class);
//				intent2.putStringArrayListExtra("musicPath", musicPath);
//				intent2.putExtra("position", position);
//				startService(intent2);

                Intent intent1 = new Intent(MusicListActivity.this,
                        MainActivity.class);
                intent1.putStringArrayListExtra("musicPath", musicPath);
                intent1.putExtra("position", position);
                startActivity(intent1);
            }
        });

        musicList.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           final int position, long id) {
                listLongClickPosition = position;
                System.out.println(musicPath.get(position));
                return false;// true为吃掉事??,因为有contextMenu,??以这里用false;
            }
        });

        this.registerForContextMenu(musicList);
        setList();
    }

    @Override
    protected void onResume() {
        musicListNameMap = new ArrayList<String>();
        Iterable<String> a = new MusicSettingTool(this).getListInfo().values();
        for (String string : a) {
            musicListNameMap.add(string);
        }
        if (proDialog.isShowing()) {
            proDialog.dismiss();
        }
        super.onResume();
    }

    private void setList() {
        showProgressDialog(getString(R.string.loading), false);
        new Thread() {
            public void run() {
                final ArrayList<String> musicName = new ArrayList<String>();
                if (musicPath != null) {
                    for (String string : musicPath) {
                        File f = new File(string);
                        if (!StaticInfo.SINGLE_MUSIC_INFO.containsKey(string)) {
                            String musicNameString = "";
                            String artist = MusicCatalogTool.getArtist(
                                    activity, string);
                            if (artist.equals("<unknown>")) {
                                musicNameString = f.getName();
                            } else {
                                musicNameString = f.getName() + "\t\tby:"
                                        + artist;
                            }
                            musicName.add(musicNameString);
                            StaticInfo.SINGLE_MUSIC_INFO.put(string,
                                    musicNameString);
                        } else {
                            musicName.add(StaticInfo.SINGLE_MUSIC_INFO
                                    .get(string));
                        }
                    }
                }
                MusicListActivity.this.run(new MyRun() {
                    @Override
                    public void run() {
                        musicList
                                .setAdapter(new ArrayAdapter<String>(activity,
                                        android.R.layout.simple_list_item_1,
                                        musicName));
                        dismissProgressDialog();
                    }
                });

            }

            ;
        }.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (canAddMusic) {
            getMenuInflater().inflate(R.menu.main2, menu);
            menu.findItem(R.id.action_add_list).setTitle("添加音乐");
        } else {
            getMenuInflater().inflate(R.menu.main, menu);
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
            dialog.setNegativeButton(R.string.Sys_confirm, new OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            }).show();

            return super.onOptionsItemSelected(item);
        } else if (item.getItemId() == R.id.action_add_list) {
            addMusic();
            return super.onOptionsItemSelected(item);
        }

        proDialog.setMessage("正在载入音乐...");
        proDialog.setCancelable(false);
        proDialog.setIndeterminate(true);
        proDialog.show();

//        Intent intent = new Intent(this, MainActivity.class);
//        intent.putStringArrayListExtra("musicPath", musicPath);
//        switch (item.getItemId()) {
//            case R.id.action_play_all:
//                intent.putExtra("position", 0);
//                break;
//            case R.id.action_random_play_all:
//                int rd = (int) (Math.random() * musicPath.size());
//                intent.putExtra("position", rd);
//                intent.putExtra("isRandom", true);
//                break;
//        }
//        startActivity(intent);

        return super.onOptionsItemSelected(item);
    }

    /**
     * 在音乐列表中增加音乐
     */
    private void addMusic() {
        Intent intent = new Intent();
        intent.setType("audio:/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    /**
     * 在音乐列表中删除音乐
     */
    private void deleteMusic(int position) {
        if (listName != "") {
            String singleMusicPath = musicPath.get(position);
            if (StaticInfo.db.deleteOneMusicPath(listName, singleMusicPath)) {
                Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
                musicPath.remove(position);
                setList();

                StaticInfo.MY_LIST_INFO_UPDATE = true;// ??要重新加载我的列??
            }
        } else {
            Toast.makeText(this, "未知错误", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 选择音乐文件之后的操作：增加到新的列表中并保??
     */
    private String tempPath = "";

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == -1) {
            Uri uri = data.getData();

            Cursor cursor = getContentResolver().query(uri, null, null, null,
                    Media.TITLE);
            if (cursor != null) {
                if (cursor.moveToNext()) {
                    tempPath = cursor.getString(cursor
                            .getColumnIndex(Media.DATA));
                    cursor.close();
                }

                if (tempPath != "") {
                    if (musicPath == null) {
                        musicPath = new ArrayList<String>();
                    }
                    for (String string : musicPath) {
                        if (tempPath.equals(string)) {
                            Toast.makeText(this, "重复增加,此音乐已存在",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                    }

                    musicPath.add(tempPath);// 新加到当前列表中
                    setList();
                    Toast.makeText(this, "增加成功", Toast.LENGTH_SHORT).show();
                    new Thread() {
                        public void run() {
                            if (StaticInfo.db.saveOneMusicPath(listName,
                                    tempPath)) {
                                System.out.println("已成功将数据插入数据??");
                                StaticInfo.MY_LIST_INFO_UPDATE = true;// ??要重新加载我的列??
                            }// 将新的数据写入数据库
                        }

                        ;
                    }.start();
                }
            } else {
                Toast.makeText(this, "无法加入，这文件不为音乐文件", Toast.LENGTH_LONG)
                        .show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        menu.setHeaderTitle("操作");

        menu.add(0, 1, Menu.NONE, "增加此音乐到列表");
        menu.add(0, 2, Menu.NONE, "删除");

        if (canAddMusic) {
            menu.findItem(1).setVisible(false);
        } else {
            menu.findItem(2).setVisible(false);
        }
    }

    private int choose = -1;// 选择的列表的位置

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AlertDialog.Builder builder;

        switch (item.getItemId()) {
            case 1:
                if (musicListNameMap.size() == 0) {
                    Toast.makeText(this, "音乐列表个数??0,请去添加列表后再??", Toast.LENGTH_LONG)
                            .show();
                    break;
                }
                // 在不可添加音乐的页面时可将没有加入音乐列表的音乐加入列表??
                final String[] name = (String[]) musicListNameMap
                        .toArray(new String[musicListNameMap.size()]);
                builder = new Builder(activity);
                builder.setTitle(R.string.main_music_select)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setSingleChoiceItems(name, -1, new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                System.out.println("已选中"
                                        + musicListNameMap.get(which));
                                choose = which;
                            }
                        }).setNegativeButton(R.string.Sys_no, null)
                        .setPositiveButton(R.string.Sys_confirm, new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new Thread() {
                                    public void run() {
                                        Message.obtain(handler, 1).sendToTarget();
                                        if (StaticInfo.db.saveOneMusicPath(
                                                name[choose], musicPath
                                                        .get(listLongClickPosition))) {
                                            proDialog.dismiss();
                                            Message.obtain(handler, 2)
                                                    .sendToTarget();
                                            StaticInfo.MY_LIST_INFO_UPDATE = true;
                                        }
                                    }

                                    ;
                                }.start();
                            }
                        }).show();
                break;
            case 2:
                // 在可以添加音乐的页面时可使用移除操作
                builder = new Builder(activity);
                builder.setMessage("你是否将要从此列表中移除这音乐？")
                        .setIcon(android.R.drawable.ic_dialog_alert).setTitle(" ")
                        .setNegativeButton(R.string.Sys_no, null)
                        .setPositiveButton(R.string.Sys_confirm, new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteMusic(listLongClickPosition);
                                StaticInfo.MY_LIST_INFO_UPDATE = true;
                            }
                        }).show();
                break;

            default:
                return super.onContextItemSelected(item);

        }
        return true;
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
