package delaney.kyle.blockbreaker;

import android.graphics.Canvas;
import android.os.Bundle;

public abstract class GameState {
	public abstract void updatePhysics();
	public abstract void doDraw(Canvas canvas);
	public abstract void sizeChanged(int w, int h);
	public abstract void touchedAt(float x, float y);
	public abstract void onPause();
	public abstract void onResume();
	public void saveState(Bundle bundle) {}
	public void restoreState(Bundle bundle) {}
}
