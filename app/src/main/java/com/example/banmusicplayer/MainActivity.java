package com.example.banmusicplayer;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
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
import android.widget.ListView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.banmusicplayer.base.StaticInfo;
import com.example.banmusicplayer.tool.MusicCatalogTool;
import com.example.banmusicplayer.tool.MusicInfo;
import com.example.banmusicplayer.tool.MusicScan;
import com.example.banmusicplayer.tool.MusicSettingTool;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class MainActivity extends Fragment {
    private ListView musicList;
    private LinkedHashMap<File, ArrayList<File>> musicCatalog;
    private ArrayList<File> filePathList;

    private int listLongClickPosition = 0;
    private ArrayList<String> musicListNameMap;
    private ProgressDialog proDialog;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    proDialog = new ProgressDialog(getActivity());
                    proDialog.setMessage(getString(R.string.main_music_join));
                    proDialog.setCancelable(false);
                    proDialog.setIndeterminate(true);
                    proDialog.show();
                    break;
                case 2:
					proDialog.cancel();
                    Toast.makeText(getActivity(), R.string.main_music_join_success, Toast.LENGTH_SHORT).show();
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
    }

    ;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_main, container, false);
        view.findViewById(R.id.a).setVisibility(View.GONE);// 毕竟复用，不用的时候要隐藏

        proDialog = new ProgressDialog(getActivity());
        ;

        List<MusicInfo> musicInfo = StaticInfo.MAIN_MUSIC_INFO;
        if (musicInfo == null) {
            musicInfo = MusicScan.scanImages(getActivity());
            StaticInfo.MAIN_MUSIC_INFO = musicInfo;
        } else {
            System.out.println("已读取，不再读取主页目录");
        }

        String sdPath = Environment.getExternalStorageDirectory()
                .getAbsolutePath();//SD卡路径
        musicCatalog = MusicCatalogTool.getMusicCatalog(musicInfo);//音乐目录信息
        ArrayList<String> arrayList = new ArrayList<>();//父级目录名称
        filePathList = MusicCatalogTool.getCatalogFilePath(musicCatalog);//音乐父目录信息

        for (File f : filePathList) {
            String catalogName = f.getName() + "\t\t\"" + f.getAbsolutePath()
                    + "\"";
            catalogName = catalogName.replace(sdPath, "/sd card");
            arrayList.add(catalogName);
        }

        musicList = (ListView) view.findViewById(R.id.my_musiclist);
        musicList.setAdapter(new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, arrayList));
        musicList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    final int position, long id) {
                Intent intent = new Intent(getActivity(),
                        MusicListActivity.class);
                intent.putStringArrayListExtra("musicPath",
                        getMusicPath(position));
                startActivity(intent);
            }
        });
        musicList.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {
                listLongClickPosition = position;
                return false;
            }
        });

        this.registerForContextMenu(musicList);
        return view;
    }

    private ArrayList<String> getMusicPath(int position) {
        ArrayList<File> musicUnderThisCatalog = musicCatalog.get(filePathList
                .get(position));
        ArrayList<String> musicPath = new ArrayList<String>();
        for (File file : musicUnderThisCatalog) {
            musicPath.add(file.getAbsolutePath());
        }
        return musicPath;
    }

    public LinkedHashMap<File, ArrayList<File>> getMusicCatalog() {
        return musicCatalog;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        menu.setHeaderTitle(R.string.main_operating);

        menu.add(0, 1, Menu.NONE, R.string.main_music_add_all);
    }

    private int choose = -1;// 选择的列表的位置

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AlertDialog.Builder builder;

        switch (item.getItemId()) {
            case 1:
                if (musicListNameMap.size() == 0) {
                    Toast.makeText(getActivity(), R.string.main_music_empty,
                            Toast.LENGTH_LONG).show();
                    break;
                }
                // 在不可添加音乐的页面时可将没有加入音乐列表的音乐加入列表中
                final String[] name = musicListNameMap
                        .toArray(new String[musicListNameMap.size()]);
                builder = new Builder(getActivity());
                builder.setTitle(R.string.main_music_select)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setSingleChoiceItems(name, -1, new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                System.out.println("已选中" + musicListNameMap.get(which));
                                choose = which;
                            }
                        }).setNegativeButton(R.string.Sys_no, null)
                        .setPositiveButton(R.string.Sys_confirm, new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new Thread() {
                                    public void run() {
                                        Message.obtain(handler, 1).sendToTarget();

                                        int successCount = 0;
                                        ArrayList<String> musicPath = getMusicPath(listLongClickPosition);
                                        for (String string : musicPath) {
                                            if (StaticInfo.db.saveOneMusicPath(
                                                    name[choose], string)) {
                                                successCount++;
                                            }
                                        }
                                        if (successCount == musicPath.size()) {
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
            default:
                return super.onContextItemSelected(item);

        }
        return true;
    }

    @Override
    public void onResume() {
        musicListNameMap = new ArrayList<String>();
        Iterable<String> a = new MusicSettingTool(getActivity()).getListInfo()
                .values();
        for (String string : a) {
            musicListNameMap.add(string);
        }
        super.onResume();
    }

    @Override
    public void onDestroy() {
        proDialog.cancel();
        super.onDestroy();
    }
}
