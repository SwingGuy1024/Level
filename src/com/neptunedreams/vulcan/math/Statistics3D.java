package com.neptunedreams.vulcan.math;

import com.neptunedreams.util.NotNull;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 4/10/16
 * <p>Time: 2:39 PM
 *
 * @author Miguel Mu\u00f1oz
 */
//@SuppressWarnings({"WeakerAccess"})
@SuppressWarnings("unused")
public class Statistics3D {
//	private float sumX = 0.0f;
//	private float sumY = 0.0f;
//	private float sumX2 = 0.0f;
//	private float sumY2 = 0.0f;
	private int count = 0;
	private double lastX = 0.0f;
	private double lastY = 0.0f;
	private double lastZ = 0.0f;
	private double minX = Double.MAX_VALUE;
	private double maxX = -Double.MAX_VALUE;
	private double minY = Double.MAX_VALUE;
	private double maxY = -Double.MAX_VALUE;
	private double minZ = Double.MAX_VALUE;
	private double maxZ = -Double.MAX_VALUE;

	@NotNull
	private Population xPop = new Population();
	@NotNull
	private Population yPop = new Population();
	@NotNull
	private Population zPop = new Population();

	public void addPoint(final double x, double y, double z) {
		xPop.addValue(x);
		yPop.addValue(y);
		zPop.addValue(z);
		count++;
		lastX = x;
		lastY = y;
		lastZ = z;
		if (minX > x) { minX = x; }
		if (maxX < x) { maxX = x; }
		if (minY > y) { minY = y; }
		if (maxY < y) { maxY = y; }
		if (minZ > z) { minZ = z; }
		if (maxZ < z) { maxZ = z; }
	}

//	public void addPoint(final float x, final float y) {
//		sumX += x;
//		sumY += y;
//		sumX2 += x * x;
//		sumY2 += y * y;
//		assert sumX2 > 0f;
//		assert sumY2 > 0f;
//		count++;
//		lastX = x;
//		lastY = y;
//		if (minX > x) { minX = x; }
//		if (maxX < x) { maxX = x; }
//		if (minY > y) { minY = y; }
//		if (maxY < y) { maxY = y; }
//	}

	/*
	 * s = sqrt(sum((x-meanX)^2)/count-1)
	 */
	@NotNull
	public Vector3D getSTDev() {
		return new Vector3D(xPop.getStDevSample(), yPop.getStDevSample(), zPop.getStDevSample());
	}

//	/*
//	 * s = sqrt(sum((x-meanX)^2)/count-1)
//	 */
//	@NotNull
//	public Point getSTDev() {
//		return new Point(getStDev(count, sumX, sumX2), getStDev(count, sumY, sumY2));
//	}

	@NotNull
	public Vector3D getMean() {
		return new Vector3D(xPop.getMean(), yPop.getMean(), zPop.getMean());
	}

	public int getCount() {
		return count;
	}

	@NotNull
	public Vector3D getLast() {
		return new Vector3D(lastX, lastY, lastZ);
	}

//	private static float getStDev(final int count, final float sum, final float sumOfSquares) {
//		return (float) Math.sqrt((sumOfSquares - ((sum * sum) / count)) / (count - 1));
//	}

	public double getXRange() { return maxX - minX; }

	public double getYRange() { return maxY - minY; }

	public double getZRange() { return maxZ - minZ; }

	@NotNull
	public Vector3D getRange() { return new Vector3D(getXRange(), getYRange(), getZRange()); }
	
	public double getMeanXRange() {
		return xPop.getMeanRange();
	}

	public double getMeanYRange() {
		return yPop.getMeanRange();
	}

	public double getMeanZRange() {
		return zPop.getMeanRange();
	}

	/**
	 * The following is called Walford's method for calculating standard deviation, and it's said to be the best way. 
	 *
	 *
	 */
//	public static double StandardDeviation(List<Double> valueList) {
//		double M = 0.0;
//		double S = 0.0;
//		int k = 1;
//		foreach( double value in valueList)
//		{
//			double tmpM = M;
//			M += (value - tmpM) / k;
//			S += (value - tmpM) * (value - M);
//			k++;
//		}
//		return Math.Sqrt(S / (k - 2));
//	}

//	private class StdPopulation {
//		private double sum = 0.0;
//		private double sumOfSquares = 0.0;
//		private int cnt = 0;
//
//		void addValue(double value) {
//			sum += value;
//			sumOfSquares += value * value;
//			cnt++;
//		}
//
//		double getMean() { return sum / cnt; }
//
//		double getStDevSample() {
//			return (float) Math.sqrt((sumOfSquares - ((sum * sum) / cnt)) / (cnt - 1));
//		}
//
//		double getStDevPopulation() {
//			return (float) Math.sqrt((sumOfSquares - ((sum * sum) / cnt)) / cnt);
//		}
//	}
}
