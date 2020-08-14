package com.neptunedreams.vulcan;

import java.util.Hashtable;
import com.codename1.ui.Component;
import com.codename1.ui.Container;
import com.codename1.ui.Graphics;
import com.codename1.ui.Image;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.events.ActionListener;
import com.codename1.ui.geom.Dimension;
import com.neptunedreams.Assert;
import com.neptunedreams.vulcan.math.Statistics;
import com.neptunedreams.util.Failure;
import com.neptunedreams.util.NotNull;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 4/10/16
 * <p>Time: 4:01 PM
 *
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings("WeakerAccess")
public class ScatterView extends Container {

	private static final int SIDE = 350;
	private static final int origin = SIDE / 2;
	private static final int P_MASK = 0xFF;
	private static final int WHITE = 0xffffff;
	private static final int CH = 20; // Cross Hair Range
	private static final int BG_COLOR = 0;
	private static final int C_360 = 360;
	private static final int ShRed = 16;
	private static final int ShGrn = 8;
	private final float range = 2.0f; // full range goes from -range to +range in each direction.
	private float scale = origin / range;
	@NotNull
	private Image image;
	//	private final Transform transform = Transform.makeIdentity();
	private final Hashtable<Integer, Statistics> statMap = new Hashtable<Integer, Statistics>();
//	@NotNull
//	private Hashtable<Integer, Integer> colorMap = new Hashtable<Integer, Integer>();
//	private final ImageViewer imageViewer;

	public ScatterView(@NotNull int... colors) {
//		super(new BorderLayout());
		super();
		setWidth(SIDE);
		setHeight(SIDE);
		image = createImage();
//		imageViewer = new ImageViewer(image);
//		add(imageViewer, BorderLayout.CENTER);
		for (int clr : colors) {
//			final int dark = darken(clr);
			statMap.put(clr, new Statistics());
//			colorMap.put(clr, dark);
		}

//		Button resetButton = new Button("Reset");
//		resetButton.addActionListener(new ActionListener() {
//			public void actionPerformed(final ActionEvent evt) {
//				reset();
//			}
//		});
//		add(resetButton, BorderLayout.SOUTH);

		addPointerReleasedListener(new ActionListener() {
			public void actionPerformed(final ActionEvent evt) {
				reset();
			}
		});
	}

	@Override
	public void addComponent(final Component cmp) {
		throw new Failure("Don't add components to a ScatterView");
	}

	@Override
	public void addComponent(final Object constraints, final Component cmp) {
		throw new Failure("Don't add components to a ScatterView");
	}

	private int darken(int color) {
		int red = ((color >> ShRed) & P_MASK) / 2;
		int grn = ((color >> ShGrn) & P_MASK) / 2;
		int blu = (color & P_MASK) / 2;
		return (red << ShRed) | (grn << ShGrn) | blu;
	}

	@NotNull
	private Image createImage() {
		Image img = Image.createImage(SIDE, SIDE, BG_COLOR);
		final Graphics graphics = img.getGraphics();
		// draw axes and grid.
		graphics.setColor(BG_COLOR);
		graphics.fillRect(0, 0, SIDE, SIDE);
		final int r = scale(range);
		graphics.setColor((0x3f3f3f));
		int max = (int) (scale * 10);
		graphics.translate(origin, origin);
		for (int x = -max; x < max; ++x) {
			int v = scale(x / 10.0f);
			graphics.drawLine(v, r, v, -r);
			graphics.drawLine(r, v, -r, v);
		}
//		imageViewer.setImage(img);
		graphics.setColor(0x7f7f7f);
		graphics.drawLine(0, r, 0, -r);
		graphics.drawLine(scale(1.0f), r, scale(1.0f), -r);
		graphics.drawLine(scale(-1.0f), r, scale(-1.0f), -r);
		graphics.drawLine(r, 0, -r, 0);
		graphics.drawLine(r, scale(1.0f), -r, scale(1.0f));
		graphics.drawLine(r, scale(-1.0f), -r, scale(-1.0f));
		graphics.translate(-origin, -origin);
		return img;
	}

	private void reset() {
		for (Integer color : statMap.keySet()) {
			statMap.put(color, new Statistics());
			image = createImage();
		}
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(SIDE, SIDE);
	}

	public void addPoint(float x, float y, int color) {
		final Statistics stats = statMap.get(color);
		assert stats != null;
		stats.addPoint(x, y);
		if (inBounds(x)) {
			int ix = scale(x);
//			int ix = (int) xScale;
			if (inBounds(y)) {
				int iy = -scale(y); // negate the y value to account for the inversion of the y axis in these drawing tools.
//				int iy = (int)yScale;
//				System.out.println("X: " + x + " -> " + ix);
//				System.out.println("Y: " + y + " -> " + iy);
				Graphics graphics = image.getGraphics();
//				graphics.getTransform(transform);
				graphics.translate(origin, origin);
				graphics.setColor(darken(color));
				graphics.drawLine(ix, iy, ix + 1, iy + 1);
				graphics.translate(-origin, -origin);
//				graphics.setTransform(transform);
			}
		}
	}

	@NotNull
	public String getRangeDescription(int color) {
		final Statistics stats = statMap.get(color);
		assert stats != null;
		Statistics.Point measuredRange = stats.getRange();
		//noinspection StringBufferReplaceableByString
		StringBuilder builder = new StringBuilder();
		builder.append("mean : ").append(stats.getMean()).append('\n');
		builder.append("stDev: ").append(stats.getSTDev()).append('\n');
		builder.append("Rng x: ").append(measuredRange.x).append('\n');
		builder.append("Rng y: ").append(measuredRange.y).append('\n');
		builder.append("mnRng: ").append(stats.getMeanXRange()).append('\n');
		builder.append("mnRng: ").append(stats.getMeanYRange()).append('\n');
		return builder.toString();
	}

	private int scale(float value) {
		return (int) (value * scale);
	}

	private boolean inBounds(float value) {
		return (value >= -range) && (value < range);
	}

//	private boolean inBounds(int value) {
//		return (value >= -origin) && (value < origin);
//	}

	// TODO: 1. DONE. Figure out why the cross hairs at the origin vanish.
	// TODO: 2.       Add a separate reset button.
	// TODO  3. DONE: Triple the size of the image, keep the scope the same.
	// TODO  4.       Put the image in a scrolling pane.
	// TODO  5.       Then, draw an arrow from the origin to the data.
	// TODO  6. Done  Draw a brighter dot at the current value.
	// TODO  7. Done  Why does the point wander uphill on a left-right tilt, but downhill on a top-bottom tilt?
	// TODO  8.       Move cross hairs to image. In fact, draw a grid on the image.

	@Override
	public void paint(@NotNull final Graphics g) {
		int abX = getAbsoluteX();
		int abY = getAbsoluteY();

//		System.out.println("Painting at " + abX + ", " + abY);
//		System.out.println("Alternate: " + getX() + ", " + getY());
		g.translate(abX, abY);
		g.drawImage(image, 0, 0, SIDE, SIDE);
		for (Integer color : statMap.keySet()) {
//			int color = darken(color);
			Statistics statistics = statMap.get(color);
//			assert statistics != null;
			Assert.doAssert(statistics != null);
			Statistics.Point last = statistics.getLast().scale(scale);
			Statistics.Point mean = statistics.getMean().scale(scale);
			// reusing transform is risky, but image drawing is complete, so it's now unfrozen.
//			g.getTransform(transform);
			g.translate(origin, origin);

			g.setColor(color);
			// Draw current point
			g.fillRect(((int) last.x) - 1, -((int) last.y) - 1, 3, 3);

			// draw cross hairs
			g.setColor(WHITE);
			g.drawLine(-CH, 1, CH, 1);
			g.drawLine(-CH, -1, CH, -1);
			g.drawLine(1, -CH, 1, CH);
			g.drawLine(-1, -CH, -1, CH);
			g.setColor(0);
			g.drawLine(-CH, 0, CH, 0);
			g.drawLine(0, -CH, 0, CH);

			// translate to mean
			int ix = (int) mean.x;
			int iy = (int) -mean.y;
			g.translate(ix, iy);
//			int originX = origin;
//			int originY = origin;
			g.setColor(color);

			// draw stdDev
			Statistics.Point stDev = statistics.getSTDev().scale(scale);
			boolean isAntiAliased = g.isAntiAliased();
			g.setAntiAliased(true);
			g.drawArc((int) -stDev.x, (int) -stDev.y, (int) stDev.x * 2, (int) stDev.y * 2, 0, C_360);
			g.setAntiAliased(isAntiAliased);
//			g.setTransform(transform);

			// draw an x at the mean
			g.setColor(0);
			g.drawLine(-5, -4, 4, 5);
			g.drawLine(-4, -5, 5, 4);
			g.drawLine(-5, 4, 4, -5);
			g.drawLine(-4, 5, 5, -4);
			g.setColor(color);
			g.drawLine(-5, -5, 5, 5);
			g.drawLine(5, -5, -5, 5);

			g.translate(-ix, -iy);
			g.translate(-origin, -origin);
		}
		g.translate(-abX, -abY);
	}

}
