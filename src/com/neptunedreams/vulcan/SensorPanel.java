package com.neptunedreams.vulcan;

import com.codename1.io.Log;
import com.codename1.sensors.SensorListener;
import com.codename1.sensors.SensorsManager;
import com.codename1.ui.Display;
import com.codename1.ui.TextArea;
import com.codename1.ui.layouts.GridBagLayout;
import com.neptunedreams.vulcan.ui.FloatFormat;
import com.neptunedreams.vulcan.ui.GridHelper;
import com.neptunedreams.util.NotNull;
import com.neptunedreams.util.Nullable;

import static com.neptunedreams.vulcan.ui.FloatFormat.round;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 4/6/16
 * <p>Time: 10:20 AM
 *
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings("HardCodedStringLiteral")
public class SensorPanel extends com.codename1.ui.Container {
	@NotNull
	private final String name;
	@NotNull
	private final TextArea title;
	@NotNull
	private final TextArea xField;
	@NotNull
	private final TextArea yField;
	@NotNull
	private final TextArea zField;
	private int maxTextLength = 0;
	private SensorListener listener;
	private SensorsManager sManager = null;
	private int precision;
	
//	public SensorPanel(@NotNull String name) {
//		this(name, 1);
//	}
	
	public SensorPanel(@NotNull String name, int precision) {
		super(new GridBagLayout());
		this.name = name;
		this.precision = precision;
		GridHelper helper = new GridHelper(this);
		title = new TextArea();
		xField = new TextArea();
		yField = new TextArea();
		zField = new TextArea();
		title.setEditable(false);
		xField.setEditable(false);
		yField.setEditable(false);
		zField.setEditable(false);
		xField.setGrowByContent(true);
		yField.setGrowByContent(true);
		zField.setGrowByContent(true);
		int y = -1;
		helper.addField(title, 0, ++y, name);
		helper.addField(xField, 0, ++y, "Side-to-Side");
		helper.addField(yField, 0, ++y, "Top-to-Bottom");
		helper.addField(zField, 0, ++y, "Up and Down");
	}

	public void installListener(@Nullable SensorsManager manager) {
		// Runs on EDT
//		String threadName = Thread.currentThread().getName();
//		System.out.println("Installing listener for " + name + " on Thread " + threadName);
		if (manager == null) {
			title.setText("Missing");
//			System.out.println("Missing " + name + " Manager");
		} else {
			sManager = manager;
//			final float resolution = manager.getResolution();
//			float angularResolution = LevelOfVulcan.angle(resolution, 9.80665f);
//			title.setText("Valid: R=" + FloatFormat.round(angularResolution, 6));
			title.setText("Valid  " + maxTextLength);
			listener = (timeStamp, x, y, z) -> {
				// This method runs on the EDT.
				try {
//						System.out.println(generateLine(timeStamp / 1000, x, y, z));;
					xField.setText(FloatFormat.round(getSideToSideValue(x, y, z), precision));
					yField.setText(FloatFormat.round(getTopToBottomValue(x, y, z), precision));
					zField.setText(FloatFormat.round(getUpAndDownValue(x, y, z), precision));
					title.setText("Valid" + maxTextLength);
					boolean newLength = isNewMaxLength(xField);
					newLength = newLength || isNewMaxLength(yField);
					newLength = newLength || isNewMaxLength(zField);
					
					// Attempts to revalidate did nothing for a text field that's too narrow.
					if (newLength) {
//							System.out.println("Revalidating " + name + "...");
						revalidate();
//							getParent().revalidate();
						layoutContainer();
					}
				} catch (Throwable e) {
					e.printStackTrace();
					//noinspection InstanceofCatchParameter
					if (e instanceof RuntimeException) {
						//noinspection ProhibitedExceptionThrown
						throw (RuntimeException) e;
					} else //noinspection InstanceofCatchParameter
					{
						//noinspection ProhibitedExceptionThrown
						throw (Error) e;
					}
				}
			};
		}
	}

	private boolean isNewMaxLength(@NotNull final TextArea field) {
		int length = field.getText().length();
		if (length > maxTextLength) {
			maxTextLength = length;
			return true;
		}
		return false;
	}

	@SuppressWarnings({"StringConcatenation", "unused"})
	private String generateLine(final long seconds, final float x, final float y, final float z) {
		int rnd = precision;
		String threadName = Thread.currentThread().getName();
		boolean edt = Display.getInstance().isEdt();
		//noinspection MagicNumber
		return "At " + seconds + " for " + name + ": (" + round(x, rnd) + ", " + round(y, rnd) + ", " + round(z, rnd) + ") [" + round(-4.3F) + "] on Thread " + threadName + " (" + edt + ")\n";
	}

	public void start() {
		if (sManager != null) {
			//noinspection ConstantConditions
			sManager.registerListener(listener);
		}
	}
	
	public void stop() {
		Log.p("Stopping SensorPanel " + name + " with sManager is " + (sManager == null ? "null" : "valid"));
		if (sManager != null) {
			//noinspection ConstantConditions
			sManager.deregisterListener(listener);
		}
	}
	
	protected float getSideToSideValue(float x, float y, float z) {
		return x;
	}
	
	protected float getTopToBottomValue(float x, float y, float z) {
		return y;
	}
	
	protected float getUpAndDownValue(float x, float y, float z) {
		return z;
	}
}
