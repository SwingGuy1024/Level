package com.neptunedreams.vulcan.settings;

import com.codename1.components.MultiButton;
import com.neptunedreams.vulcan.math.Accuracy;
import com.neptunedreams.util.NotNull;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 7/24/16
 * <p>Time: 2:14 AM
 *
 * @author Miguel Mu\u00f1oz
 */
public class AccuracyControl extends MultiButton
{
	
	@NotNull
	private final Accuracy accuracy;
	public AccuracyControl(@NotNull Accuracy accuracy) {
		super(accuracy.getLabel());
		setRadioButton(true);
		setTextLine2(fParts(accuracy.getParts()) + " : " + accuracy.getRange());
		this.accuracy = accuracy;
		
	}

	private String fParts(double parts) {
		//noinspection MagicNumber
		if (parts == 1.5) {
			return "1.5";
		}
		//noinspection MagicNumber
		return String.valueOf((int)(parts + 0.5));
	}

	@NotNull
	public Accuracy getAccuracy() {
		return accuracy;
	}
}
