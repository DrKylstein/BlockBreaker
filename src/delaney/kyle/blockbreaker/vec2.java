package delaney.kyle.blockbreaker;

import android.util.Log;

public class vec2 {
	public double x;
	public double y;
	private static final String TAG = "vec2";
	
	public vec2(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public vec2() {
		x = 0;
		y = 0;
	}
	
	public double get(int i) {
		if(i == 0) return x;
		if(i == 1) return y;
		throw new IndexOutOfBoundsException();
	}
	
	public void set(int i, double v) {
		if(i == 0) x = v;
		if(i == 1) y = v;
		throw new IndexOutOfBoundsException();
	}
	
	public void add(vec2 other) {
		x += other.x;
		y += other.y;
	}
	public void normalize() {
		double magnitude = Math.sqrt(x*x + y*y);
		x /= magnitude;
		y /= magnitude;
	}
	public vec2 scaled(double sc) {
		return new vec2(x*sc, y*sc);
	}
	public void scale(double sc) {
		x*=sc;
		y*=sc;
	}
}
