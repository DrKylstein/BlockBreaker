package delaney.kyle.blockbreaker;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;

import android.os.Bundle;
import android.util.JsonReader;
import android.util.JsonWriter;

public class PatternSet {

	private static final byte[] MAGIC_NUMBER = {
		0x42, 0x6c, 0x6f, 0x63,
		0x6b, 0x20, 0x42, 0x72,
		0x65, 0x61, 0x6b, 0x65,
		0x72, 0x20, 0x50, 0x61,
		0x74, 0x74, 0x65, 0x72,
		0x6e, 0x20, 0x46, 0x69,
		0x6c, 0x65, 0x2e, 0x1a,
		0x0a
	};
	
	private static final int NAME_LEN = 30;
	private static final int MESSAGE_LEN = 256;
	
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
	
	private String mName;
	private String mMessage;
	
	public Pattern get(int i) {
		return mData[i];
	}
	
	public int size() {
		return mData.length;
	}
	
	public PatternSet(InputStream io) throws Exception {
		byte[] magic = new byte[MAGIC_NUMBER.length];
		for(int i = 0; i < MAGIC_NUMBER.length; i++) {
			magic[i] = (byte)io.read();
		}
		if(!Arrays.equals(magic, MAGIC_NUMBER)) {
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
		
		StringBuilder sb = new StringBuilder();
		boolean endFound = false;
		for(int i = 0; i < NAME_LEN; i++) {
			int c = io.read();
			if(c == 0) {
				endFound = true;
			}
			if(endFound) continue;
			sb.append(c);
		}
		
		mName = sb.toString();
		
		sb = new StringBuilder();
		endFound = false;
		for(int i = 0; i < MESSAGE_LEN; i++) {
			int c = io.read();
			if(c == 0) {
				endFound = true;
			}
			if(endFound) continue;
			sb.append(c);
		}
		
		mMessage = sb.toString();
		
		for(int i = 0; i < patterns.length; i++) {
			mData[i] = new Pattern(patterns[i], extras[i]);
		}
	}
	
	public void save(OutputStream io) throws IOException {
		io.write(MAGIC_NUMBER);
		for(int i = 0; i < mData.length; i++) {
			for(int r = 0; r < mData[i].height(); r++) {
				for(int b = 0; b < mData[i].width(); b++) {
					io.write(mData[i].get(b, r));
				}
			}
		}
		for(int i = 0; i < mData.length; i++) {
			io.write(mData[i].awardsExtraBall()? 1 : 0);
		}
		for(int i = 0; i < NAME_LEN; i++) {
			int c = i < mName.length()? mName.charAt(i) : 0;
			io.write(c);
		}
		for(int i = 0; i < MESSAGE_LEN; i++) {
			int c = i < mMessage.length()? mMessage.charAt(i) : 0;
			io.write(c);
		}
	}
}
