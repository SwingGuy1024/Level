package com.neptunedreams.vulcan;

import java.util.Hashtable;
import java.util.LinkedList;
import com.codename1.sensors.SensorListener;
import com.codename1.sensors.SensorType3D;
import com.codename1.sensors.SensorsManager;
import com.codename1.ui.Form;
import com.codename1.ui.layouts.FlowLayout;
import com.codename1.ui.layouts.Layout;
import com.neptunedreams.vulcan.math.Vector3D;
import com.neptunedreams.util.NotNull;
import com.neptunedreams.util.Nullable;
import org.jetbrains.annotations.Contract;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 4/26/16
 * <p>Time: 11:06 PM
 * 
 * @author Miguel Mu\u00f1oz
 */
public class SensorForm extends Form implements SensorBroadcaster {
	@NotNull
//	private SensorsManager sManager;
	private Hashtable<SensorType3D, SensorsManager> managerMap = new Hashtable<>();
	@NotNull
	private Hashtable<SensorType3D, SensorListener> listenerMap = new Hashtable<>();
//	private SensorListener sensorListener = makeSensorListener();
	private boolean sensorOn = false;
	
	@NotNull
	private final Hashtable<SensorType3D, LinkedList<SensorObserver>> observerMap = new Hashtable<>();
//	private final LinkedList<SensorObserver> observers = new LinkedList<>();

	public SensorForm(@NotNull SensorType3D... managers) {
		this(new FlowLayout(), managers);
	}

	public SensorForm(@NotNull final Layout contentPaneLayout, @NotNull SensorType3D... managers) {
		super(contentPaneLayout);
		for (SensorType3D manager : managers) {
			managerMap.put(manager, installListener(manager));
		}
	}

	public SensorForm(@NotNull final String title, @NotNull SensorType3D... managers) {
		this(managers);
		setTitle(title);
	}

	public SensorForm(@NotNull final String title, @NotNull final Layout contentPaneLayout, @NotNull SensorType3D... managers) {
		this(contentPaneLayout, managers);
		setTitle(title);
	}
	
	@NotNull private LinkedList<SensorObserver> getObservers(@NotNull SensorType3D type) {
		if (!observerMap.containsKey(type)) {
			observerMap.put(type, new LinkedList<>());
		}
		final LinkedList<SensorObserver> observers = observerMap.get(type);
		assert observers != null;
		return observers;
	}

	@Contract("null->fail")
	@NotNull
	protected SensorsManager installListener(@NotNull SensorType3D type) {
		SensorsManager manager = SensorsManager.getSenorsManager(type);
//		if (manager == null) {
//			//noinspection HardCodedStringLiteral
//			final String msg = "Gravity Sensor failed to load";
//			Dialog.show("Error", msg, Dialog.TYPE_ERROR, null, "Ok", null);
////			title.setText("Missing");
////			Log.p("Missing " + name + " Manager");
//			throw new Failure(msg);
//		}
		int longIntervalMs = manager.getLongInterval() / 1000;
		int shortIntervalMs = manager.getShortInterval() / 1000;
		if (shortIntervalMs == 0) {
			// We must be in the simulator
			if (longIntervalMs == 0) {
				// Yep. We're in the simulator
				//noinspection MagicNumber
				longIntervalMs = 200;
				//noinspection MagicNumber
				shortIntervalMs = 20;
			} else {
				shortIntervalMs = longIntervalMs / 4; // Not set in stone
			}
		}
//			Log.p("Interval range: " + shortIntervalMs + " to " + longIntervalMs);
//			Log.p("Multiplier at 20ms: " + VariableRateDigitalFilter.getMultiplierForRate(0.8, 200, 20));
		LinkedList<SensorObserver> observers = getObservers(type);
		for (SensorObserver observer : observers) {
			observer.sensorInstalled(longIntervalMs, shortIntervalMs);
		}
		return manager;
	}
	
	@NotNull
	public SensorsManager getSensorManager(@NotNull SensorType3D type) {
		final SensorsManager sensorsManager = managerMap.get(type);
		assert sensorsManager != null;
		return sensorsManager;
	}

	@SuppressWarnings("unused")
	public int getLongIntervalMs(@NotNull SensorType3D type) {
		return getSensorManager(type).getLongInterval() / 1000;
	}

	@SuppressWarnings("unused")
	public int getShortIntervalMs(@NotNull SensorType3D type) {
		return getSensorManager(type).getShortInterval() / 100;
	}

	/**
	 * Adds the observer to the list of SensorObservers, and calls its sensorInstalled() method
	 * @param observer The SensorObserver
	 * @see SensorObserver#sensorInstalled(int, int)  
	 * @throws NullPointerException if type was not registered on construction.
	 */
	@Override
	public void addSensorObserver(@NotNull SensorType3D type, @NotNull SensorObserver observer) {
		LinkedList<SensorObserver> observers = getObservers(type);
		observers.add(observer);
		final SensorsManager sManager = getSensorManager(type);
		observer.sensorInstalled(sManager.getLongInterval()/1000, sManager.getShortInterval()/1000);
	}
	
	@Override
	@SuppressWarnings("unused")
	public void removeSensorObserver(@NotNull SensorType3D type, @Nullable SensorObserver observer) {
		LinkedList<SensorObserver> observers = getObservers(type);
		observers.remove(observer);
	}

//	/**
//	 * Called by installListener(SensorManager) after setting up the manager and determining the long and short
//	 * interval.
//	 * @param longIntervalMs The longest interval for the sensor readings, in milliseconds
//	 * @param shortIntervalMs // The shortest interval for the sensor readings, in milliseconds.
//	 */
//	protected abstract void installListenerFollowUp(final int longIntervalMs, final int shortIntervalMs);

	private SensorListener makeSensorListener(@NotNull SensorType3D type) {
		return (timeStamp, x, y, z) -> {
			long delta = processTime(timeStamp);
			LinkedList<SensorObserver> observers = getObservers(type);
			Vector3D vector3D = new Vector3D(x, y, z);
			for (SensorObserver observer : observers) {
				observer.processSensorData(delta, vector3D);
			}
		};
	}

	private long prevTime = 0;

	private long processTime(long newTime) {
		final long delta;
		if (prevTime == 0) {
			delta = 0;
		} else {
			delta = newTime - prevTime;
//			Log.p("Delta: " + newTime + " - " + prevTime + " = " + delta);
		}
		prevTime = newTime;
		return delta;
	}
	
	@NotNull
	private SensorListener getListenerForType(@NotNull SensorType3D type) {
		if (!listenerMap.containsKey(type)) {
			listenerMap.put(type, makeSensorListener(type));
		}
		SensorListener sensorListener = listenerMap.get(type);
		assert sensorListener != null;
		return sensorListener;
	}

	private void registerAllListeners() {
		for (SensorType3D type : managerMap.keySet()) {
			SensorListener sensorListener = getListenerForType(type);
			SensorsManager manager = getSensorManager(type);
			manager.registerListener(sensorListener);
		}
	}

	private void deRegisterAllListeners() {
		for (SensorType3D type : managerMap.keySet()) {
			SensorListener sensorListener = getListenerForType(type);
			SensorsManager manager = getSensorManager(type);
			manager.deregisterListener(sensorListener);
		}
	}

	/**
	 * This should be called by the start() method of the main class. Subclasses may not override this, but may 
	 * override doStart() to do additional tasks during the start() method.
	 */
	public final void start() {
//		Log.p("SF.start(" + getTitle() + "); // on = " + sensorOn);
		if (!sensorOn) {
			registerAllListeners();
			sensorOn = true;
		}
		doStart();
	}

	/**
	 * Called by start(). The default method does nothing, but subclasses may override.
	 */
	public void doStart() { }

	@Override
	protected void initComponent() {
//		Log.p("SF.initComp(" + getTitle() + "); // on = " + sensorOn);
		if (!sensorOn) {
			registerAllListeners();
			sensorOn = true;
		}
	}

	/**
	 * This should be called by the stop() method of the main class. Subclasses may not override this, but may
	 * override doStop() to do additional tasks during the stop() method.
	 */
	public final void stop() {
//		Log.p("SF.stop(" + getTitle() + "); // on = " + sensorOn);
//		Log.p("Stopping SensorPanel " + name + " with sManager is " + (sManager == null ? "null" : "valid"));
		if (sensorOn) {
			deRegisterAllListeners();
			sensorOn = false;
		}
		doStop();
	}

	/**
	 * Called by stop(). The default method does nothing, but subclasses may override.
	 */
	public void doStop() { }

	@Override
	protected void deinitialize() {
//		Log.p("SF.deInit(" + getTitle() + "); // on = " + sensorOn);
		if (sensorOn) {
			deRegisterAllListeners();
			sensorOn = false;
		}
	}

}
