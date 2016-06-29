package delaney.kyle.blockbreaker;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;

public class InGameRenderer implements Renderer {
	
	private final Paint mBgPaint;
	private final Paint mLinePaint;
	private final Paint mBallPaint;
	private final Paint mShadowPaint;
	
	private final int[] mBlockColors;
	
	private Paint mBlockPaint;
	
	private GameStatus gs;
	
	public InGameRenderer(Resources res, GameStatus gs) {
		this.gs = gs;
		
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inScaled = false;
		Bitmap colors = BitmapFactory.decodeResource(res, R.raw.block_colors, options);
		
		mBlockColors = new int[colors.getWidth()];
		
		for(int i = 0; i < colors.getWidth(); i++) {
			mBlockColors[i] = colors.getPixel(i, 0);
		}
        
        mBlockPaint = new Paint();
        mBgPaint = new Paint();
        mBgPaint.setColor(mBlockColors[0]);

		mLinePaint = new Paint();
		mLinePaint.setColor(0xFF000000);
		mLinePaint.setStyle(Paint.Style.STROKE);
		mLinePaint.setAntiAlias(true);

		mBallPaint = new Paint();
		mBallPaint.setColor(0xFF825147);
		mShadowPaint = new Paint();
		mShadowPaint.setColor(mBlockColors[18] & 0x7FFFFFFF);
	}
	
	@Override
	public void doDraw(Canvas canvas) {
		canvas.drawRect(0, 0, gs.bounds[0], gs.bounds[1], mBgPaint);
		
		canvas.save();
		canvas.translate((float) gs.ball.pos.x, (float) gs.ball.pos.y);
		canvas.drawCircle(1, 1, 5, mShadowPaint);
		canvas.drawCircle(0, 0, 5, mBallPaint);
		canvas.restore();
		
		canvas.save();
		for(int y = 0; y < gs.blocks.length; y++) {
			canvas.save();
			for(int x = 0; x < gs.blocks[0].length; x++) {
				if(gs.blocks[y][x] > 0) {
					canvas.drawRect(2, 2, gs.blockSize[0], gs.blockSize[1], mShadowPaint);
				}
				canvas.translate(gs.blockSize[0], 0);
			}
			canvas.restore();
			canvas.translate(0, gs.blockSize[1]);
		}
		canvas.restore();
		
		canvas.save();
		for(int y = 0; y < gs.blocks.length; y++) {
			canvas.save();
			for(int x = 0; x < gs.blocks[0].length; x++) {
				if(gs.blocks[y][x] > 0) {
					mBlockPaint.setColor(mBlockColors[gs.blocks[y][x]]);
					canvas.drawRect(1, 1, gs.blockSize[0]-1, gs.blockSize[1]-1, mBlockPaint);
					int cuts = Math.max(gs.blocks[y][x]-BlockId.HARD_BLOCK, 0);
					switch(cuts) {
						case 3:
							canvas.drawLine(gs.blockSize[0]/4, 0, gs.blockSize[0]/4, gs.blockSize[1], mLinePaint);
							canvas.drawLine(gs.blockSize[0]*3/4, 0, gs.blockSize[0]*3/4, gs.blockSize[1], mLinePaint);
						case 2:
							canvas.drawLine(0, gs.blockSize[1]/2, gs.blockSize[0], gs.blockSize[1]/2, mLinePaint);
						case 1:
							canvas.drawLine(gs.blockSize[0]/2, 0, gs.blockSize[0]/2, gs.blockSize[1], mLinePaint);
						default:
							break;
					} 
					/*if(gs.blocks[y][x] == BlockId.UNBREAKABLE_BLOCK) {
						mUnbreakableDecor.draw(canvas);
					} else {
						mBlockDecor.draw(canvas);
					}*/
				}
				canvas.translate(gs.blockSize[0], 0);
			}
			canvas.restore();
			canvas.translate(0, gs.blockSize[1]);
		}
		canvas.restore();
		
		canvas.save();
		canvas.translate(gs.bounds[0]-12, gs.bounds[1]-8);
		for(int i = 0; i < gs.balls; i++) {
			canvas.drawCircle(0, 0, 5, mBallPaint);
			canvas.translate(-12,0);
		}
		canvas.restore();
		
		if(gs.paused) {
			canvas.drawCircle(gs.tiltPos, (float)gs.paddle.pos.y-48, 8, mLinePaint);
		}
		canvas.drawLine(gs.tiltPos, (float) gs.paddle.pos.y - 64, gs.tiltPos, (float) gs.paddle.pos.y - 32, mLinePaint);
		
		canvas.save();
		canvas.translate((float) gs.paddle.pos.x, (float) gs.paddle.pos.y);
		canvas.drawRect(-22, 1, 24, 6, mShadowPaint);
		canvas.drawRect(-23, 0, 23, 5, mBallPaint);
		canvas.restore();
	}
}
