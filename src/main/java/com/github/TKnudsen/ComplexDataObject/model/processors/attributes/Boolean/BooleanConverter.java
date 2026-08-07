package com.github.TKnudsen.ComplexDataObject.model.processors.attributes.Boolean;

import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.BooleanParser;
import com.github.TKnudsen.ComplexDataObject.model.processors.attributes.AttributeConverterProcessor;

/**
 * <p>
 * Converts the values of a given attribute to {@code Boolean} using a
 * {@link BooleanParser}, based on {@link AttributeConverterProcessor}.
 * </p>
 */
public class BooleanConverter extends AttributeConverterProcessor {

	public BooleanConverter() {
		super(new BooleanParser());
	}

	public BooleanConverter(String attribute) {
		super(new BooleanParser(), attribute);
	}

}