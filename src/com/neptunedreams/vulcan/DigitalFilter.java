package com.neptunedreams.vulcan;

import com.neptunedreams.util.NotNull;

/**
 * This digital filter calculates a stable threshold. This is the number of iterations it takes to reach 95% of
 * the final value after a step change. This value is calculated from the value of the multiplier.
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 4/5/16
 * <p>Time: 1:58 PM
 *
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings("unused")
public class DigitalFilter {
	private static final float stable = 0.95f;
	private static final float defaultMultiplier = 0.75f;

	private float currentValue;
	private float rawValue;
	private int count = 0;
	private final float multiplier;
	private final float inverse;
	
	// TODO: Calculate the stable threshold from the multiplier. 
	private final int stableThreshold;
	
	public DigitalFilter() {
		this(defaultMultiplier);
	}

	public DigitalFilter(float multiplier) {
		if ((multiplier < 0.0f) || (multiplier > 1.0f)) {
			//noinspection StringConcatenation
			throw new IllegalArgumentException("Multiplier outside range of 0 to 1: " + multiplier);
		}
		this.multiplier = multiplier;
		this.inverse = 1 - multiplier;
		float value = 0.0f;
		int index = 0;
		while (value < stable) {
			value = process(value, 1.0f);
			index++;
		}
		stableThreshold = index;
//		System.out.println("DigitalFilter(" + multiplier + ") -- Threshold = " + stableThreshold);
	}
	
	@NotNull
	public Float addNext(float newValue) {
		rawValue = newValue;
		if (count > 0) {
			currentValue = process(currentValue, newValue);
		} else {
			currentValue = newValue;
		}
		count++;
		return currentValue;
	}

	/**
	 * The default processMethod is p' = (3p + n)/4, where p' is the next value, p is the current value, and n is
	 * the newly read value being added. Subclasses may override this with other formulas.
	 * @param priorValue The prior value of the filter
	 * @param newValue The new value to add to the filter
	 * @return The revised value of the filter
	 */
	protected float process(float priorValue, float newValue) {
		return (priorValue * multiplier) + (newValue * inverse);
	}

	public float getValue() {
		return currentValue;
	}

	public float getRawValue() {
		return rawValue;
	}
	
	public final int getStableThreshold() { return stableThreshold; }

	public boolean isStable() {
		return count > stableThreshold;
	}
	
	public void reset() {
		count = 0;
	}

	/**
	 * This is here if I ever need a high-pass filter. In this case, filtering will be handled by a member field of
	 * type Processor, which will be set when the constructor is called. This class isn't set up to handle both
	 * low pass and high pass simultaneously, but if I ever decide to do that, I can move the data to two Processor 
	 * instances. Maybe there should be 3 possible instances: LowPass, HighPass, and Both.
	 */
	private interface Processor {
		float process(float priorValue, float newValue);
	}
	
	@SuppressWarnings("unused")
	private class LowPass implements Processor {
		public float process(final float priorValue, final float newValue) {
			return (priorValue * multiplier) + (newValue * inverse);
		}
	}
	
	// todo: Is this right?
	// see http://developer.android.com/guide/topics/sensors/sensors_motion.html#sensors-motion-accel
	private class HighPass implements Processor {
		public float process(final float priorValue, final float newValue) {
//			float lowPass = priorValue * multiplier + (newValue * inverse);
//			return newValue - lowPass;

//			return newValue - (priorValue * multiplier + (newValue * inverse));
//			return (newValue - (newValue * inverse)) - priorValue * multiplier;
//			return newValue * multiplier - priorValue * multiplier;
			return (newValue - priorValue) * multiplier;
		}
	}
}
