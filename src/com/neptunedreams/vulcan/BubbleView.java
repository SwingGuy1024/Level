package com.neptunedreams.vulcan;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import com.codename1.io.Log;
import com.codename1.io.PreferenceListener;
import com.codename1.ui.Component;
import com.codename1.ui.Display;
import com.codename1.ui.Font;
import com.codename1.ui.Graphics;
import com.codename1.ui.Image;
import com.codename1.ui.Label;
import com.codename1.ui.MenuBar;
import com.codename1.ui.Toolbar;
import com.codename1.ui.Transform;
import com.codename1.ui.geom.Dimension;
import com.codename1.ui.geom.Point2D;
import com.codename1.ui.util.Resources;
import com.codename1.util.MathUtil;
import com.neptunedreams.Assert;
import com.neptunedreams.vulcan.app.LevelOfVulcan;
import com.neptunedreams.vulcan.calibrate.CalibrationData;
import com.neptunedreams.vulcan.calibrate.CalibrationData.View;
import com.neptunedreams.vulcan.math.Accuracy;
import com.neptunedreams.vulcan.math.Angle;
import com.neptunedreams.vulcan.math.Units;
import com.neptunedreams.vulcan.math.Vector3D;
import com.neptunedreams.vulcan.settings.Commands;
import com.neptunedreams.vulcan.settings.OrientationLock;
import com.neptunedreams.vulcan.settings.Prefs;
import com.neptunedreams.util.NotNull;
import com.neptunedreams.util.Nullable;
import com.neptunedreams.vulcan.ui.FormNavigator;

import static com.neptunedreams.Assert.*;
import static com.neptunedreams.vulcan.app.LevelOfVulcan.debug;

/**
 * TODO: Put in lights using LayeredLayout, to give us tool tips.
 * 
 * TODO: This does too many calculations in the paint methods. The calculations should be moved out of the paint
 * todo  code, so that the paint method does only painting.
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 4/14/16
 * <p>Time: 8:43 PM
 *
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings("MagicNumber")
public class BubbleView extends Component implements LevelModelListener, PreferenceListener {

//	private static final double RtAngle = 90.0;
	private static final double log91 = MathUtil.log(91.0);
	
	private static final double PI_OVER_2 = Math.PI / 2.0;
	public static final float _0_DEGREES = 0.0f;
	/** Pi/2 */
	private static final float _90_DEGREES = (float) PI_OVER_2;
	/** Pi */
	private static final float _180_DEGREES = (float) (Math.PI);
	/** 3Pi/2 */
	private static final float _270_DEGREES = (float) ((3.0 * Math.PI) / 2.0);

	private static final float _360_DEGREES = (float) (Math.PI * 2.0);

//	public static final double TO_RADIANS = Math.PI / 180.0;
	
	public static final int bubblePad = 1;

	// can't be turned into a local variable or it will get garbage collected!
	@SuppressWarnings("FieldCanBeLocal")
	private final PreferenceListener accuracyListener;

	@NotNull
	private final List<AngleChangedListener> angleChangedListenerList = new LinkedList<>();

	@SuppressWarnings("NotNullFieldNotInitialized")
	@NotNull
	private Image bubbleImage;
	@SuppressWarnings("NotNullFieldNotInitialized")
	@NotNull
	private Image domeImage;
	@SuppressWarnings("NotNullFieldNotInitialized")
	@NotNull
	private Image sideBubbleImage;
	@SuppressWarnings("NotNullFieldNotInitialized")
	@NotNull
	private Image tubeImage;
	
	private int bubbleRadius;
	private int tubeDiameter;
	private int domeRadius;
	private int domeDiameter;
	private int bubbleRange;
	private int halfBubbleImageSize;
	private int halfDomeImageSize;
	private int markerRadius;
	private int markerDiameter;
	private double markerLimit;
	@SuppressWarnings("FieldCanBeLocal") // can only be local when debug is off
	private int correctedLedLoc;
	private int theSize = Integer.MAX_VALUE; // This gets reset later.

	private int colorForHue;
	private BasicLevelModel model;
	private boolean imageLock = false;
	private boolean firstTimeOpen = true; // paint Z axis first time.

	@SuppressWarnings("FieldCanBeLocal") // can only be local when debug is off
	@NotNull
	private final Image ledImageOn;
	@SuppressWarnings("FieldCanBeLocal") // can only be local when debug is off
	@NotNull
	private final Image ledImageOff;
	
	private boolean axisPref = Prefs.prefs.get(Prefs.AXIS_INDICATORS, true);
	
	private char currentView = 'Z';

	// 1/4 of the radius covers 2/10 degrees. The numerator is in units were the radius is 1. This will
	// need to be translated into graphical units for drawing.
//	private double innerSlope = 0.25/0.2;

	@NotNull
	private Vector3D value = new Vector3D(0, 0, 0);
	@NotNull
	private Angle fullAngle = new Angle(0.0);
	private float tiltRadians;
	@NotNull
	private final VariableRateDigitalFilter directionFilter = new VariableRateDigitalFilter(0.1f, 200, 20);
	@NotNull
	private final VariableRateDigitalFilter alternateDirectionFilter = new VariableRateDigitalFilter(0.1f, 200, 20);
	@NotNull
	private final Font labelFont;
	public static final double OUTER_DIAMETER_FRACTION = 0.90;
	//	private int tubeRange;
	//	@NotNull
//	private ReversibleFunction slopeFunction = new ReversibleFunction() {
//		@Override
//		public double function(final double x) {
//			double ratio = x/RtAngle;
//			return x / (R_SQUARED * Math.sqrt(1-(ratio*ratio)));
//		}
//	};
//	private double endTheta = slopeFunction.reverse(innerSlope, 0.0, 1.0, 0.000000001);
//	private double radialFactor = 1 - (endTheta/RtAngle);

//	@NotNull
//	private final DigitalFilter xFilter = new DigitalFilter(0.8f);
//	@NotNull
//	private final DigitalFilter yFilter = new DigitalFilter(0.8f);

	/**
	 * Function to transform the angle, from -90 to 90 into a radius from 0 to 1. This has a response like this:
	 * <pre>
	 *  1.0 |----------------------------********
	 *  0.9 |                  **********
	 *  0.8 |           *******
	 *  0.7 |       ****
	 *  0.6 |    ***
	 *  0.5 |  **
	 *  0.4 | * 
	 *  0.3 |*
	 *  0.2 *
	 *  0.1 * 
	 *    0 * -----------------------------------
	 *      0   10  20  30  40  50  60  70  80  90  <-Theta
	 * </pre>
	 * This function passes exactly through (0, 0) and (90, 1.0).
	 * <p/>
	 * However, we invert the function for negative values of theta, so that radius(-theta) = -radius(theta);
	 * @param theta The angle of the sensor from any axis, which should be a number from -90 to 90.
	 * @return The radius to draw in the sensor view, which ranges from zero to one.
	 */
	private static double radius(double theta) {
		// we invert the function for negative values. So radius(-theta) = -radius(theta)

		doAssert(theta >= -90.0001, "theta=" + theta);  // We occasionally get values of 90.0000005!
		doAssert(theta <= 90.0001, "theta=" + theta);

		if (theta >= 0.0) {
			return MathUtil.log(theta + 1) / log91;
		} else {
			return -MathUtil.log(-theta + 1) / log91;
		}
	}
	
//	private static double theta(double radius) {
//		return MathUtil.exp(radius * log91) - 1.0;
//	}

//	/**
//	 * This does a linear transformation. pT(0) = endTheta, and pT(90) = 90
//	 * <p/>
//	 * (endTheta will be fairly close to zero.)
//	 * <p/>
//	 * This means we don't get an infinite slope at the origin. Instead, the slope of the actual line at the origin
//	 * is the slope at radius(endTheta), which will be very close to innerSlope.
//	 * @param theta An actual value for theta
//	 * @return A revised value for theta to send to the radius method.
//	 */
//	private double practicalTheta(double theta) {
//		return (radialFactor * theta) + endTheta;
//	}
	
//	private double actualRadius(double theta) {
//		return radius(practicalTheta(theta));
//	}

	@SuppressWarnings("FieldCanBeLocal") // can only be local when debug is off
	@NotNull
	private final int[] markers = new int[17];
	
	

	/**
	 * If we start the device in portrait orientation, it's screen dimensions will look like this: (775 x 540). But if
	 * we start in landscape, it will look like this: (897 x 454) So it's subtracting the height of the title bar. In
	 * order to create dome and tube images of the right size, I need to know the smaller of the two figures in 
	 * portrait mode. So the landscape mode figures don't do me any good. Here's the workaround. In these three methods: 
	 * {@code setWidth()}, {@code setHeight()}, and {@code setSize()}, I call {@code reviseScreenDimensions()}. This 
	 * method will look at the saved dimensions from the last time it created images, and figure out if they're the
	 * wrong ones. If so, and if it now has the right dimensions, it will recreate the dome and tube images in the
	 * proper size. Every subsequent time the method gets called, it will run a quick test and figure out that everything
	 * is fine, and return.
	 * (The call to ignore is done in {@code setCircleSize()}.) I thought we would know we're done with initialization 
	 * as soon as the paint() method is first called, but that didn't work.
	 * <p/>
	 * We stop this after initialization is complete because the commands that send email messages will attempt to resize 
	 * everything temporarily, and this component will get shrunk to something unusable.
	 * <p/>
	 * (If LayeredLayout is ever rewritten to call {@code setSize()} instead of {@code setWidth()} and 
	 * {@code setHeight()}, this mechanism will be handled with the currently untested setSize() method. It will probably
	 * work.)
	 * @param width The new width
	 */
	@Override
	public void setWidth(final int width) {
		reviseScreenDimensions();
		super.setWidth(width);
	}

	/**
	 * See setWidth(int) for an explanation.
	 * @param height the new height.
	 * @see #setWidth(int)  
	 */
	@Override
	public void setHeight(final int height) {
		reviseScreenDimensions();
		super.setHeight(height);
	}

	/**
	 * Unused and untested, but will probably work if needed.
	 * @param d the new size.
	 */
	@Override
	public void setSize(@NotNull final Dimension d) {
		reviseScreenDimensions();
		super.setSize(d);
	}

//	private Dimension priorDimension = null;
	private void reviseScreenDimensions() {
//		if ((screenDimension != null) && !screenDimension.equals(priorDimension)) {
//			Log.p("bv: reviseScreenDimensions 1: " + screenDimension);
//			priorDimension = screenDimension;
//		}
		if (screenDimension.getWidth() > screenDimension.getHeight()) {
//			Log.p("reviseScreenDimension: " + screenDimension);
			Dimension revisedDimension = BubbleForm.getScreenDimensions();
//			Log.p("bv: reviseScreenDimensions 2: " + revisedDimension);
			if (revisedDimension.getHeight() > revisedDimension.getWidth()) {
//				Log.p("RevisedDimensions: " + revisedDimension);
				screenDimension = revisedDimension;
//				Log.p("bv: reviseScreenDimensions 3: " + screenDimension);
				replaceBubbleForm(Display.getInstance());
//				replaceWrappedDisplay();
			}
		}
	}
	
//	public void replaceWrappedDisplay() {
////		Runnable delayedTask = new Runnable() {
////			@Override
////			public void run() {
////				((BubbleForm)Display.getInstance().getCurrent()).replaceWrappedView();
////			}
////		};
////		Display.getInstance().callSerially(delayedTask);
//	}

	public void replaceBubbleForm(@NotNull final Display display) {
		//noinspection StringConcatenation
		// I don't know why I need to delay this operation, but I get a strange exception if I don't. It's the sort of
		// exception I would expect if I didn't make these changes on the event thread. But this code is already on the
		// event thread, so I'm a bit mystified. But it works, so I'm happily mystified.
		Runnable callMeSerially = () -> {
			BubbleForm oldForm = (BubbleForm) display.getCurrent();
			assert oldForm != null;
			oldForm.stop();
			BubbleForm newForm = new BubbleForm();
			FormNavigator.addForm(LevelOfVulcan.BUBBLE_FORM, newForm); // replaces other form
			newForm.start();
			newForm.show();
			LevelOfVulcan.setBubbleForm(newForm);
			oldForm.removeAll();
			oldForm.removeAllCommands();
			Toolbar oldToolBar = oldForm.getToolbar();
			assert oldToolBar != null;
			MenuBar oldMenuBar = oldToolBar.getMenuBar();
			assert oldMenuBar != null;
			oldMenuBar.removeAll();
			oldMenuBar.remove();
			oldToolBar.removeAll();
			oldToolBar.remove();
		};
//		callMeSerially.run();
		display.callSerially(callMeSerially);
	}

	//	@NotNull
	private void setCircleSize(int width, int height) {
		if (imageLock) {
			return;
		}
		int minSize = Math.min(width, height);
//		Log.p("setCircleSize(" + width + ", " + height + ") to min=" + minSize + " for screen of " + width + " x " + height);

		if (minSize > 0) {
			theSize = minSize;

			bubbleRadius = theSize / 16;
			tubeDiameter = (bubbleRadius * 3) / 2;

			byte hue = (byte) Prefs.prefs.get(Prefs.FLUID_HUE, Commands.DEFAULT_HUE);
			int fontHt = labelFont.getHeight();
			domeRadius = (((int) (OUTER_DIAMETER_FRACTION * theSize)) / 2) - fontHt;  // diameter
			domeDiameter = domeRadius * 2;
			createImagesFromHue(hue);
			halfBubbleImageSize = bubbleImage.getHeight() / 2;
			bubbleRange = domeRadius - bubbleRadius;
			Accuracy accuracy = Accuracy.getAccuracyPref();
			calculateAccuracyDegrees(accuracy.getAccuracy());

			if (debug) {
				for (int ii = 0; ii < 8; ++ii) {
					markers[ii] = (int) ((radius((ii + 1) * 10) * bubbleRange) + bubbleRadius + 0.5);
				}
				for (int ii = 0; ii < 9; ++ii) {
					int i = ii + 8;
					markers[i] = (int) ((radius(ii + 1) * bubbleRange) + bubbleRadius + 0.5);
				}
			}
		}
	}

	private Dimension screenDimension;
	public BubbleView(@NotNull Accuracy accuracy) {
		super();
		setUIID("Bubble2");

		// Some menu commands, like "EMail Feedback" will mess with the size of this component. This will cause it to 
		// needlessly create new images of different sizes that will only get thrown away when it returns to the main view.
		// Furthermore, these new images often lead to an OutOfMemoryError. So before any command is launched from the 
		// menu, we set imageLock to true. When this is true, this component will never again call the code to create
		// new images for the new size.
		Commands.replacePrepareTask(() -> imageLock = true);

		labelFont = new Label().getUnselectedStyle().getFont();
		
//		Log.p("BubbleVeiw.read screen size.");

		final Resources theme = LevelOfVulcan.getTheme();
		Image imageOn = theme.getImage("on.png");
		Image imageOff = theme.getImage("off.png");
		doAssert(imageOff != null);
		doAssert(imageOn != null);
		ledImageOff = imageOff;
		ledImageOn = imageOn;

		screenDimension = BubbleForm.getScreenDimensions();
		setCircleSize(screenDimension.getWidth(), screenDimension.getHeight());

//		Log.p("Screen: " + screenWidth + " x " + screenHeight + " (size=" + theSize + ')');

		if (debug) {
			float pxPerMeter = Display.getInstance().convertToPixels(1000.0f);
			float sphereMm = 24000f / pxPerMeter;
			Log.p("Sphere: " + sphereMm + " mm");
			Log.p("Pixels/cm: " + (pxPerMeter / 100.0f));
			Log.p("Pixels/in: " + (pxPerMeter / 100.0f) * 2.54f);
		}

		calculateAccuracyDegrees(accuracy.getAccuracy());

//		Log.p("Bubble Radius: " + bubbleRadius);
//		Log.p("Bubble Diameter: " + bubbleDiameter);
//		Log.p("Bubble Image Size: " + bubbleImage.getWidth() + " x " + bubbleImage.getHeight());
//		Log.p("Tube Bubble Size: " + sideBubbleImage.getWidth() + " x " + sideBubbleImage.getHeight());
//		Log.p("Hue: " + hue);
//		Log.p("Dome Image Size: " + domeImage.getWidth() + " x " + domeImage.getHeight());
//		Log.p("Dome Radius: " + domeRadius);
//		Log.p("Led position: " + correctedLedLoc);
//		Log.p("Led size: " + ledImageOn.getWidth());
////		Log.p("Dome Diameter: " + domeDiameter);
////		Log.p("Max Radius Value: " + maxRadiusValue);
//		Log.p("Accuracy: " + accuracy);
//		Log.p("acc " + accuracyDegrees + " degrees, for displacement of " + markerLimit + " out of " + bubbleRange);
//		Log.p("Marker Radius: " + markerRadius);
//		Log.p("Marker Limit: " + markerLimit);
//		Log.p("Bubble Range: " + bubbleRange);
		
		setModel(new BasicLevelModel());
		
		getAllStyles().setBgTransparency(0);

		accuracyListener = (pref, priorValue, revisedValue)
				-> calculateAccuracyDegrees(Accuracy.decode(revisedValue).getAccuracy());
		Prefs.prefs.addPreferenceListener(Prefs.ACCURACY, accuracyListener);

//		showFormats();
	}
	
	private void calculateAccuracyDegrees(final double accuracy) {
		double degrees = (MathUtil.atan(accuracy) * 180.0) / Math.PI;
		markerLimit = radius(degrees) * bubbleRange;
		markerRadius = (int) (markerLimit + bubbleRadius + 0.5);
		markerDiameter = markerRadius * 2;
	}

//	private void showFormats() {
//		double[] testValues = { -3.234, -3.235, -3.238, 3.234, 3.235, 3.238 };
//		for (double d : testValues) {
//			Log.p(d + " comes to " + format(d, 2));
//		}
//	}
	
	int getBgColor() { return 0; }
	
	void setHue(byte hue) {
		createImagesFromHue(hue);
	}

	private void createImagesFromHue(final byte hue) {
		final int bubbleDiameter = bubbleRadius * 2;
		final int rawColor = makeColor(hue);
		final int bubbleColor = lighten(rawColor, 75);
		final int domeColor = getDomeColor(rawColor);
//		Log.p("size: " + size);
//		Log.p("Raw color: 0x" + Integer.toHexString(rawColor));
//		Log.p("Dome color: 0x" + Integer.toHexString(domeColor));
//		Log.p("Bubble color: 0x" + Integer.toHexString(bubbleColor));
		bubbleImage = createBubbleImage(bubbleDiameter, bubbleColor, rawColor, false);
		domeImage = createDomeImage(domeColor, theSize);
		sideBubbleImage = createSideBubbleImage(domeColor, tubeDiameter);
		tubeImage = createTubeImage(domeColor, tubeDiameter, domeDiameter);
		correctedLedLoc = domeImage.getWidth() - ledImageOn.getWidth();
	}

	public static int getDomeColor(final int rawColor) {return lighten(rawColor, 25);}

	public void setModel(BasicLevelModel model) {
		if (this.model != null) {
			this.model.removeLevelModelListener(this);
		}
		this.model = model;
		if (model != null) {
			model.addLevelModelListener(this);
		}
	}
	
	@NotNull
	private Image createDomeImage(final int color, final int size) {

		final int center = size/2;
		int circleD = domeRadius * 2;
		int circleR = domeRadius;
		halfDomeImageSize = center;
		Image image = Image.createImage(size, size);
		Graphics g = image.getGraphics();
		g.setAntiAliased(true);
		g.translate(center, center);
		g.setColor(color);
		g.fillArc(-circleR, -circleR, circleD, circleD, 0, 360);

//		// Draw radial grid lines. (Mainly for debugging.)
//		g.setColor(0x7f7f7f); // Med-gray
//		g.drawLine(domeRadius, 0, -domeRadius, 0);
//		g.drawLine(0, domeRadius, 0, -domeRadius);
//		final double sqrt = Math.sqrt(2);
//		final double sqrt2 = Math.sin(Math.PI/4);
//		Log.p("Sqrt(2) = " + sqrt + " = " + (sqrt2*2));
//		final int t22p5 = (int) ((domeRadius * Math.sin(Math.PI / 8)) + 0.5);
//		final int t67p5 = (int) ((domeRadius * Math.cos(Math.PI / 8)) + 0.5);
//		int diagonal = (int) ((domeRadius * sqrt) / 2.0);
//		g.drawLine(diagonal, diagonal, -diagonal, -diagonal);
//		g.drawLine(diagonal, -diagonal, -diagonal, diagonal);
//		
//		g.drawLine(t22p5, t67p5, -t22p5, -t67p5);
//		g.drawLine(-t22p5, t67p5, t22p5, -t67p5);
//		g.drawLine(t67p5, t22p5, -t67p5, -t22p5);
//		g.drawLine(t67p5, -t22p5, -t67p5, t22p5);

		g.translate(-center, -center);
		
		Image domeMask = Image.createImage(size, size);
		g = domeMask.getGraphics();
		g.setAntiAliased(true);
		g.setColor(0);    // black
		g.fillRect(0, 0, size, size);
		g.translate(center, center);
		g.setColor(0xFF); // blue = mask color;
		g.fillArc(-circleR, -circleR, circleD, circleD, 0, 360);
		image = image.applyMask(domeMask.createMask());
		return image;
	}
	
	@NotNull
	private Image createSideBubbleImage(int color, int rawDiameter) {

		// Make sure it's an even number. Otherwise the bubble will have its right and left halves reversed!
		int diameter = (rawDiameter/2) * 2;
		final int bubbleWidth = 2 * bubbleRadius;
//		tubeRange = (getWidth() - tubeHalfBubbleSize)/2;
//		final int tubeWidth = 2 * tubeHalfBubbleSize;
		int imageWidth = bubbleWidth + 2;
		int imageHt = diameter + 2;
		Image fullBubble = Image.createImage(imageWidth, imageHt);
		Graphics g = fullBubble.getGraphics();
		g.setAntiAliased(true);
		g.setColor(color);
		g.fillRect(0, 0, imageWidth, imageHt);
		g.setColor(darken(color, 25));
		g.fillArc(1, 1, bubbleWidth, diameter, 180, 360);
		g.setColor(0xbfbfbf);
		final int smallerHt = (9 * diameter) / 10;
		g.fillArc(0, 0, bubbleWidth, smallerHt, 180, 360);
		
		Image maskImage = Image.createImage(imageWidth, imageHt);
		g = maskImage.getGraphics();
		g.setAntiAliased(true);
		g.setColor(0);
		g.fillRect(0, 0, imageWidth, imageHt);
		g.setColor(0xFF);
		g.fillArc(1, 1, bubbleWidth, diameter, 180, 360);
		fullBubble = fullBubble.applyMask(maskImage.createMask());

		return bottomHalf(fullBubble);
//		return fullBubble;
//		// Take only the bottom half of the image.
//		return fullBubble.subImage(0, imageHt / 2, imageWidth, imageHt / 2, true); // Do I need to restore the old half-image code?
	}

	@NotNull
	private Image bottomHalf(@NotNull final Image fullBubble) {
		// This only works for an image with an even number of rows.
		final int imageWidth = fullBubble.getWidth();
		final int imageHt = fullBubble.getHeight();
		int[] rgbValues = fullBubble.getRGB();
		int[] bottomHalf = Arrays.copyOfRange(rgbValues, rgbValues.length / 2, rgbValues.length);
//		Log.p("Cutting image from " + imageWidth + " x " + imageHt + " to " + imageWidth + " x " + (imageHt/2));
		return Image.createImage(bottomHalf, imageWidth, imageHt/2);
	}

	@NotNull
	private Image createTubeImage(int color, int diameter, int width) {
		int gradientStart = darken(color, 25);
		int gradientEnd = lighten(color, 30);
		int delta = gradientEnd - gradientStart;
		int[] rgb = new int[diameter];
		int highPt = diameter/2;

		final int max = 3;
		// each of these arrays has length=3 for r, g, & b.
		int[] startValues = decode(gradientStart);
		int[] endValues = decode(gradientEnd);
		int[] deltaValues = decode(delta);

		int [] values = new int[max];
		for (int ii=0; ii<highPt; ++ii) {
			for (int p=0; p<max; ++p) {
				values[p] = startValues[p] + ((deltaValues[p] * ii) / highPt); 
			}
			rgb[ii] = encode(values);
		}
		int ix = 0;
		for (int ii=highPt; ii<diameter; ++ii) {
			for (int p = 0; p < max; ++p) {
				values[p] = endValues[p] - ((deltaValues[p] * ix) / highPt);
			}
			ix++;
			rgb[ii] = encode(values);
		}

//		Log.p("Gradient from 0x" + Integer.toHexString(gradientStart) + " to 0x" + Integer.toHexString(gradientEnd) + ", delta = " + Integer.toHexString(delta));
//		for (int ii=0; ii<rgb.length; ++ii) {
//			Log.p("* " + ii + ": 0x" + Integer.toHexString(rgb[ii]));
//		}
		Image gradient = Image.createImage(rgb, 1, diameter);

		return gradient.scaled(width, diameter);
	}
	
	@NotNull
	private int[] decode(int rgb) {
		int[] parts = new int[3];
		parts[0] = (rgb >> 16) & 0xFF;
		parts[1] = (rgb >> 8) & 0xFF;
		parts[2] = rgb & 0xFF;
		return parts;
	}
	
	private int encode(@NotNull int[] parts) {
		// We need to set the alpha channel to 1 or the image will be transparent. 
		return 0xFF000000 | (parts[0] << 16) | (parts[1] << 8) | parts[2];
	}

	@SuppressWarnings("SameParameterValue")
	private static int darken(int color, int percent) {
		doAssert(percent <= 100);
		doAssert(percent >= 0);
		int percentRemaining = 100 - percent;
		return (darkenPrimary((color & 0xFF0000) >> 16, percentRemaining) << 16)
				| (darkenPrimary((color & 0xFF00) >> 8, percentRemaining) << 8)
				| (darkenPrimary(color & 0xFF, percentRemaining));
	}

	private static int darkenPrimary(int primaryValue, int percentRemaining) {
		doAssert(primaryValue >= 0);
		doAssert(primaryValue < 256);

		return (primaryValue * percentRemaining) / 100;
	}

	private static int lighten(int color, int percent) {
		doAssert(percent <= 100);
		doAssert(percent >= 0);
		int percentRemaining = 100 - percent;
		return (lightenPrimary((color & 0xFF0000) >> 16, percentRemaining) << 16)
				|  (lightenPrimary((color & 0xFF00) >> 8, percentRemaining) << 8)
				|  (lightenPrimary(color & 0xFF, percentRemaining)); 
	}
	
	private static int lightenPrimary(int primaryValue, int percentRemaining) {
		doAssert(primaryValue >= 0);
		doAssert(primaryValue < 256);

		int delta = 255-primaryValue;
		return 255 - ((delta * percentRemaining) / 100);
	}

	public static int grayLightenPrimary(int color, int percent) {
		doAssert(percent <= 100);
		doAssert(percent >= 0);
		return (lightenPrimary(color, 100 - percent) * percent) / 100; 
	}
	
	public static int grayLighten(int color, int percent) {
		doAssert(percent <= 100);
		doAssert(percent >= 0);
		int percentRemaining = 100 - percent;
		return (grayLightenPrimary((color & 0xFF0000) >> 16, percentRemaining) << 16)
				| (grayLightenPrimary((color & 0xFF00) >> 8, percentRemaining) << 8)
				| (grayLightenPrimary(color & 0xFF, percentRemaining));
	}

	@SuppressWarnings("SameParameterValue")
	@NotNull
	private Image createBubbleImage(int diameter, int bubbleColor, int bubbleRimColor, boolean centered) {
		final int width = diameter + (2 * bubblePad);

		// Create the bubble image
		//noinspection SuspiciousNameCombination
		Image bImage = Image.createImage(width, width);
		Graphics g = bImage.getGraphics();
		g.setColor(grayLighten(bubbleRimColor, 45));

		//noinspection SuspiciousNameCombination
		g.fillRect(0, 0, width, width);
		g.setColor(grayLighten(bubbleRimColor, 65));
		int bubbleLip = 2;
		int loc = bubbleLip + 1;
		int bubbleInteriorDiameter = diameter - (2 * bubbleLip);
		doAssert((bubbleInteriorDiameter + (2 * loc)) == width, "BInt: " + bubbleInteriorDiameter + " loc: " + loc + " width: " + width);
		g.setAntiAliased(true);
		g.fillArc(loc, loc, bubbleInteriorDiameter, bubbleInteriorDiameter, 0, 360);
		
		loc+=2;
		bubbleInteriorDiameter-=4;
		
		g.setColor(bubbleRimColor);
		g.fillArc(loc, loc, bubbleInteriorDiameter, bubbleInteriorDiameter, 0, 360);
		
		loc++;
		bubbleInteriorDiameter -= 2;
		g.setColor(lighten(bubbleRimColor, 25));
		g.fillArc(loc, loc, bubbleInteriorDiameter, bubbleInteriorDiameter, 0, 360);

		loc++;
		bubbleInteriorDiameter -= 2;

		g.setColor(bubbleColor);
		g.fillArc(loc, loc, bubbleInteriorDiameter, bubbleInteriorDiameter, 0, 360);

		if (centered) {
			loc += 9;
			bubbleInteriorDiameter -= 18;
			g.setColor(0x00ff00); // green
			g.fillArc(loc, loc, bubbleInteriorDiameter, bubbleInteriorDiameter, 0, 360);
			g.setColor(0x408040); // darker green
			g.drawArc(loc, loc, bubbleInteriorDiameter, bubbleInteriorDiameter, 0, 360);
		}

		//noinspection SuspiciousNameCombination
		Image maskImage = Image.createImage(width, width);
		g = maskImage.getGraphics();
		g.setColor(0);
		//noinspection SuspiciousNameCombination
		g.fillRect(0, 0, width, width);
		g.setColor(0xff);
		g.setAntiAliased(true);
		g.fillArc(1, 1, diameter, diameter, 0, 360);

		bImage = bImage.applyMask(maskImage.createMask());
		return bImage;
	}

	private static final int maxHue = 255;
	private static final int redHue = maxHue;
//	private static final int yellowHue = maxHue / 6;     // 42
	private static final int greenHue = maxHue / 3;      // 85
//	private static final int cyanHue = maxHue/2;         // 127
	private static final int blueHue = greenHue * 2;     // 170
//	private static final int magenta = (maxHue * 5) / 6; // 212 

	/**
	 * Turn the hue into a color. For red, submit 0 or 255. For green submit 85. For blue, submit 170.
	 *
	 * @param hue A hue, with a range from 0 to 255.
	 * @return A color in 0xRRGGBB format, with no alpha channel.
	 */

	public static int makeColor(byte hue) {
		// convert to int to keep the signed byte in positive numbers.
		int iHue = ((int) hue) & 0xFF;
		final int red;
		final int grn;
		final int blu;
		int region = (iHue * 6) / 255;
		switch (region) {
			case 0: // up to 42
				// transition from red to yellow. Red is constant, green is rising
				red = maxHue;
				grn = interpolate(iHue, 0);
				blu = 0;
				break;
			case 1: // { up to 84
				// transition from yellow to green. Green is constant, red is falling
				red = -interpolate(iHue, greenHue);
				grn = maxHue;
				blu = 0;
				break;
			case 2: // up to 127
				// transition from green to cyan. Green is constant, blue is rising
				red = 0;
				grn = maxHue;
				blu = interpolate(iHue, greenHue);
				break;
			case 3:// up to 169
				// transition from cyan to blue. Blue is constant, green is falling
				red = 0;
				grn = -interpolate(iHue, blueHue);
				blu = maxHue;
				break;
			case 4: // up to 212
				// transition from blue to magenta. Blue is constant, red is rising
				red = interpolate(iHue, blueHue);
				grn = 0;
				blu = maxHue;
				break;
			case 5: // up to 254
			case 6: // only 255
				// transition from magenta to red. Red is constant, blue is falling
				red = maxHue;
				grn = 0;
				blu = -interpolate(iHue, redHue);
				break;
			default:
				throw new AssertionError("Unsupported value: " + region + " from " + hue);
		}
		return (red << 16) | (grn << 8) | blu;
	}

	private static int interpolate(int hue, int start) {
		// The 6 is because we divide the spectrum into 6 regions, for the 6 primary and secondary colors.
		return 6 * (hue - start);
	}

	@SuppressWarnings("unused")
	public int getColorForHue() {
		return colorForHue;
	}

	@SuppressWarnings("unused")
	public void setColorForHue(final int colorForHue) {
		this.colorForHue = colorForHue;
	}

	@Override
	public void paint(@NotNull final Graphics g) {
//		Log.p("paint on hc: " + hashCode());
		if (firstTimeOpen) {
			// force paint Z the first time. Only Z calculates sizes correctly.
			currentView = 'Z';
			firstTimeOpen = false;
		}
		g.setAntiAliased(true);
		int xLoc = getX();
		int yLoc = getY();
		g.translate(xLoc, yLoc);
		switch (currentView) {
			case 'x':
				paintXY(g, -value.getY(), -value.getX(), _270_DEGREES, 1);
				break;
			case 'X':
				paintXY(g, value.getY(), value.getX(), _90_DEGREES, 1);
				break;
			case 'y':
				paintXY(g, -value.getX(), -value.getY(), _0_DEGREES, -1);
				break;
			case 'Y':
				paintXY(g, value.getX(), value.getY(), _180_DEGREES, -1);
				break;
			case 'Z':
				paintZ(g);
				break;
			default:
				throw new IllegalStateException("Unknown state: " + currentView);
		}
		g.translate(-xLoc, -yLoc);
	}
	
	@NotNull
	public View getCurrentView() {
		switch (currentView) {
			case 'x':
			case 'X':
				return View.x;
			case 'y':
			case 'Y':
				return View.y;
			case 'Z':
				return View.z;
			default:
				throw new AssertionError("Unknown view: " + currentView);
		}
	}

//	private long dbgPriorTime = 0;
	private void paintXY(@NotNull Graphics g, double oldWide, double oldHigh, final float rotate, int reverse) {
//		Log.p("paintXY(g, " + oldWide + ", " + oldHigh + ", rotate=" + ((int)(rotate/TO_RADIANS)) + ", rev=" + reverse);
//		long dbgTime = System.currentTimeMillis()/3000; // every 3 seconds

		// rotate around wide axis: (fixes problems when locked.)
		double z = value.getZ();
		double angleAroundWideAxis = MathUtil.atan2(z, oldHigh);
		Vector3D axis = new Vector3D(1, 0, 0).normalize();
		double[] rotationMatrix = axis.rotate(angleAroundWideAxis);

		Vector3D rawVector = new Vector3D(oldWide, oldHigh, z);
		Vector3D rotatedVector = rawVector.productWith(rotationMatrix);
		double wide = rotatedVector.getX();
		double high = rotatedVector.getY();

		Transform savedTransform=null;
		if (rotate != 0) {
			savedTransform = Transform.makeIdentity();
			g.getTransform(savedTransform);
			g.rotate(rotate, getAbsoluteX() + (getWidth() / 2), getAbsoluteY() + (getHeight() / 2));
		}
		int margin = (getWidth() - tubeImage.getWidth())/2;
		int yPt = ((getHeight() - tubeDiameter) / 2) - margin;
		
		final int center = getWidth() / 2;
		g.translate(center, yPt + center);  // must get reversed at the end
		g.drawImage(tubeImage, -domeRadius, -domeRadius);

		// +x, -y: y axis: -> -wide, -high
		// +x, -y: x axis: -> wide, high
		// +x, +y: x axis: -> wide, high
		// +x, +y: y axis: -> wide, high
		// -x, +y: y axis: -> wide, high
		// -x, +y: x axis: -> -wide, -high
		// -x, -y, x axis: -> -wide, -high
		// -x, -y, y axis: -> -wide, -high

		// If high is negative zero and x is effectively zero, atan2(wide, high) will 
		// return PI, which results in an exception later on.
		if (high == -0.0) {
			high = 0.0;
		}
		Units units = Units.getPreferredUnits();
		final BasicLevelModel levelModel = getModel();
		if (debug && (levelModel != null) && levelModel.isDoLog()) {
			Log.p("atan(" + wide + ", " + high + ')');
		}
		final Angle radians = new Angle(MathUtil.atan2(wide, high));

		double delta = radius(radians.getDegrees());
		double position = delta * bubbleRange * reverse;

//		if (dbgTime > dbgPriorTime) {
//			dbgPriorTime = dbgTime;
//			Log.p("T: " + dbgTime + ":\t" + rawVector + " rotated to " + rotatedVector + " angle=\t" + angle + "\t delta=\t" + delta + "\t position:\t" + position);
//		}

		g.translate(0, margin - center);  // must get reversed at the end
		int iX = ((int) (position + 0.5)) - halfBubbleImageSize;
		Assert.doAssert(sideBubbleImage.getWidth() == (halfBubbleImageSize*2), sideBubbleImage.getWidth() + " != " + (halfBubbleImageSize*2));
//		Thread.dumpStack();
//		System.err.println("    Drawing at " + iX + " for value " + format(wide, 2) + '/' + format(high, 2) + " giving angle of " + format(angle, 2) + " (" + format(delta, 2) + ") p = " + format(position, 2));
//		Log.p("    Drawing at " + iX + " for value " + format(wide, 2) + '/' + format(high, 2) + " giving angle of " + format(angle, 2) + " (" + format(delta, 2) + ") p = " + format(position, 2));
		g.drawImage(sideBubbleImage, iX, 0);
		g.setColor(0x3f3f3f);
		g.drawLine(-markerRadius, 0, -markerRadius, tubeDiameter);
		g.drawLine(markerRadius, 0, markerRadius, tubeDiameter);

		// Draw vertical axis if in range.
		final double xRanged = delta * bubbleRange;
		final boolean xInRange = (xRanged < markerLimit) && (xRanged > -markerLimit);
		if (axisPref&& xInRange) {
			g.setColor(0xffffff); // white
			g.drawLine(0, -bubbleRange, 0, bubbleRange);
		}

//		Log.p("point at " + format(delta, 4) + " -> " + format(position, 2) + " drawn from " + center + " in range "
//				+ bubbleRange + " or " + bubbleRange + " with marker at " + domeMarkerRadius + " and halfSize = " + halfBubbleImageSize);

		g.translate(0, center - margin);

		g.translate(-center, -yPt-center);
		if (rotate != 0) {
			g.setTransform(savedTransform);
		}

		double angle = units.convert(radians.getRadians());
		fireAnglesChanged(angle, -angle);
		assert this.model != null;
//		if (LevelOfVulcan.debug && this.model.isDoLog()) {
//			Vector3D rawPoint = this.model.getDbgUncorrectedValue();
//			double rawWide;
//			double rawHigh;
//			if (rotate == _0_DEGREES) {
//				rawWide = -rawPoint.getX();
//				rawHigh = -rawPoint.getY();
//			} else if (rotate == _90_DEGREES) {
//				rawWide = rawPoint.getY();
//				rawHigh = rawPoint.getX();
//			} else if (rotate == _180_DEGREES) {
//				rawWide = rawPoint.getX();
//				rawHigh = rawPoint.getY();
//			} else if (rotate == _270_DEGREES) {
//				rawWide = -rawPoint.getY();
//				rawHigh = -rawPoint.getX();
//			} else {
//				throw new AssertionError("Unknown rotation: " + rotate);
//			}
//			// rotate around wide axis: (fixes problems when locked.)
//			double zz = value.getZ();
//			double dbgAngleAroundWideAxis = MathUtil.atan2(zz, rawHigh);
//			Vector3D rawAxis = new Vector3D(1, 0, 0).normalize();
//			double[] rMatrix = rawAxis.rotate(dbgAngleAroundWideAxis);
//
//			Vector3D dbgRawVector = new Vector3D(rawWide, rawHigh, zz);
//			Vector3D dbgRotatedVector = dbgRawVector.productWith(rMatrix);
////			double revWide = dbgRotatedVector.getX();
////			double revHigh = dbgRotatedVector.getY();
////
////			if (LevelOfVulcan.debug) {
////				double rawX = MathUtil.atan2(revWide, revHigh) / TO_RADIANS;
////				double alternate = MathUtil.atan2(revHigh, revWide) / TO_RADIANS;
//////			double rawY = MathUtil.atan2(rawPoint.getY(), Math.abs(rawPoint.getZ())) / TO_RADIANS;
////				Log.p("Angles before correction: " + LevelOfVulcan.format(rawX, 4) + "\u00b0 from " 
////						+ LevelOfVulcan.format(revWide, 4) + ", " + LevelOfVulcan.format(revHigh, 4) );
////				Log.p("               Alternate: " + LevelOfVulcan.format(alternate, 4) + '\u00b0');
////				Log.p(" Angles after correction: " + LevelOfVulcan.format(angle, 4) + units.getUnitSymbol());
////			}
//		}
	}

	private void paintZ(@NotNull Graphics g) {
		final int centerY = getHeight() / 2;
		final int centerX = getWidth() / 2;
		g.translate(centerX, centerY);
		g.setColor(colorForHue);
		g.drawImage(domeImage, -halfDomeImageSize, -halfDomeImageSize);

		Point2D bubblePoint = getBubblePoint();
		double x = bubblePoint.getX();
		double y = bubblePoint.getY();
		doAssert(x <= 1.0);
		doAssert(x >= -1.0);
		doAssert(y <= 1.0);
		doAssert(y >= -1.0);
		assert model != null;

		final double xRanged = x * bubbleRange;
		final double yRanged = y * bubbleRange;
		final boolean xInRange = (xRanged < markerLimit) && (xRanged > -markerLimit);
		final boolean yInRange = (yRanged < markerLimit) && (yRanged > -markerLimit);

		int iX = ((int) (xRanged + 0.5)) - halfBubbleImageSize;
		int iY = ((int) (yRanged + 0.5)) - halfBubbleImageSize;
		g.drawImage(bubbleImage, iX, iY);

		Units units = Units.getPreferredUnits();
		final double xAngle = units.convert(-MathUtil.atan2(value.getX(), Math.abs(value.getZ())));
		final double yAngle = units.convert(MathUtil.atan2(value.getY(), Math.abs(value.getZ())));
		fireAnglesChanged(xAngle, yAngle);
		if (debug && model.isDoLog()) {
			assert model != null;
			Vector3D rawPoint = model.getDbgUncorrectedValue();
//			Vector3D raw2 = model.getUncalibratedValue();
			double rawX = units.convert(-MathUtil.atan2(rawPoint.getX(), Math.abs(rawPoint.getZ())));
			double rawY = units.convert(MathUtil.atan2(rawPoint.getY(), Math.abs(rawPoint.getZ())));

			// KEEP THESE LOG STATEMENTS.
			Log.p("Angles before correction: " + LevelOfVulcan.format(rawX, 4) + ", " + LevelOfVulcan.format(rawY, 4));
			Log.p(" Angles after correction: " + LevelOfVulcan.format(xAngle, 4) + ", " + LevelOfVulcan.format(yAngle, 4));
		}

		g.setColor(0x3f3f3f);
		g.drawArc(-markerRadius, -markerRadius, markerDiameter, markerDiameter, 0, 360);

		// Draw the fullAngle label:
		if (Prefs.prefs.get(Prefs.ROTATING_VECTOR, true)) {
			drawFullAngle(g, centerX, centerY);
		}

		g.setColor(0xffffff); // white
		if (axisPref) {
			if (xInRange) {
				g.drawLine(0, -domeRadius, 0, domeRadius);
			}
			if (yInRange) {
				g.drawLine(-domeRadius, 0, domeRadius, 0);
			}
		}

		// draw debug info:
		assert model != null;
		if (debug) {
			CalibrationData data = model.getCalibrationData();
			drawCross(g, log, data.getDbgVector(), 0xff0000, "v1");      // red    Vector 1
			drawCross(g, log, data.getDbgOtherVector(), 0xff0000, "v2"); // red    Vector 2
			drawCross(g, log, data.getCalibration(), 0xff7f7f, "VC");    // pink   calibration
			drawCross(g, log, data.getCorrectionVector(), 0xff00, "Cr"); // green  Correction
			drawCross(g, log, model.getDbgRawValue(), 0xffff00, "Rw");   // Yellow raw value
			drawCross(g, log, model.getDbgUncorrectedValue(), 0, "uc");  // black  Uncorrected value
//		drawCross(g, log, value, 0, "cv");                           // black  corrected value
//			drawCross(g, log, data.getOldCorrectionVector(), 0x7f00, "oC"); // old correction
//			drawCross(g, log, data.oldCorrect(model.getDbgUncorrectedValue()), 0x3f3f3f, "oN", true); // old correction
			drawCross(g, log, value, 0x3f3f3f, "Vl");
		}

		if (debug) {
			for (int r : markers) {
				int r2 = 2 * r;
				g.drawArc(-r, -r, r2, r2, 0, 360);
			}
		}
		g.translate(-centerX, -centerY);

		// Draw LED
		if (debug) {

			assert model != null;
			if (model.isDataFast()) {
				g.drawImage(ledImageOn, 0, 0);
			} else {
				g.drawImage(ledImageOff, 0, 0);
			}

			if (model.isCalibrated()) {
				g.drawImage(ledImageOn, correctedLedLoc, correctedLedLoc);
			} else {
				g.drawImage(ledImageOff, correctedLedLoc, correctedLedLoc);
			}
		}
	}

	/**
	 * Draws the string showing the full angle, rotated to match the position of the tilt. Also draws a black line from
	 * the center, through the bubble, to the outer rim of the dome.
	 * @param g Graphics
	 * @param centerX Horizontal distance from the edge of the component to the center. 
	 * @param centerY Vertical distance from the edge of the component to the center. 
	 */
	private void drawFullAngle(@NotNull final Graphics g, int centerX, int centerY) {
		final double radians = fullAngle.getRadians();
		Units units = Units.getPreferredUnits();
		double rValue = units.convert(radians);
		String display = units.format(rValue);
		double displayLimit = Accuracy.getAccuracyPref().getRadians();
		int width = labelFont.charsWidth(display.toCharArray(), 0, display.length());
		int halfWidth = width/2;
		Transform savedTransform = Transform.makeIdentity();
		g.getTransform(savedTransform);

		// When the angle crosses from -180 to 180 or back again, the directionFilter spins the line all the way around the
		// circle. To prevent this rupture, I use two directionFilters, one for the right half of the view, the other for 
		// the left half.
		float altTiltRadians = tiltRadians; // range: -180 to 180 (-pi to pi) Ruptures at +/- pi
		if (altTiltRadians < 0f) { // make the range from 0 to 2 pi. Ruptures at 0 and 2pi.
			altTiltRadians += _360_DEGREES;
		}
		float direction = directionFilter.addNextAuto(tiltRadians); // range: -pi to pi
		float altDirection = alternateDirectionFilter.addNextAuto(altTiltRadians);

		// Only draw the line (and rotate the position) if we're outside the marker limit.
		if (radians > displayLimit) {
			// Right half of screen or left half?
			final float rawValue = directionFilter.getRawValue();
			float drawnDirection = ((rawValue > _90_DEGREES) || (rawValue < -_90_DEGREES)) ? altDirection : direction;

			final float rotation = drawnDirection + _90_DEGREES;
			final int pivotX = centerX + getAbsoluteX();
			final int pivotY = centerY + getAbsoluteY();
			g.rotate(rotation, pivotX, pivotY);
			g.drawLine(0, 0, 0, -domeRadius);
		}
		int delta = -domeRadius - labelFont.getAscent() - (labelFont.getDescent() / 2);
		g.translate(0, delta);
		g.setColor(0xffffff); // white
		g.setFont(labelFont);
		g.drawString(display, -halfWidth, 0);
		g.translate(0, -delta); // translate back to the center.

		g.setTransform(savedTransform); // unRotate
	}

	private void drawCross(@NotNull Graphics g, boolean logIt, @Nullable Vector3D point, int color, @SuppressWarnings("UnusedParameters") @NotNull String txt) {
		drawCross(g, logIt, point, color, txt, false);
	}
	
	// NO CONTRACT ALLOWED! This is just to suppress warnings.
	private boolean isTrue() { return true; }

	private final boolean log = !isTrue();

//	@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
//	@NotNull
//	private static final Set<String> logSet = new HashSet<>();
//	static {
//		logSet.add("v1");
//		logSet.add("v2");
//		logSet.add("Rw");
//		logSet.add("Vl");
//		logSet.add("oN");
//	}

	@SuppressWarnings("SameParameterValue")
	private void drawCross(@NotNull Graphics g,
	                       @SuppressWarnings("UnusedParameters") boolean logIt,
	                       @Nullable Vector3D point,
	                       int color,
	                       @SuppressWarnings("UnusedParameters") String txt,
	                       boolean _x) {
		if (debug) {
			if (point != null) {
				Point2D cross = getScaledPoint(point);
				g.setColor(color);
				final int x = (int) ((cross.getX() * bubbleRange) + 0.5);
				final int y = (int) ((cross.getY() * bubbleRange) + 0.5);
	//			if (logIt && logSet.contains(txt)) {
	//				Log.p("* * * Cross " + txt + " at (" + x + ", " + y + ") from " + point + " value: " + value.getX() + ", " + value.getY());
	//			}
				g.translate(x, y);
				if (_x) {
					g.drawLine(-10, -10, 10, 10);
					g.drawLine(-10, 10, 10, -10);
				} else {
					g.drawLine(-10, 0, 10, 0);
					g.drawLine(0, -10, 0, 10);
				}
				g.translate(-x, -y);
			}
		}
	}

	@NotNull
	private Point2D getBubblePoint() {
		double x = value.getX();
		double y = value.getY();
		return getScaledPoint(x, y);
	}

	@NotNull
	private Point2D getScaledPoint(@NotNull Vector3D vector) {
		return getScaledPoint(vector.getX(), vector.getY());
	}

	@NotNull
	private Point2D getScaledPoint(final double x, final double y) {
		// Pythagorean theorem says this is a diagonal. I call it the fullAngle because it's the distance of the bubble
		// from the center. Visually, this is a diagonal, but that distance represents the full angle from the normal
		// vector, in the Z-Axis view.
		final double sqrt = Math.sqrt((x * x) + (y * y));
		fullAngle = new Angle(MathUtil.asin(sqrt));
		double radius = radius(fullAngle.getDegrees());
//		Log.p("Normal of " + format(normalAngle, 2) + " ranged to " + format(normalAngle/PI_OVER_2, 2) + " Gives radius of " + radius + " for " + value);
		tiltRadians = (float) MathUtil.atan2(y, x);
		doAssert(currentView == 'Z');
		
		final double cosine = Math.cos(tiltRadians);
		final double sine = Math.sin(tiltRadians);
		return new Point2D(radius * cosine, radius * sine);
	}

	@Nullable
	public BasicLevelModel getModel() {
		return model;
	}

	// +x, -y: y axis: -> -wide, -high
	// +x, -y: x axis: -> wide, high
	// +x, +y: x axis: -> wide, high
	// +x, +y: y axis: -> wide, high
	// -x, +y: y axis: -> wide, high
	// -x, +y: x axis: -> -wide, -high
	// -x, -y, x axis: -> -wide, -high
	// -x, -y, y axis: -> -wide, -high
	@Override
	public void valueChanged(@NotNull Vector3D theValue, @NotNull Vector3D uncorrectedValue) {
		value = theValue;
		// We need to get the orientation from the uncorrected value, because the corrected value could flip it into another
		// view, which (if uncorrected) will then flip it right back. So it will flicker between two views.
		currentView = OrientationLock.lock.getOrientation(uncorrectedValue);
//		Log.p("View " + currentView + " from vector (" + format(x, 2) + ", " + format(y, 2) + ", " + format(value.getZ(), 2) + ')');
//		System.err.println("View " + currentView + " from vector (" + format(x, 2) + ", " + format(y, 2) + ", " + format(value.getZ(), 2) + ')');
		repaint();
	}
	
	@Override
	protected void initComponent() {
		super.initComponent();
//		Log.p("BubbleView.initComponent()");
		Prefs.prefs.addPreferenceListener(Prefs.AXIS_INDICATORS, this);
		axisPref = Prefs.prefs.get(Prefs.AXIS_INDICATORS, true);
	}

	@Override
	protected void deinitialize() {
		super.deinitialize();
//		Log.p("BubbleView.deinitialize()");
		Prefs.prefs.removePreferenceListener(Prefs.AXIS_INDICATORS, this);
	}

	private void fireAnglesChanged(final double xAngle, final double yAngle) {
		// This gets called from inside the paint method, so we delay firing listeners, which will update other components.
		Runnable runner = () -> {
			for (AngleChangedListener listener : angleChangedListenerList) {
				listener.angleChanged(currentView, xAngle, yAngle);
				if (debug) {
					assert model != null;
					model.setDoLog(false);
				}
			}
		};
		// This is so we don't fire the listeners during drawing.
		Display.getInstance().callSerially(runner);
	}

	public void addAngleChangedListener(AngleChangedListener listener) {
		angleChangedListenerList.add(listener);
	}

	@SuppressWarnings("unused")
	public void removeAngleChangedListener(AngleChangedListener listener) {
		angleChangedListenerList.remove(listener);
	}

	@Override
	public void preferenceChanged(
			@NotNull final String pref, 
			@Nullable final Object priorValue, 
			@Nullable final Object revisedValue
	) {
		axisPref = (revisedValue != null) && (Boolean) revisedValue;
	}

//		static {
//		for (int ii=0; ii<256; ++ii) {
//			int color = makeColor((byte) ii);
//			int color2 = makeColor2((byte) ii);
//			if (color != color2) {
//				throw new RuntimeException("Mismatch at " + ii + ": " + pad(Integer.toHexString(color)) + " != " + pad(Integer.toHexString(color2)));
//			}
//			//noinspection StringConcatenationInLoop
//			Log.p("c: " + ii + " -> 0x" + pad(Integer.toHexString(color)));
//		}
//	}
//
//	static String pad(@NotNull String num) {
//		com.neptunedreams.Assert.doAssert(num.length() <= 6, num);
//		return "000000".substring(num.length()) + num;
//	}

	public interface AngleChangedListener {
		/**
		 * Notify the listener that the angle of rotation has changed.
		 * @param xAngle The angle of rotation around the x axis, in degrees
		 * @param yAngle The angle of rotation around the y axis, in degrees
		 */
		void angleChanged(char axis, double xAngle, double yAngle);
	}
}
