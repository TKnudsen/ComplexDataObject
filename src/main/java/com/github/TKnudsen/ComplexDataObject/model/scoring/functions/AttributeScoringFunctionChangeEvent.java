package com.github.TKnudsen.ComplexDataObject.model.scoring.functions;

import javax.swing.event.ChangeEvent;

/**
 * <p>
 * Change event fired by an AttributeScoringFunction whenever its
 * configuration (e.g. weight, normalization, uncertainty handling) changes.
 * Carries the affected attribute name and a reference to the function that
 * triggered the change.
 * </p>
 */
public class AttributeScoringFunctionChangeEvent extends ChangeEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3357176953527706541L;

	private final String attribute;
	private final AttributeScoringFunction<?> function;

	public AttributeScoringFunctionChangeEvent(Object source, String attribute, AttributeScoringFunction<?> function) {
		super(source);

		this.attribute = attribute;
		this.function = function;
	}

	public String getAttribute() {
		return attribute;
	}

	public AttributeScoringFunction<?> getFunction() {
		return function;
	}

}
