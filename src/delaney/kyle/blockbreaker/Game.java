package delaney.kyle.blockbreaker;

import java.io.File;

import com.larvalabs.svgandroid.SVGParser;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Picture;
import android.os.Bundle;

class Game {

	private File mPatternsDir;
	
	private TiltInput mTilt;
	private SoundManager mSoundManager;
	
	private float mBlockScale;
	
	private long mLastTime;
	
	private float[] mOffset = new float[2];
	
	private static final int[] BLOCKSIZE = {32, 16};
	private static final int[] SCREENSIZE = {15*BLOCKSIZE[0],(12+6)*BLOCKSIZE[1]};

	
	private static final long SECOND = 1000000000;
	
	private float tiltPos;
	
	private vec2 mBallVel = new vec2();
	private vec2 mBallPos = new vec2();
	private static final double BALL_SPEED = 150;
	private static final double BALL_RADIUS = 3.5;
	private static final double BALL_ANGLE = 0.9;
	
	private vec2 mPaddlePos = new vec2();
	private static final double PADDLE_RADIUS = 20;
	private static final double PADDLE_HEIGHT = 4;
	private static final double PADDLE_Y = SCREENSIZE[1]-16-PADDLE_HEIGHT;
	
	private int mBalls;
	private int mStage;
	private int mBlocksLeft;
	
	private boolean mBallInPlay;
	private boolean mPaused;
	
	private int[][] mBlocks;

	private Paint mBlockPaint;
	private Paint mBallPaint;
	private Paint mBgPaint;
	private Paint mLinePaint;
	
	private Picture mPaddleSprite;
	private Picture mBallSprite;
	private Picture mBlockDecor;
	private Picture mUnbreakableDecor;
	
	private static final int HARD_BLOCK = 0x0E;
	private static final int UNBREAKABLE_BLOCK = 0x0D;
	
	
	private int[] mBlockColors;


	
	private PatternFile mPatterns;
	private long mFreeTime;

	private String TAG = "Game";
	
	public Game(Context context) throws NotFoundException, Exception {
        
		mPatternsDir = context.getExternalFilesDir(null);
		
		
        Resources res = context.getResources();
		
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inScaled = false;
		Bitmap colors = BitmapFactory.decodeResource(res, R.raw.block_colors, options);
		
		mBlockColors = new int[colors.getWidth()];
		
		for(int i = 0; i < colors.getWidth(); i++) {
			mBlockColors[i] = colors.getPixel(i, 0);
		}
        
        mBlockPaint = new Paint();
        mBgPaint = new Paint();
        mBgPaint.setColor(mBlockColors[0]);
		mBallPaint = new Paint();
		mBallPaint.setColor(0xFF880000);
		mBallPaint.setAntiAlias(true);
		mLinePaint = new Paint();
		mLinePaint.setColor(0xFF000000);
		mLinePaint.setStyle(Paint.Style.STROKE);
		mLinePaint.setAntiAlias(true);


		mPaddleSprite = SVGParser.getSVGFromResource(res, R.raw.paddle).getPicture();
		mBallSprite = SVGParser.getSVGFromResource(res, R.raw.ball).getPicture();
		mBlockDecor = SVGParser.getSVGFromResource(res, R.raw.blockdecor).getPicture();
		mUnbreakableDecor = SVGParser.getSVGFromResource(res, R.raw.unbreakabledecor).getPicture();

		
		
		mPatterns = new PatternFile(res.openRawResource(R.raw.system));

		mTilt = new TiltInput(context);
        
        mLastTime = System.nanoTime();
        
        mBallVel.x = 0;
        mBallVel.y = -100;
        
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
		canvas.drawRect(0, 0, SCREENSIZE[0], SCREENSIZE[1], mBgPaint);
		
		canvas.save();
		canvas.translate((float)mBallPos.x, (float)mBallPos.y);
		mBallSprite.draw(canvas);
		canvas.restore();
		
		canvas.save();
		for(int y = 0; y < mBlocks.length; y++) {
			canvas.save();
			for(int x = 0; x < mBlocks[0].length; x++) {
				if(mBlocks[y][x] > 0) {
					mBlockPaint.setColor(mBlockColors[18]&0x7FFFFFFF);
					canvas.drawRect(2, 2, BLOCKSIZE[0]+2, BLOCKSIZE[1]+2, mBlockPaint);
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
					switch(cuts) {
						case 3:
							canvas.drawLine(BLOCKSIZE[0]/4, 0, BLOCKSIZE[0]/4, BLOCKSIZE[1], mBgPaint);
							canvas.drawLine(BLOCKSIZE[0]*3/4, 0, BLOCKSIZE[0]*3/4, BLOCKSIZE[1], mBgPaint);
						case 2:
							canvas.drawLine(0, BLOCKSIZE[1]/2, BLOCKSIZE[0], BLOCKSIZE[1]/2, mBgPaint);
						case 1:
							canvas.drawLine(BLOCKSIZE[0]/2, 0, BLOCKSIZE[0]/2, BLOCKSIZE[1], mBgPaint);
						default:
							break;
					} 
					if(mBlocks[y][x] == UNBREAKABLE_BLOCK) {
						mUnbreakableDecor.draw(canvas);
					} else {
						mBlockDecor.draw(canvas);
					}
				}
				canvas.translate(BLOCKSIZE[0], 0);
			}
			canvas.restore();
			canvas.translate(0, BLOCKSIZE[1]);
		}
		canvas.restore();
		
		/*canvas.drawRect((float)(mPaddlePos.x-PADDLE_RADIUS), (float)mPaddlePos.y, 
				(float)(mPaddlePos.x+PADDLE_RADIUS), (float)(mPaddlePos.y+PADDLE_HEIGHT), mBallPaint);*/
		
		//canvas.drawCircle((float)mBallPos.x, (float)mBallPos.y, (float)BALL_RADIUS, mBallPaint);
		
		canvas.save();
		canvas.translate(SCREENSIZE[0]-12, SCREENSIZE[1]-8);
		for(int i = 0; i < mBalls; i++) {
			mBallSprite.draw(canvas);
			//canvas.drawCircle(0, 0, (float)BALL_RADIUS, mBallPaint);
			canvas.translate(-12,0);
		}
		canvas.restore();
		
		if(mPaused) {
			canvas.drawCircle(tiltPos, (float)PADDLE_Y-48, 8, mLinePaint);
		}
		canvas.drawLine(tiltPos, (float)PADDLE_Y-64, tiltPos, (float)PADDLE_Y-32, mLinePaint);
		
		
		canvas.save();
		canvas.translate((float)mPaddlePos.x, (float)mPaddlePos.y);
		mPaddleSprite.draw(canvas);
		canvas.restore();
		
		
		canvas.restore();
	}

	private void ballSound(int id) {
		mSoundManager.play(id, (float)(mBallPos.x/SCREENSIZE[0]));
	}
	
	private void rebound(boolean x, boolean y) {
		if(y) {
			mBallVel.y = -mBallVel.y;// + (float)(Math.random() - 0.5)*10;
		}
		if(x) {
			mBallVel.x = -mBallVel.x;// + (float)(Math.random() - 0.5)*10;
		}
	}
	
	public void updatePhysics() {
		long currentTime = System.nanoTime();
		long dt = currentTime - mLastTime;
		mLastTime = currentTime;
		
		tiltPos = SCREENSIZE[0]/2 + mTilt.getX()*SCREENSIZE[0]/-10;
		
		if(mPaused) return;
		mFreeTime += dt;
		
		dt = SECOND/120;
		while(mFreeTime > dt) {
			mFreeTime -= dt;

			
			
			double deltaX = tiltPos - mPaddlePos.x;
			
			mPaddlePos.x += deltaX*10*dt/SECOND;
			mPaddlePos.y = PADDLE_Y;
			
			if(mPaddlePos.x - PADDLE_RADIUS < 0) {
				mPaddlePos.x = PADDLE_RADIUS;
			}
			if(mPaddlePos.x + PADDLE_RADIUS > SCREENSIZE[0]) {
				mPaddlePos.x = SCREENSIZE[0] - PADDLE_RADIUS;
			}			
			
			if(!mBallInPlay) {
				mBallPos.x = mPaddlePos.x;
				mBallPos.y = mPaddlePos.y - BALL_RADIUS;
			} else {
				if(mBallPos.x+BALL_RADIUS > SCREENSIZE[0]) {
					rebound(true, false);
					mBallPos.x = SCREENSIZE[0]-BALL_RADIUS;
					ballSound(R.raw.bounce1);
				}
				if(mBallPos.x-BALL_RADIUS < 0) {
					rebound(true, false);
					mBallPos.x = BALL_RADIUS;
					ballSound(R.raw.bounce1);
				}
				if(mBallPos.y+BALL_RADIUS > mPaddlePos.y &&
						mBallPos.x-BALL_RADIUS < mPaddlePos.x+PADDLE_RADIUS &&
						mBallPos.x+BALL_RADIUS > mPaddlePos.x-PADDLE_RADIUS) {
					mBallVel.x = (mBallPos.x - mPaddlePos.x)/(PADDLE_RADIUS*BALL_ANGLE);
					mBallVel.y = 0 - Math.abs(mBallVel.y);
					mBallVel.normalize();
					mBallPos.y = mPaddlePos.y-BALL_RADIUS-1;
					ballSound(R.raw.bounce1);
				}
				if(mBallPos.y-BALL_RADIUS < 0) {
					rebound(false, true);
					mBallPos.y = BALL_RADIUS;
					ballSound(R.raw.bounce1);
				}
				if(mBallPos.y+BALL_RADIUS > SCREENSIZE[1]) {
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
						int bx = (int) Math.floor((mBallPos.x + BALL_RADIUS*dx)/BLOCKSIZE[0]);
						int by = (int) Math.floor((mBallPos.y + BALL_RADIUS*dy)/BLOCKSIZE[1]);
						if(by < 0 || by >= mBlocks.length || bx < 0 || bx >= mBlocks[by].length) continue;
						if(mBlocks[by][bx] != 0) {
							if(dx * mBallVel.x >= 0 && dy * mBallVel.y >= 0) {
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
							rebound(dx * mBallVel.x > 0, dy * mBallVel.y > 0);
						}
					}
				}

				mBallPos.x += mBallVel.x*BALL_SPEED*dt/SECOND;
				mBallPos.y += mBallVel.y*BALL_SPEED*dt/SECOND;
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
    		mBallVel.y = -1;
    		mBallVel.x = 1;
    		mBallVel.normalize();
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
		
		mBlocksLeft = savedInstanceState.getInt("blocks_left");
		
		mBallVel.x = savedInstanceState.getDouble("ball_dx");
		mBallVel.y = savedInstanceState.getDouble("ball_dy");
		mBallPos.x = savedInstanceState.getDouble("ball_x");
		mBallPos.y = savedInstanceState.getDouble("ball_y");
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
		outState.putInt("blocks_left", mBlocksLeft);
		outState.putDouble("ball_dx", mBallVel.x);
		outState.putDouble("ball_dy", mBallVel.y);
		outState.putDouble("ball_x", mBallPos.x);
		outState.putDouble("ball_y", mBallPos.y);
		outState.putBoolean("ball_in_play", mBallInPlay);
		
		outState.putInt("stage", mStage);
		outState.putInt("lives", mBalls);
		
	}
}
