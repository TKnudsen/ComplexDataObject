package com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects;

/**
 * <p>
 * Title: LongParser
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2018
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.02
 */

public class LongParser implements IObjectParser<Long> {

	@Override
	public Long apply(Object object) {
		String stringValue = String.valueOf(object).toLowerCase();

		stringValue = stringValue.trim();
		stringValue = stringValue.replace(",", ".");
		if (stringValue.contains(" "))
			stringValue = stringValue.substring(0, stringValue.indexOf(" "));

		try {
			return Long.parseLong(stringValue);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public Class<Long> getOutputClassType() {
		return Long.class;
	}

}
