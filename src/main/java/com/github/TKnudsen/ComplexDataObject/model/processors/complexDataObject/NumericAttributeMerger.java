package com.github.TKnudsen.ComplexDataObject.model.processors.complexDataObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;

/**
 * <p>
 * Merges values from a list of given numerical attributes.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2017-2019
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */
public class NumericAttributeMerger implements IComplexDataObjectProcessor {

	private final List<String> attributes;
	private final String targetAttribute;

	public NumericAttributeMerger(List<String> attributes, String targetAttribute) {
		this.attributes = attributes;
		this.targetAttribute = targetAttribute;
	}

	@Override
	public void process(List<ComplexDataObject> data) {

		Map<ComplexDataObject, List<Double>> valuesMap = new HashMap<>();

		// collect values
		for (ComplexDataObject cdo : data) {
			if (cdo == null)
				continue;

			List<Double> values = new ArrayList<>();

			for (String attribute : attributes) {
				Object o = cdo.getAttribute(attribute);
				if (o == null)
					continue;

				if (!(o instanceof Number))
					continue;

				Number value = (Number) o;

				if (Double.isNaN(value.doubleValue()))
					continue;

				values.add(value.doubleValue());
			}

			valuesMap.put(cdo, values);
		}

		// aggregate and assign to new attribute
		for (ComplexDataObject cdo : data) {
			if (cdo == null)
				continue;

			List<Double> list = valuesMap.get(cdo);

			if (list == null || list.isEmpty())
				cdo.add(targetAttribute, Double.NaN);
			else
				cdo.add(targetAttribute, MathFunctions.getMean(list));
		}
	}

	@Override
	public void process(ComplexDataContainer container) {
		for (ComplexDataObject complexDataObject : container)
			process(Arrays.asList(complexDataObject));
	}

	@Override
	public DataProcessingCategory getPreprocessingCategory() {
		return DataProcessingCategory.SECONDARY_DATA_PROVIDER;
	}

}
