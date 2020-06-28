package com.example.banmusicplayer.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.example.banmusicplayer.tool.MusicLRC;
import com.example.banmusicplayer.tool.MusicLRC.Statement;

public class MusicSubtitle {
	private MusicLRC lrc;
	private ArrayList<String> subtitle;
	private ArrayList<Double> subtitleTime;

	public MusicSubtitle(String filePath) {
		subtitle = new ArrayList<String>();
		subtitleTime=new ArrayList<Double>();
		loadLRC(filePath);
	}

	private void loadLRC(String filePath) {
		try {
			lrc = new MusicLRC(filePath);
			List<Statement> statements = lrc.getLrcList();

			for (Statement statement : statements) {
				subtitle.add(statement.getLyric());
				subtitleTime.add(statement.getTime());
			}
		} catch (IOException e) {
			subtitle.add("本歌曲未找到匹配的歌词文??");
			e.printStackTrace();
		}
	}

	public ArrayList<String> getSubtitle() {
		return subtitle;
	}

	public ArrayList<Double> getSubtitleTime() {
		return subtitleTime;
	}
	
	
}
