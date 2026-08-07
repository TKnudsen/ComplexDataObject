package com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

/**
 * <p>
 * little helpers for parsing primitives and collections of
 * primitives.s
 * </p>
 *
 * @version 1.06
 * @since 2022
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

	public static synchronized Boolean parseBoolean(Object o) {
		return booleanParser.apply(o);
	}

	public static synchronized Date parseDate(Object o) {
		return dateParser.apply(o);
	}

	public static synchronized String parseString(Object o) {
		return String.valueOf(o);
	}

	public static synchronized Collection<Double> parseDoubles(Collection<? extends Object> objects) {
		if (objects == null)
			return null;

		Collection<Double> values = new ArrayList<>();
		for (Object o : objects)
			values.add(parseDouble(o));
		return values;
	}

	public static synchronized Collection<Float> parseFloats(Collection<? extends Object> objects) {
		if (objects == null)
			return null;

		Collection<Float> values = new ArrayList<>();
		for (Object o : objects)
			values.add(parseFloat(o));
		return values;
	}

	public static synchronized Collection<Integer> parseIntegers(Collection<? extends Object> objects) {
		if (objects == null)
			return null;

		Collection<Integer> values = new ArrayList<>();
		if (objects == null)
			return null;

		for (Object o : objects)
			values.add(parseInteger(o));
		return values;
	}

	public static synchronized Collection<Long> parseLongs(Collection<? extends Object> objects) {
		if (objects == null)
			return null;

		Collection<Long> values = new ArrayList<>();
		for (Object o : objects)
			values.add(parseLong(o));
		return values;
	}

	public static synchronized Collection<Date> parseDates(Collection<? extends Object> objects) {
		if (objects == null)
			return null;

		Collection<Date> values = new ArrayList<>();
		for (Object o : objects)
			values.add(parseDate(o));
		return values;
	}

	public static synchronized Collection<Boolean> parseBooleans(Collection<? extends Object> objects) {
		if (objects == null)
			return null;

		Collection<Boolean> values = new ArrayList<>();
		for (Object o : objects)
			values.add(parseBoolean(o));
		return values;
	}

	public static synchronized Collection<String> parseStrings(Collection<? extends Object> objects) {
		if (objects == null)
			return null;

		Collection<String> values = new ArrayList<>();
		for (Object o : objects)
			values.add(parseString(o));
		return values;
	}

	/**
	 * Determines whether a string represents a missing or invalid value.
	 * 
	 * <p>
	 * This method identifies common representations of missing data in datasets,
	 * including null values, empty strings, whitespace-only strings, and standard
	 * missing value indicators.
	 * </p>
	 * 
	 * <p>
	 * Recognized missing value patterns (case-insensitive where applicable):
	 * </p>
	 * <ul>
	 * <li>null reference</li>
	 * <li>Empty string or whitespace-only string</li>
	 * <li>"?" (question mark)</li>
	 * <li>"\"\"" (quoted empty string literal)</li>
	 * <li>"null"</li>
	 * <li>"unknown"</li>
	 * <li>"missing"</li>
	 * </ul>
	 * 
	 * <p>
	 * <b>Performance:</b> Optimized for speed with early returns and
	 * zero-allocation string comparisons using
	 * {@link String#regionMatches(boolean, int, String, int, int)}.
	 * </p>
	 * 
	 * @param s the string to check for missing value indicators; may be null
	 * @return true if the string is null or represents a missing value, false
	 *         otherwise
	 */
	public static boolean isMissingValue(String s) {
		if (s == null)
			return true;

		int len = s.length();

		// Early return for empty string
		if (len == 0)
			return true;

		// Find first non-whitespace character
		int start = 0;
		while (start < len && Character.isWhitespace(s.charAt(start)))
			start++;

		// All whitespace
		if (start == len)
			return true;

		// Find last non-whitespace character
		int end = len - 1;
		while (end >= start && Character.isWhitespace(s.charAt(end)))
			end--;

		int trimmedLength = end - start + 1;

		// Check by trimmed length for fast filtering
		switch (trimmedLength) {
		case 1:
			// "?" as missing value
			return (s.charAt(start) == '?' || s.charAt(start) == '-' || s.charAt(start) == '_');

		case 2:
			// "" (quoted empty string)
			char first = s.charAt(start);
			char second = s.charAt(start + 1);
			return (first == '"' && second == '"') || (first == '\'' && second == '\'');

		case 3:
			// "null" (case-insensitive)
			return s.regionMatches(true, start, "NaN", 0, 3);

		case 4:
			// "null" (case-insensitive)
			return s.regionMatches(true, start, "null", 0, 4);

		case 7:
			// "unknown" or "missing" (case-insensitive)
			return s.regionMatches(true, start, "unknown", 0, 7) || s.regionMatches(true, start, "missing", 0, 7)
					|| s.regionMatches(true, start, "Unknown", 0, 7) || s.regionMatches(true, start, "Missing", 0, 7);

		default:
			return false;
		}
	}
}
