package delaney.kyle.blockbreaker;

import java.util.concurrent.TimeUnit;

import android.os.Bundle;

public class Paddle {
	public vec2 pos = new vec2();
	public static final double RADIUS = 20;
	public static final double HEIGHT = 4;
	private static final long SECOND = TimeUnit.SECONDS.toNanos(1);
	
	public void update(long dt, GameStatus gs) {
		double deltaX = gs.tiltPos - pos.x;
		
		pos.x += deltaX*10*dt/SECOND;
		//pos.y = PADDLE_Y;
		
		if(pos.x - RADIUS < 0) {
			pos.x = RADIUS;
		}
		if(pos.x + RADIUS > gs.bounds[0]) {
			pos.x = gs.bounds[0] - RADIUS;
		}
	}
	
	public void saveState(Bundle bundle) {
		bundle.putDouble("paddle_x", pos.x);
	}
	public void restoreState(Bundle bundle) {
		pos.x = bundle.getDouble("paddle_x");
	}
}
