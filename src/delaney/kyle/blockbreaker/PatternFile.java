package delaney.kyle.blockbreaker;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;

import android.util.JsonReader;
import android.util.JsonWriter;

public class PatternFile {

	public class Pattern {
		
		private int[][] mBricks = new int[12][15];
		
		private boolean mExtraBall = false;
		
		public int get(int x, int y) {
			return mBricks[y][x];
		}
		
		public void set(int x, int y, int brick) {
			mBricks[y][x] = brick;
		}
		
		public int width() {
			return mBricks[0].length;
		}
		
		public int height() {
			return mBricks.length;
		}
		
		public boolean awardsExtraBall() {
			return mExtraBall;
		}
		
		private Pattern(int[][] bricks, boolean extra) {
			mBricks = bricks;
			mExtraBall = extra;
		}
	}
	
	private Pattern[] mData = new Pattern[10];
	
	public Pattern get(int i) {
		return mData[i];
	}
	
	public int size() {
		return mData.length;
	}
	
	public PatternFile(InputStream io) throws Exception {
		char[] magic1 = new char[27];
		for(int i = 0; i < magic1.length; i++) {
			magic1[i] = (char) io.read();
		}
		if(!new String(magic1).equals("Block Breaker Pattern File.")) {
			throw new Exception("Magic string not found!");
		}
		char[] magic2 = new char[2];
		for(int i = 0; i < magic2.length; i++) {
			magic2[i] = (char) io.read();
		}
		if(!Arrays.equals(magic2, new char[] {0x1A, 0x0A})) {
			throw new Exception("Magic number not found!");
		}
		int[][][] patterns = new int[10][12][15];
		
		for(int i = 0; i < patterns.length; i++) {
			for(int r = 0; r < patterns[i].length; r++) {
				for(int b = 0; b < patterns[i][r].length; b++) {
					patterns[i][r][b] = io.read();
				}
			}
		}
		
		boolean[] extras = new boolean[patterns.length];
		for(int i = 0; i < extras.length; i++) {
			extras[i] = io.read() == 1;
		}
		
		for(int i = 0; i < patterns.length; i++) {
			mData[i] = new Pattern(patterns[i], extras[i]);
		}
	}
}
