package com.github.TKnudsen.ComplexDataObject.model.processors.attributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IObjectParser;
import com.github.TKnudsen.ComplexDataObject.model.processors.complexDataObject.DataProcessingCategory;
import com.github.TKnudsen.ComplexDataObject.model.processors.complexDataObject.IComplexDataObjectProcessor;

/**
 * <p>
 * Converts the values of a single attribute in a ComplexDataContainer or list
 * of ComplexDataObjects by applying an {@link IObjectParser} to each value
 * and replacing the attribute definition with the parser's output type. Acts
 * as the common base for the type-specific converters (boolean, date,
 * numeric, integer) in this package.
 * </p>
 */
public class AttributeConverterProcessor implements IComplexDataObjectProcessor {

	private String attribute;

	private final IObjectParser<?> parser;

	public AttributeConverterProcessor(IObjectParser<?> parser) {
		this.parser = parser;
	}

	public AttributeConverterProcessor(IObjectParser<?> parser, String attribute) {
		this(parser);

		this.attribute = attribute;
	}

	public void process(ComplexDataContainer container) {
		if (attribute == null)
			throw new IllegalArgumentException("AttributeConverterProcessor requires attribute definition first.");

		Map<ComplexDataObject, Object> values = new HashMap<>();
		for (ComplexDataObject cdo : container) {
			Object d = cdo.getAttribute(attribute);

			if (d != null)
				d = parser.apply(cdo.getAttribute(attribute));

			values.put(cdo, d);
		}

		// redefine attribute
		container.remove(attribute);
		container.addAttribute(attribute, parser.getOutputClassType(), null);

		// add converted data
		for (ComplexDataObject cdo : container)
			cdo.add(attribute, values.get(cdo));
	}

	@Override
	public void process(List<ComplexDataObject> data) {
		if (attribute == null)
			throw new IllegalArgumentException("AttributeConverterProcessor requires attribute definition first.");

		// inner part, after having disabled listeners
		for (ComplexDataObject cdo : data) {
			Object d = null;

			if (cdo.getAttribute(attribute) != null)
				d = parser.apply(cdo.getAttribute(attribute));

			cdo.add(attribute, d);
		}
	}

	@Override
	public DataProcessingCategory getPreprocessingCategory() {
		return DataProcessingCategory.DATA_CLEANING;
	}

	public String getAttribute() {
		return attribute;
	}

	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}

	public IObjectParser<?> getParser() {
		return parser;
	}
}
