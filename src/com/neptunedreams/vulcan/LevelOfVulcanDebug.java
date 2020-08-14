package com.neptunedreams.vulcan;

import java.io.IOException;
import com.codename1.io.Log;
import com.codename1.sensors.SensorsManager;
import com.codename1.ui.Button;
import com.codename1.ui.Command;
import com.codename1.ui.Display;
import com.codename1.ui.Form;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.plaf.UIManager;
import com.codename1.ui.util.Resources;
import com.codename1.util.MathUtil;
import com.neptunedreams.vulcan.ui.GridHelper;
import com.neptunedreams.vulcan.ui.ScreenDump;
import com.neptunedreams.util.NotNull;

import static com.codename1.sensors.SensorType3D.*;

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
 * // todo: test floating point rounding!
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 4/4/16
 * <p>Time: 9:19 PM
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings({"HardCodedStringLiteral", "unused"})
public class LevelOfVulcanDebug {
	private static final int RED = 0xff8080;
	private static final int BLUE = 0x00ffff;
	private static final float MULTIPLIER = 0.8f;
	private static final float A90f = 90.0f;
	private static final float A180f = 180F;
	private Form current;
	@NotNull
	private final DigitalFilter xFilter = new DigitalFilter(MULTIPLIER);
	@NotNull
	private final DigitalFilter yFilter = new DigitalFilter(MULTIPLIER);

	private SensorPanel acceleratorPanel;
	private SensorPanel rawAccPanel;
	private ScatterView scatterView;

	@SuppressWarnings("unused")
	public void init(Object context) {

		scatterView = new ScatterView(RED, BLUE);
		try {
			Resources theme = Resources.openLayered("/theme");
			
			UIManager.getInstance().setThemeProps(theme.getTheme(theme.getThemeResourceNames()[0]));
			
		} catch (IOException e) {
			e.printStackTrace();
		}

		SensorsManager acceleratorSensor = SensorsManager.getSenorsManager(accelerometer);
		acceleratorPanel = new SensorPanel("Accelerator", 2) {
			@Override
			protected float getSideToSideValue(final float x, final float y, final float z) {
				float angleX = angle(x, z);
				return xFilter.addNext(angleX);
			}

			@Override
			protected float getTopToBottomValue(final float x, final float y, final float z) {
				float angleY = angle(y, z);
				return yFilter.addNext(angleY);
			}

			@Override
			protected float getUpAndDownValue(final float x, final float y, final float z) {
				scatterView.addPoint(xFilter.getRawValue(), yFilter.getRawValue(), RED);
				scatterView.addPoint(xFilter.getValue(), yFilter.getValue(), BLUE);
				return super.getUpAndDownValue(x, y, z);
			}
		};
		acceleratorPanel.installListener(acceleratorSensor);

		rawAccPanel = new SensorPanel("Raw Acceleration", 4) {
			@Override
			protected float getSideToSideValue(final float x, final float y, final float z) {
				return angle(x, z);
			}

			@Override
			protected float getTopToBottomValue(final float x, final float y, final float z) {
				return angle(y, z);
			}
		};
		rawAccPanel.installListener(acceleratorSensor);

//		gyroPanel = new SensorPanel("Gyro", 3);
//		SensorsManager gyroSensor = SensorsManager.getSenorsManager(SensorsManager.TYPE_GYROSCOPE);
//		gyroPanel.installListener(gyroSensor);
		
//		magnetoPanel = new SensorPanel("Magneto", 3);
//		SensorsManager magnetoSensor = SensorsManager.getSenorsManager(SensorsManager.TYPE_MAGNETIC);
//		magnetoPanel.installListener(magnetoSensor);
	}

//	private String generateLine(final long seconds, final float x, final float y, final float z) {return "At " + seconds + ": (" + round(x, rnd) + ", " + round(y, rnd) + ", " + round(z, rnd) + ") [" + round(-4.3F) + "]\n";}

	/**
	 * Calculate an angle, in degrees, from an axis from two perpendicular axes values.
	 * More formally, if a is the value along the A axis and b is the value along the B axis, this
	 * calculates the angle, in degrees, between the A axis and the line from the origin to the point (a, b).
	 * <p/>
	 * This calculates arcTan(a/b), then scales the result to degrees
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

	public static boolean isPortrait() {
		// resolutions: 888 x 540, 897 x 418, 540 x 766
		final Display display = Display.getInstance();
//		System.out.println("Display: " + display.getDisplayWidth() + ", " + display.getDisplayHeight());
		return display.getDisplayWidth() < display.getDisplayHeight();
	}

	public void start() {
		assert acceleratorPanel != null;
		assert rawAccPanel != null;
		acceleratorPanel.start();
		rawAccPanel.start();

		if (current != null) {
			current.show();
			return;
		}

		final Display display = Display.getInstance();
		if (display.canForceOrientation()) {
			display.lockOrientation(true);
			Log.p("Locking to portrait");
		} else {
			Log.p("Orientation Lock unavailable.");
		}


		Form hi = new Form("Vulcan\u2019s Level");
		final GridHelper helper = new GridHelper(hi);
		int y = -1;

		helper.add(acceleratorPanel, 0, ++y, null, null, 2, null);
		helper.add(rawAccPanel, 0, ++y, null, null, 2, null);
		assert scatterView != null;
		Button dumpButton = makeDumpButton();

		// It's always portrait.
//		if (isPortrait()) {
			helper.add(scatterView, 0, ++y);
			helper.add(dumpButton, 0, ++y);
//		} else {
//			helper.add(scatterView, 2, 0);
//			helper.add(dumpButton, 2, 1);
//		}

//		Form l10N = new Form("L10N", new TableLayout(16, 2));
//		L10NManager l10n = L10NManager.getInstance();
//		l10N.add("format(double)").add(l10n.format(11.11)).
//				add("format(int)").add(l10n.format(33)).
//				add("formatCurrency").add(l10n.formatCurrency(53.267)).
//				add("formatDateLongStyle").add(l10n.formatDateLongStyle(new Date())).
//				add("formatDateShortStyle").add(l10n.formatDateShortStyle(new Date())).
//				add("formatDateTime").add(l10n.formatDateTime(new Date())).
//				add("formatDateTimeMedium").add(l10n.formatDateTimeMedium(new Date())).
//				add("formatDateTimeShort").add(l10n.formatDateTimeShort(new Date())).
//				add("getCurrencySymbol").add(l10n.getCurrencySymbol()).
//				add("getLanguage").add(l10n.getLanguage()).
//				add("getLocale").add(l10n.getLocale()).
//				add("isRTLLocale").add("" + l10n.isRTLLocale()).
//				add("parseCurrency").add(l10n.formatCurrency(l10n.parseCurrency("33.77$"))).
//				add("parseDouble").add(l10n.format(l10n.parseDouble("34.35"))).
//				add("parseInt").add(l10n.format(l10n.parseInt("56"))).
//				add("parseLong").add("" + l10n.parseLong("4444444"));
//		
////		NavigationCommand backCommand = new NavigationCommand("Back");
//		backCommand.setNextForm(hi);
//		l10N.setBackCommand(backCommand);
//		l10N.show();

//		helper.add(gyroPanel, 0, ++y, null, null, 2, null);
//		helper.add(magnetoPanel, 0, ++y, null, null, 2, null);

//		current = l10N;
		
		hi.show();
		current = hi;
	}

	@NotNull
	private Button makeDumpButton() {
		Command dumpCommand = new Command("Snapshot") {
			@Override
			public void actionPerformed(final ActionEvent evt) {
				assert scatterView != null;
				ScreenDump.snapComponent(scatterView, scatterView.getRangeDescription(BLUE));
			}
		};
		
		return new Button(dumpCommand);
	}

	public void stop() {
		assert acceleratorPanel != null;
		assert rawAccPanel != null;
		acceleratorPanel.stop();
		rawAccPanel.stop();
//		gyroPanel.stop();
//		magnetoPanel.stop();
	}

	public void destroy() {
	}
}
