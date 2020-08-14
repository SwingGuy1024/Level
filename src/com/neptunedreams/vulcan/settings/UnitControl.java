package com.neptunedreams.vulcan.settings;

import com.codename1.components.MultiButton;
import com.neptunedreams.vulcan.math.Units;
import com.neptunedreams.util.NotNull;

/**
 * This displays the Units option, to let the user choose between different Units to display the angle. 
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 7/22/16
 * <p>Time: 1:22 AM
 *
 * @see Units
 * @author Miguel Mu\u00f1oz
 */
public class UnitControl extends MultiButton {
	@NotNull
	private final Units unit;
	public UnitControl(@NotNull Units units) {
		super(units.getName());
		setRadioButton(true);
		setTextLine2(units.getDescription());
		setTextLine3(units.getSample());
		unit = units;
	}

	@NotNull
	public Units getUnit() { return unit; }
	
	
}
