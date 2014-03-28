package delaney.kyle.blockbreaker;

public class RectOF {
	public float x, y;
	public float width, height;

	public RectOF(float width, float height, float x, float y) {
		this.x = x; this.y = y; this.width = width; this.height = height;
	}
	
	public RectOF(float width, float height) {
		this(width, height, 0, 0);
	}
	
	public RectOF() {
		this(0,0);
	}
	
	public float centerX() {
		return x + width/2;
	}
	public void centerX(float x) {
		this.x = x - width/2;
	}
	public float left() {
		return x;
	}
	public void left(float x) {
		this.x = x;
	}
	public float right() {
		return x + width - 1;
	}
	public void right(float x) {
		this.x = x - width + 1;
	}
	public float centerY() {
		return y + height/2;
	}
	public void centerY(float y) {
		this.y = y - height/2;
	}
	public float top() {
		return y;
	}
	public void top(float y) {
		this.y = y;
	}
	public float bottom() {
		return y + height - 1;
	}
	public void bottom(float y) {
		this.y = y - height + 1;
	}

	public float hSide(int dx) {
		if(dx < 0) return left();
		if(dx > 0) return right();
		return centerX();
	}
	public float vSide(int dy) {
		if(dy < 0) return top();
		if(dy > 0) return bottom();
		return centerY();
	}
	
	public void hSide(int dx, float v) {
		if(dx < 0) left(v);
		else if(dx > 0) right(v);
		else  centerX(v);
	}
	public void vSide(int dy, float v) {
		if(dy < 0) top(v);
		else if(dy > 0) bottom(v);
		else  centerY(v);
	}
}
