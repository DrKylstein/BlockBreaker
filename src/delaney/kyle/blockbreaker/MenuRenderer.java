package delaney.kyle.blockbreaker;

import java.util.List;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

public class MenuRenderer implements Renderer {

	private static final String TAG = "MenuRenderer";
	
	private List<PatternSet> mPatternSets;
	private final int[] mScreenSize;
	private final int[] mBlockSize;

	private final int[] mBlockColors;
	
	private Paint mBlockPaint = new Paint();
	
	public MenuRenderer(Resources res, int[] size, int[] blockSize, List<PatternSet> patternSets) {
		mPatternSets = patternSets;
		mScreenSize = size;
		mBlockSize = blockSize;
		
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inScaled = false;
		Bitmap colors = BitmapFactory.decodeResource(res, R.raw.block_colors, options);
		
		mBlockColors = new int[colors.getWidth()];
		
		for(int i = 0; i < colors.getWidth(); i++) {
			mBlockColors[i] = colors.getPixel(i, 0);
		}
		
		mBlockPaint.setAntiAlias(true);
	}
	
	@Override
	public void doDraw(Canvas canvas) {
		//Log.d(TAG, String.format("%d %d %d", mPatternSets.size(), mScreenSize[0],  mBlockSize[0]));
		canvas.save();
		canvas.translate(0,mScreenSize[1]/2.0F - mScreenSize[1]/6.0F);
		canvas.scale(1.0F/3.0F, 1.0F/3.0F);
		for(int i = 0; i < Math.min(mPatternSets.size(), 3); i++) {
			PatternSet.Pattern pattern = mPatternSets.get(i).get(0);
			canvas.save();
			for(int y = 0; y < pattern.height(); y++) {
				canvas.save();
				for(int x = 0; x < pattern.width(); x++) {
					if(pattern.get(x,y) > 0) {
						//Log.d(TAG, "Ping!");
						mBlockPaint.setColor(mBlockColors[pattern.get(x,y)]);
						canvas.drawRect(1, 1, mBlockSize[0]-1, mBlockSize[1]-1, mBlockPaint);
					}
					canvas.translate(mBlockSize[0], 0);
				}
				canvas.restore();
				canvas.translate(0, mBlockSize[1]);
			}
			canvas.restore();
			canvas.translate(mScreenSize[0], 0);
		}
		canvas.restore();
	}

}
