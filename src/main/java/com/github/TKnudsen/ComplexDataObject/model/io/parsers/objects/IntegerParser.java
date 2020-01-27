package com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects;

/**
 * 
 * Interger parser that throws an exception when a number shall be parsed that
 * is larger than an Integer's max value.
 * 
 * <p>
 * Copyright: Copyright (c) 2016-2020
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.03
 */

public class IntegerParser implements IObjectParser<Integer> {

	private LongParser LongParser = new LongParser();

	@Override
	public Integer apply(Object object) {
		if (object == null)
			return null;

		if (object instanceof Integer)
			return ((Integer) object).intValue();

		Long l = LongParser.apply(object);
		if (l == null)
			return null;
		else if (l > Integer.MAX_VALUE)
			throw new IllegalArgumentException("IntegerParser should never parse values of size: " + l);
		else
			return l.intValue();
	}

	@Override
	public Class<Integer> getOutputClassType() {
		return Integer.class;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
}
