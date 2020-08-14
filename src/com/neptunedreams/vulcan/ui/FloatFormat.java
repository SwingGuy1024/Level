package com.neptunedreams.vulcan.ui;

import com.neptunedreams.util.Failure;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 4/6/16
 * <p>Time: 10:55 AM
 *
 * @author Miguel Mu\u00f1oz
 */
public final class FloatFormat {
	private FloatFormat() { }
	public static String round(double f, final int places) {
		if (places < 0) {
			throw new Failure("Illegal round: " + places);
		}
		int order = 1;
		int loop = places;
		while (loop-- > 0) {
			order *= 10;
		}
		double forRounding = f * order;
		double rounded = round(forRounding);
		rounded /= order;
		StringBuilder sValue = new StringBuilder();
		sValue.append(rounded);
		int dotSpot = sValue.toString().indexOf(".");
		while ((sValue.length() - dotSpot) <= places) {
			sValue.append('0');
		}
		return sValue.toString();
	}

	public static long round(double a) {
//		if (a < 0) {
//			return (long) (a - 0.5);
//		} else {
//			return (long) (a + 0.5);
//		}
		return Math.round(a);
	}

	public static int round(float a) {
//		if (a < 0) {
//			return (int) (a - 0.5f);
//		} else {
//			return (int) (a + 0.5f);
//		}
		return Math.round(a);
	}
}
