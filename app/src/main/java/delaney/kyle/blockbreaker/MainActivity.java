package delaney.kyle.blockbreaker;

import java.io.IOException;

import android.app.Activity;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;

public class MainActivity extends Activity {
	
	private static final String TAG = "MainActivity";
	private GameView gameView;
	private Game mGame;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		gameView = new GameView(this, null);
		setContentView(gameView);
		
		try {
			mGame = new Game(this);
		} catch (Exception e) {
			Log.e(TAG, Log.getStackTraceString(e));
			finish();
			return;
		}
		
		gameView.setGame(mGame);
		
        if (savedInstanceState != null) {
        	try {
				mGame.restoreState(savedInstanceState);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        } else {
        	mGame.newGame();
        }
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.in_game, menu);
		return true;
	}
	
	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		mGame.pause();
		return super.onMenuOpened(featureId, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.next:
	        	mGame.nextStage();
	            return true;
	        case R.id.prev:
	        	mGame.prevStage();
	            return true;
	        case R.id.restart:
	        	mGame.newGame();
		        return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mGame.onPause();
	}
	@Override
	protected void onResume() {
		super.onResume();
		mGame.onResume();
	}
	
	@Override
    protected void onSaveInstanceState(Bundle outState) {
        // just have the View's thread save its state into our Bundle
        super.onSaveInstanceState(outState);
        try {
			mGame.saveState(outState);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
