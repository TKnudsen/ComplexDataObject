package com.github.TKnudsen.ComplexDataObject.model.processors.attributes.Date;

import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.DateParser;
import com.github.TKnudsen.ComplexDataObject.model.processors.attributes.AttributeConverterProcessor;

public class DateConverter extends AttributeConverterProcessor {

	public DateConverter() {
		super(new DateParser());
	}

	public DateConverter(String attribute) {
		super(new DateParser(), attribute);
	}

}