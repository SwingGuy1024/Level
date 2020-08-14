package com.codename1.sensors;

import java.util.Hashtable;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 5/9/16
 * <p>Time: 2:41 PM
 *
 * @author Miguel Mu\u00f1oz
 */
public enum SensorType3D {
	gyroscope(1001),
	accelerometer(1002),
	magnetic(1003),
	simulator(999);
	
	private final int id;
	private static final Hashtable<Integer, SensorType3D> reverseMap = new Hashtable<>();
	
	SensorType3D(int id) {
		this.id = id;
	}
	
	public int id() { return id; }
	
	public static SensorType3D get(int id) {
		if (reverseMap.isEmpty()) {
			//noinspection ConstantConditions
			for (SensorType3D type : values()) {
				reverseMap.put(type.id(), type);
			}
		}
		final SensorType3D sensorType3D = reverseMap.get(id);
		if (sensorType3D == null) {
			throw new IllegalArgumentException("Unknown id: " + id);
		}
		return sensorType3D;
	}
}
