package com.github.TKnudsen.ComplexDataObject.model.processors.attributes.Date;

import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.DateParser;
import com.github.TKnudsen.ComplexDataObject.model.processors.attributes.AttributeConverterProcessor;

/**
 * <p>
 * Converts the values of a given attribute to {@code Date} using a
 * {@link DateParser}, based on {@link AttributeConverterProcessor}.
 * </p>
 */
public class DateConverter extends AttributeConverterProcessor {

	public DateConverter() {
		super(new DateParser());
	}

	public DateConverter(String attribute) {
		super(new DateParser(), attribute);
	}

}