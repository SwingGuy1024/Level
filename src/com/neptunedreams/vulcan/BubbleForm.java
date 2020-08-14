package com.neptunedreams.vulcan;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import com.codename1.components.ToastBar;
import com.codename1.impl.CodenameOneThread;
import com.codename1.io.Log;
import com.codename1.io.PreferenceListener;
import com.codename1.io.Preferences;
import com.codename1.ui.Button;
import com.codename1.ui.Command;
import com.codename1.ui.Component;
import com.codename1.ui.Container;
import com.codename1.ui.Display;
import com.codename1.ui.FontImage;
import com.codename1.ui.Image;
import com.codename1.ui.Label;
import com.codename1.ui.TextArea;
import com.codename1.ui.Toolbar;
import com.codename1.ui.animations.CommonTransitions;
import com.codename1.ui.geom.Dimension;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.FlowLayout;
import com.codename1.ui.layouts.GridBagConstraints;
import com.codename1.ui.layouts.GridBagLayout;
import com.codename1.ui.layouts.LayeredLayout;
import com.codename1.ui.plaf.Style;
import com.neptunedreams.Assert;
import com.neptunedreams.vulcan.calibrate.CalibrationData;
import com.neptunedreams.vulcan.calibrate.CalibrationData.View;
import com.neptunedreams.vulcan.calibrate.PageOne;
import com.neptunedreams.vulcan.math.Accuracy;
import com.neptunedreams.vulcan.math.Units;
import com.neptunedreams.vulcan.settings.Commands;
import com.neptunedreams.vulcan.settings.OrientationLock;
import com.neptunedreams.vulcan.settings.Prefs;
import com.neptunedreams.vulcan.ui.FormNavigator;
import com.neptunedreams.vulcan.ui.RotatedLabel;
import com.neptunedreams.util.NotNull;
import com.neptunedreams.util.Nullable;

import static com.codename1.sensors.SensorType3D.*;
import static com.neptunedreams.vulcan.app.LevelOfVulcan.debug;
import static com.neptunedreams.vulcan.calibrate.CalibrationData.View.*;
import static com.neptunedreams.vulcan.calibrate.CalibrationData.Axis.*;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 4/28/16
 * <p>Time: 10:57 PM
 *
 * @author Miguel Mu\u00f1oz
 */
public class BubbleForm extends SensorForm 
		implements PreferenceListener,
		OrientationLock.LockListener,
		OrientationLock.OrientationListener,
		BubbleView.AngleChangedListener
//		BubbleView.SizeChangedListener //, PurchaseCallback
{
//	public static final double RADIANS = Math.PI / 180.0;
//	private static final char DEGREE = '\u00B0'; // little circle
//	public static final int screenWidth
//			= (Display.getInstance().getDisplayWidth() > Display.getInstance().getDisplayHeight()) ?
//			Display.getInstance().getDisplayHeight() : Display.getInstance().getDisplayWidth();

	@NotNull
	private final BubbleView bubbleView;
	@NotNull
	private final Label xLabel = new Label("\u21E0 -00.0 \u21E2");
	@NotNull
	private final RotatedLabel yLabel = new RotatedLabel("\u21E0 -00.0 \u21E2", false);
	@NotNull
	private final RotatedLabel xInvLabel = new RotatedLabel("\u21E0 -00.0 \u21E2");
	@NotNull
	private final RotatedLabel yInvLabel = new RotatedLabel("\u21E0 -00.0 \u21E2", true);
	@NotNull
	private final Label calibratedStatusLabel;
	@NotNull
	private final Button lockButton = makeLockButton();  // must be created after xLabel!
	@Nullable
	private final Button logButton = makeLogButton(); // must be created after xLabel!
	@NotNull
	private final Button calibrationButton;

	private Image star = getStarIcon();

	private final TextArea logField;
	private final GridBagConstraints bubbleConstraints;
//	private Container wrappedBubbleView;

	public BubbleForm() {
		super("Vulcan\u2019s Level", accelerometer);
		setScrollable(false);

//		Display d = Display.getInstance();
//		String adPaddingBottom = d.getProperty("adPaddingBottom", null);
//		Log.p("adPaddingBottom: " + adPaddingBottom, Log.ERROR);

		final GridBagLayout gridBagLayout = new GridBagLayout();
		setLayout(gridBagLayout);
		
		int currentRow = -1;

		bubbleConstraints = new GridBagConstraints();
		bubbleConstraints.fill = GridBagConstraints.BOTH;
		bubbleConstraints.weightx = 1.0;
		bubbleConstraints.gridx = 0;
		bubbleConstraints.gridy = ++currentRow;

		bubbleView = new BubbleView(Accuracy.getAccuracyPref());
		add(bubbleConstraints, wrapView(bubbleView));

		if (debug) {
			logField = new TextArea(4, 40);
			logField.setEditable(false);
			logField.setGrowByContent(false);
			Log.install(createDebugLog());

			GridBagConstraints prefSize = new GridBagConstraints();
			prefSize.gridx = 0;
			prefSize.gridy = ++currentRow;
			add(prefSize, logField);
		} else {
			logField = null;
		}

		Component spacer = new Component() { };
		spacer.setUIID("Label");
		
		GridBagConstraints prefSize = new GridBagConstraints();
		prefSize.gridx = 0;
		prefSize.gridy = ++currentRow;
		prefSize.fill = GridBagConstraints.BOTH;
		prefSize.weighty = 1.0;
		add(prefSize, spacer);

		final CalibrationData calibrationData = bubbleView.getCurrentView().getCalibrationData();
		//noinspection ConstantConditions
		Assert.doAssert(calibrationData != null); 
//		final String calibratedStatus = getCalibrationStatusText(calibrationData);
		calibratedStatusLabel = new Label(getViewName(View.z));
		calibratedStatusLabel.getAllStyles().setAlignment(CENTER);
		
		prefSize = new GridBagConstraints();
		prefSize.gridx = 0;
		prefSize.gridy = ++currentRow;
		prefSize.fill = GridBagConstraints.BOTH;
		add(prefSize, calibratedStatusLabel);

		calibrationButton = new Button("Prone View Calibration Wizard");
		FontImage.setMaterialIcon(calibrationButton, FontImage.MATERIAL_BUILD);

		calibrationButton.setVerticalAlignment(CENTER);
		final Style style = calibrationButton.getUnselectedStyle();
		style.setAlignment(CENTER);
		calibrationButton.setUnselectedStyle(style);
		calibrationButton.addActionListener(evt -> {
			PageOne pageOne = new PageOne(bubbleView.getCurrentView());
			FormNavigator.slideTo(pageOne);
		});
		prefSize = new GridBagConstraints();
		prefSize.gridx = 0;
		prefSize.gridy = ++currentRow;
		add(prefSize, calibrationButton);
		
		setTransitionInAnimator(CommonTransitions.createFade(100));

		formatLabels();
		
		List<Command> commands = Commands.getCommands();
		Toolbar toolbar = getToolbar();
		if (toolbar == null) {
			toolbar = new Toolbar();
			setToolbar(toolbar);
		}
		for (Command command : commands) {
			toolbar.addCommandToOverflowMenu(command);
//			toolbar.addCommandToSideMenu(command);
		}
	}

	@NotNull
	private Image getStarIcon() {
		// I can't put the star into the label that actually uses it, because it only visible in certain states. So I 
		// use this dummy button to create it.
		Button dummy = new Button("Dummy");

		// calling FontImage.createMaterialIcon gives me one in the wrong size on tablets. This method gets the size right.
		FontImage.setMaterialIcon(dummy, FontImage.MATERIAL_STAR);
		final Image starIcon = dummy.getIcon();
		assert starIcon != null;
		return starIcon;
	}

	@Override
	public void lockStateChanged(final boolean isLocked) {
		calibrationButton.setEnabled(!isLocked);
	}

	@Override
	public void orientationChanged(final char orientation) {
		setWizardName(orientation);
	}
	
//	public BubbleView getBubbleView() { return bubbleView; }

	private Log createDebugLog() {
		return new DebugLog();
	}

	private void formatLabels() {
		final int bgColor = bubbleView.getBgColor();
		formatLabel(xLabel, bgColor);
		formatLabel(yLabel, bgColor);
		formatLabel(xInvLabel, bgColor);
		formatLabel(yInvLabel, bgColor);
	}

	@NotNull
	private String getCalibrationStatusText(@NotNull final CalibrationData calibrationData) {
		//noinspection StringConcatenation
		String extra = debug ? (" (" + Display.getInstance().getPlatformName() + ')') : "";

		//noinspection StringConcatenation
		return "  " + getViewName(getCurrentView()) + " View: " + (calibrationData.isCalibrated() ? "Calibrated" : "(Uncalibrated)  ") + extra;
	}

	private Component wrapView(@NotNull Component view) {
		final Component[] buttons = makeButtonLayers(view);
		final Container component = LayeredLayout.encloseIn(buttons);
		
		// Place it inside a component to give it a preferred size.
		final Dimension prefSize = getPrefSize();
//		Log.p("wrapView prefSize = " + prefSize);
		Container sizer = new Container(new BorderLayout()) {
			@Override
			protected Dimension calcPreferredSize() {
				return prefSize;
			}
		};
		sizer.add(BorderLayout.CENTER, component);
//		wrappedBubbleView = sizer;
		return sizer;
	}
	
//	// Used for logging, when needed.
//	private String id(@Nullable Component c) {
//		if (c == null) {
//			return "null";
//		}
//		if (c instanceof Button) {
//			return "Button(" + ((Button)c).getText() + ')';
//		}
//		return c.getClass().toString();
//	}
//
	@NotNull
	private Dimension getPrefSize() {
//		Log.p("BubbleForm.readScreenSize.");

		Dimension screenSize = getScreenDimensions();
//		Log.p("BF.getPrefSize 1: " + screenSize);
		final int maxSize = Math.min(screenSize.getWidth(), screenSize.getHeight());
		final double fraction = BubbleView.OUTER_DIAMETER_FRACTION;
		double maxFraction = ((1.0 - fraction) / 2) + fraction;
//		Log.p("wrapView fraction = " + maxFraction);

		int maxHeight = (int) (maxSize * maxFraction);
//		Log.p(String.format("Size %d x %d", maxSize, maxHeight));
		return new Dimension(maxSize, maxHeight);
	}

	// We only save the screen dimensions if they're correct. If not, we leave this null.
	private static Dimension savedScreenDimensions = null;

	/**
	 * Returns the portrait screen dimensions, if they have been previously calculated. Otherwise
	 * returns the landscape dimensions. The dimensions returned don't include the space taken up
	 * by the title bar, so we can't just swap the numbers if we're in the wrong mode.
	 * This method gets the dimensions from the Preferences, if they're there. If not, it read them
	 * from the Display instance. If it read them in portrait mode, it saves them in the Preferences.
	 * @return A Dimension with screen coordinates.
	 */
	@NotNull
	static Dimension getScreenDimensions() {
		// We only save the value int sScreenDimensions if we've found the correct value. 
		Dimension screenDimensions = savedScreenDimensions;
		if (screenDimensions == null) {
			int screenWidth = Preferences.get(Prefs.PORTRAIT_SCREEN_WIDTH, -1);
			if (screenWidth >= 0) {
				int screenHeight = Preferences.get(Prefs.PORTRIAT_SCREEN_HEIGHT, -1);
				screenDimensions = new Dimension(screenWidth, screenHeight);
				savedScreenDimensions = screenDimensions;
				return screenDimensions;
			}
		} else {
			return screenDimensions;
		}
		
		// At this point, screenDimensions will be null.
		Display display = Display.getInstance();
		int screenWidth = display.getDisplayWidth();
		int screenHeight = display.getDisplayHeight();
		screenDimensions = new Dimension(screenWidth, screenHeight);
		// We only save dimensions for portrait mode
		if (screenHeight > screenWidth) {
			Preferences.set(Prefs.PORTRAIT_SCREEN_WIDTH, screenWidth);
			Preferences.set(Prefs.PORTRIAT_SCREEN_HEIGHT, screenHeight);
			savedScreenDimensions = screenDimensions;
		}
		Assert.doAssert(screenHeight != -1);
		return screenDimensions;
	} 

//	void replaceWrappedView() {
//		bubbleView.repaint();
//		bubbleView.remove();
//		xLabel.remove();
//		yLabel.remove();
//		;
//		xInvLabel.remove();
//		yInvLabel.remove();
//		lockButton.remove();
//		if (logButton != null) {
//			logButton.remove();
//		}
//		wrappedBubbleView.removeAll();
//		final Container oldWrappedView = wrappedBubbleView;
//		replaceAndWait(oldWrappedView, wrapView(bubbleView), null, true);
//		repaint();
//	}
	
	@NotNull
	private Component[] makeButtonLayers(final Component view) {
		Component[] certainties = {
				view,
				FlowLayout.encloseRight(xLabel),
				FlowLayout.encloseBottom(yLabel),
				FlowLayout.encloseBottom(xInvLabel),
				FlowLayout.encloseRight(yInvLabel),
				FlowLayout.encloseIn(lockButton)            // top left
		};
		List<Component> buttonList = new LinkedList<>();
		Collections.addAll(buttonList, certainties);
		if (logButton != null) {
			buttonList.add(FlowLayout.encloseRightBottom(logButton));
		}
		setZLabels(0.0, 0.0); // This is just for the iPhone starting image.
		return buttonList.toArray(new Component[buttonList.size()]);
	}

	private static void formatLabel(@NotNull Label label, int bgColor) {
		final Style allStyles = label.getAllStyles();
		allStyles.setBgColor(bgColor);
		//noinspection MagicNumber
		allStyles.setFgColor(0xffffff);
		allStyles.setBgTransparency(0);
	}

	@Override
	protected void onShow() {
		setTransitionInAnimator(null);
		super.onShow();
	}

	@Override
	protected void initComponent() {
//		Log.p("BubbleForm.initComponent()@" + hashCode());
		Prefs.prefs.addPreferenceListener(Prefs.FLUID_HUE, this);
		Prefs.prefs.addPreferenceListener(Prefs.getAxisKey(y, XAxis), this);
		Prefs.prefs.addPreferenceListener(Prefs.getAxisKey(x, YAxis), this);
		Prefs.prefs.addPreferenceListener(Prefs.getAxisKey(z, ZAxis), this);
		final CalibrationData calibrationData = CalibrationData.getCalibrationDataForView(getCurrentView());
		adjustCalibrationStatusDisplay(calibrationData);
	}

	private void adjustCalibrationStatusDisplay(@NotNull final CalibrationData calibrationData) {
//		Log.p("adjustCalDisplay: " + calibrationData.view + ", isCal=" + calibrationData.isCalibrated());
		// todo: Is the calibration updated yet? 
		calibratedStatusLabel.setText(getCalibrationStatusText(calibrationData));
//		Thread.dumpStack();
		if (calibrationData.isCalibrated()) {
			final int white = 0xffffff;
			calibratedStatusLabel.getStyle().setFgColor(white);
			calibratedStatusLabel.setIcon(star);
		} else {
			final int disabled = 0x7f0000; // Dark red
			calibratedStatusLabel.getStyle().setFgColor(disabled);
			calibratedStatusLabel.setIcon(null);
		}
	}

	@Override
	protected void deinitialize() {
//		Log.p("BubbleForm.deinitialize()" + '@' + hashCode());
		Prefs.prefs.removePreferenceListener(Prefs.FLUID_HUE, this);
		Prefs.prefs.removePreferenceListener(Prefs.getAxisKey(y, XAxis), this);
		Prefs.prefs.removePreferenceListener(Prefs.getAxisKey(x, YAxis), this);
		Prefs.prefs.removePreferenceListener(Prefs.getAxisKey(z, ZAxis), this);
//		System.err.println("Stack trace on BubbleView@" + hashCode());
	}

	@Override
	public void doStart() {
//		Log.p("BubbleForm.doStart()");
		BasicLevelModel model = bubbleView.getModel();
		if (model == null) {
			model = new BasicLevelModel();
			bubbleView.setModel(model);
		}
		addSensorObserver(accelerometer, model);

		bubbleView.addAngleChangedListener(this);

		OrientationLock.lock.addLockListener(this);
		OrientationLock.lock.addOrientationListener(this);
	}

	@Override
	public void doStop() {
//		Log.p("BubbleForm.doStop()");
		final BasicLevelModel model = bubbleView.getModel();
		removeSensorObserver(accelerometer, model);
//		bubbleView.removeSizeChangedListener(this);
		bubbleView.removeAngleChangedListener(this);
		OrientationLock.lock.removeLockListener(this);
		OrientationLock.lock.removeOrientationListener(this);
		Log.e(new Error("BubbleForm.doStop()"));
//		bubbleView.setModel(null);
	}

	@Override
	public void preferenceChanged(@NotNull final String pref, @Nullable final Object priorValue, @Nullable final Object revisedValue) {
//		Log.p("PreferenceChanged: " + pref);

		//noinspection EqualsReplaceableByObjectsCall
		if (pref.equals(Prefs.FLUID_HUE)) {
			if (revisedValue != null) {
				bubbleView.setHue(((Integer) revisedValue).byteValue());
				bubbleView.repaint();
			}
//		} else if (pref.equals(Prefs.getAxisKey(z, XAxis))) {
		} else if (pref.indexOf(Prefs.AXIS_SUFFIX) >= 0) {
//			Log.p("Changing Pref " + pref);
			CalibrationData calibrationData = CalibrationData.getCalibrationDataForCurrentView();
//			calibratedStatusLabel.setText(getCalibrationStatusText(calibrationData));
			adjustCalibrationStatusDisplay(calibrationData);
		} else {
			throw new AssertionError("Undeclared switch option: " + pref);
		}
	}
	
	@NotNull
	public View getCurrentView() {
		return bubbleView.getCurrentView();
	}

	@Override
	public void angleChanged(char view, final double xAngle, final double yAngle) {
		switch(view) {
			case 'x':
			case 'X':
				setXAxisLabels(view, yAngle);
				break;
			case 'y':
			case 'Y':
				setYAxisLabels(view, xAngle);
				break;
			case 'Z':
				setZLabels(xAngle, yAngle);
				break;
			default:
				throw new IllegalStateException("Unknown view: " + view);
		}
	}

	private void setXAxisLabels(final char view, double yAngle) {
		// X axis is vertical, so we set the y labels
//		double angle = -MathUtil.atan(vector.getY() / vector.getX()) / RADIANS;
		String yText;
		String yInvText;
		Units units = Units.getPreferredUnits();
		final String yFmt = units.format(yAngle);
		if (view == 'x') {
			yText = yFmt;
			yInvText = "";
		} else {
			yText = "";
			yInvText = yFmt;
		}
		setAllLabels("", yText, "", yInvText);
	}

	private void setYAxisLabels(char view, double xAngle) {
		// Y axis is vertical, so we set the X labels.
//		double angle = -MathUtil.atan(vector.getX() / vector.getY()) / RADIANS;
		String xText;
		String xInvText;
		Units units = Units.getPreferredUnits();
		if (view == 'y') {
			xText = units.format(xAngle);
			xInvText = "";
		} else {
			xText = "";
			xInvText = units.format(xAngle);
		}
		setAllLabels(xText, "", xInvText, "");
	}

	private void setZLabels(double xAngle, double yAngle) {
		Units units = Units.getPreferredUnits();
		//noinspection StringConcatenation
		setAllLabels(
				"\u21E0 " + units.format(xAngle) + " \u21E2", 
				"\u21E0 " + units.format(yAngle) + " \u21E2",
				"",
				""
		);
	}

	/**
	 * This method is here to make sure that all labels get set.
	 * 
	 * @param x xLabel
	 * @param y yLabel
	 * @param xInv xInvLabel
	 * @param yInv yInvLabel
	 */
	private void setAllLabels(@NotNull String x, @NotNull String y, @NotNull String xInv, @NotNull String yInv) {
		xLabel.setText(x);
		yLabel.setText(y);
		xInvLabel.setText(xInv);
		yInvLabel.setText(yInv);
	}

//	public static String fmt(double angle) { // TODO: Get rid of me.
//		if (angle >= 0.0) {
//			//noinspection MagicNumber
//			return pad(String.valueOf((int) ((angle * 10) + 0.5) / 10.0), 5);
//		} else {
//			//noinspection MagicNumber
//			return pad(String.valueOf((int) ((angle * 10) - 0.5) / 10.0), 5);
//		}
//	}
//
//	private static final String spaces ="                              ";
//	@NotNull
//	private static String pad(@NotNull String number, int max) {
//		Assert.doAssert(number.length() <= max, number + " length > " + max);
//		int length = number.length();
//		if (length < max) {
//			StringBuilder nBuilder = new StringBuilder(number);
//			int dotSpot = number.indexOf('.');
//			nBuilder.append("00".substring((length - dotSpot))); // This assume we pad a max of 1 zero at the end.
//			String spacePad = spaces.substring(0, max);
//			nBuilder.insert(0, spacePad.substring(nBuilder.length())); // 5 spaces
//			Assert.doAssert(nBuilder.length() == max, "<" + nBuilder + "> from <" + number + '>');
//			return nBuilder.toString();
//		}
//		return number;
//	}

	@NotNull
	private Button makeLockButton() {
		final Button button = makeToolTipButton("Lock the current view");
		button.setUIID("LovLock"); // gets rid of the border and the left/right margins. Specifies new margins

		int toPixels = Display.getInstance().convertToPixels(1.0f);
		int fontHeight = button.getUnselectedStyle().getFont().getHeight()/toPixels;
		Image lockIcon = FontImage.createMaterial(FontImage.MATERIAL_LOCK, button.getUnselectedStyle(), fontHeight);
		Image unlockIcon = FontImage.createMaterial(FontImage.MATERIAL_LOCK_OPEN, button.getUnselectedStyle(), fontHeight);
		final Commands.ToggleIconPair iconPair = new Commands.ToggleIconPair(lockIcon, unlockIcon);
		button.setIcon(iconPair.getMaterialIcon(false));
		button.addActionListener(e -> {
			final OrientationLock lock = OrientationLock.lock;
			lock.setValue(!lock.getValue());
			button.setIcon(iconPair.getMaterialIcon(lock.getValue()));
		});
		return button;
	}

	@NotNull
	private Button makeToolTipButton(final String msg) {
		return new Button() {
				@Override
				public void longPointerPress(final int x, final int y) {
					ToastBar.showErrorMessage(msg);
				}
			};
	}
	
	@Nullable
	private Button makeLogButton() {
		Button logBtn = null;
		if (debug) {
			logBtn= makeToolTipButton("Log current reading");
			logBtn.addActionListener(e -> doLog());
			logBtn.setUIID("LovLock");
			FontImage.setMaterialIcon(logBtn, FontImage.MATERIAL_PRINT);
		}
		return logBtn;
	}

	private void doLog() {
		final BasicLevelModel model = bubbleView.getModel();
		assert model != null;
		model.setDoLog(true);
	}

	private void setWizardName(final char view) {
		char viewUp = Character.toUpperCase(view);
		//noinspection StringConcatenation
		calibrationButton.setText(getViewName(viewUp) + "-Axis Calibration Wizard");
//		Log.p(calibrationButton.getText() + " from " + view + " (" + getCurrentView() + ')');
		//noinspection ConstantConditions
		Assert.doAssert(getCurrentView().toString().equalsIgnoreCase(String.valueOf(view)), "Mismatch: " + view + " != " + getCurrentView());
		adjustCalibrationStatusDisplay(getCurrentView().getCalibrationData());
//		calibratedStatusLabel.setText(getCalibrationStatusText(getCurrentView().getCalibrationData()));
	}
	
	public static String getViewName(@NotNull View view) {
		return getViewName(view.name().charAt(0));
	}
	
	private static String getViewName(char view) {
		switch (view) {
			case 'x' :
			case 'X' : return "Wide";
			case 'y' :
			case 'Y' : return "High";
			case 'z' :
			case 'Z' : return "Prone";
			default:
				throw new IllegalArgumentException("Unknown view: " + view);
		}
	}
//	static { testPad(); }
//	private static void testPad() {
//		for (double d = -1.0; d < 1.0; d += 0.01) {
//			Log.p(fmt(d) + " from " + d);
//		}
//		for (double d = -32.0; d < -30.0; d += 0.01) {
//			Log.p(fmt(d) + " from " + d);
//		}
//		for (double d = 32.0; d < 34.0; d += 0.01) {
//			Log.p(fmt(d) + " from " + d);
//		}
//	}

//	@Override
//	public void itemPurchased(final String sku) {
//		message("Purchase {0}" + sku);
//	}
//
//	@Override
//	public void itemPurchaseError(final String sku, final String errorMessage) {
//		message("Purchase Error of " + sku + ": " + errorMessage);
//	}
//
//	@Override
//	public void itemRefunded(final String sku) {
//		message("Refund: " + sku);
//	}
//
//	@Override
//	public void subscriptionStarted(final String sku) {
//		message("Subscription started: " + sku);
//	}
//
//	@Override
//	public void subscriptionCanceled(final String sku) {
//		message("Subscription Canceled: " + sku);
//	}
//
//	@Override
//	public void paymentFailed(final String paymentCode, final String failureReason) {
//		message("Payment failed: code = " + paymentCode + ", " + failureReason);
//	}
//
//	@Override
//	public void paymentSucceeded(final String paymentCode, final double amount, final String currency) {
//		message("Payment of " + currency + amount + " succeeded : code = " + paymentCode);
//	}
//
//	private void message(String s) {
//		Log.p(s);
//		Dialog.show("Form Message", s, Dialog.TYPE_INFO, null, "OK", null);
//
//	}
	
	private class DebugLog extends Log {
		@Override
		protected void print ( @NotNull final String text, final int level){

			super.print(text, level);
			if (Log.getLevel() > level) {
				return;
			}
//			appendToLogField("Thread instanceof " + Thread.currentThread().getClass());
			assert logField != null;
			appendToLogField(text);
		}

		@Override
		protected void logThrowable (@NotNull final Throwable t){
//			Log.p("logThrowable() 1");
			String stackText = "unknown stack trace";
			Thread currentThread = Thread.currentThread();
			if (currentThread instanceof CodenameOneThread) {
				CodenameOneThread thread = (CodenameOneThread) currentThread;
				stackText = thread.getStack(t);
			}
			appendToLogField(stackText);
//			Thread thr = Thread.currentThread();
//			appendToLogField("Thread instanceof " + thr.getClass());
//			Log.p("logThrowable() 2");
			super.logThrowable(t);
//			Log.p("logThrowable() 3");
		}

		private void appendToLogField(@NotNull final String text) {
			assert logField != null;
			String oldText = logField.getText();
			//noinspection StringConcatenation
			logField.setText(oldText + '\n' + text);

			// Scroll to bottom.
			Dimension scr = logField.getScrollDimension();
			if (scr != null) {
				logField.scrollRectToVisible(0, scr.getHeight()-1, 1, 1, null);
			}
		}
	}
}
