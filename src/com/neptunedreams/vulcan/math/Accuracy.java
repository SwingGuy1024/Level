package com.neptunedreams.vulcan.math;

import com.codename1.util.MathUtil;
import com.neptunedreams.util.Nullable;
import com.neptunedreams.vulcan.settings.Prefs;
import com.neptunedreams.util.NotNull;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 7/24/16
 * <p>Time: 2:06 AM
 *
 * @author Miguel Mu\u00f1oz
 */
public enum Accuracy {
	low(2.0, 1000, "Low"),
	med(1.5, 1000, "Medium"),
	high(1.0, 1000, "High"),
	max(1.0, 2000, "Very High");

	private final double accuracy;
	private final double parts;
	private final double accuracyRadians;
	private final int range;
	@NotNull
	private final String label;
	
	Accuracy(double parts, int range, @NotNull String label) {
		this.parts = parts;
		this.range = range;
		this.accuracy = parts/range;
		this.accuracyRadians = MathUtil.atan(accuracy);
		this.label = label;
	}

	/**
	 * Gets the accuracy as a fraction (per cent)
	 * @return The accuracy, as a fraction (2/1000 to 1/2000)
	 */
	public double getAccuracy() {
		return accuracy;
	}

	/**
	 * Gets the accuracy, in Radians
	 * @return the accuracy, in radians
	 */
	public double getRadians() { return accuracyRadians; }

	/**
	 * Gets the numerator of the accuracy fraction
	 * @return the numerator of the accuracy fraction
	 */
	public double getParts() {
		return parts;
	}

	/**
	 * Gets the denominator of the accuracy fraction
	 * @return the denominator of the accuracy
	 */
	public int getRange() {
		return range;
	}

	@NotNull
	public String getLabel() {
		return label;
	}

	private static final String DEFAULT = "low";
	@SuppressWarnings({"StaticNonFinalField", "NonFinalFieldInEnum"})
	@NotNull
	private static Accuracy accuracyPref = decode(Prefs.prefs.get(Prefs.ACCURACY, "low"));
	
	@NotNull
	public static Accuracy getAccuracyPref() { return accuracyPref; }
	public static void setAccuracyPref(@NotNull Accuracy accuracy) {
		accuracyPref = accuracy;
		Prefs.prefs.set(Prefs.ACCURACY, accuracy.toString());
	}

	@NotNull
	public static Accuracy decode(@Nullable Object value) {
		value = Prefs.notNull(value, DEFAULT);
		final Accuracy[] values = values();
		assert values != null;
		for (Accuracy accuracy : values) {
			if (accuracy.toString().equals(value)) {
				return accuracy;
			}
		}
		throw new IllegalArgumentException((value == null) ? "null" : value.toString());
	}
}
