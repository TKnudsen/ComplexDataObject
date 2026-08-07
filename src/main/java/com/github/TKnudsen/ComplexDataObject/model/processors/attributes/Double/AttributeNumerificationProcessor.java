package com.github.TKnudsen.ComplexDataObject.model.processors.attributes.Double;

import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IObjectParser;
import com.github.TKnudsen.ComplexDataObject.model.processors.attributes.AttributeConverterProcessor;

/**
 * <p>
 * Converts the values of a given attribute to {@code Double} using a
 * caller-supplied {@link IObjectParser}, based on
 * {@link AttributeConverterProcessor}.
 * </p>
 */
public class AttributeNumerificationProcessor extends AttributeConverterProcessor {

	public AttributeNumerificationProcessor(IObjectParser<Double> parser) {
		super(parser);
	}

	public AttributeNumerificationProcessor(String attribute, IObjectParser<Double> doubleParser) {
		super(doubleParser, attribute);
	}

}
