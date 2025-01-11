package com.github.TKnudsen.ComplexDataObject.model.scoring.functions;

import javax.swing.event.ChangeEvent;

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
