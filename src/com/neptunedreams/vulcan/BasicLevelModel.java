package com.neptunedreams.vulcan;

import java.util.LinkedList;
import java.util.List;
import com.codename1.io.Log;
import com.neptunedreams.vulcan.app.LevelOfVulcan;
import com.neptunedreams.vulcan.calibrate.CalibrationData;
import com.neptunedreams.vulcan.math.Vector3D;
import com.neptunedreams.util.NotNull;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 4/22/16
 * <p>Time: 10:56 PM
 *
 * @author Miguel Mu\u00f1oz
 */
public class BasicLevelModel implements SensorObserver {
	private static final float BASE_MULTIPLIER = 0.8f;
	private static final int BASE_INTERVAL = 200;
	@NotNull
	private VariableRateDigitalFilter xFilter = new VariableRateDigitalFilter(BASE_MULTIPLIER, BASE_INTERVAL, BASE_INTERVAL/10);
	@NotNull
	private VariableRateDigitalFilter yFilter = new VariableRateDigitalFilter(BASE_MULTIPLIER, BASE_INTERVAL, BASE_INTERVAL/10);
	@NotNull
	private VariableRateDigitalFilter zFilter = new VariableRateDigitalFilter(BASE_MULTIPLIER, BASE_INTERVAL, BASE_INTERVAL/10);
	private boolean isDataFast = false;
	
	private boolean doLog = false;
//	@NotNull
//	private final CalibrationData calibrationData;

	public BasicLevelModel() { //@NotNull CalibrationData calibrationData) {
//		this.calibrationData = calibrationData;
	}
	
	private final List<LevelModelListener> listenerList = new LinkedList<>();

	@Override
	public void sensorInstalled(final int longIntervalMs, final int shortIntervalMs) {// adjust the multiplier for the current device to match a 0.8 multiplier at 200 ms intervals
		float multiplier = (float) VariableRateDigitalFilter.getMultiplierForRate(BASE_MULTIPLIER, BASE_INTERVAL, longIntervalMs);
//			Log.p("Using multiplier of " + multiplier);
		xFilter = new VariableRateDigitalFilter(multiplier, longIntervalMs, shortIntervalMs);
		yFilter = new VariableRateDigitalFilter(multiplier, longIntervalMs, shortIntervalMs);
		zFilter = new VariableRateDigitalFilter(multiplier, longIntervalMs, shortIntervalMs);
//			final float resolution = manager.getResolution();

		// 9.80665f is g, in meters per second squared.
//			float angularResolution = LevelOfVulcan.angle(resolution, 9.80665f);
//			title.setText("Valid: R=" + FloatFormat.round(angularResolution, 6));
//			title.setText("Valid  " + maxTextLength);
	}

//	private long priorTime = 0L;
	@Override
	public void processSensorData(final long delta, @NotNull final Vector3D vector3D) {
		Vector3D normal = vector3D.normalize();
//		Point2D loc = getProjectedVector(normal);
		xFilter.addNext((float) normal.getX(), delta);
		yFilter.addNext((float) normal.getY(), delta);
		zFilter.addNext((float) normal.getZ(), delta);
//		extra.addNext(xFilter.getValue(), delta);
//				Log.p("Model: (" + x + ',' + y + ',' + z + " -> (" + xFilter.getRawValue() + ", " + yFilter.getRawValue() + ')');

		Vector3D uncorrectedValue = new Vector3D(xFilter.getValue(), -yFilter.getValue(), zFilter.getValue());
		Vector3D calibratedValue = uncorrectedValue;
		CalibrationData calibrationData = getCalibrationData();
		if (calibrationData.isCalibrated()) {
			calibratedValue = calibrationData.correct(uncorrectedValue);
			if (doLog) {
	//		long currentTime = System.currentTimeMillis() / 3000; // print every 3 seconds
	//		if (currentTime > priorTime) {
	//			priorTime = currentTime;
				Log.p("Adjusted from " + uncorrectedValue.toShortString() + " to " + calibratedValue.toShortString());
			}
		}
		
		setValue(calibratedValue, uncorrectedValue); // TODO: RESTORE THIS LINE!
//		setValue(getDbgRawValue(), uncorrectedValue); //  todo: DO NOT CHECK IN!
		setUncalibratedValue(uncorrectedValue);
		isDataFast = delta < (LevelOfVulcan.FRAME_RATE_MILLIS / 2);
		doLog = false;
	}
	
	@NotNull
	private Vector3D uncalibratedValue = new Vector3D(0, 0, 0);
	private void setUncalibratedValue(@NotNull Vector3D uncalibratedValue) { this.uncalibratedValue = uncalibratedValue; }
	@SuppressWarnings("unused")
	@NotNull
	public Vector3D getUncalibratedValue() { return uncalibratedValue; }
	
	public Vector3D getDbgRawValue() {
		return new Vector3D(xFilter.getRawValue(), -yFilter.getRawValue(), zFilter.getRawValue()).normalize();
	}
	
	@NotNull
	public Vector3D getDbgUncorrectedValue() {
		return new Vector3D(xFilter.getValue(), -yFilter.getValue(), zFilter.getValue());
	}
	
	public boolean isCalibrated() { return getCalibrationData().isCalibrated(); }

	private void setValue(@NotNull Vector3D value, @NotNull Vector3D uncorrectedValue) {
		for (LevelModelListener listener : listenerList) {
			listener.valueChanged(value, uncorrectedValue);
		}
	}

	@NotNull
	public CalibrationData getCalibrationData() {
		return LevelOfVulcan.getCurrentView().getCalibrationData();
	}

	public boolean isDataFast() {
		return isDataFast;
	}

	@SuppressWarnings("unused")
	public boolean isDoLog() {
		return doLog;
	}

	public void setDoLog(final boolean doLog) {
		this.doLog = doLog;
	}

	public void addLevelModelListener(LevelModelListener listener) {
		listenerList.add(listener);
	}
	
	@SuppressWarnings("unused")
	public void removeLevelModelListener(LevelModelListener listener) {
		listenerList.remove(listener);
	}
}
