package com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects;

/**
 * <p>
 * Title: DoubleParser
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2016
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */

public class DoubleParser implements IObjectParser<Double> {

	@Override
	public Double apply(Object object) {
		String stringValue = String.valueOf(object).toLowerCase();

		stringValue = stringValue.trim();
		stringValue = stringValue.replace(",", ".");
		stringValue = stringValue.replace("%", "");
		if (stringValue.contains(" "))
			stringValue = stringValue.substring(0, stringValue.indexOf(" "));
		try {
			return Double.parseDouble(stringValue);
		} catch (Exception e) {
			return Double.NaN;
		}
	}
}
