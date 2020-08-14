package com.neptunedreams.vulcan;

import com.codename1.util.MathUtil;
import com.neptunedreams.util.NotNull;

/**
 * This filter supports "faster multipliers" for cases where the data comes in too fast. For the android, the data
 * comes in at either 200 milliseconds or 20 milliseconds per sample, and the filter stabilizes much too quickly 
 * when it comes in at the faster rate. So this supports several multipliers, for data that comes in more quickly than
 * usual.
 * For example, if the filter multiplier for slow data is 0.75, the fast multiplier for data coming in ten times as 
 * fast is 0.971641, which is 0.75^(1/10), or the 10th root of 0.75. So the formula for converting a slow multiplier 
 * to a fast one is:
 * <pre>
 *   f = s^r
 * </pre>
 * where f is the fast multiplier, s is the slow multiplier, and r is the ratio of the slow time interval to the fast 
 * time interval.
 * <p/>
 * When R is 10, here are sample values for the slow and fast multipliers:
 *  * <pre>
 *   Slow    Fast
 *   0.60 -- 0.9502002
 *   0.65 -- 0.9578363
 *   0.70 -- 0.9649611
 *   0.75 -- 0.9716416
 *   0.80 -- 0.9779327
 *   0.85 -- 0.9838794
 *   0.90 -- 0.9895192
 *   0.95 -- 0.9948838
 * </pre>
 * You can verify with a calculator that, for each line, when you raise the "fast" number to the 10th power, you get the "slow" number. 
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 4/22/16
 * <p>Time: 11:54 AM
 *
 * @author Miguel Mu\u00f1oz
 */
public final class VariableRateDigitalFilter {
	private static final double HALF = 0.5;
	private static final float STABLE = 0.99f;
	@NotNull
	private final float[] multipliers;
	@NotNull
	private final float[] inverses;
	
	private float rawValue = 0F;
	private float currentValue = 0F;
	private int count = 0;
	private final long longInterval;
	private final int filterRange;
	private final long stabilizeTime;
	private long priorTime;

	public VariableRateDigitalFilter(float baseMultiplier, int longIntervalMs, int shortIntervalMs) {
		// Delay range goes from 10000 to 200000 microseconds, or 10 to 200 milliseconds
		filterRange = longIntervalMs / shortIntervalMs;
		this.longInterval = longIntervalMs;
		multipliers = new float[filterRange];
		inverses = new float[filterRange];
		multipliers[0] = baseMultiplier;
		inverses[0] = 1.0f - baseMultiplier;
		for (int ii = 1; ii< filterRange; ++ii) {
			int ratio = ii+1;
			multipliers[ii] = (float) MathUtil.pow(baseMultiplier, 1.0/ratio);
			inverses[ii] = 1.0f - multipliers[ii];
		}
		stabilizeTime = calculateStableTime();
		count = 0;
	}

	/**
	 * Copy constructor. Construct a digital filter from another digital filter.
	 * @param other The other VariableRateDigitalFilter
	 */
	@SuppressWarnings("unused")
	public VariableRateDigitalFilter(@NotNull VariableRateDigitalFilter other) {
		multipliers = other.multipliers;
		inverses = other.inverses;
		longInterval = other.longInterval;
		filterRange = other.filterRange;
		stabilizeTime = other.stabilizeTime;
	}
	
	private long calculateStableTime() {
		float value = 0.0f;
		int index = 0;
		while (value < STABLE) {
			value = process(value, 1.0f, 0);
			index++;
		}
//		Log.p("Stable: " + index + " iterations");
//		Log.p("Interval: " + (index * longInterval) + " ms");
		return index * longInterval;
	}
	
	@SuppressWarnings("unused")
	public long getStabilizeTimeMillis() {
		return stabilizeTime;
	}

	/**
	 * Add a new value, calculating the interval from the previous value automatically. A filter should either use this
	 * method or {@code addNext()}, but not both, since the other does not store the current time.
	 * @param newValue The new value to add
	 * @return the filtered value
	 * @see #addNext(float, long) 
	 */
	float addNextAuto(float newValue) {
		long time = System.currentTimeMillis();
		long interval = time - priorTime;
		priorTime = time;
		return addNext(newValue, interval);
	}

	/**
	 * Add a new value, using the specified interval. A filter should use either this method or {@code addNextAuto()}, 
	 * but not both, since this method does not store the current time.
	 * @param newValue The new value to add
	 * @param interval The interval, in milliseconds
	 * @return the filtered value
	 */
	float addNext(float newValue, long interval) {
		rawValue = newValue;
		if (count == 0) {
			currentValue = newValue;
			priorTime = System.currentTimeMillis();
		} else {
			if (interval < 0) {
				// Yes, I've seen this happen and I can't explain it. So we handle it as a regular interval
				interval = longInterval;
			}
			
			// Experiments show that this is the fastest way to divide one integer by another and get a result that's
			// rounded to the nearest integer, instead of truncated. On my Mac, this takes roughly 8 nano seconds
			int ratio = (int) ((((float) longInterval) / interval) + HALF);
			if (ratio >= filterRange) {
				ratio = filterRange;
			}
			int index;
			if (ratio == 0) {
				index = 0;
			} else {
				index = ratio - 1;
			}
//			Log.p("addNext(" + interval + " ms) with ratio = " + ratio + " and m = " + multipliers[index]);
			currentValue = process(currentValue, newValue, index);

//			Log.p("New value at " + count + ", ratio = " + longInterval + '/' + interval + " = " + ratio + " and multiplier = " + multipliers[index]);
		}
		count++;
		return currentValue;
	}

	/**
	 * Process the new value, without using any of the members besides the multipliers. This lets us use it to calculate
	 * the stable time without using actual data.
	 * @param priorValue The previous value
	 * @param newValue the new value
	 * @param index The multiplier index
	 * @return the new filtered value
	 */
	private float process(final float priorValue, final float newValue, final int index) {
		return (priorValue * multipliers[index]) + (newValue * inverses[index]);
	}

	public float getRawValue() {
		return rawValue;
	}

	public float getValue() {
		return currentValue;
	}
	
	public static double getMultiplierForRate(double baseMultiplier, int baseInterval, int actualInterval) {
		double ratio = actualInterval/(double) baseInterval;
		return MathUtil.pow(baseMultiplier, ratio);
	}
}
