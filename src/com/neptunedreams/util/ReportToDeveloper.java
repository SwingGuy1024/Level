package com.neptunedreams.util;

import java.io.IOException;
import java.util.TimerTask;
import com.codename1.io.Log;
import com.codename1.io.Preferences;
import com.codename1.io.Storage;
import com.codename1.io.Util;
import com.codename1.messaging.Message;
import com.codename1.system.CrashReport;
import com.codename1.ui.Display;
import com.neptunedreams.vulcan.app.LevelOfVulcan;

import static com.neptunedreams.vulcan.app.LevelOfVulcan.format;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 6/17/16
 * <p>Time: 2:05 AM
 *
 * @author Miguel Mu\u00f1oz
 */
public final class ReportToDeveloper implements CrashReport {
	private static final int TEN_SECONDS = 60000;
//	private boolean promptUser = false;
//	@NotNull
//	private static final String errorText = "The application encountered an error, do you wish to email a report?";
//	@NotNull
//	private static final String sendButtonText = "Send";
//	@NotNull
//	private static final String dontSendButtonText = "Don't Send";
//	@NotNull
//	private static final String checkboxText = "Don't show this dialog again";
	@NotNull
	public static final String pleaseKeepAbove = "\nVulcan\u2019s Level has crashed. We apologize for the inconvenience. You may provide (optional) additional feedback above this line. Then please hit the send button.\n\nCrash Details: \n";
	@NotNull
	public static final String crashSubject = "LevelOfVulcan Crash Report";
	@NotNull
	public static final String pleaseKeepBelow = "\n (End of crash details.)\n\nVulcan\u2019s Level has crashed. We apologize for the inconvenience. You may provide (optional) additional feedback below this line. Then please hit the send button. Thank you from NeptuneDreams.com\n";
	public static final String CN_1_PENDING_CRASH = "$CN1_pendingCrash";
	public static final String CN_1_CRASH_BLOCKED = "$CN1_crashBlocked";

//	/**
//	 * The text for the user prompt dialog
//	 *
//	 * @return the errorText
//	 */
//	public static String getErrorText() {
//		return errorText;
//	}
//
//	/**
//	 * The text for the user prompt dialog
//	 *
//	 * @param aErrorText the errorText to set
//	 */
//	public static void setErrorText(String aErrorText) {
//		errorText = aErrorText;
//	}
//
//	/**
//	 * The text for the user prompt dialog
//	 *
//	 * @return the sendButtonText
//	 */
//	public static String getSendButtonText() {
//		return sendButtonText;
//	}
//
//	/**
//	 * The text for the user prompt dialog
//	 *
//	 * @param aSendButtonText the sendButtonText to set
//	 */
//	public static void setSendButtonText(String aSendButtonText) {
//		sendButtonText = aSendButtonText;
//	}
//
//	/**
//	 * The text for the user prompt dialog
//	 *
//	 * @return the dontSendButtonText
//	 */
//	public static String getDontSendButtonText() {
//		return dontSendButtonText;
//	}
//
//	/**
//	 * The text for the user prompt dialog
//	 *
//	 * @param aDontSendButtonText the dontSendButtonText to set
//	 */
//	public static void setDontSendButtonText(String aDontSendButtonText) {
//		dontSendButtonText = aDontSendButtonText;
//	}
//
//	/**
//	 * The text for the user prompt dialog
//	 *
//	 * @return the checkboxText
//	 */
//	public static String getCheckboxText() {
//		return checkboxText;
//	}
//
//	/**
//	 * The text for the user prompt dialog
//	 *
//	 * @param aCheckboxText the checkboxText to set
//	 */
//	public static void setCheckboxText(String aCheckboxText) {
//		checkboxText = aCheckboxText;
//	}

	private ReportToDeveloper() {}

	/**
	 * Installs a crash reporter within the system
	 *
	 * @param promptUser indicates whether the user should be prompted on crash reporting
	 * @param frequency  the frequency with which we send the log to the server in debug mode in minutes
	 *                   frequency must be at least 1. Any lower level automatically disables this feature
	 */
	public static void init(@SuppressWarnings("UnusedParameters") boolean promptUser, int frequency) {
		if (Preferences.get(CN_1_CRASH_BLOCKED, false) || (Log.getReportingLevel() == Log.REPORTING_NONE)) {
			return;
		}
		if (Preferences.get(CN_1_PENDING_CRASH, false)) {
			// we must have crashed during a report, send it.
			Log.sendLog(); // todo: rewrite this to send the report properly.
			final String report = readLogText();
			Display.getInstance().callSerially(() -> sendEmail("Recovery Error Report", report));
			Preferences.set(CN_1_PENDING_CRASH, false);
		}
		if ((Log.getReportingLevel() == Log.REPORTING_DEBUG) && (frequency > 0)) {
			java.util.Timer t = new java.util.Timer();
			t.schedule(new TimerTask() {
				@Override
				public void run() {
					if (!Display.getInstance().isEdt()) {
						Display.getInstance().callSerially(this);
						return;
					}
					Log.sendLog();
				}
			}, frequency * TEN_SECONDS, frequency * TEN_SECONDS);
		}
		ReportToDeveloper d = new ReportToDeveloper();
//		d.promptUser = promptUser && Preferences.get("$CN1_prompt", true);
		Display.getInstance().setCrashReporter(d);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("StringConcatenation")
	@Override
	public void exception(@NotNull Throwable th) {
		Preferences.set(CN_1_PENDING_CRASH, true);
//		if (promptUser) {
//			Dialog error = new Dialog("Error");
//			error.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
//			TextArea txt = new TextArea(errorText);
//			txt.setEditable(false);
//			txt.setUIID("DialogBody");
//			error.addComponent(txt);
//			CheckBox cb = new CheckBox(checkboxText);
//			cb.setUIID("DialogBody");
//			error.addComponent(cb);
//			Container grid = new Container(new GridLayout(1, 2));
//			error.addComponent(grid);
//			Command ok = new Command(sendButtonText);
//			Command dont = new Command(dontSendButtonText);
//			Button send = new Button(ok);
//			Button dontSend = new Button(dont);
//			grid.addComponent(send);
//			grid.addComponent(dontSend);
//			Command result = error.showPacked(BorderLayout.CENTER, true);
//			//noinspection ObjectEquality
//			if (result == dont) {
//				if (cb.isSelected()) {
//					Preferences.set("$CN1_crashBlocked", true);
//				}
//				Preferences.set("$CN1_pendingCrash", false);
//				return;
//			} else {
//				if (cb.isSelected()) {
//					Preferences.set("$CN1_prompt", false);
//				}
//			}
//		}
		final String logText = readLogText();

		final Display display = Display.getInstance();
		final String deviceInfo = display.getProperty("User-Agent", "unknown");
		final String displayInfo = getDisplayInfo(display);
		final String appVersion = "\nVersion " + display.getProperty("AppVersion", "unknown");
		final Thread currentThread = Thread.currentThread();
		final String isEdt = "\nThread " + currentThread.getName() + " (edt = " + display.isEdt() + ") of "
				+ currentThread.getName() + " of " + currentThread.getClass();

		final String message = pleaseKeepAbove + deviceInfo + '\n' + displayInfo + appVersion + isEdt + '\n' + logText + pleaseKeepBelow;
		Log.p(message);
		Runnable runner = () -> sendEmail(crashSubject, message);
		if (display.isEdt()) {
			runner.run();
		} else {
			display.callSerially(runner);
		}
//		Log.sendLog(); // requires more expensive plan.
		Preferences.set(CN_1_PENDING_CRASH, false);
//		display.exitApplication();
	}
	
	@SuppressWarnings("StringConcatenation")
	private String getDisplayInfo(@NotNull Display display) {
		final float METER = 1000.0f;
		float pixelsPerMeter = display.convertToPixels(METER);
		String ppm = "Pixels Per Meter: " + pixelsPerMeter;
		final float cmPerInch = 2.54f; // exact.
		final float toCm = 100.0f; // to centimeters
		String ppi = "\nPixels Per inch: " + format((pixelsPerMeter * cmPerInch) / toCm, 1);
		int width = display.getDisplayWidth();
		int height = display.getDisplayHeight();
		String dim = "\nDisplay " + width + " x " + height + " pixels";
		String size = "\napprox  " + format((width * toCm) / pixelsPerMeter, 1) + " x " 
				+ format((height * toCm) / pixelsPerMeter, 1) + " cm";
		String sizeIn = "\napprox  " + format(((width * toCm) / cmPerInch) / pixelsPerMeter, 1) + " x "
				+ format(((height * toCm) / cmPerInch) / pixelsPerMeter, 1) + " inches";
		return ppm + ppi + dim + size + sizeIn;
	}

	@NotNull
	public static String readLogText() {
		String logText;// = getMyLogContent();
		//noinspection OverlyBroadCatchBlock
		try {
			byte[] read = Util.readInputStream(Storage.getInstance().createInputStream("CN1Log__$"));
			assert read != null;
			logText = new String(read, 0, read.length, "UTF-8");
		} catch (IOException ignored) {
			logText = "Unable to read log file";
		}
		return logText;
	}

	public static void sendEmail(final String subject, String content) {
		Message message = new Message(content);

		// This is necessary to prevent us from stepping on the other dialog that EDT presents. Without this, the 
		// software hangs after the second crash report.
//		Display.getInstance().callSerially(() -> Message.sendMessage(new String[]{"SwingGuyDomain@yahoo.com"}, "LevelOfVulcan Crash Report", message));
		// The above line is commented out, because it's not necessary once the other dialog is suppressed. This is done by
		// setting a do-nothing error handler. This problem might also get fixed by the new cancel-dialog code in 
		// LevelOfVulcan.stop(), but I haven't tested that.
		
		String email;
		if (LevelOfVulcan.debug) {
			email = "SwingGuyDomain@yahoo.com";
		} else {
			email = "vulcan@neptunedreams.com";
		}
		final String[] recipients = { email };
		Message.sendMessage(recipients, subject, message);
	}
}
