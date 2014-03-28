package delaney.kyle.blockbreaker;


import android.graphics.Canvas;
import android.graphics.Paint;

public class TextSprite {
	public RectOF rect;
	public float opacity;
	
	
	public TextSprite(String text, Paint paint) {
		mPaint = paint;
		rect = new RectOF();
		mText = text;
	}
	
	public void draw(Canvas canvas) {
		if(opacity < 0.01) return;
		mPaint.setAlpha((int)(opacity*255));
		canvas.drawText(mText, rect.x, rect.y, mPaint);
	}
	
	public void setText(String text) {
		mText = text;
		rect.width = mPaint.measureText(text);
	}
	
	
	private Paint mPaint;
	private String mText;
	
}
