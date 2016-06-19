package delaney.kyle.blockbreaker;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {
	private Game mGame;
	private GameThread mThread;
	private SurfaceHolder mSurfaceHolder;
	public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // register our interest in hearing about changes to our surface
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        
        setFocusable(true); // make sure we get key events
    }
    
    public void setGame(Game g) {
    	mGame = g;
    }
    
    private class GameThread extends Thread {
    	public boolean running = true;
    	@Override
    	public void run() {
    		while(running) {
    			if(mGame == null) return;
    	        Canvas c = null;
    	        try {
    	            c = mSurfaceHolder.lockCanvas(null);
    	            if(c != null) {
	    	            synchronized (mSurfaceHolder) {
	    	                mGame.doDraw(c);
	    	            }
    	            }
    	        } finally {
    	            if (c != null) {
    	                mSurfaceHolder.unlockCanvasAndPost(c);
    	            }
    	        }
    	        mGame.updatePhysics();
    		}
    	}
    }
    
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
    	if(mGame != null) mGame.sizeChanged(width, height);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mThread = new GameThread();
        mThread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // we have to tell thread to shut down & wait for it to finish, or else
        // it might touch the Surface after we return and explode
        boolean retry = true;
        mThread.running = false;
        while (retry) {
            try {
                mThread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	if(event.getActionMasked() != MotionEvent.ACTION_DOWN) return false;
    	if(mGame == null) return false;
    	mGame.touchedAt(event.getX(), event.getY());
    	return true;
    }
    
    /*@Override
    protected void onMeasure(int w, int h) {
    	setMeasuredDimension(w>h?h:w, w>h?h:w);
    }*/
}
