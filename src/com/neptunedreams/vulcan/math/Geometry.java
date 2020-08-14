package com.neptunedreams.vulcan.math;

import com.codename1.ui.geom.Point2D;
import com.codename1.util.MathUtil;
import com.neptunedreams.util.NotNull;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 4/24/16
 * <p>Time: 12:05 AM
 *
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings("unused")
public final class Geometry {
	private static final double TO_DEGREES = 180.0 / Math.PI;

	private Geometry() { }
	@NotNull
	public static Point2D getProjectedVector(@NotNull Vector3D vector) {
		Vector3D normal = vector.normalize();
		
		double nX = normal.getX();
		double nY = normal.getY();
		return new Point2D(nX, nY);
//		double projectedRadius = Math.sqrt((nX * nX) + (nY * nY));
//		double theta = MathUtil.atan2(nY, nX);
	}
	
	public static double abs(@NotNull Point2D point) {
		double x = point.getX();
		double y = point.getY();
		return Math.sqrt((x*x) + (y*y));
	}
	
	public static double arcSinDegrees(double sin) {
		return MathUtil.asin(sin) * TO_DEGREES;
	}
	
	public static double arcTanDegrees(double y, double x) {
		return MathUtil.atan2(y, x) * TO_DEGREES;
	}
	
	public static double diagonal(double x, double y) {
		return Math.sqrt((x * x) + (y * y));
	}

	public static double diagonal(double x, double y, double z) {
		return Math.sqrt((x * x) + (y * y) + (z * z));
	}
	
	public static double getAngle(@NotNull Vector3D a, @NotNull Vector3D b) {
		double dotProduct = a.dotProduct(b);
		double magA = a.abs();
		double magB = b.abs();
		double cosTheta = (dotProduct / magA) / magB;
		return MathUtil.acos(cosTheta);
	}
}
