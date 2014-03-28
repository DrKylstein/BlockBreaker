package delaney.kyle.blockbreaker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import com.larvalabs.svgandroid.SVGParser;

class Game {

	private File mPatternsDir;
	
	private TiltInput mTilt;
	private SoundManager mSoundManager;

	private Picture mBlockDecor;
	private Picture mSolidBlock;
	private Picture[] mToughBlock;
	private Picture mBallIcon;
	
	private float mBrickScale;
	
	private long mLastTime;

	private float mBallDx;
	private float mBallDy;
	
	private float[] mOffset = new float[2];
	
	private static final int[] IDEAL_SIZE = {15*32,(12+6)*16};
	
	private static final long SECOND = 1000000000;
	
	private Sprite mPaddle;
	private Sprite mBall;
	
	private boolean mBallInPlay;
	private boolean mPaused;
	
	private int[][] mBricks;

	private Paint mBlockPaint;
	
	private static final int HARD_BLOCK = 0x0E;
	private static final int UNBREAKABLE_BLOCK = 0x0D;
	
	private static final float BOTTOM = IDEAL_SIZE[1]-16;
	
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
			0xFFcccccc
	};

	private int mLives;
	private int mStage;
	private int mBricksLeft;
	
	private PatternFile mPatterns;
	private long mFreeTime;
	
	public Game(Context context) throws NotFoundException, Exception {
        
		mPatternsDir = context.getExternalFilesDir(null);
		
		
        Resources res = context.getResources();
        
        mPaddle = new Sprite(res, R.raw.paddle);
        mBall = new Sprite(res, R.raw.ball);
		mBlockDecor = SVGParser.getSVGFromResource(res, R.raw.blockdecor).getPicture();
		mSolidBlock = SVGParser.getSVGFromResource(res, R.raw.solidblock).getPicture();
		mBallIcon = SVGParser.getSVGFromResource(res, R.raw.ball).getPicture();
		
		
		mToughBlock = new Picture[4];
		mToughBlock[0] = SVGParser.getSVGFromResource(res, R.raw.toughblock).getPicture();
		mToughBlock[1] = SVGParser.getSVGFromResource(res, R.raw.splitblock).getPicture();
		mToughBlock[2] = SVGParser.getSVGFromResource(res, R.raw.splitblock2).getPicture();
		mToughBlock[3] = SVGParser.getSVGFromResource(res, R.raw.splitblock3).getPicture();
		
		mBlockPaint = new Paint();

		mPatterns = new PatternFile(context.getResources().openRawResource(R.raw.system));

		mTilt = new TiltInput(context);
        
        mLastTime = System.nanoTime();
        
        mBallDx = 0;
        mBallDy = -100;
        
        mBricks = new int[15][15];
        
        mBrickScale = 0;
        
        mBallInPlay = false;
        mLives = 3;
        
        mSoundManager = new SoundManager(context, 1, 2);
        mSoundManager.load(R.raw.bounce1, 1);
        mSoundManager.load(R.raw.destroy, 2);
	}

	public void sizeChanged(int width, int height) {
		mBrickScale = Math.min((float)width/(float)(IDEAL_SIZE[0]), (float)height/(float)(IDEAL_SIZE[1]));
		
		mOffset[0] = (width - IDEAL_SIZE[0]*mBrickScale)/2;
		mOffset[1] = (height - IDEAL_SIZE[1]*mBrickScale)/2;
	}
	
	public void doDraw(Canvas canvas) {
		if(canvas == null) return;
		
		canvas.drawColor(0xFF008800); //ARGB
		
		canvas.save();
		canvas.translate(mOffset[0], mOffset[1]);
		canvas.scale(mBrickScale, mBrickScale);
		canvas.clipRect(0, 0, IDEAL_SIZE[0], IDEAL_SIZE[1]);
		mBlockPaint.setColor(0xFF000000);
		canvas.drawRect(-1, -1, IDEAL_SIZE[0]+1, IDEAL_SIZE[1]+1, mBlockPaint);
		mBlockPaint.setColor(0xFFFFFFFF);
		canvas.drawRect(0, 0, IDEAL_SIZE[0], IDEAL_SIZE[1], mBlockPaint);
		
		canvas.save();
		for(int y = 0; y < 15; y++) {
			canvas.save();
			for(int x = 0; x < 15; x++) {
				if(mBricks[y][x] > 0) {
					if(mBricks[y][x] >= HARD_BLOCK) {
						canvas.drawPicture(mToughBlock[mBricks[y][x]-HARD_BLOCK]);
					} else {
						mBlockPaint.setColor(mBlockColors[mBricks[y][x]]);
						canvas.drawRect(0, 0, 32, 16, mBlockPaint);
						canvas.drawPicture(mBlockDecor);
					}
				}
				canvas.translate(32, 0);
			}
			canvas.restore();
			canvas.translate(0, 16);
		}
		canvas.restore();
		
		mPaddle.draw(canvas);
		mBall.draw(canvas);
		
		canvas.save();
		canvas.translate(IDEAL_SIZE[0]-12, BOTTOM);
		for(int i = 0; i < mLives; i++) {
			canvas.drawPicture(mBallIcon);
			canvas.translate(-12,0);
		}
		canvas.restore();
		
		if(mPaused) {
			mBlockPaint.setColor(0x80000000);
			canvas.drawRect(0, 0, IDEAL_SIZE[0], IDEAL_SIZE[1], mBlockPaint);
		}
		
		canvas.restore();
	}

	private void ballSound(int id) {
		mSoundManager.play(id, mBall.rect.centerX()/IDEAL_SIZE[0]);
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

			double deltaX = IDEAL_SIZE[0]/2 + mTilt.getX()*IDEAL_SIZE[0]/-10 - mPaddle.rect.centerX();
			
			mPaddle.rect.x += deltaX*10*dt/SECOND;
			mPaddle.rect.bottom(BOTTOM);
			
			if(mPaddle.rect.left() < 0) {
				mPaddle.rect.x -= mPaddle.rect.left();
			}
			if(mPaddle.rect.right() > IDEAL_SIZE[0]) {
				mPaddle.rect.x -= mPaddle.rect.right() - IDEAL_SIZE[0];
			}			
			
			if(!mBallInPlay) {
				mBall.rect.centerX(mPaddle.rect.centerX());
				mBall.rect.bottom(mPaddle.rect.top());
			} else {
				if(mBall.rect.right() > IDEAL_SIZE[0]-1) {
					rebound(true, false);
					mBall.rect.right(IDEAL_SIZE[0]-1);
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
				if(mBall.rect.bottom() > BOTTOM) {
					mBallInPlay = false;
					mLives--;
					if(mLives < 0) {
						newGame();
						return;
					}
				}
				
				for(int dy = -1; dy <= 1; dy++) {
					for(int dx = -1; dx <= 1; dx++) {
						if(Math.abs(dx) == Math.abs(dy)) continue;
						int bx = (int) Math.floor((mBall.rect.hSide(dx))/32);
						int by = (int) Math.floor((mBall.rect.vSide(dy))/16);
						if(by < 0 || by >= mBricks.length || bx < 0 || bx >= mBricks[by].length) continue;
						if(mBricks[by][bx] != 0) {
							if(dx * mBallDx >= 0 && dy * mBallDy >= 0) {
								if(mBricks[by][bx] < UNBREAKABLE_BLOCK || mBricks[by][bx] == HARD_BLOCK+4) {
									mBricks[by][bx] = 0;
									ballSound(R.raw.destroy);
									if(--mBricksLeft == 0) {
										if(mPatterns.get(mStage).awardsExtraBall()) {
											mLives++;
										}
										nextStage();
									}
								} else if (mBricks[by][bx] == UNBREAKABLE_BLOCK) {
									ballSound(R.raw.bounce1);
								} else {
									mBricks[by][bx]++;
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
		mBricksLeft = 0;
		for(int r = 0; r < mBricks.length; r++) {
        	for(int c = 0; c <  mBricks[r].length; c++) {
        		if(r >= mPatterns.get(mStage).height() || c >= mPatterns.get(mStage).width()) {
        			mBricks[r][c] = 0;
        		} else {
	        		mBricks[r][c] = mPatterns.get(mStage).get(c, r);
	        		if(mBricks[r][c] != 0 && mBricks[r][c] != UNBREAKABLE_BLOCK) {
	        			mBricksLeft++;
	        		}
        		}
        	}
        }
		mBallInPlay = false;
	}
	
	public void newGame() {
		newStage();
		mLives = 3;
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
			mBricks[y][x++] = i;
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
		mLives = savedInstanceState.getInt("lives");
	}

	public void saveState(Bundle outState) {
		int[] bricks = new int[mBricks.length*mBricks[0].length];
		int c = 0;
		for(int[] row: mBricks) {
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
		outState.putInt("lives", mLives);
		
	}
}
