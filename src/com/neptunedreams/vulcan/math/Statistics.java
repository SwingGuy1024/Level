package com.neptunedreams.vulcan.math;

import com.neptunedreams.util.NotNull;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 4/10/16
 * <p>Time: 2:39 PM
 *
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Statistics {
//	private float sumX = 0.0f;
//	private float sumY = 0.0f;
//	private float sumX2 = 0.0f;
//	private float sumY2 = 0.0f;
//	private int count = 0;
	private float lastX = 0.0f;
	private float lastY = 0.0f;
	private float minX = Float.MAX_VALUE;
	private float maxX = -Float.MAX_VALUE;
	private float minY = Float.MAX_VALUE;
	private float maxY = -Float.MAX_VALUE;
	
	@NotNull
	private Population xPop = new Population();
	@NotNull
	private Population yPop = new Population();
	
	public void addPoint(final float x, final float y) {
		xPop.addValue(x);
		yPop.addValue(y);
//		count++;
		lastX = x;
		lastY = y;
		if (minX > x) { minX = x; }
		if (maxX < x) { maxX = x; }
		if (minY > y) { minY = y; }
		if (maxY < y) { maxY = y; }
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
	public Point getSTDev() {
		return new Point((float) xPop.getStDevSample(), (float) yPop.getStDevSample());
	}

//	/*
//	 * s = sqrt(sum((x-meanX)^2)/count-1)
//	 */
//	@NotNull
//	public Point getSTDev() {
//		return new Point(getStDev(count, sumX, sumX2), getStDev(count, sumY, sumY2));
//	}

	@NotNull
	public Point getMean() {
		return new Point((float) xPop.getMean(), (float) yPop.getMean());
	}

//	@NotNull
//	public Point getMean() {
//		return new Point(sumX / count, sumY / count);
//	}

	@NotNull
	public Point getLast() {
		return new Point(lastX, lastY);
	}

	private static float getStDev(final int count, final float sum, final float sumOfSquares) {
		return (float) Math.sqrt((sumOfSquares - ((sum * sum) / count)) / (count - 1));
	}

	public float getXRange() { return maxX - minX; }

	public float getYRange() { return maxY - minY; }

	@NotNull
	public Point getRange() { return new Point(getXRange(), getYRange()); }
	
	public double getMeanXRange() {
		return xPop.getMeanRange();
	}
	
	public double getMeanYRange() {
		return yPop.getMeanRange();
	}

	@SuppressWarnings("MagicCharacter")
	public static class Point {
		public final float x;
		public final float y;

		Point(final float xx, final float yy) {
			x = xx;
			y = yy;
		}

		@NotNull
		public Point scale(float scale) {
			return new Point(x * scale, y * scale);
		}

		@Override
		public String toString() {
			//noinspection StringConcatenation
			return "(" + x + ", " + y + ')';
		}
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

	private class StdPopulation {
		private double sum = 0.0;
		private double sumOfSquares = 0.0;
		private int cnt = 0;

		void addValue(double value) {
			sum += value;
			sumOfSquares += value * value;
			cnt++;
		}

		double getMean() { return sum / cnt; }

		double getStDevSample() {
			return (float) Math.sqrt((sumOfSquares - ((sum * sum) / cnt)) / (cnt - 1));
		}

		double getStDevPopulation() {
			return (float) Math.sqrt((sumOfSquares - ((sum * sum) / cnt)) / cnt);
		}
	}
}
