package com.neptunedreams.vulcan.settings;

import com.codename1.ui.Graphics;
import com.codename1.ui.Image;
import com.codename1.ui.Label;
import com.codename1.ui.events.ActionListener;
import com.codename1.ui.plaf.Style;
import com.codename1.ui.util.Resources;
import com.codename1.util.MathUtil;
import com.neptunedreams.vulcan.BubbleView;
import com.neptunedreams.vulcan.app.LevelOfVulcan;
import com.neptunedreams.util.NotNull;

/**
 * Thumb Original: http://esciencelog.com/data_images/wlls/47/331868-sphere.jpg
 * Alternate: http://www.clipartpanda.com/clipart_images/sphere-clipart-sphere-19902831
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 5/12/16
 * <p>Time: 10:38 PM
 *
 * @author Miguel Mu\u00f1oz
 */
public class HueControl extends Label {
	private static final int HUE_MAX = 255;
	private static final int WHITE = 0xFFFFFF;
	private static final int CCL_MAX = 360;
	private static final double HALF = 0.5;
	private static final double RADIANS = Math.PI / 180.0;
	private static final int FF = 0xFF;
	private int size;
	private int half;
	private int iconRadius;
	private Image imageBg;
	private Image thumb;
	private int halfThumb;
	private byte chosenHue;
	private int chosenColor;
	private int thumbX;
	private int thumbY;
	@SuppressWarnings("NegativelyNamedBooleanVariable")
	private boolean notYetSized = true;

	private int marginX; // for drawing
	private int marginY; // for drawing

	public HueControl(byte hue) {
		super();
		this.chosenHue = hue;
	}
	
	private void calculateSize() {

		if (notYetSized) {
			this.size = Math.min(getWidth(), getHeight());
			chosenColor = BubbleView.makeColor(chosenHue);
			final Image icon = makeHueIcon(size);
			setIcon(icon);
			assert icon != null;
			imageBg = icon;
			half = size/2;
			addPointerDraggedListener(makePointerDraggedListener());
			final Resources theme = LevelOfVulcan.getTheme();
			final Image image = theme.getImage("Sphere.png");
			assert image != null;
			thumb = image;
			halfThumb = thumb.getWidth() / 2;
			boolean rtl = isRTL();
			Style style = getUnselectedStyle();
			marginX = style.getPaddingLeft(rtl) + style.getMarginLeft(rtl);
			marginY = style.getPaddingTop() + style.getMarginTop();
			calculateThumb(angleForHue(chosenHue));
			notYetSized = false;
		}
//		testHueConversion();
	}

	private Image makeHueIcon(int size) {
		Image image = Image.createImage(size, size, 0);
		int colorDiameter = (size * 8) / 10;

		int deltaX = (size - colorDiameter) / 2;
		int deltaY = (size - colorDiameter) / 2;
		Graphics g = image.getGraphics();
		g.setAntiAliased(true);
		g.setColor(0);
		g.fillRect(0, 0, size, size);
		int lastAngle = 0;
		for (int h = 1; h <= HUE_MAX; ++h) {
			g.setColor(BubbleView.getDomeColor(BubbleView.makeColor((byte) h)));
			int angle = angleForHue(h);

			// I add one to the arcAngle to prevent gray spots in the final ring.
			g.fillArc(deltaX, deltaY, colorDiameter, colorDiameter, lastAngle, (1 + angle) - lastAngle);
			lastAngle = angle;
		}
		int stroke = deltaX/4;
		int dblStroke = stroke * 2;
		g.setColor(0);
		g.fillArc(deltaX + stroke, deltaY + stroke, colorDiameter-dblStroke, colorDiameter-dblStroke, 0, CCL_MAX);
		iconRadius = colorDiameter / 2;
		
		Image mask = Image.createImage(size, size, WHITE);
		g = mask.getGraphics();
		g.setAntiAliased(true);
		g.setColor(FF);
		g.fillRect(0, 0, size, size);
		int maskDiameter = (size * 7) / 10;
		deltaX = (size - maskDiameter) / 2;
		deltaY = (size - maskDiameter) / 2;
		g.setColor(0);
		g.fillArc(deltaX, deltaY, maskDiameter, maskDiameter, 0, CCL_MAX);
		image = image.applyMask(mask.createMask());
		
		return image;
	}
	
	private static int angleForHue(int hue) {
		return (hue * CCL_MAX) / HUE_MAX;
	}
	
	private static byte hueForAngle(int angle) {
		// I don't know why I need the +1, but when it's there, this method becomes the inverse of angleForHue().
		// Without it, it's often the inverse, but it fails for certain numbers. I believe this works 
		// when CCL_MAX > HUE_MAX. 
		return (byte)(((angle + 1) * HUE_MAX) / CCL_MAX);
	}

	/**
	 * Remember that a hue is not a color! You must call BubbleView.makeColor(hue) to turn this into an RGB color.
	 * @param x x
	 * @param y y
	 * @return A hue for x and y
	 */
	private int getHue(int x, int y) {
		int xp = x - getAbsoluteX() - marginX;
		int yp = y - getAbsoluteY() - marginY;
		int dX = half - xp;
		int dY = half - yp;
		return getTheta(dX, dY);
	}

	private static int getTheta(final int dX, final int dY) {
		double dTheta = MathUtil.atan2(dY, -dX) / RADIANS;
		if (dTheta < 0) {
			dTheta += CCL_MAX;
		}
		return (int) (dTheta + HALF);
	}

	private ActionListener makePointerDraggedListener() {
		return evt -> {
			int theta = getHue(evt.getX(), evt.getY());
			chosenHue = hueForAngle(theta);
			chosenColor = BubbleView.makeColor(chosenHue);
			repaint();
			calculateThumb(theta);
		};
	}

	private void calculateThumb(final int theta) {
		final int xx =  (int) ((iconRadius * Math.cos(theta * RADIANS)) + HALF);
		final int yy = -(int) ((iconRadius * Math.sin(theta * RADIANS)) + HALF);
		thumbX = ((xx - halfThumb) + half);
		thumbY = ((yy - halfThumb) + half);
	}

	@Override
	public void paint(@NotNull final Graphics g) {
		calculateSize();  // This used to be done in the constructor, but now its done here because 
											// the constructor had the size wrong
		Style s = getUnselectedStyle();
		boolean rtl = isRTL();
		final int x = getX() + s.getPaddingLeft(rtl) + s.getMarginLeft(rtl);
		final int y = getY() + s.getPaddingTop() + s.getMarginTop();
		g.translate(x, y);
		g.setColor(BubbleView.getDomeColor(chosenColor));
		g.fillRect(0, 0, size, size);
		assert imageBg != null;
		g.drawImage(imageBg, 0, 0);
		g.setColor(WHITE);
		g.drawRect(0, 0, size, size);

		assert thumb != null;
		g.drawImage(thumb, thumbX, thumbY);
		g.translate(-x, -y);
	}

//	private void testHueConversion() {
//		int badCount = 0;
//		for (int ii=0; ii<=HUE_MAX; ++ii) {
//			int angle = angleForHue(ii);
//			int iHue = hueForAngle(angle);
//			if (iHue != ii) {
//				badCount++;
//				Log.p("Bad angle: " + ii + " -> " + angle + " -> " + iHue);
//			}
//		}
//		Log.p("Faults found: " + badCount);
//	}
	
	public int getChosenHue() { return chosenHue; }
}
