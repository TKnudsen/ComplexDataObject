package com.github.TKnudsen.ComplexDataObject.model.scoring;

import javax.swing.event.ChangeEvent;

import com.github.TKnudsen.ComplexDataObject.model.scoring.functions.AttributeScoringFunction;

public class AttributeScoringChangeEvent extends ChangeEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3357176953527706541L;

	private final String attribute;
	private final AttributeScoringFunction<?> function;

	public AttributeScoringChangeEvent(Object source, String attribute, AttributeScoringFunction<?> function) {
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
