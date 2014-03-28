package delaney.kyle.blockbreaker;

import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParser;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.graphics.RectF;

public class Sprite {
	
	public RectOF rect;
	
	public Sprite(Resources resources, int id) {
		mSVG = SVGParser.getSVGFromResource(resources, id);
		mPic = mSVG.getPicture();
		mBounds = mSVG.getLimits();
		rect = new RectOF(mBounds.width(), mBounds.height());
	}
	
	public void draw(Canvas canvas) {
		canvas.save();
		canvas.translate(rect.x-mBounds.left, rect.y-mBounds.top);
		canvas.drawPicture(mPic);
		canvas.restore();
	}
	
	private SVG mSVG;
	private Picture mPic;
	private RectF mBounds;
}
