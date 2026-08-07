package com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects;

/**
 * <p>
 * Integer parser that handles conversion from various object types to Integer
 * values.
 * </p>
 *
 * <p>
 * Key behaviors:
 * <ul>
 * <li>Returns existing Integer instances without creating new objects</li>
 * <li>Handles numeric types, booleans, and string representations</li>
 * <li>Clamps values that exceed Integer bounds (logs warning)</li>
 * <li>Delegates to LongParser for string parsing</li>
 * <li>Returns null for un-parseable values or NaN</li>
 * </ul>
 * </p>
 *
 * @version 1.04
 * @since 2016
 */

public class IntegerParser implements IObjectParser<Integer> {

	// Reuse singleton instance - LongParser is state-less and thread-safe
	private static final LongParser LONG_PARSER = new LongParser();

	@Override
	public Integer apply(Object object) {
		if (object == null)
			return null;

		// Fast path: already an Integer
		if (object instanceof Integer)
			return (Integer) object;

		// Fast path: direct conversion for small numeric types
		if (object instanceof Number)
			return parseNumber((Number) object);

		// Delegate to LongParser for booleans and strings
		Long l = LONG_PARSER.apply(object);
		if (l == null)
			return null;

		return clampToIntegerRange(l);
	}

	/**
	 * Efficiently handles numeric types without going through string conversion.
	 */
	private Integer parseNumber(Number number) {
		// Types that always fit in Integer range
		if (number instanceof Byte || number instanceof Short)
			return number.intValue();

		// Handle floating-point types
		if (number instanceof Float || number instanceof Double) {
			double d = number.doubleValue();

			// Check for special values
			if (Double.isNaN(d) || Double.isInfinite(d))
				return null;

			// Check if value is within integer range (before truncation)
			if (d > Integer.MAX_VALUE) {
				logWarning("Double value " + d + " exceeds Integer.MAX_VALUE. Clamping to MAX_VALUE.");
				return Integer.MAX_VALUE;
			}
			if (d < Integer.MIN_VALUE) {
				logWarning("Double value " + d + " is less than Integer.MIN_VALUE. Clamping to MIN_VALUE.");
				return Integer.MIN_VALUE;
			}

			return (int) d;
		}

		// Handle Long and other numeric types (BigInteger, BigDecimal, etc.)
		long longValue = number.longValue();
		return clampToIntegerRange(longValue);
	}

	/**
	 * Clamps a long value to Integer range and logs a warning if clamping occurs.
	 * 
	 * @param value the long value to clamp
	 * @return the clamped integer value
	 */
	private Integer clampToIntegerRange(long value) {
		if (value > Integer.MAX_VALUE) {
			logWarning("Value " + value + " exceeds Integer.MAX_VALUE (" + Integer.MAX_VALUE
					+ "). Clamping to MAX_VALUE.");
			return Integer.MAX_VALUE;
		}

		if (value < Integer.MIN_VALUE) {
			logWarning("Value " + value + " is less than Integer.MIN_VALUE (" + Integer.MIN_VALUE
					+ "). Clamping to MIN_VALUE.");
			return Integer.MIN_VALUE;
		}

		return (int) value;
	}

	/**
	 * Centralized logging for overflow/underflow warnings. Can be replaced with
	 * proper logger if needed.
	 * 
	 * @param message the warning message
	 */
	private void logWarning(String message) {
		System.err.println("IntegerParser: " + message);
	}

	@Override
	public Class<Integer> getOutputClassType() {
		return Integer.class;
	}

	@Override
	public String toString() {
		return "IntegerParser";
	}
}
