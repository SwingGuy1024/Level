package com.neptunedreams.vulcan.calibrate;

import java.util.HashMap;
import java.util.Map;
import com.codename1.io.Log;
import com.codename1.sensors.SensorType3D;
import com.codename1.ui.Display;
import com.codename1.ui.Form;
import com.codename1.ui.Slider;
import com.codename1.ui.util.UITimer;
import com.codename1.util.MathUtil;
import com.neptunedreams.Assert;
import com.neptunedreams.vulcan.DigitalFilter;
import com.neptunedreams.vulcan.app.LevelOfVulcan;
import com.neptunedreams.vulcan.SensorBroadcaster;
import com.neptunedreams.vulcan.SensorObserver;
import com.neptunedreams.vulcan.math.Statistics3D;
import com.neptunedreams.vulcan.math.Vector3D;
import com.neptunedreams.vulcan.settings.Prefs;
import com.neptunedreams.util.NotNull;
import com.neptunedreams.util.Nullable;

import static com.neptunedreams.vulcan.settings.Prefs.*;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 4/29/16
 * <p>Time: 11:25 PM
 *
 * @author Miguel Mu\u00f1oz
 */
public final class CalibrationData {
	public static final double TO_RADIANS = Math.PI / 180.0;
	private static final double TWO = 2.0;
	private static final double _90_degrees = Math.PI / 2.0;

	//	private static final double RADIANS = Math.PI / 180.0;
	private Vector3D gravityVector;
	//	private Vector3D secondVector;
	@Nullable
	private Vector3D calibration;
	private boolean isCalibrated = false;
	private UITimer sliderTimer = null;
	@NotNull
	private static final Vector3D X_VECTOR = new Vector3D(1.0, 0.0, 0.0).normalize();
	@NotNull
	private static final Vector3D Y_VECTOR = new Vector3D(0.0, 1.0, 0.0).normalize();
	@NotNull
	private static final Vector3D Z_VECTOR = new Vector3D(0.0, 0.0, 1.0).normalize();
	private final double[] unitVector = { 1.0, 0, 0, 0, 1.0, 0, 0, 0, 1.0 };
	private double[] correctionMatrix = unitVector;
	@NotNull
	private static final Map<View, CalibrationData> calibrationMap = new HashMap<>();

	@NotNull
	private final Statistics3D stats = new Statistics3D();
	@NotNull
	private final DigitalFilter filterX = new DigitalFilter(0.8f);
	@NotNull
	private final DigitalFilter filterY = new DigitalFilter(0.8f);
	@NotNull
	private final DigitalFilter filterZ = new DigitalFilter(0.8f);

	@SuppressWarnings("PublicField")
	@NotNull
	public final View view;

	private Vector3D dbgOtherVector;

	public CalibrationData(@NotNull View view) {
		this.view = view;
		double x = prefs.get(Prefs.getAxisKey(view, Axis.XAxis), 0.0);
		double y = prefs.get(Prefs.getAxisKey(view, Axis.YAxis), 0.0);
		double z = prefs.get(Prefs.getAxisKey(view, Axis.ZAxis), 0.0);
		isCalibrated = (x != 0.0) || (y != 0.0) || (z != 0.0);
		if (isCalibrated) {
			calibration = new Vector3D(x, y, z);
			switch(view) {
				case x:
					createCorrection(X_VECTOR);
					break;
				case y:
					createCorrection(Y_VECTOR);
					break;
				case z:
					createZCorrection();
					break;
				default:
					throw new AssertionError("Unhandled Axis: " + view);
			}
		}
	}

	public static CalibrationData getCalibrationDataForCurrentView() {
		final View currentView = LevelOfVulcan.getCurrentView();
		return CalibrationData.getCalibrationDataForView(currentView);
	}

	@NotNull
	public static CalibrationData getCalibrationDataForView(@NotNull View view) {
		CalibrationData data = calibrationMap.get(view);
		// We lazily instantiate these because it crashes in the simulator otherwise. This is only done on the EDT, 
		// so it's safe.
		if (data == null) {
			for (View initView : View.values()) {
				calibrationMap.put(initView, new CalibrationData(initView));
			}
			data = calibrationMap.get(view);
			assert data != null;
		}
		return data;
	}

	public void calibrate(@NotNull CalibrationData other) {
//		Angle angle;
		VectorTransformer vData;
		switch (view) {
			case x:
//				angle = v -> MathUtil.atan2(v.getY(), v.getX());
				// It doesn't seem to matter if the first term is positive or negative. The 2nd must be negative.
				vData = v -> new Vector3D(-v.getY(), -v.getX(), v.getZ());
				calXY(other, vData, X_VECTOR, true);
				break;
			case y:
				vData = v -> new Vector3D(v.getX(), v.getY(), v.getZ());
//				angle = v -> MathUtil.atan2(v.getX(), v.getY());
				calXY(other, vData, Y_VECTOR, false);
				break;
			case z:
				calibrateZ(other);
				break;
			default:
				//noinspection StringConcatenation
				String data = "\nthis:  " + gravityVector + "\nother: " + other.gravityVector;
				throw new IllegalStateException("Unhandled state: " + view + data);
		}
	}

	private interface VectorTransformer {
		@NotNull
		Vector3D getVector(@NotNull Vector3D v);
	}

	/**
	 * Here's how this works. Imagine we take two one-dimensional readings, 180 degrees apart, and they're 0.8 degrees 
	 * and -0.4 degrees. From these, we need to calculate the incline angle and the error. The incline is the half the
	 * difference between the two readings, which in this case is 0.6 degrees. The error is the incline minus the first 
	 * reading, which is actually the negated mean, which in this case is -0.2. 
	 * @param other The other reading, 180 degrees from this reading
	 * @param vData The VectorTransformer for extracting a relative vector for the orientation.
	 * @param normalVector normal vector along the axis closest to gravity.
	 */
	private void calXY(
			@NotNull CalibrationData other, 
			@NotNull VectorTransformer vData, 
			@NotNull Vector3D normalVector,
	    boolean isX
	) {
		final Vector3D secondVector = other.gravityVector;
		Assert.doAssert(gravityVector != null);
		Assert.doAssert(secondVector != null);
		// process() may return the original vector or one which swapped x with -y, depending on which axis we're on.
		Vector3D firstRelativeVector = process(vData.getVector(gravityVector));
		Vector3D secondRelativeVector = process(vData.getVector(secondVector));
		Log.p("calXY() gv = " + gravityVector.toShortString());
		Log.p("          to " + firstRelativeVector.toShortString());
		Log.p("  other gv = " + secondVector.toShortString());
		Log.p("          to " + secondRelativeVector.toShortString());
		
		double firstAngle = reorient(MathUtil.atan2(firstRelativeVector.getX(), firstRelativeVector.getY()));
		double otherAngle = -reorient(MathUtil.atan2(secondRelativeVector.getX(), secondRelativeVector.getY()));
		
		double sign = isX ? 1.0 : -1.0;

		// mean gives us the inclination of the surface.
		double mean = (sign * (firstAngle + otherAngle)) / TWO;
		Log.p("mean = " + mean / TO_RADIANS + " from " + firstAngle/TO_RADIANS + " and " + otherAngle/TO_RADIANS);
		Log.p("Deg: " + fmt(mean / TO_RADIANS) + " from " + fmt(firstAngle / TO_RADIANS) + " and " + fmt(otherAngle / TO_RADIANS));
//		Log.p("Dg2: " + fmt2(mean / TO_RADIANS) + " from " + fmt2(firstAngle / TO_RADIANS) + " and " + fmt2(otherAngle / TO_RADIANS));
		// delta gives us the correction angle
//		double delta = (firstAngle + otherAngle) / TWO;

		Log.p("delta: " + LevelOfVulcan.format(mean/TO_RADIANS, 4));
		

		calibration = normalVector.productWith(Z_VECTOR.rotate(mean));
		Log.p("Calibration: " + calibration);
		prefs.set(Prefs.getAxisKey(view, Axis.XAxis), calibration.getX());
		prefs.set(Prefs.getAxisKey(view, Axis.YAxis), calibration.getY());
		prefs.set(Prefs.getAxisKey(view, Axis.ZAxis), calibration.getZ());
		isCalibrated = true;
		createCorrection(normalVector);
	}

	/**
	 * Converts an angle close to 180 or -180 into one close to zero.
	 * @param angle The angle
	 * @return The modified angle.
	 */
	private double reorient(double angle) {
		if (angle > _90_degrees) {
			return Math.PI - angle;
		}
		if (angle < -_90_degrees) {
			return angle + Math.PI;
		}
		return angle;
	}

	private static double fmt(double value) { return LevelOfVulcan.format(value, 4); }

	private void calibrateZ(@NotNull CalibrationData other) {
		Assert.doAssert(view == View.z, "Mismatch: z != " + view);
		//no inspection ObjectToString
//		Log.p("Calibrating from " + this + " using " + other);
		Vector3D secondVector = other.gravityVector;
		dbgOtherVector = secondVector;
		Assert.doAssert(gravityVector != null);
		Assert.doAssert(secondVector != null);
		Vector3D first = gravityVector.normalize();
		Vector3D second = secondVector.normalize();
//		Log.p("First:  " + first);
//		Log.p("Second: " + second);
		Vector3D correctedOne = second.negateXY().mean(first);
//		Log.p("Cor 1: " + correctedOne);
		Vector3D calibrationOne = Z_VECTOR.subtract(correctedOne.subtract(first));
		Vector3D correctedTwo = second.mean(first.negateXY());
//		Log.p("Cor 2: " + correctedTwo);
		Vector3D calibrationTwo = Z_VECTOR.subtract(correctedTwo.subtract(second));
//		Log.p("c1: " + calibrationOne);
//		Log.p("c2: " + calibrationTwo);
		calibration = calibrationOne.mean(calibrationTwo);
		prefs.set(Prefs.getAxisKey(View.z, Axis.XAxis), calibration.getX());
		prefs.set(Prefs.getAxisKey(View.z, Axis.YAxis), calibration.getY());
		prefs.set(Prefs.getAxisKey(View.z, Axis.ZAxis), calibration.getZ());
		isCalibrated = true;
		createZCorrection();
//		Log.p("Calibration Vector: " + calibration);
	}

	@NotNull
	private Vector3D process(@NotNull Vector3D vector) {
		double oldWide = vector.getX();
		double oldHigh = vector.getY();
		double z = vector.getZ();
		Log.p("Angle: atan2(" + z + ", " + oldHigh + ");");
		double angleAroundWideAxis = MathUtil.atan2(z, oldHigh);
		Log.p("pAngle = " + (angleAroundWideAxis/TO_RADIANS) + " from process(" + vector.toShortString() + ") ");
		angleAroundWideAxis = reorient(angleAroundWideAxis);
		Log.p("revised to " + (angleAroundWideAxis/TO_RADIANS));
		Vector3D axis = new Vector3D(1, 0, 0).normalize(); // This is always the X axis because wide is now in X.
		double[] rotationMatrix = axis.rotate(angleAroundWideAxis);

		Vector3D rawVector = new Vector3D(oldWide, oldHigh, z);
		return rawVector.productWith(rotationMatrix);
	}

	@Nullable
	public Vector3D getCalibration() {
		return calibration;
	}

	@NotNull
	public Vector3D getCorrectionVector() {
		return correct(Z_VECTOR);
	}

	@Nullable
	public Vector3D getDbgVector() {
		return gravityVector;
	}

	@Nullable
	public Vector3D getDbgOtherVector() {
		return dbgOtherVector;
	}

	public boolean isCalibrated() {
		return isCalibrated;
	}
	
	private void createZCorrection() {
		createCorrection(Z_VECTOR);
	}

	private void createCorrection(@NotNull Vector3D normalVector) {
		assert calibration != null;
		double angle = normalVector.angle(calibration); // calibration.angle(normalVector); // normalVector.angle(calibration); 
//		Log.p("Correction: " + (angle / TO_RADIANS) + " degrees");
//		Log.p("From Normal:  " + normalVector);
		Vector3D rotationAxis = normalVector.cross(calibration).normalize();
//		Log.p("RotationAxis: " + rotationAxis);
		correctionMatrix = rotationAxis.rotate(angle);
	}

	@NotNull
	public Vector3D correct(@NotNull Vector3D uncorrectedValue) {
		if (!isCalibrated) {
			return uncorrectedValue;
		}

		//noinspection UnnecessaryLocalVariable
		final Vector3D correctedValue = uncorrectedValue.productWith(correctionMatrix);
		return correctedValue;
	}

	/**
	 * This executes after a 1/4 second delay.
	 */
	public void takeData(
			@NotNull final Slider stageSlider,
			@NotNull final SensorBroadcaster broadcaster,
			@Nullable Runnable finish) {
		SensorObserver observer = new SensorObserver() {
			@Override
			public void sensorInstalled(final int longIntervalMs, final int shortIntervalMs) { }

			@Override
			public void processSensorData(final long delta, @NotNull final Vector3D data) {
				Vector3D vector3D = data.normalize();
				stats.addPoint(
						filterX.addNext((float) vector3D.getX()),
						filterY.addNext((float) -vector3D.getY()),
						filterZ.addNext((float) vector3D.getZ())
				);
			}
		};
//		assert acceleratorSensor != null;
		broadcaster.addSensorObserver(SensorType3D.accelerometer, observer);

		// Constants: for the duration of the stable threshold, 1000 ms/second, 5 samples per second.
		int durationMs = (2 * (filterX.getStableThreshold()) * 1000) / 5; // 50 for Debug Only: Make this 5 in Prod. 

		stageSlider.setMaxValue(durationMs);
		long startTime = System.currentTimeMillis();

		final Runnable progressTask = () -> {
			long now = System.currentTimeMillis();
			final int progress = (int) (now - startTime);
			stageSlider.setProgress(progress);
			if (progress > durationMs) {
				assert sliderTimer != null;
				sliderTimer.cancel();
			}
		};
		sliderTimer = new UITimer(progressTask);
		final int FPS_20 = 50; // 50 ms delay gives us 20 frames per second
		final Form form = Display.getInstance().getCurrent();
		assert form != null;
		sliderTimer.schedule(FPS_20, true, form);
		UITimer stopTimer = new UITimer(() -> {
			broadcaster.removeSensorObserver(SensorType3D.accelerometer, observer);
			gravityVector = stats.getMean();
			if (finish != null) {
				finish.run();
			}
		});
		stopTimer.schedule(durationMs, false, form);
	}

	public void clearCalibration() {
		calibration = null;
		isCalibrated = false;
		correctionMatrix = unitVector;
		Prefs.prefs.set(Prefs.getAxisKey(view, Axis.XAxis), 0.0);
		Prefs.prefs.set(Prefs.getAxisKey(view, Axis.YAxis), 0.0);
		Prefs.prefs.set(Prefs.getAxisKey(view, Axis.ZAxis), 0.0);
	}

	// Enums for type safety.
	public enum View {
		x, y, z;

		@NotNull
		public CalibrationData getCalibrationData() {
			return getCalibrationDataForView(this);
		}
	}

	public enum Axis {
		XAxis("x"), YAxis("y"), ZAxis("z");
		private final String id;

		Axis(@NotNull String letter) {
			id = letter;
		}

		@NotNull
		@Override
		public String toString() {
			return id;
		}
	}
}
