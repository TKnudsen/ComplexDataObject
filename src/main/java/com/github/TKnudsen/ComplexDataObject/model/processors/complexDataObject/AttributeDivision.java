package com.github.TKnudsen.ComplexDataObject.model.processors.complexDataObject;

import java.util.Arrays;
import java.util.List;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.DoubleParser;

/**
 * <p>
 * Divides two numerical attributes to create a new (secondary data) attribute.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2017-2018
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */
public class AttributeDivision implements IComplexDataObjectProcessor {

	private final String dividend;
	private final String divisor;
	private final String targetAttributeName;

	private DoubleParser doubleParser = new DoubleParser();

	public AttributeDivision(String dividend, String divisor, String targetAttributeName) {
		this.dividend = dividend;
		this.divisor = divisor;
		this.targetAttributeName = targetAttributeName;
	}

	@Override
	public void process(List<ComplexDataObject> data) {
		for (ComplexDataObject cdo : data) {

			if (cdo == null)
				return;

			double value = Double.NaN;

			Object dividendObject = cdo.getAttribute(dividend);
			Object divisorObject = cdo.getAttribute(divisor);

			try {
				Double dividendValue = doubleParser.apply(dividendObject);
				Double divisorValue = doubleParser.apply(divisorObject);
				value = dividendValue / divisorValue;
			} catch (Exception e) {
			}

			cdo.add(targetAttributeName, value);
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
