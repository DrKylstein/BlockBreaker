package delaney.kyle.blockbreaker;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.SparseIntArray;

public class SoundManager {
	private SoundPool mSoundPool;
	private SparseIntArray mSounds;
	private SparseIntArray mPriorities;
	private Context mContext;
	
	public SoundManager(Context context, int maxStreams, int soundCount) {
		mContext = context;
        mSoundPool = new SoundPool(maxStreams, AudioManager.STREAM_MUSIC, 100);
        mSounds = new SparseIntArray(soundCount);
        mPriorities = new SparseIntArray(soundCount);
	}
	
	public SoundManager(Context context, int maxStreams) {
		this(context, maxStreams, 10);
	}
	
	public void load(int resID, int priority) {
		mSounds.put(resID, mSoundPool.load(mContext, resID, 1));
		mPriorities.put(resID, priority);
	}
	
	public void play(int id) {
		mSoundPool.play(mSounds.get(id), 1F, 1F, mPriorities.get(id), 0, 1F);
	}
	
	public void play(int id, float d) {
		mSoundPool.play(mSounds.get(id), 1-d, d, mPriorities.get(id), 0, 1F);
	}
}
