package delaney.kyle.blockbreaker;

import java.util.concurrent.TimeUnit;

import android.os.Bundle;

public class Ball {
	public vec2 vel = new vec2();
	public vec2 pos = new vec2();
	public static final double SPEED = 150;
	public static final double RADIUS = 3.5;
	public static final double ANGLE = 0.9;
	private static final double RANDOM_SKEW = 0.01;
	private static final long SECOND = TimeUnit.SECONDS.toNanos(1);
	
	public static interface Listener {
		void run(vec2 pos);
	}
	
	Listener drainListener;
	Listener blockListener;
	Listener paddleListener;
	Listener wallListener;
	
	public void update(long dt, GameStatus gs) {
		if(!gs.served) {
			gs.ball.pos.x = gs.paddle.pos.x;
			gs.ball.pos.y = gs.paddle.pos.y - Ball.RADIUS;
		} else {
			if(pos.x+Ball.RADIUS > gs.bounds[0]) {
				rebound(true, false);
				pos.x = gs.bounds[0]-Ball.RADIUS;
				wallListener.run(pos);
			}
			if(pos.x-Ball.RADIUS < 0) {
				rebound(true, false);
				pos.x = Ball.RADIUS;
				wallListener.run(pos);
			}
			if(pos.y+Ball.RADIUS > gs.paddle.pos.y &&
					pos.x-Ball.RADIUS < gs.paddle.pos.x+Paddle.RADIUS &&
					pos.x+Ball.RADIUS > gs.paddle.pos.x-Paddle.RADIUS) {
				vel.x = (pos.x - gs.paddle.pos.x)/(Paddle.RADIUS*Ball.ANGLE);
				vel.y = 0 - Math.abs(vel.y);
				vel.normalize();
				pos.y = gs.paddle.pos.y-Ball.RADIUS-1;
				paddleListener.run(pos);
			}
			if(pos.y-Ball.RADIUS < 0) {
				rebound(false, true);
				pos.y = Ball.RADIUS;
				wallListener.run(pos);
			}
			if(pos.y+Ball.RADIUS > gs.bounds[1]) {
				drainListener.run(pos);
				if(!gs.served) return;
			}
			
			for(int dy = -1; dy <= 1; dy++) {
				for(int dx = -1; dx <= 1; dx++) {
					if(Math.abs(dx) == Math.abs(dy)) continue;
					int bx = (int) Math.floor((pos.x + Ball.RADIUS*dx)/gs.blockSize[0]);
					int by = (int) Math.floor((pos.y + Ball.RADIUS*dy)/gs.blockSize[1]);
					if(by < 0 || by >= gs.blocks.length || bx < 0 || bx >= gs.blocks[by].length) continue;
					if(gs.blocks[by][bx] != 0) {
						if(dx * vel.x >= 0 && dy * vel.y >= 0) {
							blockListener.run(new vec2(bx,by));
							if(!gs.served) return;
						}
						rebound(dx * vel.x > 0, dy * vel.y > 0);
					}
				}
			}
			pos.x += vel.x*Ball.SPEED*dt/SECOND;
			pos.y += vel.y*Ball.SPEED*dt/SECOND;
		}
	}
	
	public void rebound(boolean x, boolean y) {
		if(y) {
			vel.y *= -1;
		}
		if(x) {
			vel.x *= -1;
		}
		vel.x += (Math.random() - 0.5) * RANDOM_SKEW;
		vel.y += (Math.random() - 0.5) * RANDOM_SKEW;
		vel.normalize();
	}
	
	public void restoreState(Bundle savedInstanceState) {
		vel.x = savedInstanceState.getDouble("ball_dx");
		vel.y = savedInstanceState.getDouble("ball_dy");
		pos.x = savedInstanceState.getDouble("ball_x");
		pos.y = savedInstanceState.getDouble("ball_y");
	}
	
	public void saveState(Bundle outState) {
		outState.putDouble("ball_dx", vel.x);
		outState.putDouble("ball_dy", vel.y);
		outState.putDouble("ball_x", pos.x);
		outState.putDouble("ball_y", pos.y);
	}

}
