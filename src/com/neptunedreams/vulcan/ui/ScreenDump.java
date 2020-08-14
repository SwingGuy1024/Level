package com.neptunedreams.vulcan.ui;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.StringTokenizer;
import com.codename1.io.FileSystemStorage;
import com.codename1.io.Log;
import com.codename1.l10n.L10NManager;
import com.codename1.messaging.Message;
import com.codename1.ui.Component;
import com.codename1.ui.Dialog;
import com.codename1.ui.Graphics;
import com.codename1.ui.Image;
import com.codename1.ui.util.ImageIO;
import com.codename1.util.StringUtil;
import com.neptunedreams.util.NotNull;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 4/12/16
 * <p>Time: 9:59 AM
 *
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings({"UtilityClassCanBeEnum", "HardCodedStringLiteral"})
public final class ScreenDump {

	private static final String PNG_MIME_TYPE = "image/png";

	private ScreenDump() {}

	public static void snapComponent(@NotNull Component c, @NotNull String notes) {
		Image screenShot = Image.createImage(c.getWidth(), c.getHeight());
		final Graphics graphics = screenShot.getGraphics();
		// I'm going to assume that the component's paint method translates before painting, so I'll invert that.
		graphics.translate(-c.getAbsoluteX(), -c.getAbsoluteY());
		// I don't know if I'm supposed to use c.getX() or getAbsoluteX(), but they both have been returning the same values
		// for now.
		c.paintComponent(graphics, true);
		graphics.translate(c.getX(), c.getY());
//		Dialog.show("Debug", "Snap at " + c.getX() + ", " + c.getY(), Dialog.TYPE_INFO, null, "OK", null);

		try {
			saveSnapshot(screenShot, notes);
		} catch (IOException err) {
			String message = "Snapshot failed: " + err.getMessage();
			Log.e(err);
			Dialog.show("Snapshot Error", message, Dialog.TYPE_ERROR, null, "OK", null);
		}
	}

	private static void saveSnapshot(@NotNull final Image screenShot, String notes) throws IOException {
		final FileSystemStorage fileSystemStorage = FileSystemStorage.getInstance();

		//noinspection StringConcatenationMissingWhitespace
		String appHomePath = fileSystemStorage.getAppHomePath(); // NON-NLS
		if (!fileSystemStorage.exists(appHomePath)) {
			makeDirectory(appHomePath);
			if (!fileSystemStorage.exists(appHomePath)) {
				throw new IOException("Unable to create directory: " + appHomePath);
			}
		}

		String time = generateTimeString();
		String baseName = "snapshot-" + time + ".png"; // NON-NLS
		String imageFile = appHomePath + baseName;
		try (OutputStream os = FileSystemStorage.getInstance().openOutputStream(imageFile)) {
			ImageIO.getImageIO().save(screenShot, os, ImageIO.FORMAT_PNG, 1);
		}
		Message emailMessage = new Message(imageFile + "\n\n" + notes);
		emailMessage.setAttachment(imageFile);
		emailMessage.setMimeType(PNG_MIME_TYPE);
		String[] recipients = {"SwingGuy1024@yahoo.com"};
		try {
			Message.sendMessage(recipients, "Snap", emailMessage);
//			long size = fileSystemStorage.getLength(imageFile);
//			Dialog.show("Snapshot File", imageFile + "  " + size + " bytes", Dialog.TYPE_INFO, null, "OK", null);
		} finally {
			fileSystemStorage.deleteRetry(imageFile, 2);
		}
	}

	private static void makeDirectory(String directory) throws IOException {
		FileSystemStorage fileSystemStorage = FileSystemStorage.getInstance();
		StringTokenizer tokenizer = new StringTokenizer(directory, "/");
		StringBuilder currentPath = new StringBuilder("/");
		while (tokenizer.hasMoreElements()) {
			String token = tokenizer.nextToken();
			//noinspection MagicCharacter
			currentPath.append(token).append('/');
			final String currentDir = currentPath.toString();
			if (!fileSystemStorage.exists(currentDir)) {
				fileSystemStorage.mkdir(currentDir);
				if (!fileSystemStorage.exists(currentDir)) {
					throw new IOException("Unable to create " + currentDir);
				}
			}
		}
	}

	private static String generateTimeString() {
		long time = System.currentTimeMillis();
		Date date = new Date(time);
		final L10NManager l10NManager = L10NManager.getInstance();
		String dateTime = l10NManager.formatDateTime(date);
		String secondTxt = ":";
		//noinspection MagicNumber
		long millis = time % 60000;
		long seconds = millis / 1000;
		if (seconds < 10) {
			secondTxt += l10NManager.format(0);
		}
		secondTxt += l10NManager.format(seconds);
		int lastDigit = dateTime.length() - 1;
		while (!Character.isDigit(dateTime.charAt(lastDigit))) {
			lastDigit--;
		}
		int sSpot = lastDigit + 1;
		//noinspection StringConcatenationMissingWhitespace
		String dateString = dateTime.substring(0, sSpot) + secondTxt + dateTime.substring(sSpot);

		// I need to strip out these three characters to create a String that can be used as a valid file name.
		// (I may be wrong about commas.)
		dateString = StringUtil.replaceAll(dateString, " ", "-"); // strip out spaces.
		dateString = StringUtil.replaceAll(dateString, ",", "-"); // strip out commas.
		dateString = StringUtil.replaceAll(dateString, ":", "."); // strip out colons.
		return dateString;
	}
}
