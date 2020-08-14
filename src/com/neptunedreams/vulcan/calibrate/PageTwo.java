package com.neptunedreams.vulcan.calibrate;

import com.codename1.components.SpanLabel;
import com.codename1.ui.Button;
import com.codename1.ui.Component;
import com.codename1.ui.FontImage;
import com.codename1.ui.Form;
import com.codename1.ui.Graphics;
import com.codename1.ui.Image;
import com.codename1.ui.Label;
import com.codename1.ui.Slider;
import com.codename1.ui.Transform;
import com.codename1.ui.animations.CommonTransitions;
import com.codename1.ui.geom.Dimension;
import com.codename1.ui.plaf.Style;
import com.codename1.ui.table.TableLayout;
import com.codename1.ui.util.Resources;
import com.codename1.ui.util.UITimer;
import com.neptunedreams.Assert;
import com.neptunedreams.vulcan.app.LevelOfVulcan;
import com.neptunedreams.vulcan.SensorForm;
import com.neptunedreams.vulcan.calibrate.CalibrationData.View;
import com.neptunedreams.vulcan.ui.FormNavigator;
import com.neptunedreams.util.NotNull;

import static com.codename1.sensors.SensorType3D.*;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 5/1/16
 * <p>Time: 10:10 AM
 *
 * @author Miguel Mu\u00f1oz
 */
public class PageTwo extends SensorForm {
	private static final int HALF_SECOND = 500;
	private Image rotatingPhone;
	private final Label blankLabel;
	private final AnimatingLabel phoneLabel;
	@NotNull
	private final Label stageLabel;
	@NotNull
	private final Slider stageSlider = new Slider();
	@NotNull
	private final CalibrationData priorData;
	private static final double RADIANS = Math.PI / 180.0;
	private static final int ROTATION = 180;
	private static final int FPS = 18; // Frames per second
	private static final int FRAME_MS = 1000 / FPS; // milliseconds per frame
	private static final int DUR = 1000; // animation duration, ms
	private static final int ANGLE_DELTA = (ROTATION * 1000) / (DUR * FPS); // Degrees per frame
	private static final int gray = 0x333333;

	// TODO: Rewrite this to use an AnimatingImage instead of an AnimatingLabel.

	public PageTwo(@NotNull View view, @NotNull CalibrationData priorData) {
		super("Calibration Stage 2 of 2", accelerometer);
		this.priorData = priorData;
		TableLayout layout = new TableLayout(6, 1);
		layout.setGrowHorizontally(true);
//		BoxLayout layout = new BoxLayout(BoxLayout.Y_AXIS);
//		BorderLayout layout = new BorderLayout(BorderLayout.CENTER_BEHAVIOR_CENTER);
		
		setLayout(layout);
//		setTransitionInAnimator(CommonTransitions.createSlide(CommonTransitions.SLIDE_HORIZONTAL, false, 100));
		final Resources theme = LevelOfVulcan.getTheme();
		rotatingPhone = theme.getImage("smartPhoneIcon.png");
//		rotatingPhone = theme.getImage("TestImage.png");
		Assert.doAssert(rotatingPhone != null);
		int width = rotatingPhone.getWidth();
		int height = rotatingPhone.getHeight();
		final Image blank = makeBlankImage(width, height);

		phoneLabel = makePhoneLabel();
		blankLabel = new Label(blank) {
			@Override
			@NotNull
			public Dimension getPreferredSize() {
				return phoneLabel.getPreferredSize();
			}
		};
		final Style blankStyles = blankLabel.getAllStyles();
		blankStyles.setBgTransparency(0);
		blankLabel.setShowEvenIfBlank(true);
//		blankLabel.set
		blankStyles.setMargin(0, 0, 0, 0);
		blankStyles.setPadding(0, 0, 0, 0);
		blankStyles.setAlignment(CENTER);
		final Style phoneLabelStyles = phoneLabel.getAllStyles();
		phoneLabelStyles.setMargin(0, 0, 0, 0);
		phoneLabelStyles.setPadding(0, 0, 0, 0);
		phoneLabelStyles.setAlignment(CENTER);
		int row = -1;
		add(getConstraint(++row), blankLabel);

		SpanLabel message = new SpanLabel("Turn the device around, then press Stage Two");
		message.setTextBlockAlign(Component.CENTER);
		add(getConstraint(++row), message);

		Button next = new Button("Stage Two");
		FontImage.setMaterialIcon(next, FontImage.MATERIAL_BUILD);
		next.addActionListener(evt -> startStageTwo(view));
		add(getConstraint(++row), next);

		UITimer timer = new UITimer(this::startRotationAnimation);
		timer.schedule(HALF_SECOND, false, PageTwo.this);
//		add(BorderLayout.SOUTH, BoxLayout.encloseY(message, next));

		add(getConstraint(++row), new Label(" ")); // span

		stageLabel = new Label("    Stabilizing    ");
		add(getConstraint(++row), stageLabel);
		add(getConstraint(++row), stageSlider);
		stageLabel.setHidden(true);
		stageSlider.setHidden(true);
	}

	@NotNull
	private TableLayout.Constraint getConstraint(int row) {
		TableLayout layout = (TableLayout) getLayout();
		assert layout != null;
		return layout.createConstraint(row, 0).horizontalAlign(CENTER);
	}

	@NotNull
	private AnimatingLabel makePhoneLabel() {
		assert rotatingPhone != null;
		return new AnimatingLabel(rotatingPhone);
	}

	@SuppressWarnings("BooleanVariableAlwaysNegated")
	private boolean insidePaint = false;

	private void startRotationAnimation() {
		// 10 degrees per frame, 18 frames per second = 180 degrees in one second.
		
//		Log.p("Replace blank -> phone...");
		assert phoneLabel != null;
		phoneLabel.resetAngle();
		replaceAndWait(blankLabel, phoneLabel, CommonTransitions.createFade(HALF_SECOND));
		layoutContainer();
		phoneLabel.turnOn(); // ~56 milliseconds / frame
	}

	private void startStageTwo(@NotNull View view) {

		// show additional components
		stageLabel.setText(PageOne.STABILIZING_THE_DEVICE);
		stageLabel.setHidden(false);
		stageSlider.setHidden(false);
		repaint();

		// put in a 1/4 second delay
		CalibrationData calibrationData = new CalibrationData(view);
		UITimer timer = new UITimer(() -> startData(calibrationData, view));
		final int quarterSecond = 500;
		timer.schedule(quarterSecond, false, this);
//		activeTimer = timer;
	}
	
	private void startData(@NotNull CalibrationData calibrationData, @NotNull View view) {
		stageLabel.setText(PageOne.GATHERING_DATA);
		calibrationData.takeData(
				stageSlider,
				this,
				() -> finish(view, calibrationData));
	}

	private void finish(@NotNull View view, @NotNull CalibrationData secondData) {
		Assert.doAssert(priorData.view == view, "View mismatch: " + priorData.view + " != " + view);
		priorData.calibrate(secondData);
		FormNavigator.navigateBack(this);
//		FormNavigator.slideTo(LevelOfVulcan.BUBBLE_FORM);
	}

	/**
	 *  Creates a dark gray rectangle.
	 * 
	 * @param width The width
	 * @param height the height
	 * @return a gray Image of the specified dimensions
	 */
	@NotNull
	private Image makeBlankImage(final int width, final int height) {
		Image image = Image.createImage(width, height);
		Graphics graphics = image.getGraphics();
		graphics.setColor(gray);
		graphics.fillRect(0, 0, width, height);
		Image mask = Image.createImage(width, height);
		graphics = mask.getGraphics();
		final int blue = 0xFF;
		graphics.setColor(blue);
		graphics.fillRect(0, 0, width, height);

		return image.applyMask(mask.createMask());
	}

	private void swapRotationIcon() {
		Assert.doAssert(!insidePaint);
//		Log.p("replace phone -> blank");
		final CommonTransitions fade = CommonTransitions.createFade(HALF_SECOND);
//		fade.
		replaceAndWait(phoneLabel, blankLabel, fade);
		UITimer waitTimer = new UITimer(this::startRotationAnimation);
		waitTimer.schedule(HALF_SECOND, false, PageTwo.this);
	}

	private class AnimatingLabel extends Component {
		@NotNull
		private final Image icon;
		@NotNull
		private final Dimension prefSize;
		
		public void resetAngle() {
			angle = 0;
		}

		AnimatingLabel(@NotNull Image icon) {
			super();
			Assert.doAssert(!insidePaint);
			this.icon = icon;

			final int revisedWidth = icon.getWidth();// + deltaWidth;
			final int revisedHeight = icon.getHeight();// + deltaHeight;
			setWidth(revisedWidth);
			setHeight(revisedHeight);
			prefSize = new Dimension(revisedWidth, revisedHeight);

			getUnselectedStyle().setBgColor(gray); // fixes animation bug
		}

		@NotNull
		@Override
		public Dimension getPreferredSize() {
			return prefSize;
		}

		@NotNull
		public Image getIcon() {
			return icon;
		}

		public void turnOn() {
			Assert.doAssert(!insidePaint);
//			this.delay = delay;
			startAnimation();
		}

		private int angle = 0;
		private long lastFrameTime;

		@Override
		protected void paintBackground(@NotNull final Graphics g) {
			insidePaint = true;

//			final int white = 0xffffff;
			g.setColor(gray);
			g.fillRect(getX(), getY(), getWidth(), getHeight());
			Image image = getIcon();
			int centerX = image.getWidth() / 2;
			int centerY = image.getHeight() / 2;
			float rotation = (float) (angle * RADIANS);
			g.rotate(rotation, (getAbsoluteX() + centerX)/* + getX()*/, (getAbsoluteY() + centerY) /* + getY()*/);
			g.drawImage(getIcon(), getX(), getY());
			g.resetAffine();
			insidePaint = false;
		}

		@Override
		public boolean animate() {
			if (!isVisible() || isHidden()) {
				return false;
			}
			final long now = System.currentTimeMillis();
			if (now < (lastFrameTime + FRAME_MS)) {
				return false;
			} else {
				lastFrameTime = now;
				if (angle >= ROTATION) {
					stopAnimation();
					return false;
				}
				angle += ANGLE_DELTA;
				return true;
			}
		}

		public void startAnimation() {
			Assert.doAssert(!insidePaint);
			final Form componentForm = getComponentForm();
			assert componentForm != null;
			componentForm.registerAnimated(this);
		}
		
		protected void stopAnimation() {
			Assert.doAssert(!insidePaint);
			final Form componentForm = getComponentForm();
			assert componentForm != null;
			componentForm.deregisterAnimated(this);
			swapRotationIcon();
		}
	} 
}
