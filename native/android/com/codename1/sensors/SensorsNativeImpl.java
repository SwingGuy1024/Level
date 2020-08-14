package com.codename1.sensors;

import java.util.Hashtable;

import com.codename1.impl.android.AndroidNativeUtil;
import android.hardware.*;
import android.content.Context;

import static com.codename1.sensors.SensorType3D.*;

//import java.util.Set;
//import java.util.TreeSet;

/**
 */
public class SensorsNativeImpl {

	/*
	 * Here's the output from the code to print the system properties:
	 * I/System.out( 6053): System.out: System Property http.agent: Dalvik/2.1.0 (Linux; U; Android 5.1; XT1528 Build/LPI23.29-17.5)
	 * I/System.out( 6053): System.out: System Property java.io.tmpdir: /data/data/com.neptunedreams.vulcan/cache
	 * I/System.out( 6053): System.out: System Property user.home: 
	 * I/System.out( 6053): System.out: Total of 3 properties
	 * 
	 * TODO: See the todo in SensorsManager.
	 */

//	static {
//		java.util.Properties props = System.getProperties();
//		Set<Object> keySet = new TreeSet<Object>(props.keySet());
//		for (Object o : keySet) {
//			keySet.add(o.toString());
//		}
//		for (Object key : keySet) {
//			System.out.println("System.out: System Property " + key + ": " + props.get(key));
//		}
//		System.out.println("System.out: Total of " + keySet.size() + " properties");
//	}

	private final Hashtable<Integer, SensorManager> nativeManagerList = new Hashtable<Integer, SensorManager>();
	private final Hashtable<Integer, SensorInfo> sensorMap = new Hashtable<Integer, SensorInfo>();

	private android.hardware.SensorEventListener sensorListener;

//	public float getResolution(int type) {
//		return sensorMap.get(type).sensor.getResolution();
//	}

	/**
	 * Returns the long interval in microseconds
	 * @return The long interval in microseconds
	 */
	public int getLongInterval(int type) {
		return sensorMap.get(type).sensor.getMaxDelay();
	}

	/**
	 * Returns the short interval in microseconds
	 * @return The short interval in microseconds
	 */
	public int getShortInterval(int type) {
		return sensorMap.get(type).sensor.getMinDelay();
	}

	public void setInterval(int type, int delayMicroSeconds) {
		sensorMap.get(type).delay = delayMicroSeconds;
	}

//	public String getStringType(int type) {
//		return sensorMap.get(type).sensor.getStringType();
//	}

	public boolean initSensor(int type) {
		SensorManager sensorManager = nativeManagerList.get(type);
		if (sensorManager == null) {
			sensorManager = (SensorManager) AndroidNativeUtil.getActivity().getSystemService(Context.SENSOR_SERVICE);
			nativeManagerList.put(type, sensorManager);
		}
		SensorInfo sInfo = sensorMap.get(type);
		boolean success = true;
		if (sInfo == null) {
			success = false;
			// getDefaultSensor returns null if the sensor is not found.
			Sensor sensor = sensorManager.getDefaultSensor(toNativeSensorConstant(type));
			if (sensor != null) {
				success = true;
				sensorMap.put(type, new SensorInfo(sensor, type));
			}
		}
		return success;
	}

	private int toNativeSensorConstant(int typeId) {
		SensorType3D type = SensorType3D.get(typeId);
		assert type != null;
		switch (type) {
			case gyroscope:
				return Sensor.TYPE_GYROSCOPE;
			case accelerometer:
				return Sensor.TYPE_ACCELEROMETER;
			case magnetic:
				return Sensor.TYPE_MAGNETIC_FIELD;
			default:
				throw new IllegalArgumentException("Illegal type: " + type);
		}
	}

	public void deregisterListener(int type) {
		SensorManager sensorManager = nativeManagerList.get(type);
		sensorManager.unregisterListener(sensorListener);
	}

	public void registerListener(final int typeId) {
		sensorListener = new android.hardware.SensorEventListener() {

			public void onSensorChanged(SensorEvent event) {
//				SensorType3D type = SensorType3D.get(typeId);
				com.codename1.sensors.SensorsManager.onSensorChanged(typeId,
						event.values[0], event.values[1], event.values[2]);
			}

			public void onAccuracyChanged(Sensor sensor, int accuracy) {
			}

		};
		SensorManager sensorManager = nativeManagerList.get(typeId);
		SensorInfo info = sensorMap.get(typeId);
		Sensor sensor = info.sensor;
		
		// Android docs say that the 3rd parameter may be an interval in microseconds. This is only supported in 
		// Android 2.3 and beyond. But CodenameOne only supports Android 2.6 and beyond, so we can do that if we want.
		sensorManager.registerListener(sensorListener, sensor, info.delay);
	}

	public boolean isSupported() {
		return true;
	}

	/**
	 *  The SensorInfo class lets us store a delay with a Sensor instance, to implement the setInterval() method.
	 */
	private class SensorInfo {
		final Sensor sensor;
		int delay;
		final int sType;

		// todo: Mark sensor @Nullable
		SensorInfo(Sensor sensor, int type) {
			super();
			sType = type;
			this.sensor = sensor;
			delay = 200000; // microseconds
		}

		@Override
		public int hashCode() {
			return sType;
		}

		@Override
		public boolean equals(final Object other) {
			if (other instanceof SensorInfo) {
				SensorInfo otherInfo = (SensorInfo) other;
				return this.sType == otherInfo.sType;
			}
			return false;
		}
	}

	public boolean useEllipseWorkaround() { return false; }
}
