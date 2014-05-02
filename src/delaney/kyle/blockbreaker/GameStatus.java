package delaney.kyle.blockbreaker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import android.os.Bundle;

public class GameStatus {
	public int balls = 3;
	public int stage = 0;
	public int blocksLeft;
	public boolean served = false;
	public boolean paused = false;
	public float tiltPos;
	public int[][] blocks = new int[15][15];
	
	public final int[] bounds;
	public final int[] blockSize;
	
	public Ball ball = new Ball();
	public Paddle paddle = new Paddle();
	
	private PatternSet mPatternFile;
	
	public GameStatus(int[] bounds, int[] blockSize) {
		this.bounds = bounds;
		this.blockSize = blockSize;
	}
	
	public void setPatternFile(PatternSet patternFile) {
		mPatternFile = patternFile;
		newGame();
	}
	
	private void newStage() {
		assert mPatternFile.size() > 0;
		blocksLeft = 0;
		for(int r = 0; r < blocks.length; r++) {
        	for(int c = 0; c <  blocks[r].length; c++) {
        		if(r >= mPatternFile.get(stage).height() || c >= mPatternFile.get(stage).width()) {
        			blocks[r][c] = 0;
        		} else {
	        		blocks[r][c] = mPatternFile.get(stage).get(c, r);
	        		if(blocks[r][c] != 0 && blocks[r][c] != BlockId.UNBREAKABLE_BLOCK) {
	        			blocksLeft++;
	        		}
        		}
        	}
        }
		served = false;
	}
	
	public void nextStage() {
		stage = (stage+1) % mPatternFile.size();
		newStage();
	}
	public void prevStage() {
		stage--;
		if(stage < 0) stage = mPatternFile.size()-1;
		newStage();
	}
	public void newGame() {
		newStage();
		balls = 3;
	}
	
	public boolean stageAwardsBall() {
		return mPatternFile.get(stage).awardsExtraBall();
	}
	
	public void restoreState(Bundle bundle) throws Exception {
		int[] bricks = bundle.getIntArray("bricks");
		int x = 0;
		int y = 0;
		for(int i:bricks) {
			blocks[y][x++] = i;
			if(x >= 15) {
				x = 0;
				y++;
			}
		}
		blocksLeft = bundle.getInt("blocks_left");
		served = bundle.getBoolean("ball_in_play", false);
		stage = bundle.getInt("stage");
		balls = bundle.getInt("lives");
		mPatternFile = new PatternSet(new ByteArrayInputStream(bundle.getByteArray("pattern_file")));
		ball.restoreState(bundle);
		paddle.saveState(bundle);
	}

	public void saveState(Bundle bundle) throws IOException {
		int[] bricks = new int[blocks.length*blocks[0].length];
		int c = 0;
		for(int[] row: blocks) {
			for(int i: row) {
				bricks[c++] = i;
			}
		}
		bundle.putIntArray("bricks", bricks);
		bundle.putInt("blocks_left", blocksLeft);
		bundle.putBoolean("ball_in_play", served);
		bundle.putInt("stage", stage);
		bundle.putInt("lives", balls);
		ByteArrayOutputStream io = new ByteArrayOutputStream();
		mPatternFile.save(io);
		bundle.putByteArray("pattern_file", io.toByteArray());
		ball.saveState(bundle);
		paddle.saveState(bundle);
	}
}
