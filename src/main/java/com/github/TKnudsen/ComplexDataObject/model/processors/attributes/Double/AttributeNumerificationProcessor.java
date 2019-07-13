package com.github.TKnudsen.ComplexDataObject.model.processors.attributes.Double;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.DoubleParser;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IObjectParser;
import com.github.TKnudsen.ComplexDataObject.model.processors.complexDataObject.DataProcessingCategory;
import com.github.TKnudsen.ComplexDataObject.model.processors.complexDataObject.IComplexDataObjectProcessor;

public class AttributeNumerificationProcessor implements IComplexDataObjectProcessor {

	private String attribute;

	private final IObjectParser<Double> doubleParser;

	public AttributeNumerificationProcessor() {
		this(null);
	}

	public AttributeNumerificationProcessor(String attribute) {
		this(attribute, new DoubleParser());
	}

	public AttributeNumerificationProcessor(String attribute, IObjectParser<Double> doubleParser) {
		this.attribute = attribute;
		this.doubleParser = doubleParser;
	}

	public void process(ComplexDataContainer container) {
		if (attribute == null)
			throw new IllegalArgumentException("AttributeNumerificationProcessor requires attribute definition first.");

		// strategy: parse to double and buffer the values
		// then remove attribute and re-create attribute in the container
		Map<ComplexDataObject, Double> values = new HashMap<>();
		for (ComplexDataObject cdo : container) {
			Double d = Double.NaN;

			if (cdo.getAttribute(attribute) != null)
				d = doubleParser.apply(cdo.getAttribute(attribute));

			values.put(cdo, d);
		}

		container.remove(attribute);
		container.addAttribute(attribute, Double.class, Double.NaN);
		for (ComplexDataObject cdo : container)
			cdo.add(attribute, values.get(cdo));
	}

	@Override
	public void process(List<ComplexDataObject> data) {
		if (attribute == null)
			throw new IllegalArgumentException("AttributeNumerificationProcessor requires attribute definition first.");

		for (ComplexDataObject cdo : data) {
			Double d = Double.NaN;

			if (cdo.getAttribute(attribute) != null)
				d = doubleParser.apply(cdo.getAttribute(attribute));

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
}
