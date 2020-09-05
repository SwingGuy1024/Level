package com.neptunedreams.vulcan.settings;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import com.codename1.components.MultiButton;
import com.codename1.io.Log;
import com.codename1.io.Util;
import com.codename1.payment.Purchase;
import com.codename1.ui.BrowserComponent;
import com.codename1.ui.Button;
import com.codename1.ui.Command;
import com.codename1.ui.Container;
import com.codename1.ui.Dialog;
import com.codename1.ui.Display;
import com.codename1.ui.FontImage;
import com.codename1.ui.Form;
import com.codename1.ui.Image;
import com.codename1.ui.Label;
import com.codename1.ui.TextArea;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.events.ActionListener;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.FlowLayout;
import com.codename1.ui.plaf.Style;
import com.codename1.ui.plaf.UIManager;
import com.neptunedreams.vulcan.BubbleForm;
import com.neptunedreams.vulcan.app.LevelOfVulcan;
import com.neptunedreams.vulcan.calibrate.CalibrationData;
import com.neptunedreams.vulcan.calibrate.CalibrationData.View;
import com.neptunedreams.vulcan.math.Accuracy;
import com.neptunedreams.vulcan.math.Units;
import com.neptunedreams.util.NotNull;
import com.neptunedreams.util.Nullable;
import com.neptunedreams.util.ReportToDeveloper;
import com.neptunedreams.vulcan.ui.FormNavigator;

import static com.codename1.ui.Component.*;
import static com.neptunedreams.vulcan.app.LevelOfVulcan.*;
import static com.neptunedreams.vulcan.settings.Commands.TestPurchase.*;

/**
 * Commands appear on the overflow menu (or, conceivably, on the side menu). Their appearance is controlled by the
 * Command UIID.
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 5/26/16
 * <p>Time: 4:40 PM
 *
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings({"HardCodedStringLiteral"})
public final class Commands {

	private Commands() { }
	public static final int DEFAULT_HUE = 170;
	
	private static final String adFreeProductId = "level.of.vulcan.payment.1";
	
	@NotNull
	private static final List<Runnable> prepareList = new LinkedList<>();
	
	@NotNull
	public static List<Command> getCommands() {
		List<Command> commandList = new LinkedList<>();
		createCommand(commandList, "Fluid Color", FontImage.MATERIAL_VISIBILITY, evt -> selectColor());
		createCommand(commandList, "Clear Calibration  ", FontImage.MATERIAL_HIGHLIGHT_OFF, (evt) -> clearCalibration());
		createCommand(commandList, "Units", FontImage.MATERIAL_STRAIGHTEN, evt -> units());
		createCommand(commandList, "Set Accuracy", FontImage.MATERIAL_LINEAR_SCALE, evt -> accuracy());
		final BooleanGetter vectorGetter = () -> Prefs.prefs.get(Prefs.ROTATING_VECTOR, true);
		createBooleanCommand(commandList, "Rotating Angle Display  ", vectorGetter, (value) -> Prefs.prefs.set(Prefs.ROTATING_VECTOR, value));
		final BooleanGetter booleanGetter = () -> Prefs.prefs.get(Prefs.AXIS_INDICATORS, true);
		createBooleanCommand(commandList, "Axis Markers", booleanGetter, (value) -> Prefs.prefs.set(Prefs.AXIS_INDICATORS, value));
//		createCommand(commandList, "Size Change", FontImage.MATERIAL_FORMAT_SIZE, v -> doSizeChange());
//		createCommand(commandList, "Show Watches", FontImage.MATERIAL_SLIDESHOW, v -> showWatches());
		if (debug) {
//			createCommand(commandList, "Lock to View", FontImage.MATERIAL_LOCK, evt -> lockView());
			createCommand(commandList, "Crash", FontImage.MATERIAL_BUG_REPORT, evt -> crash());
			createCommand(commandList, "See Prefs", FontImage.MATERIAL_VISIBILITY, evt -> seePrefs());
			createCommand(commandList, "TestPurchase", FontImage.MATERIAL_ATTACH_MONEY, evt -> testPurchase());
		}
		createCommand(commandList, "EMail Error Log", FontImage.MATERIAL_REPORT_PROBLEM, evt -> emailLog());
//		createBooleanCommand(
//				commandList, 
//				"Lock Orientation", 
//				OrientationLock.lock, 
//				OrientationLock.lock, 
//				FontImage.MATERIAL_LOCK, 
//				FontImage.MATERIAL_LOCK_OPEN
//		);
		Purchase purchase = Purchase.getInAppPurchase();
		if (debug && !LevelOfVulcan.isPaid && purchase.isManagedPaymentSupported()) {
			createCommand(commandList, "Go Ad-Free", FontImage.MATERIAL_UPDATE, e -> upgrade());
//		} else {
			
		}
		createCommand(commandList, "Email Feedback", FontImage.MATERIAL_EMAIL, evt -> feedback());
		createCommand(commandList, "License Agreement", FontImage.MATERIAL_PERM_DEVICE_INFORMATION, e -> license());
		createCommand(commandList, "Privacy Policy", FontImage.MATERIAL_PERM_DEVICE_INFORMATION, e -> privacy());

		return commandList;
	}

//	private static void doSizeChange() {
//		Runnable runnable = new Runnable() {
//			public void run() {
//				try {
//					Thread.sleep(1000);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//				Display display = Display.getInstance();
//				Form currentForm = display.getCurrent();
//				if (currentForm instanceof BubbleForm) {
//					BubbleForm bubbleForm = (BubbleForm) currentForm;
//		//			bubbleForm.sizeChanged(new Dimension(10, 10));
////					bubbleForm.getBubbleView().replaceBubbleForm(display);
//					bubbleForm.getBubbleView().replaceWrappedDisplay();
//				}
//			}
//		};
//		Display.getInstance().callSerially(runnable); 
//	}
//	
//	private static void showWatches() {
//		System.gc();
////		LeakWatch.watch.showAll();
//	}
//
	/**
	 * Adds a "prepareTask" that gets run by all commands before they execute.
	 * @param prepareTask The prepare task
	 */
	public static void addPrepareTask(@NotNull Runnable prepareTask) {
		prepareList.add(prepareTask);
	}

	/**
	 * Replace a current PrepareTask with a new one of the same class. This is in case a component that has set the
	 * task gets destroyed, and a new one gets created. This replaces the old task, installed by the destroyed
	 * component, with a new one
	 * @param prepareTask The prepare task.
	 */
	public static void replacePrepareTask(@NotNull Runnable prepareTask) {
		for (Runnable currentTask: prepareList) {
			if (currentTask.getClass() == prepareTask.getClass()) {
//				Log.p("Replacing task " + currentTask.hashCode() + " with " + prepareTask.hashCode());
				prepareList.remove(currentTask);
				prepareList.add(prepareTask);
				return;
			}
		}
//		Log.p("Adding task " + prepareTask.hashCode() + " for " + (prepareList.size() + 1) + " tasks.");
		prepareList.add(prepareTask);
	}

	@SuppressWarnings("unused")
	public static void removePrepareTask(@NotNull Runnable prepareTask) {
		prepareList.remove(prepareTask);
	}

	@SuppressWarnings("unused")
	private static String getActualEncryptedProductId() {
		final long id = Prefs.prefs.get(Prefs.INSTALLATION_ID, 0);
		String value = adFreeProductId + '.' + id;
		int hashCode = value.hashCode();
		return String.valueOf(hashCode);
	}

	@SuppressWarnings("unused")
	private static String getStoredProductId() {
		return Prefs.prefs.get(Prefs.PURCHASE_ID, "");
	}

	private static void units() {
		Units[] values = Units.values();
		showOptionList("Units", values, Units.getPreferredUnits(), null, UnitControl::new, UnitControl::getUnit, Units::setPreferredUnits);
	}

	private static void emailLog() {
		ReportToDeveloper.sendEmail("Vulcan\u2019s Level Log File", ReportToDeveloper.readLogText());
	}

	private static void seePrefs() {
		final String allPrefs = Prefs.getAllPrefs();
//		Log.p(allPrefs);
		ReportToDeveloper.sendEmail("Prefs", allPrefs);
	}

	private static void upgrade() {
		Purchase purchase = Purchase.getInAppPurchase();
		purchase.purchase(adFreeProductId);
	}

	private static void accuracy() {
		final Accuracy[] values = Accuracy.values();
		showOptionList(
				"Accuracy",
				values,
				Accuracy.getAccuracyPref(),
				"Sets the size of the center target. This does not affect the accuracy of the internal sensor.",
				AccuracyControl::new,
				AccuracyControl::getAccuracy,
				Accuracy::setAccuracyPref
				);
	}

	private static <T, TC extends MultiButton> void showOptionList(
			@NotNull final String title,
			@NotNull T[] values,
			@NotNull T currentValue,
			@Nullable String instructions,
			@NotNull Factory<T, TC> factory,
			@NotNull Getter<T, TC> getter,
			@NotNull Setter<T> preferenceSetter
	) {
		Container masterBox = new Container(new BorderLayout());
		Container box = new Container(new BoxLayout(BoxLayout.Y_AXIS));
		masterBox.add(BorderLayout.CENTER, box);

		if (instructions != null) {
			// I tried a SpanLabel, but it didn't work. It took up space but no text was visible.
			TextArea label = new TextArea(instructions); // A non-editable TextArea serves the same purpose
			label.setEditable(false);
			label.setUIID("Label"); // this gets rid of the border.
//			label.getAllStyles().setFgColor(0);
			// We put it in North, instead of the main box, to prevent it from scrolling.
			masterBox.add(BorderLayout.NORTH, label);
			
		}
		box.setScrollableY(true);

		List<TC> selectedControl = new LinkedList<>();
		for (T t: values) {
			TC control = factory.buildComponent(t);
			control.setGroup(title);
			box.add(control);
			//noinspection ObjectEquality
			if (t == currentValue) {
				control.setSelected(true);
				selectedControl.add(control);
				control.requestFocus();
			}
			control.addActionListener((evt) -> {
				selectedControl.clear();
				selectedControl.add(control);
			});
		}

		Command ok = new Command("Ok");
		Command cancel = new Command("Cancel");
		Command result = Dialog.show(title, masterBox, ok, cancel);
		//noinspection ObjectEquality
		if ((result == ok) && !selectedControl.isEmpty()) {

			// Get the selected Unit.
			TC control = selectedControl.get(0);
			// This should always be true.
			if (control != null) {
				final T item = getter.getValue(control);

				// If the chosen units have changed...
				//noinspection ObjectEquality
				if (item != currentValue) {
					preferenceSetter.setValue(item);
//					Accuracy.setAccuracyPref(item);
				}
			}
		}
	}

	@SuppressWarnings("unused")
	private static void notYet() {
		Dialog.show(
				"Coming", 
				"Not yet available. This feature will be added in a future release", 
				Dialog.TYPE_INFO, 
				null, 
				"OK", 
				null
		);
	}

	private static void feedback() {
		ReportToDeveloper.sendEmail("Vulcan\u2019s Level Feedback", "");
	}
	
	public static void license() {

		final int size = 2048;
		//noinspection TooBroadScope
		int count = 0;
		//noinspection TooBroadScope
		byte[] byteBuf = new byte[size];
		//noinspection TooBroadScope
		StringBuilder builder = new StringBuilder();
		try (
			InputStream i = getLicenseStream()
		) {
			while (count >= 0) {
				builder.append(new String(byteBuf, 0, count, StandardCharsets.UTF_8)); // "UTF-8"
				count = i.read(byteBuf, 0, size);
			}
			String licenseAgreement = builder.toString();
			Dialog.show("License Agreement", licenseAgreement, Dialog.TYPE_INFO, null, "OK", null);
		} catch (IOException ioe) {
			Log.e(ioe);
		}
	}

	@NotNull
	private static InputStream getLicenseStream() throws IOException {
		InputStream i = Display.getInstance().getResourceAsStream(Command.class, "/license.txt");
		if (i == null) {
			throw new IOException("Resource not found: license.txt");
		}
		return i;
	}

	public static void privacy() {
//		String url = "https://www.iubenda.com/privacy-policy/7954283";
		String privacyFile = "privacy.html";
//		String privacyFile = "/com/neptunedreams/vulcan/settings/privacy.html";
		InputStream inputStream = LevelOfVulcan.getTheme().getData(privacyFile);
		String html;
		try {
			html = Util.readToString(inputStream);
		} catch (IOException e) {
			IllegalStateException illegalStateException = new IllegalStateException(e.getMessage());
			//noinspection UnnecessaryInitCause
			illegalStateException.initCause(e); // Codename1 doesn't have the constructor with the cause.
			throw illegalStateException;
		}
		BrowserComponent browserComponent = new BrowserComponent();
		browserComponent.setPage(html, privacyFile);
		
		Form privacyForm = new Form("Vulcan's Level", new BorderLayout());
		final Label title = new Label("Neptune Dreams Privacy Policy");
		final Container okPanel = new Container(new FlowLayout(CENTER));
		final Button okay = new Button("Ok");
		okPanel.add(okay);

		privacyForm.add(BorderLayout.NORTH, title);
		privacyForm.add(BorderLayout.CENTER, browserComponent);
		privacyForm.add(BorderLayout.SOUTH, okPanel);
		FormNavigator.slideToSymmetric(privacyForm);    // sets a new back command...
		okay.setCommand(privacyForm.getBackCommand());  // So we delay getting the back command until now.
	}

	private static void clearCalibration() {
		final View currentView = LevelOfVulcan.getCurrentView();
		//noinspection StringConcatenation
		final String text = "Clear the calibration for the " + BubbleForm.getViewName(currentView) + " view?";
		boolean clear = Dialog.show("Calibration", text, Dialog.TYPE_CONFIRMATION, null, "Ok", "Cancel");
		if (clear) {
			CalibrationData calibrationData = CalibrationData.getCalibrationDataForView(currentView);
			calibrationData.clearCalibration();
		}
	}

//	// TODO: REMOVE ME! debug only.
//	private static void lockView() {
//		View[] values = View.values();
//		assert  values != null;
//		showOptionList(
//				"Lock to View",
//				values,
//				LevelOfVulcan.getCurrentView(),
//				null,
//				(v) -> {
//					MultiButton multiButton = new MultiButton(v.toString());
//					multiButton.setRadioButton(true);
//					return multiButton;
//				},
//				c -> {
//					final String textLine1 = c.getTextLine1();
//					assert textLine1 != null;
//					return View.valueOf(textLine1);
//				},
//				v -> OrientationLock.lock.setValue(v.toString().charAt(0))
//			);
//	}
	
	private static void crash() {
		throw new AssertionError("Crash Test");
	}

	/**
	 * Create a command and add it to the end of the Command list.
	 * There's a slight problem with this method. Normally, the command is run before the menu is removed. But if the
	 * command opens a dialog, the method return after the menu is removed. This means it sometimes runs before
	 * the underlying Form's initComponent() method has run, and other times it runs afterwards. This is only a problem
	 * when listeners have been installed in the initComponent() method and de-installed in the deinitialize() method.
	 * Those listeners will get fired from commands that use a dialog, but not from those that don't. Wrapping the 
	 * actionListener inside a callSerially() method doesn't help.
	 *
	 * @param commandList    The List of Commands
	 * @param name           The name of the command
	 * @param fontImageIcon  The Material Icon name, specified by the FontImage class
	 * @param actionListener The actionListener to execute when the command is fired.
	 */
	public static void createCommand(@NotNull List<Command> commandList,
	                                 @NotNull String name,
	                                 char fontImageIcon,
	                                 @NotNull final ActionListener<ActionEvent> actionListener) {
		final Image icon = createClearMaterialIcon(fontImageIcon);
		commandList.add(new Command(name, icon) {
			@Override
			public void actionPerformed(@NotNull final ActionEvent evt) {
				for (Runnable task: prepareList) {
					task.run();
				}
//				showSource(evt);
				actionListener.actionPerformed(evt);
			}
		});
	}

	/**
	 * Create a command and add it to the end of the Command list
	 *
	 * @param commandList The List of Commands
	 * @param name        The name of the command
	 * @param getter      Getter to get the boolean value
	 * @param setter      Setter to set the new boolean value
	 */
	public static void createBooleanCommand(
			@NotNull List<Command> commandList, 
			@NotNull String name, 
			@NotNull BooleanGetter getter,
			@NotNull BooleanSetter setter
	) {
		createBooleanCommand(commandList, name, getter, setter, FontImage.MATERIAL_CHECK_BOX, FontImage.MATERIAL_CHECK_BOX_OUTLINE_BLANK);
	}

	public static void createBooleanCommand(
			@NotNull List<Command> commandList,
			@NotNull String name,
			@NotNull BooleanGetter getter,
			@NotNull BooleanSetter setter,
	    char trueIcon,
	    char falseIcon
	) {
		boolean currentValue = getter.getValue();
		final ToggleIconPair iconPair = new ToggleIconPair(trueIcon, falseIcon);
		final Image icon = iconPair.getMaterialIcon(currentValue);
		commandList.add(new Command(name, icon) {
			@Override
			public void actionPerformed(@NotNull final ActionEvent evt) {
				boolean newValue = !getter.getValue();
				setter.setValue(newValue);
				Command source = (Command) evt.getSource();
				assert source != null;
				source.setIcon(iconPair.getMaterialIcon(newValue));
//				showSource(evt);
			}
		});
	}
	
//	private static void showSource(@NotNull final ActionEvent evt) {
//		String actual = "actual ";
//		Component src = evt.getActualComponent();
//		if (src == null) {
//			src = evt.getComponent();
//			actual = "";
//		}
//		if (src != null) {
//			Log.p("UUID of " + evt.getCommand() + " is " + src.getUIID() + " for" + actual + src.getClass());
////		} else if (src == null) {
//		} else {
//			Log.p("Null source for " + evt.getCommand());
////		} else {
////
////			Class<?> aClass = src.getClass();
////			Log.p("Source of " + aClass);
////			while (aClass != null) {
////				aClass = aClass.getSuperclass();
////				Log.p(" --> " + aClass);
////			}
//		}
//	}

	static void selectColor() {
		Command okayCommand = new Command("OK");
		Command cancelCommand = new Command("Cancel");
		byte initialHue = (byte) Prefs.prefs.get(Prefs.FLUID_HUE, DEFAULT_HUE);
		final HueControl hueControl = makeColorChooser(initialHue);
		Command result = Dialog.show("Choose Colors", hueControl, new Command[] { okayCommand, cancelCommand }, Dialog.TYPE_INFO, null);
//		Log.p("Comparing " + result + " with " + okayCommand + " and " + cancelCommand);
		//noinspection ObjectEquality
		if (result == okayCommand) {
			int hue = hueControl.getChosenHue();
//			Log.p("Setting hue to " + hue); 
			Prefs.prefs.set(Prefs.FLUID_HUE, hue); // should trigger the listener.
		}
//		com.codename1.ui.Toolbar toolbar = Display.getInstance().getCurrent().getToolbar();
//		Log.p("Toolbar UIID: " + toolbar.getUIID());
//		com.codename1.ui.Container parent = toolbar.getParent();
//		while (parent != null) {
//			Log.p("Parent of " + parent.getClass() + " UIID: " + parent.getUIID());
//			parent = parent.getParent();
//		}
	}

	@NotNull
	private static HueControl makeColorChooser(byte initialHue) {
		return new HueControl(initialHue);
	}

	@NotNull
	private static final Style materialIconStyle = UIManager.getInstance().getComponentStyle("Command");
//	static { materialIconStyle.setBgTransparency(0, true); }

	/**
	 * The icon created by this method gets passed to the Command constructor.
	 * @param icon The FontImage constant for the desired icon
	 * @return The icon in an Image.
	 */
	@Nullable
	public static Image createClearMaterialIcon(char icon) {
		if (icon == 0) {
			return null;
		} else {
			return FontImage.createMaterial(icon, materialIconStyle);
		}
	}
	
	private static void testPurchase() {
		TestPurchase[] purchases = TestPurchase.values();
		showOptionList(
				"Test", purchases, TestPurchase.unavailable, null, 
				(e) -> {
					final MultiButton multiButton = new MultiButton(e.toString());
					multiButton.setRadioButton(true);
					return multiButton; 
				},
				(tc) -> TestPurchase.valueOf(tc.getTextLine1()),
				(v) -> Prefs.prefs.set(Prefs.AD_FREE, v == purchase)
		);
	}

	public interface BooleanGetter {
		boolean getValue();
	}

	public interface BooleanSetter {
		void setValue(boolean value);
	}
	
	public static class ToggleIconPair {
		@NotNull private final Image trueIcon;
		@NotNull private final Image falseIcon;
		
		public ToggleIconPair(char trueIconKey, char falseIconKey) {
			//noinspection ConstantConditions
			this(createClearMaterialIcon(trueIconKey), createClearMaterialIcon(falseIconKey));
			assert trueIconKey != 0;
			assert falseIconKey != 0;
		}
		
		public ToggleIconPair(@NotNull Image trueIcon, @NotNull Image falseIcon) {
			this.trueIcon = trueIcon;
			this.falseIcon = falseIcon;
		}
		
		@NotNull
		public Image getMaterialIcon(boolean value) {
			return value? trueIcon : falseIcon;
		}
	}
	
	public interface Factory<T, TC> {
		@NotNull TC buildComponent(@NotNull T value);
	}
	
	public interface Getter<T, TC> {
		@NotNull
		T getValue(@NotNull TC control);
	}
	
	public interface Setter<T> {
		void setValue(@NotNull T value);
	}
	
	enum TestPurchase {
		purchase,
		cancel,
		refund,
		unavailable
//		@NotNull;
//		TestPurchase valueOf(@NotNull String value) {
////			MultiButton btn = new MultiButton("hello");
////			btn.getNameLine1();
////			return valueOf(value);
//			for (TestPurchase tp : values()) {
//				if (value.equals(tp.toString())) {
//					return tp;
//				}
//			}
//			return unavailable;
//		} 
	}
}
