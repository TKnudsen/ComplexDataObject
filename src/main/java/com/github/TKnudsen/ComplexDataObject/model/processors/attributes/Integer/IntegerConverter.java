package com.github.TKnudsen.ComplexDataObject.model.processors.attributes.Integer;

import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IntegerParser;
import com.github.TKnudsen.ComplexDataObject.model.processors.attributes.AttributeConverterProcessor;

public class IntegerConverter extends AttributeConverterProcessor {

	public IntegerConverter() {
		super(new IntegerParser());
	}

	public IntegerConverter(String attribute) {
		super(new IntegerParser(), attribute);
	}
}