package com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects;

/**
 * <p>
 * Copyright: Copyright (c) 2018-2020
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.03
 */

public class LongParser implements IObjectParser<Long> {

	@Override
	public Long apply(Object object) {
		if (object == null)
			return null;

		if (object instanceof Long)
			return new Long((long) object);

		if (object instanceof Number) {
			Number n = (Number) object;
			if (Double.isNaN(n.doubleValue()))
				return null;
			else
				return new Long(((Number) object).longValue());
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
