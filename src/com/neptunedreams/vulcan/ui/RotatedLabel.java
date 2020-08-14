package com.neptunedreams.vulcan.ui;

import com.codename1.ui.Graphics;
import com.codename1.ui.Label;
import com.codename1.ui.Transform;
import com.codename1.ui.geom.Dimension;
import com.codename1.ui.plaf.Style;
import com.neptunedreams.util.NotNull;

/**
 * A simplified Label, that draws the text rotated 90 degrees. It may be rotated clockwise or counter clockwise.
 * This is a stripped-down label that only supports text. Icons won't work. 
 * <p/>
 * This only honors these properties of the Style object:  MarginLeft, PaddingLeft, MarginTop, PaddingTop, fgColor,
 * bgColor, font, textDecoration. It only honors bgColor if opaque is true. It's false by default.
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 5/18/16
 * <p>Time: 6:31 PM
 *
 * @author Miguel Mu\u00f1oz
 */
public class RotatedLabel extends Label {
	private static final float PI_OVER_2 = (float) (Math.PI / 2);
	private final boolean clockwise;
	private final boolean inverted;
	private final float angle;
	private final boolean opaque;

	public RotatedLabel(@NotNull String text, boolean clockwise) {
		this(text, clockwise, false);
	}

	public RotatedLabel(@NotNull String text, boolean clockwise, boolean opaque) {
		this(text, clockwise, false, opaque);
	}


	public RotatedLabel(@NotNull String text, boolean clockwise, boolean inverted, boolean opaque) {
		super(text);
		this.clockwise = clockwise;
		this.inverted = inverted;
		this.opaque = opaque;
		if (inverted) {
			angle = (float) Math.PI;
		} else if (clockwise) {
			angle = PI_OVER_2;
		} else {
			angle = -PI_OVER_2;
		}
	}
	
	public RotatedLabel(@NotNull String text) {
		this(text, true, true, false);
	}

	@Override
	@NotNull
	public Dimension getPreferredSize() {
		final Dimension originalSize = super.getPreferredSize();
		if (inverted) {
			return originalSize;
		} else {
			return new Dimension(originalSize.getHeight(), originalSize.getWidth());
		}
	}

	@Override
	public void paint(@NotNull final Graphics g) {
		g.translate(getX(), getY());
		try {
			rotateAndPaint(g);
		} finally {
			g.translate(-getX(), -getY());
		}
	}

	private void rotateAndPaint(@NotNull final Graphics g) {
		// inner method avoids nested try statements
		Transform savedTransform = Transform.makeIdentity();
		g.getTransform(savedTransform);

		g.rotate(angle, getAbsoluteX(), getAbsoluteY());
		Dimension prefs = getPreferredSize();
//		extraDrawing(g, prefs);
		try {
			if (inverted) {
				paintTextInverted(g, prefs);
			} else if (clockwise) {
				paintTextClockwise(g, prefs);
			} else {
				paintTextCounterClockwise(g, prefs);
			}
		} finally {
			g.setTransform(savedTransform);
		}
	}

//	@SuppressWarnings("MagicNumber")
//	void extraDrawing(@NotNull final Graphics g, @NotNull final Dimension prefSize) {
//		Dimension superPref = super.getPreferredSize();
//
//		// Draw a rectangle around the text.
//		g.setColor(0x00ffff);
////		g.drawRect(getX(), (getY() + (superPref.getWidth() / 2)) - (superPref.getHeight() / 2), superPref.getWidth()-1, superPref.getHeight());
//		g.drawRect(0, - superPref.getHeight(), superPref.getWidth(), superPref.getHeight());
//
//		g.setColor(0xff0000); // red
//		final int k = prefSize.getWidth() * 5;
//		g.drawLine(-k, -k, k, k);
//		g.setColor(0x00ff00); // green
//		g.drawLine(-k, k, k, -k);
//		g.setColor(0x0000ff); // blue
//		g.drawLine(k, -k, k, k);
//		g.setColor(0xff8000); // orange
//		g.drawLine(-k, -2*k, k, 2*k);
//		int r = getHeight();
//		int d = r * 2;
//		g.setColor(0xff0000);
//		g.drawArc(-r, -r, d, d, 0, 360);
//		g.setColor(0xff8000);
//		g.drawArc(-2*r, -2*r, 2*d, 2*d, 0, 360);
//		g.setColor(0x80FF00);
//		g.drawArc(-3*r, -3*r, 3*d, 3*d, 0, 360);
//		g.setColor(0xFF00);
//		g.drawArc(-4*r, -4*r, 4*d, 4*d, 0, 360);
//	}

	private void paintTextCounterClockwise(@NotNull Graphics g, @NotNull final Dimension pref) {
		g.translate(-pref.getHeight(), 0);
		paintRotatedText(g, pref);
		g.translate(pref.getHeight(), 0);
	}

	private void paintTextClockwise (@NotNull Graphics g, @NotNull final Dimension pref){
		g.translate(0, -pref.getWidth());
		paintRotatedText(g, pref);
		g.translate(0, pref.getWidth());
	}

	private void paintTextInverted(@NotNull Graphics g, @NotNull final Dimension pref) {
		g.translate(-pref.getWidth(), -pref.getHeight());
		paintRotatedText(g, pref);
		g.translate(pref.getWidth(), pref.getHeight());
	}

	/**
	 * I don't call super.paint() because that does something weird that prevents it from working. That's why icons 
	 * won't work.
	 * @param g Graphics
	 * @param pref result of getPreferredSize();
	 */
	private void paintRotatedText(@NotNull final Graphics g, @NotNull final Dimension pref) {
		Style style = getStyle();
		if (opaque) {
			int bgColor = style.getBgColor();
			g.setColor(bgColor);
			g.fillRect(0, 0, pref.getHeight(), pref.getWidth());
		}

		boolean rtl = isRTL();
		int leftIndent = style.getMarginLeft(rtl) + style.getPaddingLeft(rtl);
		int topIndent = (style.getMarginTop() + style.getPaddingTop());
		g.translate(leftIndent, topIndent);

		final int fgColor = style.getFgColor();
		g.setColor(fgColor);
		g.setFont(style.getFont());
		int decoration = style.getTextDecoration();
		final String text = getText();
		assert text != null;
		g.drawString(text, 0, 0, decoration);

		g.translate(-leftIndent, -topIndent);
	}
}
