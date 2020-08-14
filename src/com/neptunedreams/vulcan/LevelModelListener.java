package com.neptunedreams.vulcan;

import com.neptunedreams.vulcan.math.Vector3D;
import com.neptunedreams.util.NotNull;

/**
 * The LevelModelListener listens to changes to the filtered value of the sensor output. All minor fluctuations are 
 * filtered out of the data before calling valueChanged().
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 4/22/16
 * <p>Time: 11:10 PM
 *
 * @author Miguel Mu\u00f1oz
 */
public interface LevelModelListener {
	/**
	 * Notify the listener of the new value
	 * @param newValue The new value of the vector, after filtering and calibration correction
	 */
	void valueChanged(@NotNull Vector3D newValue, @NotNull Vector3D uncorrectedValue);
}
