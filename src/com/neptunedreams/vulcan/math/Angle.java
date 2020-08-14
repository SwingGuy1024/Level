package com.neptunedreams.vulcan.math;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 7/23/16
 * <p>Time: 1:13 AM
 *
 * @author Miguel Mu\u00f1oz
 */
public class Angle {
	private final double radians;

	private double degrees = Double.NaN;

	public Angle(double radians) {
		this.radians = radians;
	}

	public double getRadians() { return radians; }

	public double getDegrees() {
		if (Double.isNaN(degrees)) {
			degrees = Units.degrees.convert(radians);
		}
		return degrees;
	}
}
