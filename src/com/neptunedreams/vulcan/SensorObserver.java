package com.neptunedreams.vulcan;

import com.neptunedreams.vulcan.math.Vector3D;
import com.neptunedreams.util.NotNull;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 5/9/16
 * <p>Time: 6:41 PM
 *
 * @author Miguel Mu\u00f1oz
 */
public interface SensorObserver {
	/**
	 * Called by installListener(SensorManager) after setting up the manager and determining the long and short
	 * interval.
	 *
	 * @param longIntervalMs  The longest interval for the sensor readings, in milliseconds
	 * @param shortIntervalMs // The shortest interval for the sensor readings, in milliseconds.
	 */
	void sensorInstalled(int longIntervalMs, int shortIntervalMs);

	void processSensorData(long delta, @NotNull Vector3D data);
}
