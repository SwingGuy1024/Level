package com.neptunedreams.vulcan.settings;

import com.codename1.io.PreferenceListener;
import com.codename1.io.Preferences;
import com.neptunedreams.util.NotNull;
import com.neptunedreams.util.Nullable;
import org.jetbrains.annotations.Contract;
import static com.neptunedreams.vulcan.calibrate.CalibrationData.*;
import static com.neptunedreams.util.ReportToDeveloper.*;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 5/14/16
 * <p>Time: 1:14 AM
 *
 * @author Miguel Mu\u00f1oz
 */
public enum Prefs {
	prefs;

//	public static final String Z_CALIBRATION_X = getAxisKey(View.x, Axis.XAxis);
//	public static final String Z_CALIBRATION_Y = "zCalibration.y";
//	public static final String Z_CALIBRATION_Z = "zCalibration.z";
	public static final String AXIS_SUFFIX = "Calibration.";
	
	@NotNull
	public static String getAxisKey( View axisKey, Axis branch) {
		//noinspection StringConcatenation
		return axisKey + AXIS_SUFFIX + branch;
	}
	
	public static final String FLUID_HUE = "fluid.hue";
	
	public static final String AXIS_INDICATORS = "axisIndicators";
	
	public static final String ROTATING_VECTOR = "rotatingVector";

	public static final String UNITS = "Units";
	
	public static final String ACCURACY = "accuracy";
	
	public static final String AD_FREE = "adFree";
	
	public static final String INSTALLATION_ID = "hardware_id";

	public static final String PURCHASE_ID = "software_hash";
	
	public static final String PORTRAIT_SCREEN_WIDTH = "screen.width";
	public static final String PORTRIAT_SCREEN_HEIGHT = "screen.height";

	/**
	 * Adds a preference listener for the specified property to the list of listeners. When calling this method, it is 
	 * advisable to also read the current value and set it, since the value may have changed since the last time the 
	 * listener was removed. (Should this return the current value of the preference?)
	 * @param pref The preference to listen to
	 * @param listener The listener to add
	 */
	public void addPreferenceListener(@NotNull String pref, @NotNull PreferenceListener listener) {
		Preferences.addPreferenceListener(pref, listener);
	}
	
	public void removePreferenceListener(@NotNull String pref, @NotNull PreferenceListener listener) {
		Preferences.removePreferenceListener(pref, listener);
	}

	public double get(@NotNull String pref, double def) {
		return Preferences.get(pref, def);
	}

	public int get(@NotNull String pref, int def) {
		return Preferences.get(pref, def);
	}

	public boolean get(@NotNull String pref, boolean def) {
		return Preferences.get(pref, def);
	}

	public float get(@NotNull String pref, float def) {
		return Preferences.get(pref, def);
	}

	public long get(@NotNull String pref, long def) {
		return Preferences.get(pref, def);
	}

	public char get(@NotNull String pref, char def) {
		return (char) Preferences.get(pref, (int) def);
	}

	@Nullable
	@Contract("_,null->null;_,!null->!null")
	public String get(@NotNull String pref, @Nullable String def) {
		return Preferences.get(pref, def);
	}

	public void set(@NotNull String pref, double value) {
		Object priorValue = Preferences.get(pref, 0.0);
		Preferences.set(pref, value);
//		fireChange(pref, priorValue, value);
	}

	public void set(@NotNull String pref, float value) {
		Object priorValue = Preferences.get(pref, 0.0f);
		Preferences.set(pref, value);
//		fireChange(pref, priorValue, value);
	}

	public void set(@NotNull String pref, int value) {
		Object priorValue = Preferences.get(pref, 0);
		Preferences.set(pref, value);
//		fireChange(pref, priorValue, value);
	}

	public void set(@NotNull String pref, long value) {
		Object priorValue = Preferences.get(pref, 0L);
		Preferences.set(pref, value);
//		fireChange(pref, priorValue, value);
	}

	public void set(@NotNull String pref, boolean value) {
		Object priorValue = Preferences.get(pref, false);
		Preferences.set(pref, value);
//		fireChange(pref, priorValue, value);
	}

	public void set(@NotNull String pref, @Nullable String value) {
		Object priorValue = Preferences.get(pref, null);
		Preferences.set(pref, value);
//		fireChange(pref, priorValue, value);
	}

	public void set(@NotNull String pref, char value) {
		Integer priorInt = Preferences.get(pref, 0);
		char priorValue = (char) priorInt.intValue();
		Preferences.set(pref, value);
//		fireChange(pref, priorValue, value);
	}

	@NotNull
	public static String getAllPrefs() {
		String[] keys = { FLUID_HUE, AXIS_INDICATORS, ROTATING_VECTOR, CN_1_PENDING_CRASH, CN_1_CRASH_BLOCKED, UNITS, 
				ACCURACY, AD_FREE, INSTALLATION_ID, PORTRAIT_SCREEN_WIDTH, PORTRIAT_SCREEN_HEIGHT};
		StringBuilder all = new StringBuilder();
		for (String key : keys) {
			appendIfExists(all, key);
		}
		//noinspection ConstantConditions
		for (View view : View.values()) {
			//noinspection ConstantConditions
			for (Axis axis : Axis.values()) {
				appendIfExists(all, getAxisKey(view, axis));
			}
		}
		return all.toString();
	}

	private static void appendIfExists(@NotNull final StringBuilder all, @NotNull final String key) {
		String v1 = prefs.get(key, "a");
		String v2 = prefs.get(key, "b");
		if (v1.equals(v2)) {
			all.append(key).append('=').append(v1).append('\n');
		}
	}

	public static <T> T notNull(@Nullable T value, @NotNull T defaultValue) {
		return (value == null) ? defaultValue : value;
	}
}
