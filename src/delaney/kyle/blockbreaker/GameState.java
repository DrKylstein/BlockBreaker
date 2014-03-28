package delaney.kyle.blockbreaker;

import android.graphics.Canvas;

public interface GameState {
	public void updatePhysics();
	public void draw(Canvas canvas);
	public void onTouch();
	public void onPause();
	public void onResume();
}
