package delaney.kyle.blockbreaker;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.SparseIntArray;

public class SoundManager {
	private SoundPool mSoundPool;
	private SparseIntArray mSounds;
	private Context mContext;
	
	public SoundManager(Context context, int maxStreams, int soundCount) {
		mContext = context;
        mSoundPool = new SoundPool(maxStreams, AudioManager.STREAM_MUSIC, 100);
        mSounds = new SparseIntArray(soundCount);
	}
	
	public SoundManager(Context context, int maxStreams) {
		this(context, maxStreams, 10);
	}
	
	public void load(int resID, int priority) {
		mSounds.put(resID, mSoundPool.load(mContext, resID, priority));
	}
	
	public void play(int id) {
		mSoundPool.play(mSounds.get(id), 1F, 1F, 1, 0, 1F);
	}
	
	public void play(int id, float pos) {
		mSoundPool.play(mSounds.get(id), 1-pos, pos, 1, 0, 1F);
	}
}
