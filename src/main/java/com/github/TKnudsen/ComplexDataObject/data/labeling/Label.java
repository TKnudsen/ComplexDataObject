package com.github.TKnudsen.ComplexDataObject.data.labeling;

import java.awt.Color;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;

/**
 * <p>
 * Label represented with a name and a color.
 * </p>
 *
 * @version 1.02
 * @since 2015
 */
public class Label extends ComplexDataObject {

	private Color labelColor;

	@SuppressWarnings("unused")
	private Label() {

	}

	public Label(Color labelColor, String labelName) {
		super(labelName, "");
		this.labelColor = labelColor;
	}

	/**
	 * 
	 * @return
	 */
	public Color getColor() {
		return labelColor;
	}

	/**
	 * convenient method. still not sure if it would make more sense to just
	 * re-instantiate a new object.
	 * 
	 * @param color
	 */
	public void setColor(Color color) {
		this.labelColor = color;
	}

	@Override
	/**
	 * 
	 */
	public String toString() {
		return getName() + ", " + getColor();
	}

	@Override
	/**
	 * 
	 */
	public Label clone() {
		return new Label(new Color(labelColor.getRed(), labelColor.getGreen(), labelColor.getBlue()), getName());
	}
}
