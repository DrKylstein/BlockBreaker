package delaney.kyle.blockbreaker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import delaney.kyle.blockbreaker.Ball.Listener;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;

class Game {
	private static final int[] BLOCKSIZE = {32, 16};
	private static final int[] SCREENSIZE = {15*BLOCKSIZE[0],(12+6)*BLOCKSIZE[1]};
	private static final long SECOND = TimeUnit.SECONDS.toNanos(1);
	private static final String TAG = "Game";
	
	private static final double PADDLE_Y = SCREENSIZE[1]-16-Paddle.HEIGHT;
	
	GameStatus gs = new GameStatus(SCREENSIZE, BLOCKSIZE);

	private List<PatternSet> mPatterns;
	
	private long mLastTime;
	private long mFreeTime;

	private Renderer mRenderer;
	private TiltInput mTilt;
	private SoundManager mSoundManager;
	
	private float mBlockScale;
	private float[] mOffset = new float[2];
	
	private boolean inMenu = true;
	
	private Resources res;
	
	private void loadPatterns(Resources res, File dir) throws NotFoundException, Exception {
		if(!dir.exists()) {
			dir.mkdir();
		}
		
		mPatterns = new ArrayList<PatternSet>();
		mPatterns.add(new PatternSet(res.openRawResource(R.raw.system)));
		
		for(File f : dir.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.toLowerCase(Locale.US).endsWith(".blc");
				}
			})) {
			Log.d(TAG, f.getName());
			mPatterns.add(new PatternSet(new FileInputStream(f)));
		}
		
		gs.setPatternFile(mPatterns.get(mPatterns.size()-1));
	}
	
	private void loadSounds(SoundManager soundMan) {
		soundMan.load(R.raw.bounce_paddle, 1);
		soundMan.load(R.raw.bounce_wall, 1);
		soundMan.load(R.raw.bounce_hard, 1);
		soundMan.load(R.raw.destroy, 2);
		soundMan.load(R.raw.crack, 2);
	}
	
	public Game(Context context) throws NotFoundException, Exception {
		res = context.getResources();
		loadPatterns(res, context.getExternalFilesDir(null));
		mRenderer = new MenuRenderer(res, SCREENSIZE, BLOCKSIZE, mPatterns);//new InGameRenderer(res, gs);
        mSoundManager = new SoundManager(context, 1, 5);
        loadSounds(mSoundManager);
        
		mTilt = new TiltInput(context);
        mLastTime = System.nanoTime();
        
    	gs.ball.drainListener = new Listener() {
    		@Override
    		public void run(vec2 pos) {
    			gs.served = false;
    			gs.balls--;
    			if(gs.balls < 0) {
    				newGame();
    				return;
    			}
    		}
    	};
    	gs.ball.blockListener = new Listener() {
    		@Override
    		public void run(vec2 pos) {
    			int by = (int) pos.y;
    			int bx = (int) pos.x;
				if(gs.blocks[by][bx] < BlockId.UNBREAKABLE_BLOCK || gs.blocks[by][bx] == BlockId.HARD_BLOCK+3) {
					gs.blocks[by][bx] = 0;
					ballSound(R.raw.destroy);
					if(--gs.blocksLeft == 0) {
						if(gs.stageAwardsBall()) {
							gs.balls++;
						}
						gs.nextStage();
						return;
					}
				} else if (gs.blocks[by][bx] == BlockId.UNBREAKABLE_BLOCK) {
					ballSound(R.raw.bounce_hard);
				} else {
					gs.blocks[by][bx]++;
					ballSound(R.raw.crack);
				}
    		}
    	};
    	gs.ball.paddleListener = new Listener() {
    		@Override
    		public void run(vec2 pos) {
    			ballSound(R.raw.bounce_paddle);
    		}
    	};
    	gs.ball.wallListener = new Listener() {
    		@Override
    		public void run(vec2 pos) {
    			ballSound(R.raw.bounce_wall);
    		}
    	};
	}

	public void sizeChanged(int width, int height) {
		mBlockScale = Math.min((float)width/(float)(SCREENSIZE[0]), (float)height/(float)(SCREENSIZE[1]));
		
		mOffset[0] = (width - SCREENSIZE[0]*mBlockScale)/2;
		mOffset[1] = (height - SCREENSIZE[1]*mBlockScale)/2;
	}
	
	public void doDraw(Canvas canvas) {
		canvas.drawColor(0xFF000000); //ARGB
		
		canvas.save();
		canvas.translate(mOffset[0], mOffset[1]);
		canvas.scale(mBlockScale, mBlockScale);
		canvas.clipRect(0, 0, SCREENSIZE[0], SCREENSIZE[1]);
		
		mRenderer.doDraw(canvas);
		
		canvas.restore();
	}

	private void ballSound(int id) {
		mSoundManager.play(id, (float)(gs.ball.pos.x/SCREENSIZE[0]));
	}
	
	public void updatePhysics() {
		long currentTime = System.nanoTime();
		long dt = currentTime - mLastTime;
		mLastTime = currentTime;
		
		gs.tiltPos = SCREENSIZE[0]/2 + mTilt.getX()*SCREENSIZE[0]/-10;
		
		if(!gs.paused) {
			mFreeTime += dt;
			
			dt = SECOND/120;
			while(mFreeTime > dt) {
				mFreeTime -= dt;
				gs.paddle.pos.y = PADDLE_Y;
				gs.paddle.update(dt, gs);
				gs.ball.update(dt, gs);
			}
		}
	}

	public void nextStage() {
		gs.nextStage();
	}
	public void prevStage() {
		gs.prevStage();
	}

	public void pause() {
		gs.paused = true;
	}
	
    public void touchedAt(float x, float y) {
    	x /= mBlockScale;
    	y /= mBlockScale;
    	if(inMenu) {
    		int i = (int)(x/(SCREENSIZE[0]/3));
    		Log.d(TAG, i+"");
    		if(i < mPatterns.size()) {
    			gs.setPatternFile(mPatterns.get(i));
    			inMenu = false;
    			mRenderer = new InGameRenderer(res, gs);
    			return;
    		}
    	} else {
	    	if(gs.paused) {
	    		gs.paused = false;
	    	} else if(!gs.served) {
	    		gs.ball.vel.y = -1;
	    		gs.ball.vel.x = 1;
	    		gs.ball.vel.normalize();
	    		gs.served = true;
	    	} else {
	    		gs.paused = true;
	    	}
    	}
    }
	
	public void onPause() {
		mTilt.disconnect();
		gs.paused = true;
	}

	public void onResume() {
		mTilt.connect();
		mLastTime = System.nanoTime();
	}
	
	public void newGame() {
		gs.newGame();
	}
	
	public void restoreState(Bundle bundle) throws Exception {
		inMenu = bundle.getBoolean("in_menu");
		if(!inMenu) {
			mRenderer = new InGameRenderer(res, gs);
		}
		gs.restoreState(bundle);
	}

	public void saveState(Bundle bundle) throws IOException {
		bundle.putBoolean("in_menu", inMenu);
		gs.saveState(bundle);
	}
}
