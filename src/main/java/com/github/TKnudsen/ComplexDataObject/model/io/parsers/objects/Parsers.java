package com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

/**
 * <p>
 * Title: Parsers
 * </p>
 * *
 * <p>
 * Description: little helpers for parsing primitives and collections of
 * primitives.s
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2022-2024
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.04
 */
public class Parsers {
	private static DoubleParser doubleParser = new DoubleParser();
	private static IntegerParser integerParser = new IntegerParser();
	private static LongParser longParser = new LongParser();
	private static BooleanParser booleanParser = new BooleanParser();
	private static DateParser dateParser = new DateParser();

	public static synchronized Double parseDouble(Object o) {
		return doubleParser.apply(o);
	}

	public static synchronized Float parseFloat(Object o) {
		Double d = doubleParser.apply(o);
		return d == null ? null : d.floatValue();
	}

	public static synchronized Integer parseInteger(Object o) {
		return integerParser.apply(o);
	}

	public static synchronized Long parseLong(Object o) {
		return longParser.apply(o);
	}

	/**
	 * 
	 * @deprecated naming convention changed to parseBoolean
	 */
	public static synchronized Boolean booleanParser(Object o) {
		return parseBoolean(o);
	}

	public static synchronized Boolean parseBoolean(Object o) {
		return booleanParser.apply(o);
	}

	public static synchronized Date parseDate(Object o) {
		return dateParser.apply(o);
	}

	public static synchronized String parseString(Object o) {
		return String.valueOf(o);
	}

	public static synchronized Collection<Double> parseDoubles(Collection<Object> objects) {
		if (objects == null)
			return null;

		Collection<Double> values = new ArrayList<>();
		for (Object o : objects)
			values.add(parseDouble(o));
		return values;
	}

	public static synchronized Collection<Float> parseFloats(Collection<Object> objects) {
		if (objects == null)
			return null;

		Collection<Float> values = new ArrayList<>();
		for (Object o : objects)
			values.add(parseFloat(o));
		return values;
	}

	public static synchronized Collection<Integer> parseIntegers(Collection<Object> objects) {
		if (objects == null)
			return null;

		Collection<Integer> values = new ArrayList<>();
		if (objects == null)
			return null;

		for (Object o : objects)
			values.add(parseInteger(o));
		return values;
	}

	public static synchronized Collection<Long> parseLongs(Collection<Object> objects) {
		if (objects == null)
			return null;

		Collection<Long> values = new ArrayList<>();
		for (Object o : objects)
			values.add(parseLong(o));
		return values;
	}

	public static synchronized Collection<Date> parseDates(Collection<Object> objects) {
		if (objects == null)
			return null;

		Collection<Date> values = new ArrayList<>();
		for (Object o : objects)
			values.add(parseDate(o));
		return values;
	}

	public static synchronized Collection<Boolean> parseBooleans(Collection<Object> objects) {
		if (objects == null)
			return null;

		Collection<Boolean> values = new ArrayList<>();
		for (Object o : objects)
			values.add(parseBoolean(o));
		return values;
	}

	public static synchronized Collection<String> parseStrings(Collection<Object> objects) {
		if (objects == null)
			return null;

		Collection<String> values = new ArrayList<>();
		for (Object o : objects)
			values.add(parseString(o));
		return values;
	}
}
