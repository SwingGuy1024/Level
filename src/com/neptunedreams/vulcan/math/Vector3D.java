package com.neptunedreams.vulcan.math;

import java.util.Arrays;
import com.codename1.util.MathUtil;
import com.neptunedreams.Assert;
import com.neptunedreams.vulcan.app.LevelOfVulcan;
import com.neptunedreams.util.NotNull;
import com.neptunedreams.util.Nullable;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 4/10/16
 * <p>Time: 10:05 PM
 *
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings("unused")
public final class Vector3D {
	private final int MAX = 3;
	private final boolean isNormal;

	@NotNull
	private final double[] data;

	public Vector3D(double x, double y, double z) {
		this(x, y, z, false);
	}
	
	private Vector3D(double x, double y, double z, boolean normal) {
		Assert.doAssert(!Double.isNaN(x), "x:NaN");
		Assert.doAssert(!Double.isNaN(y), "y:NaN");
		Assert.doAssert(!Double.isNaN(z), "z:NaN");
		data = new double[MAX];
		data[0] = x;
		data[1] = y;
		data[2] = z;
		isNormal = normal;
	}
	
	private Vector3D(@NotNull Vector3D original) {
		data = Arrays.copyOfRange(original.data, 0, MAX);
		isNormal = original.isNormal();
	}
	
	private Vector3D(@NotNull double[] inData) {
		Assert.doAssert(!Double.isNaN(inData[0]), "x:NaN");
		Assert.doAssert(!Double.isNaN(inData[1]), "y:NaN");
		Assert.doAssert(!Double.isNaN(inData[2]), "z:NaN");

		data = inData;
		isNormal = false;
	}
	
	public double getX() { return data[0]; }
	public double getY() { return data[1]; }
	public double getZ() { return data[2]; }

	public double dotProduct(@NotNull Vector3D other) {
		double product = 0.0f;
		for (int ii=0; ii<MAX; ++ii) {
			product += data[ii] * other.data[ii];
		}
		return product;
	}

	public double abs() {
		double product = 0.0;
		for (double d : data) {
			product += d*d;
		}
		return Math.sqrt(product);
	}

	public double angle(@NotNull Vector3D other) {
		final double thisAbs = abs();
		if (thisAbs != 0) {
			final double otherAbs = other.abs();
			if (otherAbs != 0) {
				return MathUtil.acos(dotProduct(other) / (thisAbs * otherAbs));
			}
		}
		// If either vector is zero, this means no rotation, so we return zero. 
		return 0.0;
	}

	/**
	 * Returns the cross product of this vector with the other vector. More formally, returns <b>A</b> x <b>B</b>, where
	 * <b>A</b> is <code>this</code> and <b>B</b> is <code>other</code>. 
	 * @param other The vector to cross
	 * @return The cross product.
	 */
	public Vector3D cross(@NotNull Vector3D other) {
		return new Vector3D(
				(data[1] * other.data[2]) - (data[2] * other.data[1]),
				(data[2] * other.data[0]) - (data[0] * other.data[2]),
				(data[0] * other.data[1]) - (data[1] * other.data[0])
		);
	}

	/**
	 * Returns a rotation matrix to rotate around <code>this</code> vector by the specified angle. 
	 * This vector is the axis of rotation.
	 * <p/>
	 * http://stackoverflow.com/questions/22745937/understanding-the-math-behind-rotating-around-an-arbitrary-axis-in-webgl
	 * @param theta The rotation angle
	 * @return The rotation matrix.
	 */
	@NotNull
	public double[] rotate(double theta) {
		if (theta == 0) {
			return new double[] { 1, 0, 0, 0, 1, 0, 0, 0, 1 };
		}
		double c = Math.cos(theta);
		double nc = 1.0 - c;
		double s = Math.sin(theta);
		double x = data[0];
		double y = data[1];
		double z = data[2];
		Assert.doAssert(!Double.isNaN(x), "x");
		Assert.doAssert(!Double.isNaN(y), "y");
		Assert.doAssert(!Double.isNaN(z), "z");
		if (!isNormal) {
			double len = Math.sqrt((x * x) + (y * y) + (z * z));
			if (len != 1.0) {
				x /= len;
				y /= len;
				z /= len;
			}
		}
		double xy = x * y;
		double yz = y * z;
		double zx = z * x;
		double xs = x * s;
		double ys = y * s;
		double zs = z * s;

		double[] e = new double[9];
		e[0] = (x * x * nc) + c;
		e[1] = (xy * nc) + zs;
		e[2] = (zx * nc) - ys;

		e[3] = (xy * nc) - zs;
		e[4] = (y * y * nc) + c;
		e[5] = (yz * nc) + xs;

		e[6] = (zx * nc) + ys;
		e[7] = (yz * nc) - xs;
		e[8] = (z * z * nc) + c;
		for (double v : e) {
			//noinspection StringConcatenationInLoop
			Assert.doAssert(!Double.isNaN(v), "Nan for theta = " + theta + '\n' + Arrays.toString(e));
		}
		return e;
	}

	/**
	 * Returns <b>M</b>V, where <b>M</b> is a matrix with 9 elements, and V is the Vector3D represented by this.
	 * <p/>
	 * https://en.wikipedia.org/wiki/Matrix_multiplication
	 * @param matrix The 9-element matrix
	 * @return The resulting vector.
	 */
	@NotNull
	public Vector3D productWith(@NotNull double[] matrix) {
		double[] result = new double[MAX];
		int index = 0;
		for (int ii=0; ii<MAX; ++ii) {
			double sum = 0.0;
			for (int jj=0; jj<MAX; ++jj) {
				sum += matrix[index]*data[jj];
				index++;
			}
			result[ii] = sum;
		}
		return new Vector3D(result[0], result[1], result[2]);
	}

	@NotNull
	public Vector3D mean(@NotNull Vector3D other) {
		double[] inData = new double[MAX];
		for (int ii=0; ii<MAX; ++ii) {
			inData[ii] = (data[ii] + other.data[ii])/2;
		}
		return new Vector3D(inData);
	}

	public boolean isNormal() {
		return isNormal;
	}
	
	public boolean isZero() {
		return (data[0] == 0) && (data[1] == 0) && (data[2] == 0);
	}

	@NotNull
	public Vector3D normalize() {
		if (isNormal) {
			// Just return a copy.
			return new Vector3D(this);
		}
		double absValue = abs();
		if (absValue == 0.0) {
			return new Vector3D(0, 0, 0);
		}
		return new Vector3D(data[0]/absValue, data[1]/absValue, data[2]/absValue, true);
	}
	
	public Vector3D subtract(@NotNull Vector3D other) {
		return new Vector3D(getX() - other.getX(), getY() - other.getY(), getZ() - other.getZ());
	}
	
	public Vector3D negateXY() {
		return new Vector3D(-getX(), -getY(), getZ(), isNormal);
	}

	@Override
	public String toString() {
		return "(\t" + data[0] + '\t' + data[1] + '\t' + data[2] + "\t)";
	}

	@NotNull
	public String toShortString() {
		return toShortString(4);
	}

	@NotNull
	public String toShortString(int places) {
		return "(\t" + LevelOfVulcan.format(data[0], places) + '\t' + LevelOfVulcan.format(data[1], places) + '\t' + LevelOfVulcan.format(data[2], places) + "\t)";
	}

	@Override
	public boolean equals(@Nullable final Object obj) {
		if (obj instanceof Vector3D) {
			Vector3D other = (Vector3D) obj;
			return Arrays.equals(data, other.data);
		}
		return false;
	}

	@Override
	public int hashCode() {
		final long bits = Double.doubleToLongBits(data[0]) + Double.doubleToLongBits(data[1]) + Double.doubleToLongBits(data[2]);
		return (int) (bits ^ (bits >>> 32));
	}
}
