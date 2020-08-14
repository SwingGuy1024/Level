package com.neptunedreams.vulcan.ui;

import com.codename1.ui.layouts.GridBagConstraints;
import com.neptunedreams.util.NotNull;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 7/21/16
 * <p>Time: 5:06 PM
 *
 * @author Miguel Mu\u00f1oz
 */
public class GridBagHelper {
	@SuppressWarnings("unused")
	public static class Builder {
		@NotNull
		private final GridBagConstraints constraint;
		public Builder() {
			constraint = new GridBagConstraints();
			constraint.fill = GridBagConstraints.BOTH;
		}

		public Builder(int row, int col) {
			this();
			constraint.gridx = col;
			constraint.gridy = row;
		}

		@NotNull
		public Builder weightX(double wx) {
			constraint.weightx = wx;
			return this;
		}

		@NotNull
		public Builder weightY(double wy) {
			constraint.weighty = wy;
			return this;
		}

		@NotNull
		public Builder gWidth(int gridWidth) {
			constraint.gridwidth = gridWidth;
			return this;
		}

		@NotNull
		public Builder gHeight(int gridHeight) {
			constraint.gridheight = gridHeight;
			return this;
		}
		
		@NotNull
		public Builder fill(int fill) {
			constraint.fill = fill;
			return this;
		}
		
		@NotNull
		public GridBagConstraints build() {
			//noinspection UseOfClone
			return (GridBagConstraints) constraint.clone();
		}
	}
}
