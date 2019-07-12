package com.github.TKnudsen.ComplexDataObject.model.processors.attributes.Integer;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IntegerParser;
import com.github.TKnudsen.ComplexDataObject.model.processors.complexDataObject.DataProcessingCategory;
import com.github.TKnudsen.ComplexDataObject.model.processors.complexDataObject.IComplexDataObjectProcessor;

public class IntegerConverter implements IComplexDataObjectProcessor {

	private String attribute;

	private IntegerParser integerParser = new IntegerParser();

	public IntegerConverter() {
		this.attribute = null;
	}

	public IntegerConverter(String attribute) {
		this.attribute = attribute;
	}

	public void process(ComplexDataContainer container) {
		if (attribute == null)
			throw new IllegalArgumentException("IntegerConverter requires attribute definition first.");

		Map<ComplexDataObject, Integer> values = new HashMap<>();
		for (ComplexDataObject cdo : container) {
			if (cdo.getAttribute(attribute) != null) {
				Integer d = integerParser.apply(cdo.getAttribute(attribute));
				values.put(cdo, d);
			} else
				values.put(cdo, null);
		}

		container.remove(attribute);
		container.addAttribute(attribute, Date.class, null);
		for (ComplexDataObject cdo : container)
			cdo.add(attribute, values.get(cdo));
	}

	@Override
	public void process(List<ComplexDataObject> data) {
		if (attribute == null)
			throw new IllegalArgumentException("IntegerConverter requires attribute definition first.");

		for (ComplexDataObject cdo : data) {
			if (cdo.getAttribute(attribute) != null) {
				Integer d = integerParser.apply(cdo.getAttribute(attribute));
				cdo.add(attribute, d);
			} else
				cdo.add(attribute, null);
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