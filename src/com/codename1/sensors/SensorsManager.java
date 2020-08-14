/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.codename1.sensors;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import com.codename1.system.NativeLookup;
import com.codename1.ui.Display;
import com.neptunedreams.Assert;
import com.neptunedreams.util.NotNull;
import com.neptunedreams.util.SensorException;

import static com.codename1.sensors.SensorType3D.*;

/**
 * This is the SensorsManager
 * This is written to support only a single listener per manager. It should be rewritten to support multiple listeners.
 * Original: https://github.com/chen-fishbein/sensors-codenameone
 *
 * @author Chen
 */
@SuppressWarnings("unused")
public final class SensorsManager {

	@NotNull
	private SensorType3D type;
	
	@SuppressWarnings({"NullableProblems", "StaticNonFinalField"})
	@NotNull
	private static SensorsNative sensorsNative;

	/**
	 * For Android, listenerList is not necessary. It's probably not necessary for other platforms either, but we 
	 * should keep it around, because it allows us to put platform-related questions in the SensorsNative implementation
	 * instead of here. However, there is a case to be made for moving this to the SensorsNative implementation.
	 */
	private final List<SensorListener> listenerList = new LinkedList<>();

	@NotNull
	private static final Hashtable<SensorType3D, SensorsManager> managerMap = new Hashtable<>();

	private SensorsManager(@NotNull SensorType3D type) {
		this.type = type;
	}
	
	/**
	 * Returns SensorsManager instance.
	 *
	 * @param type one of the following TYPE_GYROSCOPE, TYPE_ACCELEROMETER
	 *             or TYPE_MAGNETIC
	 * @return SensorsManager instance or null if this sensor does not exist on
	 * the device.
	 * @throws SensorException if this platform or sensor is not supported.
	 */
	@NotNull
	public static SensorsManager getSenorsManager(@NotNull SensorType3D type) {
		if (type == simulator) {
			return createSimulatorManager();
		}
		//noinspection ConstantConditions
		if (sensorsNative == null) {
			sensorsNative = NativeLookup.create(SensorsNative.class);
			//noinspection ConstantConditions
			if (sensorsNative == null) {
				throw new SensorException("Platform not supported");
			}
			boolean failure = !sensorsNative.initSensor(type.id());
			if (failure) {
				throw new SensorException("Specified sensor not supported on this device.");
			}
		}
//        sensorMap.put(type, sensorsNative);
//        } else {
//            manager = managerMap.get(type); // May return null.
//        }
		
		SensorsManager sensorsManager = managerMap.get(type);
		if (sensorsManager == null) {
			sensorsManager = new SensorsManager(type);
			managerMap.put(type, sensorsManager);
		}
		return sensorsManager;
	}

	/**
	 * Returns the long interval in microseconds
	 * @return The long interval in microseconds
	 */
	public int getLongInterval() { return sensorsNative.getLongInterval(type.id()); }

	/**
	 * Returns the short interval in microseconds
	 * @return the short interval in microseconds
	 */
	public int getShortInterval() { return sensorsNative.getShortInterval(type.id()); }
	
	public void setInterval(int intervalMilliSeconds) {
		sensorsNative.setInterval(type.id(), intervalMilliSeconds * 1000);
	}

	/**
	 * Registers a SensorListener to get sensor notifications from the device, using the default delay, or the
	 * delay specified by the setInterval() method.
	 */
	public void registerListener(SensorListener listener) {
		if (listener != null) {
//      Log.p("SMgr: Sensors MGR.registerListener: " + type);
//			Log.p("SMgr:   Listener: " + listener);
			if (listenerList.isEmpty()) {
//				Log.p("SMgr:   * * * ACTUAL Registration * * *");
				sensorsNative.registerListener(type.id());
			}
			listenerList.add(listener);
//			Log.p("SMgr:   total of " + listenerList.size() + " listeners");
		}
	}

	/**
	 * De-registers a SensorListener from getting callbacks from the device
	 */
	public void deregisterListener(SensorListener listener) {
//    Log.p("SMgr: De-registering " + (listener == null ? "null" : "valid") +" listener of " + listenerList.size() + " listeners" );
//		Log.p("SMgr:    Listener: " + listener);
		if (listener != null) {
			listenerList.remove(listener);
//			Log.p("SMgr: De-registered. Down to " + listenerList.size() + " listeners");
			if (listenerList.isEmpty()) {
	//            Log.p("No more listeners, De-registering for type.");
//				Log.p("SMgr:   # # # actual de-registration # # #");
				sensorsNative.deregisterListener(type.id());
			}
		}
	}

	/**
	 * This method is used by the underlying native platform. It is NOT called on EDT.
	 */
	public static void onSensorChanged(int typeId, final float x, final float y, final float z) {
		SensorType3D type = SensorType3D.get(typeId);
		final long timeStamp = System.currentTimeMillis();
		SensorsManager manager = managerMap.get(type);
//        if (timeStamp > manager.nextAllowedTime) {
//            manager.nextAllowedTime = timeStamp + interval;
		Assert.doAssert(manager != null);
		processListeners(x, y, z, timeStamp, manager);
//        }
//        if (type == TYPE_ACCELEROMETER) {
//            processListeners(x, y, z, timeStamp, accel);
//        } else if (type == TYPE_GYROSCOPE) {
//            processListeners(x, y, z, timeStamp, gyro);
//        }else if (type == TYPE_MAGNETIC) {
//            processListeners(x, y, z, timeStamp, magnetic);
//        }
	}

	private static void processListeners(final float x, final float y, final float z, final long timeStamp, final SensorsManager sensorsManager) {
		// This doesn't run on the EDT.
		if (sensorsManager != null) {
			final Display instance = Display.getInstance();
			for (final SensorListener listener : sensorsManager.listenerList) {
				instance.callSerially(() -> listener.onSensorChanged(timeStamp, x, y, z));
			}
		}
	}

	@NotNull
	private static SensorsManager createSimulatorManager() {
		final SensorsManager sensorsManager = new SensorsManager(accelerometer);
		managerMap.put(accelerometer, sensorsManager);
		sensorsNative = makeSimulatorSensorsNative();
		return sensorsManager;
	}
	
	private boolean drawNative() { return sensorsNative.useEllipseWorkaround(); }
	
	public static boolean useEllipseWorkaround() {
		// Any sensor type will do.
		return getSenorsManager(SensorType3D.accelerometer).drawNative();
	}

	@SuppressWarnings("MagicNumber")
	@NotNull
	private static SensorsNative makeSimulatorSensorsNative() {
		return new SensorsNative() {
			@Override
			public boolean initSensor(final int type) { return true; }

			@Override
			public void registerListener(final int type) { }

			@Override
			public void deregisterListener(final int type) { }

//			@Override
//			public float getResolution(final int type) { return 0; }

//			@NotNull
//			@Override
//			public String getStringType(final int type) {
//				//noinspection HardCodedStringLiteral
//				return "Simulator";
//			}

			@Override
			public int getLongInterval(final int type) { return 200000; }

			@Override
			public int getShortInterval(final int type) { return 20000; }

			@Override
			public void setInterval(final int type, final int delayMicroSeconds) { }

			@Override
			public boolean isSupported() {
				return true;
			}

			@Override
			public boolean useEllipseWorkaround() {
				return true;
			}
		};
	}

}
