package com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects;

/**
 * <p>
 * Parses various object types to Long values.
 * </p>
 * 
 * <p>
 * Key behaviors:
 * <ul>
 * <li>Returns existing Long instances without creating new objects</li>
 * <li>Handles numeric types, booleans, and string representations</li>
 * <li>Supports scientific notation (e.g., "1.5E10")</li>
 * <li>Normalizes string inputs (trims, replaces commas, handles spaces)</li>
 * <li>Returns null for un-parseable values or NaN</li>
 * </ul>
 * </p>
 *
 * @version 1.05
 * @since 2018
 */

public class LongParser implements IObjectParser<Long> {

	@Override
	public Long apply(Object object) {
		if (object == null)
			return null;

		// Fast path: already a Long
		if (object instanceof Long) {
			return (Long) object;
		}

		// Fast path: other numeric types
		if (object instanceof Number) {
			return parseNumber((Number) object);
		}

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

		// String parsing (most expensive path)
		return parseString(object.toString());
	}

	/**
	 * Parses a string representation to Long. Handles scientific notation, comma
	 * decimals, and whitespace.
	 */
	public static Long parseString(String str) {
		if (str == null || str.isEmpty()) {
			return null;
		}

		// Check for scientific notation first (before expensive string operations)
		// Using indexOf is faster than contains for single character
		int eIndex = str.indexOf('E');
		if (eIndex == -1) {
			eIndex = str.indexOf('e');
		}

		if (eIndex != -1) {
			try {
				return (long) Double.parseDouble(str);
			} catch (NumberFormatException e) {
				return null;
			}
		}

		// Normalize string
		String normalized = normalizeString(str);
		if (normalized == null || normalized.isEmpty()) {
			return null;
		}

		// Parse the normalized string
		try {
			return Long.parseLong(normalized);
		} catch (NumberFormatException e) {
			// Try as double first (handles decimal strings like "42.7")
			try {
				return (long) Double.parseDouble(normalized);
			} catch (NumberFormatException e2) {
				return null;
			}
		}
	}

	/**
	 * Normalizes a string for parsing: trims, replaces commas, handles spaces.
	 * Returns null if the string becomes invalid after normalization.
	 */
	private static String normalizeString(String str) {
		// Trim whitespace
		str = str.trim();

		if (str.isEmpty()) {
			return null;
		}

		// Convert to lower case for case-insensitive handling
		str = str.toLowerCase();

		// Replace comma decimal separator with period
		// Only do this if there's exactly one comma (avoid replacing thousands
		// separators incorrectly)
		int firstComma = str.indexOf(',');
		if (firstComma != -1 && str.indexOf(',', firstComma + 1) == -1) {
			str = str.replace(',', '.');
		}

		// Handle space: take everything before first space
		int spaceIndex = str.indexOf(' ');
		if (spaceIndex != -1) {
			str = str.substring(0, spaceIndex);
		}

		return str;
	}

	/**
	 * Efficiently handles numeric types.
	 */
	private Long parseNumber(Number number) {
		// Check for NaN (only applies to floating-point types)
		if (number instanceof Float || number instanceof Double) {
			double d = number.doubleValue();
			if (Double.isNaN(d)) {
				return null;
			}
			// Check for infinity
			if (Double.isInfinite(d)) {
				return null;
			}

			// For floating-point, check if within Long range
			if (d > Long.MAX_VALUE || d < Long.MIN_VALUE) {
				// Clamp or return null based on preference
				return null;
			}
		}

		return number.longValue();
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
