package com.neptunedreams.vulcan.settings;

import java.util.LinkedList;
import java.util.List;
import com.codename1.ui.Display;
import com.neptunedreams.vulcan.math.Vector3D;
import com.neptunedreams.util.NotNull;
import com.neptunedreams.util.Nullable;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 6/20/16
 * <p>Time: 7:48 PM
 *
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings("NonFinalFieldInEnum")
public enum OrientationLock implements Commands.BooleanSetter, Commands.BooleanGetter {
	lock;
	private static final char X = 'X';
	private static final char x = 'x';
	private static final char Y = 'Y';
	private static final char y = 'y';
	private static final char Z = 'Z';
	@Nullable private Character lockedOrientation;
	private char orientation;

	private final List<LockListener> lockListeners = new LinkedList<>();
	private final List<OrientationListener> orientListeners = new LinkedList<>();

	public char getOrientation(@NotNull Vector3D vector) {
		if (lockedOrientation != null) {
			switch (lockedOrientation) {
				case x:
					if (vector.getX() > 0) {
						lockedOrientation = X;
					}
					break;
				case X:
					if (vector.getX() < 0) {
						lockedOrientation = x;
					}
					break;
				case y:
					if (vector.getY() > 0) {
						lockedOrientation = Y;
					}
					break;
				case Y:
					if (vector.getY() < 0) {
						lockedOrientation = y;
					}
					break;
				default: // empty;
			}
			return lockedOrientation;
		}
		return getActualOrientation(vector);
	}
//	private long priorTime = 0;

	public char getActualOrientation(@NotNull final Vector3D vector) {
		final double xx = vector.getX();
		double ax = Math.abs(xx);
		final double yy = vector.getY();
		double ay = Math.abs(yy);
		double az = Math.abs(vector.getZ());
		char priorOrientation = Character.toUpperCase(orientation);
		if (ax > ay) {
			if (xx < 0) {
				orientation = (ax > az) ? x : Z;
			} else {
				orientation = (ax > az) ? X : Z;
			}
		} else {
			if (yy < 0) {
				orientation = (ay > az) ? y : Z;
			} else {
				orientation = (ay > az) ? Y : Z;
			}
		}
//		//noinspection MagicNumber
//		long time = System.currentTimeMillis() / 3000; // log an orientation every 3 seconds.
//		if (time != priorTime) {
//			Log.p("o: " + orientation + " from " + vector);
//			priorTime = time;
//		}
////		Thread.dumpStack();

		if (priorOrientation != Character.toUpperCase(orientation)) {
//			Log.p("O=" + orientation + " from getActualOrientation(" + vector + ')');
			Display.getInstance().callSerially(() -> fireOrientationChange(orientation));
		}
		return orientation;
	}

//	public View getActiveView() {
//		char value;
//		if (lockedOrientation == null) {
//			value = orientation;
//		} else {
//			value = lockedOrientation;
//		}
//		switch (value) {
//			case x:
//			case X:
//				return View.x;
//			case y:
//			case Y:
//				return View.y;
//			case Z:
//				return View.z;
//			default:
//				throw new IllegalStateException("Invalid state: " + value);
//		}
//	}

	@SuppressWarnings("unused")
	public boolean isLocked() {
		return getValue();
	}

	@Override
	public boolean getValue() {
		return lockedOrientation != null;
	}

	@Override
	public void setValue(final boolean value) {
		boolean oldValue = (lockedOrientation != null);
		if (value) {
			lockedOrientation = orientation;
		} else {
			lockedOrientation = null;
		}
		fireLockListeners(oldValue, value);
	}

	/**
	 * TODO: Remove me.
	 * Debug only.
	 * @param value new value of lock.
	 */
	public void setValue(final char value) {
		if (lockedOrientation == null) {
			lockedOrientation = value;
			fireLockListeners(false, true);
		}
	}

	private void fireLockListeners(boolean oldValue, boolean value) {
		if (oldValue != value) {
			for (LockListener listener : lockListeners) {
				listener.lockStateChanged(value);
			}
		}
	}

	private void fireOrientationChange(char newValue) {
		for (OrientationListener listener : orientListeners) {
			listener.orientationChanged(newValue);
		}
	}

	public void addLockListener(LockListener lockListener) {
		lockListeners.add(lockListener);
	}

	@SuppressWarnings("unused")
	public void removeLockListener(LockListener listener) {
		lockListeners.remove(listener);
	}

	public void addOrientationListener(OrientationListener orientationListenerListener) {
		orientListeners.add(orientationListenerListener);
	}

	@SuppressWarnings("unused")
	public void removeOrientationListener(OrientationListener listener) {
		orientListeners.remove(listener);
	}

	public interface LockListener {
		void lockStateChanged(boolean isLocked);
	}
	
	public interface OrientationListener {
		void orientationChanged(char orientation);
	}
}
