package com.neptunedreams.vulcan.calibrate;

import com.codename1.components.SpanLabel;
import com.codename1.ui.Button;
import com.codename1.ui.FontImage;
import com.codename1.ui.Label;
import com.codename1.ui.Slider;
import com.codename1.ui.table.TableLayout;
import com.codename1.ui.util.UITimer;
import com.neptunedreams.vulcan.app.LevelOfVulcan;
import com.neptunedreams.vulcan.SensorForm;
import com.neptunedreams.vulcan.calibrate.CalibrationData.View;
import com.neptunedreams.vulcan.ui.FormNavigator;
import com.neptunedreams.util.NotNull;

import static com.codename1.sensors.SensorType3D.*;

/**
 * Screen 1: Place device on a firm, stable surface. It need not be level. Then press Stage 1
 * Screen 2: Stage 1: Progress bar until it has enough data. Goes to screen 4 on success, screen 3 on failure
 * Screen 3: Warning about keeping device still. Return to screen 1
 * Screen 4: Instructions to turn device around, with animation and stage 2 button.
 * Screen 5: Stage 2: Progress bar until it has enough data. Goes to Screen 7 on success, screen 6 on failure.
 * Screen 6: Start-over button. 
 * Screen 7: All done. Return to level.
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 4/13/16
 * <p>Time: 6:22 PM
 *
 * @author Miguel Mu\u00f1oz
 */
public class PageOne extends SensorForm {
	public static final String GATHERING_DATA = "  Gathering Data  ";
	public static final String STABILIZING_THE_DEVICE = "Stabilizing the Device";
	private UITimer activeTimer = null;
	@NotNull
	private final Label stageLabel = new Label(GATHERING_DATA);
	@NotNull
	private final Slider stageSlider = new Slider();
	
	@NotNull
	private final CalibrationData calibrationData;


	public PageOne(@NotNull View view) {
		// 5 rows: Label, start button, Span, stage label, Slider 
		super("Calibration Stage 1 of 2", new TableLayout(5, 1), accelerometer);
		this.calibrationData = view.getCalibrationData();
		//noinspection StringConcatenation
		SpanLabel label = new SpanLabel("Place your device on a firm, stable, non-metallic surface. The surface need " +
				"not be level. Then press the Stage One button.");
		TableLayout.Constraint horCenter = new TableLayout.Constraint().horizontalAlign(CENTER);
		add(horCenter, label);

		Button stage1Button = new Button("Stage 1");
		FontImage.setMaterialIcon(stage1Button, FontImage.MATERIAL_BUILD);
		add(horCenter, stage1Button);
		stage1Button.addActionListener((ln) -> startStageOne(view));

		Label span = new Label(" ");
		add(horCenter, span);
		add(horCenter, stageLabel);
		add(horCenter, stageSlider);
		
		stageLabel.setHidden(true);
		stageLabel.getAllStyles().setAlignment(CENTER);
		stageSlider.setHidden(true);
//		stageSlider.setUnselectedStyle();
//		setTransitionInAnimator(CommonTransitions.createSlide(CommonTransitions.SLIDE_HORIZONTAL, false, 100));
//		Transition out = getTransitionOutAnimator();
//		Transition in = getTransitionInAnimator();
//		Log.p("In transition:  " + ((in == null) ? "null" : (in.getClass().toString() + in.toString())));
//		Log.p("Out transition: " + ((out == null) ? "null" : (out.getClass().toString() + out.toString())));
//		setTransitionOutAnimator(null);
	}

	private void startStageOne(@NotNull final View view) {
		// show additional components
		stageLabel.setText(STABILIZING_THE_DEVICE);
		stageLabel.setHidden(false);
		stageSlider.setHidden(false);
		repaint();

		// put in a 1/4 second delay
		UITimer timer = new UITimer(() -> startData(view));
		final int quarterSecond = 500; // TODO: Change back to 250
		timer.schedule(quarterSecond, false, PageOne.this);
		activeTimer = timer;
	}

	private void startData(@NotNull View view) {
		stageLabel.setText(PageOne.GATHERING_DATA);
		calibrationData.takeData(
				stageSlider,
				this,
				() -> finish(view));
	}

	private void finish(@NotNull View view) {
		stageLabel.repaint();
		// second timer to give the label time to repaint.
		UITimer timer = new UITimer(() -> {
			PageTwo pageTwo = new PageTwo(view, calibrationData);
			FormNavigator.slideTo(pageTwo, LevelOfVulcan.BUBBLE_FORM);
		});
		timer.schedule(10, false, PageOne.this);
	}

	@Override
	public void doStop() {
		if (activeTimer != null) {
			activeTimer.cancel();
		}
	}
}
