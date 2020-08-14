package com.neptunedreams.vulcan.ui;

import com.codename1.ui.Component;
import com.codename1.ui.Container;
import com.codename1.ui.Form;
import com.codename1.ui.Label;
import com.codename1.ui.layouts.GridBagConstraints;
import com.codename1.ui.layouts.GridBagLayout;
import com.neptunedreams.util.NotNull;
import com.neptunedreams.util.Nullable;

/**
 * The GridHelper exists to make it easier to use the GridBagLayout. The simplest way to use it is to instantiate
 * one, add all the components using the GridHelper API, then extract the Form and put it wherever you need it.
 * Or you can create your own Form and pass it to the GridHelper's constructor, which is just as easy.
 * <p>
 * The various {@code add( )} methods take a lot of optional parameters, most of which let you specify null to
 * use the default value. The only ones that don't are the component that you are adding (for which there is no
 * default), and the x and y values. All other parameters may be null. (Default Values are defined by
 * the GridBagConstraints class.)
 * <p>
 * So, for example, to add a button to the cell at 3, 5, you would do this:
 * <pre>
 *   GridHelper gh = new GridHelper();
 *   ...
 *   gh.add(button, 3, 5);
 *   ...
 *   Form panel = gh.getPanel();
 * </pre>
 * This uses default values for all the constraint's optional parameters.
 * <p>
 * The first optional parameter that you may add is the anchor. To specify an anchor of LINE_END, You would write it
 * like this:
 * <pre>
 *   gh.add(button, 3, 5, GridBagConstraints.LINE_END);
 * </pre>
 * The second parameter is the Fill parameter. To specify a fill of BOTH, you can write it like this:
 * <pre>
 *   gh.add(button, 3, 5, null, GridBagConstraints.BOTH);
 * </pre>
 * That specified a fill of BOTH and used the default value for Anchor.
 * <p>
 * Following the fill, you may specify either a pair of weights, which are doubles, or a pair of grid values (
 * gridx and gridy), which are Integers. So this
 * <pre>
 *   gh.add(button, 3, 5, null, null, 2.0, null);
 * </pre>
 * specifies a weightx of 2.0, and uses the default value for weighty. (It also specifies default values for anchor
 * and fill.)
 * <P>
 * Whereas this
 * <pre>
 *   gh.add(button, 3, 5, null, null, 2, null);
 * </pre>
 * specifies a gridx of 2 and a gridy of its default value.
 * <p>
 * If you need to specify both the weights and grid values, you specify the weights first, like this:
 * <pre>
 *   gh.add(button, 3, 5, null, null, 1.0, 1.0, 2, null);
 * </pre>
 * Following the gridx and gridy parameters are the padx and pady parameters.
 * <P>
 * In addition to the add methods, there are some {@code addField( )} methods that are used to
 * add an object and automatically insert a Label. This is very useful for making forms. To use
 * these methods, you specify a component, a grid location, and a String of text for the label.
 * This will create a Label from the text and place it at the specified location. Then it will
 * add one to the x position and place the specified component there. For example, you could
 * write this:
 * <pre>
 *   gh.addField(nameField, 3, 5, "Name:");
 * </pre>
 * This will pace a Label at (3, 5) in the grid, and place the {@code nameField} component
 * at (3, 6). It will right-justify the text and pad the space between them with an approprate
 * amount of space.
 * <p>
 * Each of the {@code add( )} and {@code addField( )} methods returns a GridBagConstraints object
 * if you wish to re-use the values.
 * <p>Created by IntelliJ IDEA.
 * <p>Date: Jul 23, 2010
 * <p>Time: 11:49:29 AM
 *
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class GridHelper {
	private static final int labelSpace = 5;

	@NotNull
	private final Container panel;
	public GridHelper(@NotNull Container thePanel) {
		panel = thePanel;
		if (!(panel.getLayout() instanceof GridBagLayout)) {
			panel.setLayout(new GridBagLayout());
		}
	}

	public GridHelper() { panel = new Form(new GridBagLayout()); }
	
	@NotNull
	public Container getPanel() { return panel; }

	@NotNull
	public static GridBagConstraints makeConstraint(int x, int y) {
		GridBagConstraints constraints = new GridBagConstraints();
//		constraints.weightx = 1.0;
//		constraints.weighty = 1.0;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridx = x;
		constraints.gridy = y;
//		constraints.fill = GridBagConstraints.HORIZONTAL;
		return constraints;
	}

	public GridBagConstraints add(@NotNull Component component, int x, int y) {
		final GridBagConstraints cns = makeConstraint(x, y);
		panel.add(cns, component);
		return cns;
	}

	@NotNull
	public static GridBagConstraints makeConstraint(int x, int y, @Nullable Integer anchor, @Nullable Integer fill) {
		return makeConstraint(x, y, anchor, fill, null, null, null, null, null, null);
	}

	@NotNull
	public static GridBagConstraints makeTemplate(@Nullable Integer anchor) {
		return makeConstraint(0, 0, anchor);
	}

	@NotNull
	public static GridBagConstraints makeTemplate(@Nullable Integer anchor, @Nullable Integer fill) {
		return makeConstraint(0, 0, anchor, fill);
	}

	@NotNull
	public GridBagConstraints add(Component component, int x, int y, @Nullable Integer anchor, @Nullable Integer fill) {
		final GridBagConstraints cns = makeConstraint(x, y, anchor, fill);
		panel.add(cns, component);
		return cns;
	}

	@NotNull
	public static GridBagConstraints makeConstraint(int x, int y, @Nullable Integer anchor) {
		return makeConstraint(x, y, anchor, null, null, null, null, null, null, null);
	}

	@NotNull
	public GridBagConstraints add(Component component, int x, int y, @Nullable Integer anchor) {
		final GridBagConstraints cns = makeConstraint(x, y, anchor);
		panel.add(cns, component);
		return cns;
	}

	/**
	 * Makes a constraint with the specified parameters. Except for x and y, which are required, null may be
	 * passed in for any parameter to specify the default value for that field.
	 * @param x x
	 * @param y y
	 * @param anchor the anchor
	 * @param fill fill
	 * @param weightX weightY
	 * @param weightY weightY
	 * @return a GridBagConstraint
	 */
	@NotNull
	public static GridBagConstraints makeConstraint(int x, int y, @Nullable Integer anchor, @Nullable Integer fill, @Nullable Double weightX, @Nullable Double weightY) {
		return makeConstraint(x, y, anchor, fill, weightX, weightY, null, null, null, null);
	}

	@NotNull
	public static GridBagConstraints makeTemplate(@Nullable Integer anchor, @Nullable Integer fill, @Nullable Double weightX, @Nullable Double weightY) {
		return makeConstraint(0, 0, anchor, fill, weightX, weightY, null, null, null, null);
	}

	/**
	 * Adds the component, using a constraint with the specified parameters. Except for x and y, which are required, null may be
	 * passed in for any parameter to specify the default value for that field.
	 * @param component The component to add
	 * @param x x
	 * @param y y
	 * @param anchor the anchor
	 * @param fill fill
	 * @param weightX weightY
	 * @param weightY weightY
	 * @return a GridBagConstraint
	 */
	@NotNull
	public GridBagConstraints add(Component component, int x, int y, @Nullable Integer anchor, @Nullable Integer fill, @Nullable Double weightX, @Nullable Double weightY) {
		final GridBagConstraints cns = makeConstraint(x, y, anchor, fill, weightX, weightY);
		panel.add(cns, component);
		return cns;
	}

	/**
	 * Makes a constraint with the specified parameters. Except for x and y, which are required, null may be
	 * passed in for any parameter to specify the default value for that field.
	 * @param x x
	 * @param y y
	 * @param anchor the anchor
	 * @param fill fill
	 * @param gridWidth gridWidth
	 * @param gridHeight gridHeight
	 * @return a GridBagConstraint
	 */
	@NotNull
	public static GridBagConstraints makeConstraint(int x, int y, @Nullable Integer anchor, @Nullable Integer fill, @Nullable Integer gridWidth, @Nullable Integer gridHeight) {
		return makeConstraint(x, y, anchor, fill, null, null, gridWidth, gridHeight, null, null);
	}

	@NotNull
	public static GridBagConstraints makeTemplate(@Nullable Integer anchor, @Nullable Integer fill, @Nullable Integer gridWidth, @Nullable Integer gridHeight) {
		return makeConstraint(0, 0, anchor, fill, gridWidth, gridHeight);
	}

	@NotNull
	public GridBagConstraints add(Component component, int x, int y, @Nullable Integer anchor, @Nullable Integer fill, @Nullable Integer gridWidth, @Nullable Integer gridHeight) {
		final GridBagConstraints constraints = makeConstraint(x, y, anchor, fill, gridWidth, gridHeight);
		panel.add(constraints, component);
		return constraints;
	}

	/**
	 * Makes a constraint with the specified parameters. Except for x and y, which are required, null may be
	 * passed in for any parameter to specify the default value for that field.
	 * @param x x
	 * @param y y
	 * @param anchor the anchor
	 * @param fill fill
	 * @param weightX weightY
	 * @param weightY weightY
	 * @param gridWidth gridWidth
	 * @param gridHeight gridHeight
	 * @return a GridBagConstraint
	 */
	@NotNull
	public static GridBagConstraints makeConstraint(int x, int y, @Nullable Integer anchor, @Nullable Integer fill, @Nullable Double weightX, @Nullable Double weightY, @Nullable Integer gridWidth, @Nullable Integer gridHeight) {
		return makeConstraint(x, y, anchor, fill, weightX, weightY, gridWidth, gridHeight, null, null);
	}

	@NotNull
	public static GridBagConstraints makeTemplate(@Nullable Integer anchor, @Nullable Integer fill, @Nullable Double weightX, @Nullable Double weightY, @Nullable Integer gridWidth, @Nullable Integer gridHeight) {
		return makeConstraint(0, 0, anchor, fill, weightX, weightY, gridWidth, gridHeight, null, null);
	}

	@NotNull
	public GridBagConstraints add(Component component, int x, int y, @Nullable Integer anchor, @Nullable Integer fill, @Nullable Double weightX, @Nullable Double weightY, @Nullable Integer gridWidth, @Nullable Integer gridHeight) {
		final GridBagConstraints constraints = makeConstraint(x, y, anchor, fill, weightX, weightY, gridWidth, gridHeight);
		panel.add(constraints, component);
		return constraints;
	}

	@NotNull
	public static GridBagConstraints makeTemplate(@Nullable Integer anchor, @Nullable Integer fill, @Nullable Double weightX, @Nullable Double weightY, @Nullable Integer gridWidth, @Nullable Integer gridHeight, @Nullable Integer iPadX, @Nullable Integer iPadY) {
		return makeConstraint(0, 0, anchor, fill, weightX, weightY, gridWidth, gridHeight, iPadX, iPadY);
	}

	/**
	 * Makes a constraint with the specified parameters. Except for x and y, which are required, null may be
	 * passed in for any parameter to specify the default value for that field.
	 * @param x x
	 * @param y y
	 * @param anchor the anchor
	 * @param fill fill
	 * @param weightX weightY
	 * @param weightY weightY
	 * @param gridWidth gridWidth
	 * @param gridHeight gridHeight
	 * @param iPadX ipadx
	 * @param iPadY ipady
	 * @return a GridBagConstraint
	 */
	@NotNull
	public static GridBagConstraints makeConstraint(int x, int y, @Nullable Integer anchor, @Nullable Integer fill, @Nullable Double weightX, @Nullable Double weightY, @Nullable Integer gridWidth, @Nullable Integer gridHeight, @Nullable Integer iPadX, @Nullable Integer iPadY) {
		GridBagConstraints cns = makeConstraint(x, y);
		if (anchor != null) {
			cns.anchor = anchor;
		}
		if (fill != null) {
			cns.fill = fill;
		}
		if (weightX != null) {
			cns.weightx = weightX;
		}
		if (weightY != null) {
			cns.weighty = weightY;
		}
		if (gridWidth != null) {
			cns.gridwidth = gridWidth;
		}

		if (gridHeight != null) {
			cns.gridheight = gridHeight;
		}

		if (iPadX != null) {
			cns.ipadx = iPadX;
		}

		if (iPadY != null) {
			cns.ipady = iPadY;
		}
//		//noinspection HardCodedStringLiteral
//		System.err.printf("Constraint at (%d, %d) a=%s%d f=%s%d weight=(%s%3.1f, %s%3.1f), grid=(%s%d, %s%d)\n", x, y,
//						anchor==null? "D:" : "", cns.anchor,
//						fill==null? "D:" : "", cns.fill,
//						weightX==null? "D:" : "", cns.weight x,
//						weightY==null? "D:" : "", cns.weight y,
//						gridWidth==null? "D:" : "", cns.grid width,
//						gridHeight==null? "D:" : "", cns.grid height
//		); // NON-NLS
		return cns;
	}

	@NotNull
	public GridBagConstraints add(Component component, int x, int y, @Nullable Integer anchor, @Nullable Integer fill, @Nullable Double weightX, @Nullable Double weightY, @Nullable Integer gridWidth, @Nullable Integer gridHeight, @Nullable Integer iPadX, @Nullable Integer iPadY) {
		final GridBagConstraints constraints = makeConstraint(x, y, anchor, fill, weightX, weightY, gridWidth, gridHeight, iPadX, iPadY);
		panel.add(constraints, component);
		return constraints;
	}

	@NotNull
	public static GridBagConstraints makeLeadingConstraint(int x, int y) {
		return makeConstraint(x, y, GridBagConstraints.LINE_START, null, null, null, null, null, null, null);
	}

	@NotNull
	public GridBagConstraints addLeading(Component component, int x, int y) {
		final GridBagConstraints constraints = makeLeadingConstraint(x, y);
		panel.add(constraints, component);
		return constraints;
	}

	/**
	 * This makes the constraints for the labeled component in an addField() call.
	 * The extra 3 points in the ipadx parameter are for a Nimbus (?) visual bug in
	 * JComboBoxes. They're supposed to allocate enough space to show all the text of their
	 * widest element. They're wide enough for the popup list, but not when just displaying
	 * the string in the component. By padding with 3 extra pixels, it makes the JComboBoxes
	 * wide enough to show all the text. (Rather than figure out which items are combo boxes,
	 * I just add this padding to all components used here.)
	 * @param x gridx
	 * @param y gridy
	 * @return the weighted constraint.
	 */
	@NotNull
	private static GridBagConstraints makeWeightedConstraint(int x, int y) {
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = x;
		constraints.gridy = y;
		constraints.anchor = GridBagConstraints.LINE_START;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 1.0;
//		constraints.weighty = 1.0;
		constraints.ipadx = 3; // extra space for JComboBoxes. See note above.
		return constraints;
	}

	@NotNull
	public static GridBagConstraints makeLabelConstraint(int x, int y) {
		GridBagConstraints constraints = makeConstraint(x, y);
		constraints.anchor = GridBagConstraints.LINE_END;
		constraints.ipadx = labelSpace;
		return constraints;
	}

	@NotNull
	public GridBagConstraints addLabel(String text, int x, int y) {
		GridBagConstraints constraints = makeLabelConstraint(x, y);
		panel.add(constraints, new Label(text));
		return constraints;
	}

	@NotNull
	public static GridBagConstraints addField(int gridX, int gridY, @NotNull String labelText, @NotNull Component fld, @NotNull Container panel) {
		return addField(gridX, gridY, 1, labelText, fld, panel, null);
	}

	@NotNull
	public GridBagConstraints addField(@NotNull Component component, int x, int y, @NotNull String labelText) {
		return addField(x, y, labelText, component, panel);
	}

	@NotNull
	public static GridBagConstraints addField(int gridX, int gridY, @NotNull String labelText, @NotNull Component fld, @NotNull Container panel, @NotNull GridBagConstraints template) {
		return addField(gridX, gridY, template.gridwidth, labelText, fld, panel, template);
	}

	@NotNull
	public GridBagConstraints addField(@NotNull Component field, int x, int y, @NotNull String labelText, @NotNull GridBagConstraints componentTemplate) {
		return addField(x, y, labelText, field, panel, componentTemplate);
	}

	@NotNull
	public static GridBagConstraints addField(int gridX, int gridY, int gridWidth, @NotNull String labelText, @NotNull Component fld, @NotNull Container panel) {
		return addField(gridX, gridY, gridWidth, labelText, fld, panel, null);
	}

	@NotNull
	public GridBagConstraints addField(@NotNull Component component, int x, int y, int gridWidth, @NotNull String labelText) {
		return addField(x, y, gridWidth, labelText, component, panel);
	}

	@NotNull
	public static GridBagConstraints addField(int gridX, int gridY, @NotNull String labelText, @NotNull Component fld, @NotNull Container panel, @NotNull GridBagConstraints labelTemplate, GridBagConstraints fieldTemplate) {
		Label label = new Label(labelText);
		GridBagConstraints cns = makeLabelConstraint(gridX, gridY);
		copyFromTemplate(labelTemplate, cns);
		panel.add(cns, label);

		cns = makeWeightedConstraint(gridX + labelTemplate.gridwidth, gridY);
		copyFromTemplate(fieldTemplate, cns);
		panel.add(cns, fld);
		return cns;
	}

	@NotNull
	public GridBagConstraints addField(
			@NotNull Component component, 
			int x, 
			int y, 
			@NotNull String labelText, 
			@NotNull GridBagConstraints labelTemplate, 
			@NotNull GridBagConstraints fieldTemplate
	) {
		return addField(x, y, labelText, component, panel, labelTemplate, fieldTemplate);
	}

	@NotNull
	private static GridBagConstraints addField(int gridX, int gridY, int gridWidth, String labelText, Component fld, @NotNull Container panel, @Nullable GridBagConstraints template) {
		Label label = new Label(labelText);
		GridBagConstraints cns = makeLabelConstraint(gridX, gridY);
		panel.add(cns, label);

		cns = makeWeightedConstraint(gridX + 1, gridY);
		cns.gridwidth = gridWidth;
		cns.anchor = GridBagConstraints.LINE_START;

		copyFromTemplate(template, cns);
		panel.add(cns, fld);
		return cns;
	}

	private static void copyFromTemplate(@Nullable GridBagConstraints template, @NotNull GridBagConstraints constraints) {
		if (template != null) {
			int anchor = template.anchor;
			if (anchor != GridBagConstraints.CENTER) {
				constraints.anchor = anchor;
			}
			if (template.gridwidth != 1) {
				constraints.gridwidth = template.gridwidth;
			}
			double tmpWeightX = template.weightx;
			if (tmpWeightX != 0.0) {
				constraints.weightx = tmpWeightX;
			}
			constraints.weighty = template.weighty;
			constraints.fill = template.fill;
			constraints.gridheight = template.gridheight;
			constraints.insets = template.insets;
			constraints.ipadx = template.ipadx;
			constraints.ipady = template.ipady;
		}
	}

	@NotNull
	public GridBagConstraints addField(@NotNull Component component, int x, int y, int gridWidth, @NotNull String labelText, @Nullable GridBagConstraints template) {
		return addField(x, y, gridWidth, labelText, component, panel, template);
	}

	@NotNull
	public static GridBagConstraints addRightField(int gridX, int gridY, @NotNull String labelText, @NotNull Component fld, @NotNull Container panel) {
		return addRightField(gridX, gridY, 1, labelText, fld, panel);
	}

	@NotNull
	public static GridBagConstraints addRightField(int gridX, int gridY, @NotNull String labelText, @NotNull Component fld, @NotNull Container panel, @Nullable GridBagConstraints template) {
		return addRightField(gridX, gridY, 1, labelText, fld, panel, template);
	}

	@NotNull
	public GridBagConstraints addRightField(@NotNull Component component, int x, int y, @NotNull String labelText) {
		return addRightField(x, y, labelText, component, panel);
	}

	@NotNull
	public GridBagConstraints addRightField(@NotNull Component component, int x, int y, @NotNull String labelText, @Nullable GridBagConstraints template) {
		return addRightField(x, y, 1, labelText, component, panel, template);
	}

	@NotNull
	public static GridBagConstraints addRightField(int gridX, int gridY, int gridWidth, @NotNull String labelText, @NotNull Component fld, @NotNull Container panel) {
		return addRightField(gridX, gridY, gridWidth, labelText, fld, panel, null);
	}

	@NotNull
	public static GridBagConstraints addRightField(int gridX, int gridY, int gridWidth, String labelText, @NotNull Component fld, @NotNull Container panel, @Nullable GridBagConstraints template) {
		Label label = new Label(labelText);
		GridBagConstraints cns = makeLabelConstraint(gridX, gridY);
		cns.anchor = GridBagConstraints.LINE_END;
		panel.add(cns, label);

		cns = makeWeightedConstraint(gridX + 1, gridY);
		cns.gridwidth = gridWidth;
		cns.fill = GridBagConstraints.HORIZONTAL;
		cns.anchor = GridBagConstraints.PAGE_START;
		copyFromTemplate(template, cns);
		panel.add(cns, fld);
		return cns;
	}

	@NotNull
	public GridBagConstraints addRightField(@NotNull Component component, int gridX, int gridY, int gridWidth, @NotNull String labelText) {
		return addRightField(gridX, gridY, gridWidth, labelText, component, panel);
	}

//	public static void addHorizontalStrut(int gridX, int gridY, Container panel, int width) {
//		GridBagConstraints cns = makeConstraint(gridX, gridY);
//		panel.add(Box.createHorizontalStrut(width), cns);
//	}

//	public void addHorizontalStrut(int x, int y, int width) {
//		addHorizontalStrut(x, y, panel, width);
//	}

//	public static GridBagConstraints addVerticalStrut(int gridX, int gridY, Container panel, int height) {
//		GridBagConstraints cns = makeConstraint(gridX, gridY);
//		panel.add(Box.createVerticalStrut(height), cns);
//		return cns;
//	}

//	public void addVerticalStrut(int gridX, int gridY, int height) {
//		addVerticalStrut(gridX, gridY, panel, height);
//	}

//	public void setTitle(String title) {
//		Borders.addTitledBorder(panel, title);
//	}

	@NotNull
	public GridBagConstraints addHorizontalGlue(int x, int y) {
		GridBagConstraints constraints = makeConstraint(x, y, null, null, 1.0, null);
		panel.add(constraints, new Form());
		return constraints;
	}

	@NotNull
	public GridBagConstraints addHorizontalGlue(int x, int y, double weightX) {
		GridBagConstraints constraints = makeConstraint(x, y, null, null, weightX, null);
		panel.add(constraints, new Form());
		return constraints;
	}

	@NotNull
	public GridBagConstraints addVerticalGlue(int x, int y) {
		GridBagConstraints constraints = makeConstraint(x, y, null, null, null, 1.0);
		panel.add(constraints, new Form());
		return constraints;
	}
}
