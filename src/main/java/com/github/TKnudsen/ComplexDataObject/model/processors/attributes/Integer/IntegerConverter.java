package com.github.TKnudsen.ComplexDataObject.model.processors.attributes.Integer;

import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IntegerParser;
import com.github.TKnudsen.ComplexDataObject.model.processors.attributes.AttributeConverterProcessor;

/**
 * <p>
 * Converts the values of a given attribute to {@code Integer} using an
 * {@link IntegerParser}, based on {@link AttributeConverterProcessor}.
 * </p>
 */
public class IntegerConverter extends AttributeConverterProcessor {

	public IntegerConverter() {
		super(new IntegerParser());
	}

	public IntegerConverter(String attribute) {
		super(new IntegerParser(), attribute);
	}
}