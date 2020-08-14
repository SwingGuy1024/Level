package com.neptunedreams.vulcan.app;

import java.util.Hashtable;
import com.codename1.io.Log;
import com.codename1.io.Preferences;
import com.codename1.payment.Purchase;
import com.codename1.payment.PurchaseCallback;
import com.codename1.sensors.SensorsManager;
import com.codename1.system.NativeLookup;
import com.codename1.ui.Dialog;
import com.codename1.ui.Display;
import com.codename1.ui.Form;
import com.codename1.ui.Toolbar;
import com.codename1.ui.plaf.UIManager;
import com.codename1.ui.util.Resources;
import com.codename1.util.MathUtil;
import com.neptunedreams.util.Nullable;
import com.neptunedreams.util.ProductTestNative;
import com.neptunedreams.vulcan.BubbleForm;
import com.neptunedreams.vulcan.SensorForm;
import com.neptunedreams.vulcan.calibrate.CalibrationData.View;
import com.neptunedreams.vulcan.settings.Prefs;
import com.neptunedreams.vulcan.ui.FormNavigator;
import com.neptunedreams.util.Failure;
import com.neptunedreams.util.NotNull;
import com.neptunedreams.util.ReportToDeveloper;

import static com.codename1.sensors.SensorType3D.*;

/*
Obfuscation note: To de-obfuscate a stack trace, do this:
1. Download the mapping file. It will have a long, strange name, like this: 
   398423ec-57fa-4b78-82ff-d8cbd0f54258-1475833906792-mapping.txt
2. Execute these commands: 
      cd ~/Development/androidStandalone/android-sdk-macosx/tools/proguard/bin
      ./retrace.sh ~/Downloads/398423ec-57fa-4b78-82ff-d8cbd0f54258-1475833906792-mapping.txt ~/Development/LevelOfVulcan/trace.txt
      
   That's ./retrace.sh <mapping file> <stack trace file>
 */

/**
 * Level Notes:
 * Professional levels measure their accuracy in mm per meter or inches per inch, which (I presume) is the slope
 * of the line. Most professional levels range from .5mm/m to 2.0mm/m although some go as low as 0.2mm/m. Here
 * are the angles for those values:
 * <pre>
 * 0.2mm/m = 0.0115 degrees
 * 0.5mm/m = 0.0286 degrees
 * 1.0mm/m = 0.0573 degrees
 * 1.5mm/m = 0.0869 degrees
 * 2.0mm/m = 0.1146 degrees
 * </pre>
 * Source: http://bdk.force.com/FAQm/servlet/fileField?retURL=%2FFAQm%2Fapex%2FPKB_Article_Mobile%3Fid%3DkA0C0000000ChfXKAS%26brand%3DStanley%26group%3D%26model%3D%26type%3D%26terms%3D42-466%26lang%3D%26returl%3D%252Fapex%252FPKB_Search_mobile%253F_ga%253D1.17691514.297055763.1459960160%2526brand%253DStanley%2526terms%253D42-466&entityId=ka0C0000000CmGoIAK&field=File_1__Body__s
 * Level products accuracies.doc
 * <p/>
 * Tidal forces of the moon are 4.3 E+6 times weaker than the Earth's gravity, so the level shouldn't be affected.
 * <p/>
 * <pre>
 * TO DO: Done   1) Separate Data model from BubbleView
 * TO DO: Done   2) Fix Geometry.
 * TO DO: Done   3) Test exceptions in sensor listener
 * TO DO: Cancel 4) Insert animation layer between data model and bubble view ?
 * TO DO: Cancel 5) Properly handle back button?
 * TO DO: Done   6) Finer control of frame rate
 * TO DO: Done   7) Add calibration windows.
 * TO DO: Cancel 7b) Experiment with 4 calibration windows.
 * TO DO: Cancel 8) Add direction indicator. Follows 2
 * TO DO: 8 Done 9) Fix background color
 * TO DO: 1 Done 10) Add two more orientations.
 * TO DO:   Done 11) Add Four buttons: left-right, top-bottom, circle, Auto (default)? (Done as lock button)
 * TO DO: 2 Done 12) Add numeric displays.
 * TO DO: 7 Done 13) Let the user set the color.
 * TO DO: Done   14) Let the user set the accuracy.
 * TO DO: Cancel 15) Adjust the range for the accuracy.
 * TO DO: Cancel 16) Adjust bubble size for display size.
 * TODO:         17) Reduce permissions in installer
 * TODO: 4       18) Create ad version and ad-free version.
 * TO DO: Done   19) Figure out logging, and how to get log files from the client.
 * TO DO: Done   20) Figure out themes.
 * TO DO: Done   21) Turn on light when level.
 * TO DO: Done   22) Turn on axis lights, too.
 * TO DO: Done   23) Add screen to submit log file.
 * TO DO: Done   24) Figure out persistence. Persist the 3 calibrations?
 * TODO:   *     25) Fill SensorsNativeImpl with null checks
 * TODO:   *     26) Three choices for calibration complete: sound, flash, vibrate, nothing.
 * TO DO: Done   27) Bug in back to BubbleView: Doesn't restart. Do I need to move start code to onShow()?
 * TODO:   *     28) Test behavior on missing sensor.
 * TO DO: Done   29) Put a dark circle around green light.
 * TODO:         30) Disable stage 1/2 buttons on click. reEnable on exit
 * TO DO: Cancel 31) Add a 2nd-stage mean-filter to the BasicLevelModel
 * TO DO: Done   32) Add a white star to the calibrated label when calibrated.
 * TO DO: Done   33) Add choice of units.
 * TO DO: Done   34) Change the wording of the clear-calibration messages.
 * TODO: 10      35) Increase the speed of the sampling.
 * TO DO: Done   36) Test limits of percent readings.
 * TODO: 11      37) New animations for X and Y view.
 * TODO:         38) Experiment (Android Studio) with licencing options.
 * TO DO: Done    39) Set up an email address at neptuneDreams.com
 *
 * Bugs:
 * TO DO: Done   1) Sensor doesn't turn off on stop()
 * TO DO: Done   2) Transitions are wrong
 * TODO:         3) Rotating animation doesn't turn off.
 * TO DO: Cancel 4) Can't cancel (back out of) theme dialog.
 * TO DO: Done   5) Fix white portion of rotation animation.
 * TO DO: Done   6) Black lines on X & Y views aren't symmetrical.
 * TO DO: Done   7) Change calibration status on calibration complete.
 * TO DO: Done   8) Fix size of toast bar text.
 * TO DO: Done   9) Include display info/pixel info in crash report.
 * TO DO: Done   10) Crash: Setting per-cent view, then locking to x or y, then rotate past 100%
 *
 * TODO: MenuItems:
 * todo  Fluid Color          (settings)
 * todo  Accuracy             (settings)
 * todo  Report Bug
 * todo  Calibration Complete (settings)
 * Test of Commit.
 * </pre>
 * Logging should include:
 * Make/model of phone/tablet
 * screen resolution
 * Sensor resolution
 * sensor long and short interval
 * <p/>
 * Note: <br>
 * CodenameOne supports Android 2.6 and higher. Here's a link on how to get the android version:
 * http://stackoverflow.com/questions/3014117/how-can-i-check-in-code-the-android-version-like-1-5-or-1-6
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 4/4/16
 * <p>Time: 9:19 PM
 *
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings("HardCodedStringLiteral")
public class LevelOfVulcan implements PurchaseCallback
{
	// I set this value with a call to isTrue() instead of just true or false, to avoid warnings on if (debug)

	public static final boolean debug = !isTrue(); // debug off
//	public static final boolean debug = isTrue(); // debug on

	public static final boolean isPaid = !isTrue();

	// DO NOT GIVE THIS METHOD A CONTRACT! Its purpose is to suppress warnings.
	private static boolean isTrue() {
		return true;
	}

	private static final float A90f = 90.0f;
	private static final float A180f = 180F;
	public static final int FRAME_RATE_MILLIS = 50; // 50 ms comes to 20 frames per second.
	@SuppressWarnings("StaticNonFinalField")
	private static long installationId;

	private static long loadInstallationId() {
		long id = Prefs.prefs.get(Prefs.INSTALLATION_ID, 0L);
		if (id == 0) {
			id = ~System.currentTimeMillis();
			Log.p("idn: 0x" + toHex(id), Log.ERROR);
			Log.p("idp: 0x" + toHex(~id), Log.ERROR);
			Log.p("idp:   " + ~id, Log.ERROR);
			Log.p("now:   " + System.currentTimeMillis(), Log.ERROR);
			Prefs.prefs.set(Prefs.INSTALLATION_ID, id);
		}
		return id;
	}

//	private static void testHex(final long id) {
//		if (!Long.toHexString(id).equals(toHex(id))) {
//			Log.p("Mismatch! " + Long.toHexString(id) + " != " + toHex(id), Log.ERROR);
//		} else {
//			Log.p("ok: " + toHex(id), Log.ERROR);
//		}
//	}

	/**
	 * Convert a long to a hex string. 
	 * This method exists because codenameOne has Integer.toHexString() but not Long.toHexString(). 
	 * @param v The long value
	 * @return the hex representation of the long value
	 */
	@SuppressWarnings("MagicNumber")
	private static String toHex(long v) {
		int topHalf = (int)((v >> 32L) & 0xFFFF_FFFFL);
		int bottomHalf = (int) (v & 0xFFFF_FFFFL);
		return Integer.toHexString(topHalf) + Integer.toHexString(bottomHalf);
	}

	@SuppressWarnings("StaticNonFinalField")
	private static Resources theme;
	@SuppressWarnings("StaticNonFinalField")
	
	private static BubbleForm bubbleForm;

	@SuppressWarnings("NullableProblems")
	@NotNull
	private static LevelOfVulcan instance;

	private static final String OS_NAME = System.getProperty("os.name");
	public static final String BUBBLE_FORM = "BUBBLE_FORM";
	
	@Nullable
	private static final ProductTestNative productTest = createProductTest(isSimulation());

	@Nullable
	private static ProductTestNative createProductTest(boolean simulate) {
		if (simulate) {
			return simulateProductTest();
		}
		if (productTest == null) {
			return NativeLookup.create(ProductTestNative.class);
		}
		return productTest;
	}

	private static ProductTestNative simulateProductTest() {
		return new ProductTestNative() {
			@Override
			public String getPurchaseTestId() {
				return "Dummy";
			}

			@Override
			public String getCanceledTestId() {
				return "DummyCanceled";
			}

			@Override
			public String getRefundedTestId() {
				return "DummyRefunded";
			}

			@Override
			public String getUnavailableTestId() {
				return "DummyUnavailable";
			}

			@Override
			public boolean isSupported() {
				return false;
			}
		};
	}

	@SuppressWarnings("unused")
	public void init(Object context) {
		
//		java.util.Properties props = System.getProperties();
//		TreeSet<String> set = new TreeSet<>(props.stringPropertyNames());
//		for (Object key: set) {
//			Log.p("showProp(\"" + key + "\");");
//		}
		//noinspection AssignmentToStaticFieldFromInstanceMethod
		instance = this;
		
		initializeSimSize(); // simulation only.

		//		DefaultCrashReporter.init(true, 0);
		enableEmailCrashReporting();
		checkForPaidUpgrade();
		//noinspection AssignmentToStaticFieldFromInstanceMethod
		installationId=loadInstallationId();
		String initialTheme = "Theme 1";
		
		//noinspection AssignmentToStaticFieldFromInstanceMethod
		theme = UIManager.initNamedTheme("/vulcan", initialTheme); // "Theme", "BusinessTheme", "NativeTheme", "Social", "Blue", "Chrome"
		assert theme != null;
		if (isTablet()) {
			UIManager.getInstance().addThemeProps(theme.getTheme("tablet"));
		}

		Hashtable<Object, Object> newThemeProps = new Hashtable<>();
		// I need to start these with @, which tells the addThemeProps() method that these are constants, not theme props.
		// (The @ character gets stripped out.)
//		newThemeProps.put("@menuTransitionIn", "bubble");
//		newThemeProps.put("@menuTransitionOut", "fade");
//		newThemeProps.put("@showMenuBelowTitleBool", "false");
//		newThemeProps.put("@paintsTitleBarBool", "false");
		UIManager.getInstance().addThemeProps(newThemeProps);

//		final float METER = 1000.0f;
//		float pixelsPerMeter = Display.getInstance().convertToPixels(METER);
//		Log.p("Pixels Per Meter: " + pixelsPerMeter, Log.ERROR);
//		final float cmPerInch = 2.54f;
//		final float toCm = 100.0f; // to centimeters
//		Log.p("Pixels Per inch: " + format((pixelsPerMeter * cmPerInch) / toCm, 1), Log.ERROR);
//		int width = Display.getInstance().getDisplayWidth();
//		int height = Display.getInstance().getDisplayHeight();
//		Log.p("Display " + width + " x " + height + " pixels", Log.ERROR);
//		Log.p("approx  " + format((width * toCm) / pixelsPerMeter, 2) + " x " 
//				+ format((height * toCm) / pixelsPerMeter, 2) + " cm", Log.ERROR);
//		Log.p("approx  " + format(((width * toCm) / cmPerInch) / pixelsPerMeter, 2) + " x " 
//				+ format(((height * toCm) / cmPerInch) / pixelsPerMeter, 2) + " inches", Log.ERROR);

//		Log.p(initialTheme);
//		showConstant("menuPrefSizeBool");
//		showConstant("menuButtonBottomBool");
//		showConstant("menuButtonTopBool");
//		showConstant("menuHeightPercent");
//		showConstant("menuImage");
//		showConstant("menuSlideDirection");
//		showConstant("menuSlideInDirBool");
//		showConstant("menuSlideOutDirBool");
//		showConstant("menuTransitionIn");
//		showConstant("menuTransitionInImage");
//		showConstant("menuTransitionOut");
//		showConstant("menuTransitionOutImage");
//		showConstant("menuWidthPercent");
//		showConstant("showMenuBelowTitleBool");
//		showConstant("paintsTitleBarBool");

		/*
		 * Notes on the Social Theme:
		 * 1) The toolbar shows its icons in black, which can't be seen easily. To change them to white, you need to know 
		 *    their UIIDs. For the ... icon on the right, it's TitleCommand, and for the hamburger menu, it's MenuButton. I 
		 *    had to add these to the Social theme to change the icons to white. I did this by setting the foreground color
		 *    to white. I also set the transparency to 0, which is fully transparent. So it should be called opacity.
		 * 2) The MenuButtons were adapted from the default style. I changed the foreground, background, and transparency.
		 * 3) The Side Menu items are way too small. To modify them, I added a SideMenu UIID to the theme.
		 * 4) In an effort to expand the ... button in the Social theme, I'm setting margin and padding to match that of
		 *    the Blue theme. Original values were, for padding: 2, 2, 1, 2 mm. Margin: 1, 1, 0, 0 mm
		 * 5) I removed the button image from behind the ... button by removing the images from the TitleCommand style in
		 *    the resource editor.
		 * 6) I added menu animations by calling addNewThemeProps in the init() method.
		 *    (I no-longer know what that means.) Menu animations are determined by theme constants, defined in the resource
		 *    editor in the same place where you define the look of UIIDs. It's one of the choices (often hidden) along 
		 *    with selected and unselected, but off to the right.
		 * 7) The Overflow menu items seem to be controlled by the Command UIID, but selected and pressed don't seem to 
		 *    work.
		 * 8) The Social theme was apparently based on the native theme, which has different settings for the different
		 *    android platforms, which made it very hard to work with, since menus and dialog looked different on each
		 *    device. I rebuilt a whole new theme based on the flat blue theme, and transferred the graphics to it, 
		 *    making for a much simpler and more reliable theme.
		 */

		Toolbar.setGlobalToolbar(true);

		// imageSizes to support: 28, 36, 48, 72, 96, 196, 250, 330, 400, 

		//noinspection MagicNumber
		if (theme != null) {
			initializeSimulation(); // Does nothing unless we're in the simulator.
		}

//		showProp("AppName");
//		showProp("User-Agent");
//		showProp("AppVersion");
//		showProp("Platform");
//		showProp("OS");
//		showProp("OSVer");
//		showProp("AppArg");
////		showProp("AppName");
	}
	
	@SuppressWarnings("MagicNumber")
	private static boolean isTablet() {
		Display display = Display.getInstance();
		int shortSide = display.getDisplayWidth();
		int otherSide = display.getDisplayHeight();
		if (otherSide < shortSide) {
			shortSide = otherSide;
		}
		double pixelsPerInch = display.convertToPixels(1000) * 0.0254;
		double shortLengthInches = shortSide/pixelsPerInch;
		Log.p("Px/Inch: " + pixelsPerInch);
		Log.p("Inches: " + shortLengthInches);
		return shortLengthInches > 4.0;
	}
	
	@NotNull
	public static View getCurrentView() {
		assert bubbleForm != null;
		return bubbleForm.getCurrentView();
	}
	
	private void enableEmailCrashReporting() {
		// There will be no log to send if we don't set this to debug. 
		Log.setReportingLevel(Log.REPORTING_DEBUG);
//		if (!debug) {
//			Log.setLevel(Log.WARNING);
//		}
		
		// We need to set an empty error handler so it doesn't pop up a second dialog. This can't be null, so we 
		// create a listener that does nothing. Without this, Display will pop up its own dialog, which interferes with
		// the crash reporting dialog, and the application will usually hang the second time this method gets called.
		Display.getInstance().addEdtErrorHandler(e->{ });
		
		// This installs the crash reporter.
		ReportToDeveloper.init(true, 0);
	}

//	private static void showProp(String prop) {
//		Log.p(prop + '=' + Display.getInstance().getProperty(prop, "unknown"));
//	}

//	private static void showConstant(String constantName) {
//		String value = UIManager.getInstance().getThemeConstant(constantName, "--");
//		Log.p(constantName + ": " + value);
//	}

//	public static int count(HashMap<String, Object> hash) {
//		Set<String> keySet = new TreeSet<>(hash.keySet());
//		for (String key : keySet) {
//			Log.p(key + ": " + hash.get(key));
//		}
//		return hash.size();
//	}

//	private class ThemeItem implements Comparable<ThemeItem> {
//		@NotNull
//		private final String key;
//		@NotNull
//		private final String type;
//		private Object value;
//
//		ThemeItem(@NotNull String key, @NotNull String type, @Nullable Object value) {
//			this.key = key;
//			this.type = type;
//			this.value = value;
//		}
//
//		@Override
//		public String toString() {
//			return "    (" + type + ") " + key + ": " + value;
//		}
//
//		@Override
//		public int compareTo(@NotNull final ThemeItem o) {
//			int diff = type.compareTo(o.type);
//			if (diff == 0) {
//				diff = key.compareTo(o.key);
//			}
//			return diff;
//		}
//
//		@Override
//		public boolean equals(final Object obj) {
//			if (obj instanceof ThemeItem) {
//				ThemeItem item = (ThemeItem) obj;
//				return type.equals(item.type) && key.equals(item.key);
//			}
//			return false;
//		}
//
//		@Override
//		public int hashCode() {
//			return key.hashCode() + type.hashCode();
//		}
//	}

	@NotNull
	public static Resources getTheme() {
		//noinspection ConstantConditions
		return theme;
	}

//	private String generateLine(final long seconds, final float x, final float y, final float z) 
// {return "At " + seconds + ": (" + round(x, rnd) + ", " + round(y, rnd) + ", " + round(z, rnd) + ") [" 
// + round(-4.3F) + "]\n";}

	/**
	 * Calculate an angle, in degrees, from an axis from two perpendicular axes values.
	 * More formally, if a is the value along the A axis and b is the value along the B axis, this
	 * calculates the angle, in degrees, between the A axis and the line from the origin to the point (a, b).
	 * <p/>
	 * This calculates arcTan(a/b), then scales the result to degrees
	 *
	 * @param a The value along the A axis
	 * @param b The value along the B axis
	 * @return The angle, in degrees, between the A axis and the line from the origin to the point (a, b)
	 */
	public static float angle(float a, float b) {
		if (b == 0.0) {
			return A90f * ((a < 0) ? -1 : 1);
		}
		return (float) ((MathUtil.atan(a / b) * A180f) / Math.PI);
	}

//	public static boolean isPortrait() {
//		// resolutions: 888 x 540, 897 x 418, 540 x 766
//		final Display display = Display.getInstance();
////		Log.p("Display: " + display.getDisplayWidth() + ", " + display.getDisplayHeight());
//		return display.getDisplayWidth() < display.getDisplayHeight();
//	}

	@SuppressWarnings("unused")
	public void start() {
		final Display display = Display.getInstance();
		Form current = display.getCurrent();
		if (current == null) {
			if (display.canForceOrientation()) {
				display.lockOrientation(true);
//				Log.p("Locking to portrait");
//			} else {
//				Log.p("Orientation Lock unavailable.");
			}
//			display.setCommandBehavior(Display.COMMAND_BEHAVIOR_NATIVE);
			display.setCommandBehavior(Display.COMMAND_BEHAVIOR_SIDE_NAVIGATION);

			SensorsManager acceleratorSensor = SensorsManager.getSenorsManager(accelerometer);
			acceleratorSensor.setInterval(FRAME_RATE_MILLIS);

			//noinspection AssignmentToStaticFieldFromInstanceMethod
			bubbleForm = new BubbleForm();
			current = bubbleForm;
			FormNavigator.addForm(BUBBLE_FORM, current);
		}

		if (current instanceof SensorForm) {
			((SensorForm) current).start();
		}
		current.show();
//		FormNavigator.setCurrent(current);
		simulate(); // Does nothing unless we're in the simulator.
	}
	
	public static void setBubbleForm(BubbleForm bubbleForm) {
		//noinspection AccessStaticViaInstance
		instance.bubbleForm = bubbleForm;
	}
	
	private static void initializeSimSize() {
		if (isSimulation()) {
			Display display = Display.getInstance();
			int width = display.getDisplayWidth();
			int height = display.getDisplayHeight();
			if (width < height) {
				int swap = width;
				//noinspection SuspiciousNameCombination
				width = height;
				height = swap;
			}
			int hash = hashDimension(width, height);
			// Are there preferences yet?
			int savedWidth = Preferences.get(Prefs.PORTRAIT_SCREEN_WIDTH, -1);
			int savedHeight = Preferences.get(Prefs.PORTRIAT_SCREEN_HEIGHT, -1);
			if (savedWidth >= 0) {
				if (savedWidth != width) {
					int prefHash = hashDimension(savedWidth, savedHeight);
					Preferences.set(Prefs.PORTRAIT_SCREEN_WIDTH+prefHash, savedWidth);
					Preferences.set(Prefs.PORTRIAT_SCREEN_HEIGHT+prefHash, savedHeight);
					int revisedWidth = Preferences.get(Prefs.PORTRAIT_SCREEN_WIDTH+hash, -1);
					int revisedHeight = Preferences.get(Prefs.PORTRIAT_SCREEN_HEIGHT+hash, -1);
					if (revisedWidth >= 0) {
						Preferences.set(Prefs.PORTRAIT_SCREEN_WIDTH, revisedWidth);
						Preferences.set(Prefs.PORTRIAT_SCREEN_HEIGHT, revisedHeight);
					} else {
						Preferences.delete(Prefs.PORTRAIT_SCREEN_WIDTH);
						Preferences.delete(Prefs.PORTRIAT_SCREEN_HEIGHT);
					}
				}
			}
		}
	}
	
	private static int hashDimension(int width, int height) {
		int sum = width + height;
		return ((sum * (sum + 1)) / 2) + width;
	}

	private static void initializeSimulation() {
		if (isSimulation()) {
//			SensorsManager manager = 
			SensorsManager.getSenorsManager(simulator);
		}
	}

	private static boolean isSimulation() {return (OS_NAME != null) && OS_NAME.startsWith("Mac");}

	private static void simulate() {
		// Simulator only
		if (isSimulation()) {
			Log.p("Sim OS Name: " + OS_NAME);
			final double f_3_over_4 = 0.75;
			final float g = 9.81f;
			final Runnable r = new Runnable() {
				private int index = 0;
				private int bigIndex = 0;
				private static final int positions = 160;
//				private long prevTime = 0;
				private float pythagorTheta = (float) MathUtil.atan(f_3_over_4);
				private float pX = g * (float) Math.sin(pythagorTheta);
				private float pY = g * (float) Math.cos(pythagorTheta);
//				private float pZ = g * 

				@Override
				public void run() {
					final int TICKS = 1;
					index = bigIndex / TICKS;
					final int position = index % positions;
					int axis = (index / positions) % 3;
					int sign = (((index / (positions * 3)) % 2) * 2) - 1; // toggle between 1 & -1
					float theta = (float) (position * Math.PI * 2) / positions;
					float w = g * (float) Math.sin(theta);
					float h = g * (float) Math.cos(theta);
					//noinspection MultipleVariablesInDeclaration
					float x, y, z;
//					if (sign == 1) {
						switch (axis) {
							case 1:
								x = sign * w;
								y = h;
								z = 0;
								break;
							case 0:
								x = 0;
								y = sign * w;
								z = h;
								break;
							case 2:
								x = sign * h;
								y = 0;
								z = w;
								break;
							default:
								throw new Failure("axis = " + axis);
						}
//					} else {
//						switch (axis) {
//							case 0:
//								x = 0;
//								y = 0;
//								z = g;
//								break;
//							case 1:
//								x = pX;
//								y = 0;
//								z = pY;
//								break;
//							case 2:
//								x = 0;
//								y = pX;
//								z = pY;
//								break;
//							default:
//								throw new Failure("Axis = " + axis);
//						}
//					}
					//noinspection MagicNumber
//					final long now = System.currentTimeMillis();
//					long delta = now - prevTime;
//					prevTime = now;
//					System.out.print(" " + delta);
//					if ((bigIndex % TICKS) == 0) {
//						//noinspection MagicNumber
//						Log.p("LOV. at " + delta + " (" + x + ", " + y + ", " + z + ") axis=" + axis + " position=" + position + " theta=" + (theta * 180.0 / Math.PI));
//					}
					SensorsManager.onSensorChanged(accelerometer.id(), x, y, z);
					++bigIndex;
				}
			};
			final int intervalMs = 50; // 500;
//			UITimer timer = 
//			UITimer.timer(intervalMs, true, form, r);
			Runnable timer = new Runnable() {
				@Override
				public void run() {
					//noinspection InfiniteLoopStatement
					while (true) {
						long time = System.currentTimeMillis();
						long nextTime = ((time + intervalMs) / intervalMs) * intervalMs;
						//noinspection TooBroadScope
						long delta = nextTime - time;
						try {
							Thread.sleep(delta);
						} catch (InterruptedException ignored) {
							Thread.currentThread().interrupt();
						}
						r.run();
					}
				}
			};
			new Thread(timer, "Simulation Timer Thread").start();
		}
	}

////	private Button makeDumpButton() {
////		Command dumpCommand = new Command("Snapshot") {
////			@Override
////			public void actionPerformed(final ActionEvent evt) {
////				ScreenDump.snapComponent(scatterView, scatterView.getRangeDescription(BLUE));
////			}
////		};
////
////		return new Button(dumpCommand);
////	}
////
//	private Button makeDumpButton() {
//		Command dumpCommand = new Command("Snapshot") {
//			@Override
//			public void actionPerformed(final ActionEvent evt) {
//				ScreenDump.snapComponent(bubbleView, "BubbleView");
//			}
//		};
//
//		return new Button(dumpCommand);
//	}

	@SuppressWarnings("unused") // used for logging.
	public static double format(double value, int places) {
		int exp = 1;
		for (int ii = 0; ii < places; ++ii) {
			exp *= 10;
		}

		final double half = 0.5;
		// Works for both negative and positive values.
		return Math.floor((value * exp) + half) / ((double)exp);
	}
	
	@SuppressWarnings("unused")
	public static void showStyleColors(@NotNull com.codename1.ui.Component component) {
		com.codename1.ui.plaf.Style uStyle = component.getUnselectedStyle();
		Log.p(component.getUIID() + ": fg=" + Integer.toHexString(uStyle.getFgColor()) + " bg=" + Integer.toHexString(uStyle.getBgColor()) + " t=" + Integer.toHexString(uStyle.getBgTransparency()));
	}

	@SuppressWarnings("unused")
	public void stop() {
		Form current = Display.getInstance().getCurrent();
		
		// This may fix the problem of dialogs stepping on each other, when an exception is thrown, but I need to test that.
		while (current instanceof Dialog) {
			((Dialog) current).dispose();
			current = Display.getInstance().getCurrent();
		}
		if (current instanceof SensorForm) {
			Log.p("Calling stop() on form " + current.getTitle() + ", (" + current.getName() + ") of " + current.getClass());
			((SensorForm) current).stop();
		}

	}
	
	private void checkForPaidUpgrade() {
		boolean showAds = !Prefs.prefs.get(Prefs.AD_FREE, false);
		if (showAds && (productTest != null)) {
			Purchase purchase = Purchase.getInAppPurchase();
			if (purchase.isItemListingSupported()) {
//				ProductTestNative productTest = 
				boolean purchased = purchase.wasPurchased(productTest.getPurchaseTestId());
				if (purchased) {
					Prefs.prefs.set(Prefs.AD_FREE, true);
				}
			}
		}
	}

	@SuppressWarnings("unused")
	public void destroy() {
		Log.p("Calling Destroy() on for " + Display.getInstance().getCurrent().getTitle());
	}

	@Override
	public void itemPurchased(final String sku) {
		//noinspection StringConcatenation
		message("Purchase {0}" + sku);
	}

	@Override
	public void itemPurchaseError(final String sku, final String errorMessage) {
		//noinspection StringConcatenation
		message("Purchase Error of " + sku + ": " + errorMessage);
	}

	@Override
	public void itemRefunded(final String sku) {
		//noinspection StringConcatenation
		message("Refund: " + sku);
	}

	@Override
	public void subscriptionStarted(final String sku) {
		//noinspection StringConcatenation
		message("Subscription started: " + sku);
	}

	@Override
	public void subscriptionCanceled(final String sku) {
		//noinspection StringConcatenation
		message("Subscription Canceled: " + sku);
	}

	@Override
	public void paymentFailed(final String paymentCode, final String failureReason) {
		//noinspection StringConcatenation
		message("Payment failed: code = " + paymentCode + ", " + failureReason);
	}

	@Override
	public void paymentSucceeded(final String paymentCode, final double amount, final String currency) {
		//noinspection StringConcatenation
		message("Payment of " + currency + amount + " succeeded : code = " + paymentCode);
	}
	
	private void message(String s) {
		Log.p(s);
		Dialog.show("Message", s, Dialog.TYPE_INFO, null, "OK", null);
		
	}
}

/*
  Social
  ComponentGroupBool: true
  PopupDialogArrowBool: true
  PopupDialogArrowBottomImage: com.codename1.ui.EncodedImage@5e54e346
  PopupDialogArrowLeftImage: com.codename1.ui.EncodedImage@39646c74
  PopupDialogArrowRightImage: com.codename1.ui.EncodedImage@680fdd02
  PopupDialogArrowTopImage: com.codename1.ui.EncodedImage@4fc13579
  TabSelectedImage: com.codename1.ui.EncodedImage@36f6a114
  TabUnselectedImage: com.codename1.ui.EncodedImage@2ceb6c72
  alwaysTensileBool: true
  backUsesTitleBool: true
  checkBoxOppositeSideBool: true
  comboImage: com.codename1.ui.EncodedImage@3a1a4320
* commandBehavior: Title
  dialogButtonCommandsBool: true
  dialogPosition: Center
  dialogTransitionIn: fade
  dialogTransitionOut: fade
  dlgButtonCommandUIID: DialogButton
  dlgCommandButtonSizeInt: 80
  dlgCommandGridBool: true
  dlgInvisibleButtons: bfbfbf
  formTransitionOut: slidefade
  hideEmptyTitleBool: true
  includeExitBool: false
  includeNativeBool: true
  ios7SpinnerBool: false
  largeMaskImage: com.codename1.ui.EncodedImage@7c36d3e7
  maskImage: com.codename1.ui.EncodedImage@7f4b2374
  mediaBackImage: com.codename1.ui.EncodedImage@8b44813
  mediaFwdImage: com.codename1.ui.EncodedImage@605f1966
  mediaPauseImage: com.codename1.ui.EncodedImage@2a3c5761
  mediaPlayImage: com.codename1.ui.EncodedImage@2a3c5761
  noTextModeBool: true
  onOffIOSModeBool: true
* paintsTitleBarBool: true
  popupNoTitleAddPaddingInt: 2
  pureTouchBool: true
  radioOppositeSideBool: true
  radioSelectedFocusImage: com.codename1.ui.EncodedImage@cca91d1
  radioSelectedImage: com.codename1.ui.EncodedImage@2614b48b
  radioUnselectedFocusImage: com.codename1.ui.EncodedImage@3e180d06
  radioUnselectedImage: com.codename1.ui.EncodedImage@3e180d06
  shrinkPopupTitleBool: true
  switchMaskImage: com.codename1.ui.EncodedImage@76d7c2fb
  switchOffImage: com.codename1.ui.EncodedImage@2ecb81e9
  switchOnImage: com.codename1.ui.EncodedImage@2df8c857
  tabPlacementInt: 2
  tabsGridBool: true
  textCmpVAlignInt: 4
  textFieldCursorColorInt: 2251746
  tickerSpeedInt: 0
  tintColor: 0

  BusinessTheme
  PopupDialogArrowBool: false
  calTitleDayStyleBool: true
  calTransitionVertBool: false
  calendarLeftImage: com.codename1.ui.EncodedImage@68c27abe
  calendarRightImage: com.codename1.ui.EncodedImage@6d2fbde1
  centeredPopupBool: false
  changeTabContainerStyleOnFocusBool: true
  changeTabOnFocusBool: true
  checkBoxCheckDisFocusImage: com.codename1.ui.EncodedImage@2c7c8e8b
  checkBoxCheckDisImage: com.codename1.ui.EncodedImage@4941f91f
  checkBoxCheckedFocusImage: com.codename1.ui.EncodedImage@32ad7d03
  checkBoxCheckedImage: com.codename1.ui.EncodedImage@32ad7d03
  checkBoxOppositeSideBool: true
  checkBoxUncheckDisFocusImage: com.codename1.ui.EncodedImage@22fd0e02
  checkBoxUncheckDisImage: com.codename1.ui.EncodedImage@6abdc400
  checkBoxUncheckedFocusImage: com.codename1.ui.EncodedImage@2c7c8e8b
  checkBoxUncheckedImage: com.codename1.ui.EncodedImage@2c7c8e8b
  comboImage: com.codename1.ui.EncodedImage@2cf25149
* commandBehavior: Side
  defaultCommandImage: com.codename1.ui.EncodedImage@75ee74b1
  dialogButtonCommandsBool: true
  dialogPosition: Center
  dialogTransitionIn: fade
  dialogTransitionOut: fade
  dlgButtonCommandUIID: DialogButton
  dlgCommandButtonSizeInt: 80
  dlgCommandGridBool: true
  dlgInvisibleButtons: 1a1a1a
  fadeScrollBarBool: true
  fadeScrollEdgeBool: true
  formTransitionIn: empty
  formTransitionOut: slide
  hideBackCommandBool: true
  hideEmptyTitleBool: true
  includeNativeBool: true
  infiniteImage: com.codename1.ui.EncodedImage@3f29e910
* menuTransitionIn: bubble
* menuTransitionOut: fade
  noTextModeBool: true
  onOffIOSModeBool: true
  otherPopupRendererBool: false
  popupCancelBodyBool: true
  popupTitleBool: true
  pureTouchBool: true
  radioOppositeSideBool: true
  radioSelectedFocusImage: com.codename1.ui.EncodedImage@3bb0b731
  radioSelectedImage: com.codename1.ui.EncodedImage@3bb0b731
  radioUnselectedFocusImage: com.codename1.ui.EncodedImage@576d117
  radioUnselectedImage: com.codename1.ui.EncodedImage@576d117
  rendererShowsNumbersBool: false
  switchMaskImage: com.codename1.ui.EncodedImage@33b76f4
  switchOffImage: com.codename1.ui.EncodedImage@732a6eea
  switchOnImage: com.codename1.ui.EncodedImage@4d9d4681
  tabPlacementInt: 0
  tabsGridBool: true
  tensileHighlightBool: true
  tensileHighlightBottomImage: com.codename1.ui.EncodedImage@7e50d84c
  tensileHighlightTopImage: com.codename1.ui.EncodedImage@26848da4
  textCmpVAlignInt: 4
  tintColor: 0
  touchCommandFlowBool: false
  touchMenuBool: true

 */
