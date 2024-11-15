package com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects;

/**
 * <p>
 * 
 * Decision: if the parser discovers a numeric input object, it returns the
 * object as long, but does not create a new Long instance.
 * 
 * Copyright: Copyright (c) 2018-2024
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.04
 */

public class LongParser implements IObjectParser<Long> {

	@Override
	public Long apply(Object object) {
		if (object == null)
			return null;

		if (object instanceof Number) {
			if (Double.isNaN(((Number) object).doubleValue()))
				return null;
			else
				return ((Number) object).longValue();
		}

		if (object instanceof Boolean)
			return ((boolean) object) ? 1L : 0L;

		if (object.toString().contains("E")) {
			try {
				return Double.valueOf(object.toString()).longValue();
			} catch (Exception e) {
			}
		}

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

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
}
