package com.github.TKnudsen.ComplexDataObject.model.processors.attributes.Boolean;

import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.BooleanParser;
import com.github.TKnudsen.ComplexDataObject.model.processors.attributes.AttributeConverterProcessor;

public class BooleanConverter extends AttributeConverterProcessor {

	public BooleanConverter() {
		super(new BooleanParser());
	}

	public BooleanConverter(String attribute) {
		super(new BooleanParser(), attribute);
	}

}