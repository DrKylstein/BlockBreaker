package delaney.kyle.blockbreaker;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

public class TiltInput implements SensorEventListener {

	private float mRawX;
	private float mRawY;
	private float mX;
	private float mY;
	private Display mDisplay;
	private SensorManager mSensorManager;
	private Sensor mAccel;
	
	final float alpha = 0.8F;

	TiltInput(Context context) {
		mDisplay = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		mAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	}
	
	public void disconnect() {
		mSensorManager.unregisterListener(this, mAccel);
	}

	public void connect() {
		mSensorManager.registerListener(this, mAccel, SensorManager.SENSOR_DELAY_GAME);
	}
	
	public float getX() {
		return mX;
	}
	
	public float getY() {
		return mY;
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}

	@Override
	public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
            return;

        switch (mDisplay.getRotation()) {
            case Surface.ROTATION_0:
            	mRawX = event.values[0];
            	mRawY = event.values[1];
                break;
            case Surface.ROTATION_90:
            	mRawX = -event.values[1];
            	mRawY = event.values[0];
                break;
            case Surface.ROTATION_180:
            	mRawX = -event.values[0];
            	mRawY = -event.values[1];
                break;
            case Surface.ROTATION_270:
            	mRawX = event.values[1];
            	mRawY = -event.values[0];
                break;
        }

        // Isolate the force of gravity with the low-pass filter.
        mX = alpha * mX + (1 - alpha) * mRawX;
        mY = alpha * mY + (1 - alpha) * mRawY;
	}

}
