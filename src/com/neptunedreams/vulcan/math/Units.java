package com.neptunedreams.vulcan.math;

import com.neptunedreams.Assert;
import com.neptunedreams.vulcan.settings.Prefs;
import com.neptunedreams.util.NotNull;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 7/22/16
 * <p>Time: 12:26 AM
 *
 * @author Miguel Mu\u00f1oz
 */
public enum Units {
	degrees(180.0/Math.PI, true, false, '\u00B0', "Degrees", "Angle in Degrees", ""), // little circle
	perCent(100.0, false, false, '%', "Percent", "Inclination as a Percent", "(45\u00b0 = 100%)"),
	roofPitch(12.0, false, true, '"', "Roof Pitch", "Inches of Rise/Foot of Run", "(45\u00b0 = 12\")");

	private static final double HALF = 0.5;
	@SuppressWarnings({"StaticNonFinalField", "NonFinalFieldInEnum"})
	@NotNull
	private static Units units = Units.load(Prefs.prefs.get(Prefs.UNITS, Units.degrees.getUnitSymbol()));

	private final double conversion;
	private final boolean direct;
	private final char display;
	private final boolean useFractions;
	@NotNull
	private final String name;
	@NotNull
	private final String description;
	@NotNull
	private final String sample;

	Units(
			double conversion, 
			boolean direct,
			boolean useFractions,
			char display, 
			@NotNull String name, 
			@NotNull String description, 
			@NotNull String sample
	) {
		this.conversion = conversion;
		this.direct = direct;
		this.useFractions = useFractions;
		this.display = display;
		this.name = name;
		this.description = description;
		this.sample = sample;
	}
	
	public double convert(double radians) {
		if (direct) {
			return radians * conversion;
		}
		return Math.tan(radians) * conversion;
	}

	public char getUnitSymbol() { return display; }

	@NotNull
	public String getName() { return name; }

	@NotNull
	public String getDescription() { return description; }
	
	@NotNull
	public String getSample() { return sample; }
	
	@NotNull
	public static Units load(char symbol) {
		//noinspection ConstantConditions
		for (Units units: values()) {
			if (units.display == symbol) {
				return units;
			}
		}
		throw new IllegalArgumentException("Unknown symbol: " + symbol);
	}

	public static Units getPreferredUnits() { return units; }
	public static void setPreferredUnits(@NotNull Units chosenUnits) {
		units = chosenUnits;
		Prefs.prefs.set(Prefs.UNITS, chosenUnits.getUnitSymbol());
	}

	private static final String fractions = " \u215B\u00BC\u215C\u00BD\u215D\u00BE\u215E"; // 1/8 1/4 3/8 1/2 5/8 3/4 7/8

	/**
	 * Format the string according to the conventions of the units.
	 * <p/>
	 * This uses fraction characters. 
	 * You may also express a fraction using superscripts and subscripts, like this: 
	 * {@literal \}u2079{@literal \}u2044{@literal \}u2081{@literal \}u2086 which shows up as \u2079\u2044\u2081\u2086. 
	 * Super scripts are {@literal \}u2070 - {@literal \}u2079, except for 1, 2, & 3, which are {@literal \}u00b9, 
	 * {@literal \}u00b2, and {@literal \}u00b3. Subscripts are {@literal \}u2080 - {@literal \}u2089, and the fraction 
	 * slash is {@literal \}u2044.
	 * <p/>
	 * Code charts: <br>
	 * slash: http://www.unicode.org/charts/PDF/U2000.pdf<br>
	 * sub and super scripts: http://www.unicode.org/charts/PDF/U2070.pdf<br>
	 * Fraction characters: http://www.unicode.org/charts/PDF/U2150.pdf
	 * @param convertedAngle The angle, which has already been converted to the proper units.
	 * @return A formatted string, with the units symbol or proper fraction at the end.
	 */
	@NotNull
	public String format(double convertedAngle) {
		if (useFractions) {
			StringBuilder builder = new StringBuilder();
//			double original = convertedAngle;
			if (convertedAngle < 0) {
				builder.append('-');
				convertedAngle = -convertedAngle;
			}
			int truncated = (int) convertedAngle;
			int eighths = (int) (((convertedAngle - truncated) * 8) + HALF);
			if (eighths > 7) {
				eighths -= 8;
				truncated++;
			}
			builder.append(truncated);
//			final double radians = MathUtil.atan(original) / conversion;
//			Log.p("Converting " + LevelOfVulcan.format(radians, 4) + " (" + LevelOfVulcan.format(radians * degrees.conversion, 4)
//					+ "\u00b0) = " + LevelOfVulcan.format(convertedAngle, 4) + " to " + builder + ' ' + eighths + "/8");
			if (eighths > 0) {
				builder.append(fractions.charAt(eighths));
			}
			builder.append(":12");
//			Log.p("Converting " + LevelOfVulcan.format(radians, 4) + " (" + LevelOfVulcan.format(radians * degrees.conversion, 4) 
//					+ "\u00b0) = " + fmt(convertedAngle) + "to " + builder);
			return builder.toString();
		} else {
			return fmt(convertedAngle) + getUnitSymbol();
		}
	}

	public static String fmt(double angle) {
		if (angle >= 0.0) {
			//noinspection MagicNumber
			return pad(String.valueOf((int) ((angle * 10) + HALF) / 10.0), 5);
		} else {
			//noinspection MagicNumber
			return pad(String.valueOf((int) ((angle * 10) - HALF) / 10.0), 5);
		}
	}

	private static final String spaces = "                              ";

	@NotNull
	private static String pad(@NotNull String number, int max) {
		int length = number.length();
		if (length < max) {
			StringBuilder nBuilder = new StringBuilder(number);
			int dotSpot = number.indexOf('.');
			nBuilder.append("00".substring((length - dotSpot))); // This assume we pad a max of 1 zero at the end.
			String spacePad = spaces.substring(0, max);
			nBuilder.insert(0, spacePad.substring(nBuilder.length())); // 5 spaces
			Assert.doAssert(nBuilder.length() == max, "<" + nBuilder + "> from <" + number + '>');
			return nBuilder.toString();
		}
		return number;
	}
}
