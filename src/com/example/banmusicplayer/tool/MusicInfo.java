package com.example.banmusicplayer.tool;

public class MusicInfo {
	private String musicName;
	private String musicPath;

	public MusicInfo(String musicName, String musicPath) {
		super();
		this.musicName = musicName;
		this.musicPath = musicPath;
	}

	public String getMusicName() {
		return musicName;
	}

	public void setMusicName(String musicName) {
		this.musicName = musicName;
	}

	public String getMusicPath() {
		return musicPath;
	}

	public void setMusicPath(String musicPath) {
		this.musicPath = musicPath;
	}

}
