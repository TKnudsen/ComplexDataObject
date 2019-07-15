package com.github.TKnudsen.ComplexDataObject.model.processors.attributes.Double;

import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.DoubleParser;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IObjectParser;
import com.github.TKnudsen.ComplexDataObject.model.processors.attributes.AttributeConverterProcessor;

public class AttributeNumerificationProcessor extends AttributeConverterProcessor {

	public AttributeNumerificationProcessor() {
		super(new DoubleParser());
	}

	public AttributeNumerificationProcessor(String attribute) {
		this(attribute, new DoubleParser());
	}

	public AttributeNumerificationProcessor(String attribute, IObjectParser<Double> doubleParser) {
		super(doubleParser, attribute);
	}

}
