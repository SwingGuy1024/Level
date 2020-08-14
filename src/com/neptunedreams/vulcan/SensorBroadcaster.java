package com.neptunedreams.vulcan;

import com.codename1.sensors.SensorType3D;
import com.neptunedreams.util.NotNull;
import com.neptunedreams.util.Nullable;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 5/9/16
 * <p>Time: 6:40 PM
 *
 * @author Miguel Mu\u00f1oz
 */
public interface SensorBroadcaster {
	void addSensorObserver(@NotNull SensorType3D type, @NotNull SensorObserver observer);

	void removeSensorObserver(@NotNull SensorType3D type, @Nullable SensorObserver observer);
}
