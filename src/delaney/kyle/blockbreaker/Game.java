package delaney.kyle.blockbreaker;

import java.io.File;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Picture;
import android.os.Bundle;
import com.larvalabs.svgandroid.SVGParser;

class Game {

	private File mPatternsDir;
	
	private TiltInput mTilt;
	private SoundManager mSoundManager;

	private Picture mBallIcon;
	
	private float mBlockScale;
	
	private long mLastTime;

	private float mBallDx;
	private float mBallDy;
	
	private float[] mOffset = new float[2];
	
	private static final int[] BLOCKSIZE = {32, 16};
	private static final int[] SCREENSIZE = {15*BLOCKSIZE[0],(12+6)*BLOCKSIZE[1]};

	
	private static final long SECOND = 1000000000;
	
	private Sprite mPaddle;
	private Sprite mBall;
	
	private boolean mBallInPlay;
	private boolean mPaused;
	
	private int[][] mBlocks;

	private Paint mBlockPaint;
	
	private static final int HARD_BLOCK = 0x0E;
	private static final int UNBREAKABLE_BLOCK = 0x0D;
	
	private static final float PADDLE_Y = SCREENSIZE[1]-16;
	
	private static final int BLACK = 0xFF000000;
	private static final int WHITE = 0xFFFFFFFF;
	
	private static final int[] mBlockColors = {
			0, 
			0xFFFF0000,
			0xFFc0dcc0,
			0xFFa6caf0,
			0xFFffff00,
			0xFFff0088, 
			0xFF00ffff,
			0xFF00ff00, 
			0xFFff8800,
			0xFFff00ff,
			0xFF0000ff,
			0xFFe3a276,
			0xFF880000,
			0xFFCCCC00,
			0xFFcccccc,
			0xFFcccccc,
			0xFFcccccc,
			0xFFcccccc
	};

	private int mBalls;
	private int mStage;
	private int mBlocksLeft;
	
	private PatternFile mPatterns;
	private long mFreeTime;
	
	public Game(Context context) throws NotFoundException, Exception {
        
		mPatternsDir = context.getExternalFilesDir(null);
		
		
        Resources res = context.getResources();
        
        mPaddle = new Sprite(res, R.raw.paddle);
        mBall = new Sprite(res, R.raw.ball);
		mBallIcon = SVGParser.getSVGFromResource(res, R.raw.ball).getPicture();
		
		mBlockPaint = new Paint();

		mPatterns = new PatternFile(context.getResources().openRawResource(R.raw.system));

		mTilt = new TiltInput(context);
        
        mLastTime = System.nanoTime();
        
        mBallDx = 0;
        mBallDy = -100;
        
        mBlocks = new int[15][15];
        
        mBlockScale = 0;
        
        mBallInPlay = false;
        mBalls = 3;
        
        mSoundManager = new SoundManager(context, 1, 2);
        mSoundManager.load(R.raw.bounce1, 1);
        mSoundManager.load(R.raw.destroy, 2);
	}

	public void sizeChanged(int width, int height) {
		mBlockScale = Math.min((float)width/(float)(SCREENSIZE[0]), (float)height/(float)(SCREENSIZE[1]));
		
		mOffset[0] = (width - SCREENSIZE[0]*mBlockScale)/2;
		mOffset[1] = (height - SCREENSIZE[1]*mBlockScale)/2;
	}
	
	public void doDraw(Canvas canvas) {
		if(canvas == null) return;
		
		canvas.drawColor(0xFF008800); //ARGB
		
		canvas.save();
		canvas.translate(mOffset[0], mOffset[1]);
		canvas.scale(mBlockScale, mBlockScale);
		canvas.clipRect(0, 0, SCREENSIZE[0], SCREENSIZE[1]);
		mBlockPaint.setColor(BLACK);
		canvas.drawRect(-1, -1, SCREENSIZE[0]+1, SCREENSIZE[1]+1, mBlockPaint);
		mBlockPaint.setColor(WHITE);
		canvas.drawRect(0, 0, SCREENSIZE[0], SCREENSIZE[1], mBlockPaint);
		
		mBlockPaint.setColor(0xFFCCCCCC);
		canvas.save();
		for(int y = 0; y < mBlocks.length; y++) {
			canvas.save();
			for(int x = 0; x < mBlocks[0].length; x++) {
				if(mBlocks[y][x] > 0) {
					canvas.drawRect(2, 2, BLOCKSIZE[0] + 2, BLOCKSIZE[1] + 2, mBlockPaint);
				}
				canvas.translate(BLOCKSIZE[0], 0);
			}
			canvas.restore();
			canvas.translate(0, BLOCKSIZE[1]);
		}
		canvas.restore();
		

		
		canvas.save();
		for(int y = 0; y < mBlocks.length; y++) {
			canvas.save();
			for(int x = 0; x < mBlocks[0].length; x++) {
				if(mBlocks[y][x] > 0) {
					mBlockPaint.setColor(mBlockColors[mBlocks[y][x]]);
					canvas.drawRect(0, 0, BLOCKSIZE[0], BLOCKSIZE[1], mBlockPaint);
					int cuts = Math.max(mBlocks[y][x]-HARD_BLOCK, 0);
					mBlockPaint.setColor(BLACK);
					switch(cuts) {
						case 3:
							canvas.drawLine(BLOCKSIZE[0]/4, 0, BLOCKSIZE[0]/4, BLOCKSIZE[1], mBlockPaint);
							canvas.drawLine(BLOCKSIZE[0]*3/4, 0, BLOCKSIZE[0]*3/4, BLOCKSIZE[1], mBlockPaint);
						case 2:
							canvas.drawLine(0, BLOCKSIZE[1]/2, BLOCKSIZE[0], BLOCKSIZE[1]/2, mBlockPaint);
						case 1:
							canvas.drawLine(BLOCKSIZE[0]/2, 0, BLOCKSIZE[0]/2, BLOCKSIZE[1], mBlockPaint);
						default:
							break;
					} 
				}
				canvas.translate(BLOCKSIZE[0], 0);
			}
			canvas.restore();
			canvas.translate(0, BLOCKSIZE[1]);
		}
		canvas.restore();

		
		mBlockPaint.setColor(BLACK);
		canvas.save();
		for(int y = 0; y < mBlocks.length; y++) {
			canvas.save();
			for(int x = 0; x < mBlocks[0].length; x++) {
				if(mBlocks[y][x] > 0) {
					canvas.drawLines(new float[] {}, mBlockPaint);
					canvas.drawLine(0, 0, BLOCKSIZE[0], 0, mBlockPaint);
					canvas.drawLine(0, 0, 0, BLOCKSIZE[1], mBlockPaint);
					canvas.drawLine(0, BLOCKSIZE[1], BLOCKSIZE[0], BLOCKSIZE[1], mBlockPaint);
					canvas.drawLine(BLOCKSIZE[0], 0, BLOCKSIZE[0], BLOCKSIZE[1], mBlockPaint);
				}
				canvas.translate(BLOCKSIZE[0], 0);
			}
			canvas.restore();
			canvas.translate(0, BLOCKSIZE[1]);
		}
		canvas.restore();
		
		mPaddle.draw(canvas);
		mBall.draw(canvas);
		
		canvas.save();
		canvas.translate(SCREENSIZE[0]-12, SCREENSIZE[1]-12);
		for(int i = 0; i < mBalls; i++) {
			canvas.drawPicture(mBallIcon);
			canvas.translate(-12,0);
		}
		canvas.restore();
		
		canvas.restore();
	}

	private void ballSound(int id) {
		mSoundManager.play(id, mBall.rect.centerX()/SCREENSIZE[0]);
	}
	
	private void rebound(boolean x, boolean y) {
		if(y) {
			mBallDy = -mBallDy + (float)(Math.random() - 0.5)*10;
		}
		if(x) {
			mBallDx = -mBallDx + (float)(Math.random() - 0.5)*10;
		}
	}
	
	public void updatePhysics() {
		long currentTime = System.nanoTime();
		long dt = currentTime - mLastTime;
		mLastTime = currentTime;
		
		if(mPaused) return;
		mFreeTime += dt;
		
		dt = SECOND/120;
		while(mFreeTime > dt) {
			mFreeTime -= dt;

			double deltaX = SCREENSIZE[0]/2 + mTilt.getX()*SCREENSIZE[0]/-10 - mPaddle.rect.centerX();
			
			mPaddle.rect.x += deltaX*10*dt/SECOND;
			mPaddle.rect.bottom(PADDLE_Y);
			
			if(mPaddle.rect.left() < 0) {
				mPaddle.rect.x -= mPaddle.rect.left();
			}
			if(mPaddle.rect.right() > SCREENSIZE[0]) {
				mPaddle.rect.x -= mPaddle.rect.right() - SCREENSIZE[0];
			}			
			
			if(!mBallInPlay) {
				mBall.rect.centerX(mPaddle.rect.centerX());
				mBall.rect.bottom(mPaddle.rect.top());
			} else {
				if(mBall.rect.right() > SCREENSIZE[0]-1) {
					rebound(true, false);
					mBall.rect.right(SCREENSIZE[0]-1);
					ballSound(R.raw.bounce1);
				}
				if(mBall.rect.left() < 0) {
					rebound(true, false);
					mBall.rect.left(0);
					ballSound(R.raw.bounce1);
				}
				if(mBall.rect.bottom() > mPaddle.rect.top() &&
						mBall.rect.left() < mPaddle.rect.right() &&
						mBall.rect.right() > mPaddle.rect.left()) {
					mBallDx = (mBall.rect.centerX() - mPaddle.rect.centerX())*6;
					mBallDy *= -1;
					mBall.rect.bottom(mPaddle.rect.top()-1);
					ballSound(R.raw.bounce1);
				}
				if(mBall.rect.top() < 0) {
					rebound(false, true);
					mBall.rect.top(0);
					ballSound(R.raw.bounce1);
				}
				if(mBall.rect.bottom() > SCREENSIZE[1]) {
					mBallInPlay = false;
					mBalls--;
					if(mBalls < 0) {
						newGame();
						return;
					}
				}
				
				for(int dy = -1; dy <= 1; dy++) {
					for(int dx = -1; dx <= 1; dx++) {
						if(Math.abs(dx) == Math.abs(dy)) continue;
						int bx = (int) Math.floor((mBall.rect.hSide(dx))/32);
						int by = (int) Math.floor((mBall.rect.vSide(dy))/16);
						if(by < 0 || by >= mBlocks.length || bx < 0 || bx >= mBlocks[by].length) continue;
						if(mBlocks[by][bx] != 0) {
							if(dx * mBallDx >= 0 && dy * mBallDy >= 0) {
								if(mBlocks[by][bx] < UNBREAKABLE_BLOCK || mBlocks[by][bx] == HARD_BLOCK+3) {
									mBlocks[by][bx] = 0;
									ballSound(R.raw.destroy);
									if(--mBlocksLeft == 0) {
										if(mPatterns.get(mStage).awardsExtraBall()) {
											mBalls++;
										}
										nextStage();
									}
								} else if (mBlocks[by][bx] == UNBREAKABLE_BLOCK) {
									ballSound(R.raw.bounce1);
								} else {
									mBlocks[by][bx]++;
									ballSound(R.raw.destroy);
								}
							}
							rebound(dx * mBallDx > 0, dy * mBallDy > 0);
						}
					}
				}
				mBall.rect.x += mBallDx*dt/SECOND;
				mBall.rect.y += mBallDy*dt/SECOND;
			}
		}
	}

	public void nextStage() {
		mStage = (mStage+1) % mPatterns.size();
		newStage();
	}
	public void prevStage() {
		mStage--;
		if(mStage < 0) mStage = mPatterns.size()-1;
		newStage();
	}

	public void pause() {
		mPaused = true;
	}
	
    public void touchedAt(float x, float y) {
    	if(mPaused) {
    		mPaused = false;
    	} else if(!mBallInPlay) {
    		mBallDy = -100;
    		mBallDx = 100;
    		mBallInPlay = true;
    	} else {
    		mPaused = true;
    	}
    }
	
	public void onPause() {
		mTilt.disconnect();
		mPaused = true;
	}

	public void onResume() {
		mTilt.connect();
		mLastTime = System.nanoTime();
	}
	
	private void newStage() {
		assert mPatterns.size() > 0;
		mBlocksLeft = 0;
		for(int r = 0; r < mBlocks.length; r++) {
        	for(int c = 0; c <  mBlocks[r].length; c++) {
        		if(r >= mPatterns.get(mStage).height() || c >= mPatterns.get(mStage).width()) {
        			mBlocks[r][c] = 0;
        		} else {
	        		mBlocks[r][c] = mPatterns.get(mStage).get(c, r);
	        		if(mBlocks[r][c] != 0 && mBlocks[r][c] != UNBREAKABLE_BLOCK) {
	        			mBlocksLeft++;
	        		}
        		}
        	}
        }
		mBallInPlay = false;
	}
	
	public void newGame() {
		newStage();
		mBalls = 3;
	}
	
	public void restoreState(Bundle savedInstanceState) {
		int[] bricks = savedInstanceState.getIntArray("bricks");
		if(bricks == null) {
			newGame();
			return;
		}
		int x = 0;
		int y = 0;
		for(int i:bricks) {
			mBlocks[y][x++] = i;
			if(x >= 15) {
				x = 0;
				y++;
			}
		}
		
		mBallDx = savedInstanceState.getFloat("ball_dx");
		mBallDy = savedInstanceState.getFloat("ball_dy");
		mBall.rect.x = savedInstanceState.getFloat("ball_x");
		mBall.rect.y = savedInstanceState.getFloat("ball_y");
		mBallInPlay = savedInstanceState.getBoolean("ball_in_play", false);
		mStage = savedInstanceState.getInt("stage");
		mBalls = savedInstanceState.getInt("lives");
	}

	public void saveState(Bundle outState) {
		int[] bricks = new int[mBlocks.length*mBlocks[0].length];
		int c = 0;
		for(int[] row: mBlocks) {
			for(int i: row) {
				bricks[c++] = i;
			}
		}
		outState.putIntArray("bricks", bricks);
		outState.putFloat("ball_dx", mBallDx);
		outState.putFloat("ball_dy", mBallDy);
		outState.putFloat("ball_x", mBall.rect.x);
		outState.putFloat("ball_y", mBall.rect.y);
		outState.putBoolean("ball_in_play", mBallInPlay);
		
		outState.putInt("stage", mStage);
		outState.putInt("lives", mBalls);
		
	}
}
