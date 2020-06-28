package com.example.banmusicplayer.tool;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

/**
 * parse lrc file tool eg?? utilLrc lrc = new utilLrc("/sdcard/test.lrc"); get
 * song name : String title = lrc.getTitle(); get performer name : String artist
 * = lrc.getArtist(); get album name: String album = lrc.getAlbum(); get
 * lrcmaker name: String lrcMaker = lrc.getLrcMaker(); get song list:
 * List<Statement> list = lrc.getLrcList();
 * 
 * @author xuweilin
 * 
 */
public class MusicLRC {
	private static String TAG = "utilLrc";

	public class Statement {
		private double time = 0.0; // time, 0.01s
		private String lyric = ""; // song word

		/*
		 * get time
		 */
		public double getTime() {
			return time;
		}

		/*
		 * set time
		 */
		public void setTime(double time) {
			this.time = time;
		}

		/*
		 * set time.format:mm:ss.ms
		 */
		public void setTime(String time) {
			String str[] = time.split(":|\\.");
			this.time = Integer.parseInt(str[0]) * 60
					+ Integer.parseInt(str[1]) + Integer.parseInt(str[2])
					* 0.01;
		}

		/*
		 * get lrc word
		 */
		public String getLyric() {
			return lyric;
		}

		/*
		 * set lrc word
		 */
		public void setLyric(String lyric) {
			this.lyric = lyric;
		}
	}

	private BufferedReader bufferReader = null;
	private String title = "";
	private String artist = "";
	private String album = "";
	private String lrcMaker = "";
	private List<Statement> statements = new ArrayList<Statement>();

	/*
	 * 
	 * fileName
	 */
	public MusicLRC(String filePath) throws IOException {
		FileInputStream file = new FileInputStream(filePath);
		bufferReader = new BufferedReader(new InputStreamReader(file, "utf-8"));
		readData();
	}

	/*
	 * read the file
	 */
	private void readData() throws IOException {
		statements.clear();
		String strLine;
		while (null != (strLine = bufferReader.readLine())) {
			if ("".equals(strLine.trim())) {
				continue;
			}
			if (null == title || "".equals(title.trim())) {
				Pattern pattern = Pattern.compile("\\[ti:(.+?)\\]");
				Matcher matcher = pattern.matcher(strLine);
				if (matcher.find()) {
					title = matcher.group(1);
					continue;
				}
			}
			if (null == artist || "".equals(artist.trim())) {
				Pattern pattern = Pattern.compile("\\[ar:(.+?)\\]");
				Matcher matcher = pattern.matcher(strLine);
				if (matcher.find()) {
					artist = matcher.group(1);
					continue;
				}
			}
			if (null == album || "".equals(album.trim())) {
				Pattern pattern = Pattern.compile("\\[al:(.+?)\\]");
				Matcher matcher = pattern.matcher(strLine);
				if (matcher.find()) {
					album = matcher.group(1);
					continue;
				}
			}
			if (null == lrcMaker || "".equals(lrcMaker.trim())) {
				Pattern pattern = Pattern.compile("\\[by:(.+?)\\]");
				Matcher matcher = pattern.matcher(strLine);
				if (matcher.find()) {
					lrcMaker = matcher.group(1);
					continue;
				}
			}
			int timeNum = 0;
			String str[] = strLine.split("\\]");
			for (int i = 0; i < str.length; ++i) {
				String str2[] = str[i].split("\\[");
				str[i] = str2[str2.length - 1];
				if (isTime(str[i])) {
					++timeNum;
				}
			}
			for (int i = 0; i < timeNum; ++i) {
				Statement sm = new Statement();
				sm.setTime(str[i]);
				if (timeNum < str.length) {
					sm.setLyric(str[str.length - 1]);
				}
				statements.add(sm);
			}
		}
		sortLyric();
	}

	/*
	 * judge the string is or not date format.
	 */
	private boolean isTime(String string) {
		String str[] = string.split(":|\\.");
		if (3 != str.length) {
			return false;
		}
		try {
			for (int i = 0; i < str.length; ++i) {
				Integer.parseInt(str[i]);
			}
		} catch (NumberFormatException e) {
			Log.e(TAG, "isTime exception??" + e.getMessage());
			return false;
		}
		return true;
	}

	/*
	 * sort the word by time.
	 */
	private void sortLyric() {
		for (int i = 0; i < statements.size() - 1; ++i) {
			int index = i;
			double delta = Double.MAX_VALUE;
			boolean moveFlag = false;
			for (int j = i + 1; j < statements.size(); ++j) {
				double sub;
				if (0 >= (sub = statements.get(i).getTime()
						- statements.get(j).getTime())) {
					continue;
				}
				moveFlag = true;
				if (sub < delta) {
					delta = sub;
					index = j + 1;
				}
			}
			if (moveFlag) {
				statements.add(index, statements.get(i));
				statements.remove(i);
				--i;
			}
		}
	}

	/**
	 * get title
	 * 
	 * @return
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * get artist
	 * 
	 * @return
	 */
	public String getArtist() {
		return artist;
	}

	/**
	 * get album
	 * 
	 * @return
	 */
	public String getAlbum() {
		return album;
	}

	/**
	 * get lrc maker
	 * 
	 * @return
	 */
	public String getLrcMaker() {
		return lrcMaker;
	}

	/**
	 * get song list
	 * 
	 * @return
	 */
	public List<Statement> getLrcList() {
		return statements;
	}
	
	public static void main(String[] args) throws Exception {
		MusicLRC musicLRC=new MusicLRC("C:/Users/gxa/Desktop/甜蜜??.lrc");
	}
}
