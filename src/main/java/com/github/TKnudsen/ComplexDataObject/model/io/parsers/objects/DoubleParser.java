package com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * <p>
 * Parser for converting various data representations to Double.
 * Handles multiple formats: decimals, percentages, fractions, different
 * locales.
 * </p>
 *
 * @version 2.0 (optimized)
 * @since 2016
 */
public class DoubleParser implements IObjectParser<Double> {

	// Configuration
	private boolean dotMeansThousands;
	private boolean printStackTrace = false;

	// Thread-local NumberFormat instances (expensive to create)
	// Java 8 compatible using lambda
	private static final ThreadLocal<NumberFormat> GERMAN_FORMAT = new ThreadLocal<NumberFormat>() {
		@Override
		protected NumberFormat initialValue() {
			NumberFormat nf = NumberFormat.getInstance(Locale.GERMAN);
			nf.setGroupingUsed(true);
			return nf;
		}
	};

	private static final ThreadLocal<DecimalFormat> US_FORMAT = new ThreadLocal<DecimalFormat>() {
		@Override
		protected DecimalFormat initialValue() {
			DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
			DecimalFormat df = new DecimalFormat("#,##0.###", symbols);
			df.setGroupingUsed(true);
			return df;
		}
	};

	/**
	 * Default constructor - assumes dot means decimal point
	 */
	public DoubleParser() {
		this(false);
	}

	/**
	 * Constructor with locale configuration
	 * 
	 * @param dotMeansThousands if true, interprets dot as thousands separator
	 */
	public DoubleParser(boolean dotMeansThousands) {
		this.dotMeansThousands = dotMeansThousands;
	}

	/**
	 * Parses an object to Double
	 * 
	 * @param object the object to parse
	 * @return parsed Double value, or Double.NaN if parsing fails
	 */
	@Override
	public Double apply(Object object) {
		// Fast path: null
		if (object == null)
			return Double.NaN;

		// Fast path: already a number
		if (object instanceof Number)
			return ((Number) object).doubleValue();

		// Convert to string and normalize
		String normalized = normalizeString(String.valueOf(object));

		// Fast path: empty after normalization
		if (normalized.isEmpty())
			return Double.NaN;

		// Try parsing based on format detection
		return parseNormalizedString(normalized);
	}

	/**
	 * Normalizes input string - single pass for performance Java 8 compatible
	 * 
	 * @param input the raw input string
	 * @return normalized string ready for parsing
	 */
	private String normalizeString(String input) {
		if (input == null || input.isEmpty())
			return "";

		// Single pass normalization using StringBuilder
		StringBuilder sb = new StringBuilder(input.length());
		boolean hasContent = false;

		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);

			// Skip whitespace
			if (Character.isWhitespace(c))
				continue;

			// Skip trailing semicolon (only if it's the last char)
			if (c == ';' && i == input.length() - 1)
				continue;

			// Skip percent sign
			if (c == '%')
				continue;

			// Keep everything else
			sb.append(c);
			hasContent = true;
		}

		if (!hasContent)
			return "";

		// Convert to lowercase - Java 8 compatible
		String result = sb.toString().toLowerCase(Locale.ROOT);

		// Handle special values
		if ("nan".equals(result) || "na".equals(result) || "null".equals(result))
			return "";

		return result;
	}

	/**
	 * Parses a normalized string to Double
	 */
	private Double parseNormalizedString(String normalized) {
		// Detect format and parse accordingly
		if (dotMeansThousands || isGermanFormat(normalized))
			return parseGermanFormat(normalized);

		// Try fraction format (e.g., "3/4")
		if (normalized.indexOf('/') >= 0)
			return parseFraction(normalized);

		// Try standard decimal format
		return parseStandardFormat(normalized);
	}

	/**
	 * Detects if string uses German number format
	 */
	private boolean isGermanFormat(String normalized) {
		int dotIndex = normalized.indexOf('.');
		int commaIndex = normalized.indexOf(',');

		// If both present, German format has dot before comma
		if (dotIndex >= 0 && commaIndex >= 0)
			return dotIndex < commaIndex;

		// If only comma present, likely German decimal
		if (commaIndex >= 0 && dotIndex < 0) {
			int charsAfterComma = normalized.length() - commaIndex - 1;
			return charsAfterComma <= 3;
		}

		return false;
	}

	/**
	 * Parses German format number (e.g., "1.234,56")
	 */
	private Double parseGermanFormat(String normalized) {
		try {
			Number number = GERMAN_FORMAT.get().parse(normalized);
			return number != null ? number.doubleValue() : Double.NaN;
		} catch (ParseException e) {
			if (printStackTrace)
				e.printStackTrace();

			// Fallback: try converting comma to dot and parsing
			return parseStandardFormat(normalized.replace(',', '.'));
		}
	}

	/**
	 * Parses standard format number (e.g., "1,234.56" or "1234.56")
	 */
	private Double parseStandardFormat(String normalized) {
		// Remove thousand separators (commas in US format)
		String cleaned = normalized.replace(",", "");

		try {
			return Double.parseDouble(cleaned);
		} catch (NumberFormatException e) {
			if (printStackTrace)
				e.printStackTrace();
			return Double.NaN;
		}
	}

	/**
	 * Parses fraction format (e.g., "3/4" to 0.75)
	 */
	private Double parseFraction(String normalized) {
		int slashIndex = normalized.indexOf('/');

		// Validate fraction format
		if (slashIndex <= 0 || slashIndex >= normalized.length() - 1)
			return Double.NaN;

		try {
			String numeratorStr = normalized.substring(0, slashIndex).trim();
			String denominatorStr = normalized.substring(slashIndex + 1).trim();

			// Validate both parts exist
			if (numeratorStr.isEmpty() || denominatorStr.isEmpty())
				return Double.NaN;

			double numerator = Double.parseDouble(numeratorStr);
			double denominator = Double.parseDouble(denominatorStr);

			// Validate denominator
			if (denominator == 0.0)
				return Double.NaN;

			return numerator / denominator;

		} catch (NumberFormatException e) {
			if (printStackTrace)
				e.printStackTrace();
			return Double.NaN;
		}
	}

	// ==================== INTERFACE METHODS ====================

	@Override
	public Class<Double> getOutputClassType() {
		return Double.class;
	}

	@Override
	public String toString() {
		return "DoubleParser[dotMeansThousands=" + dotMeansThousands + "]";
	}

	// ==================== GETTERS/SETTERS ====================

	public boolean isDotMeansThousands() {
		return dotMeansThousands;
	}

	public void setDotMeansThousands(boolean dotMeansThousands) {
		this.dotMeansThousands = dotMeansThousands;
	}

	public boolean isPrintStackTrace() {
		return printStackTrace;
	}

	public void setPrintStackTrace(boolean printStackTrace) {
		this.printStackTrace = printStackTrace;
	}

	// ==================== UTILITY METHODS ====================

	/**
	 * Parses with explicit locale override
	 */
	public Double applyWithLocale(Object object, Locale locale) {
		if (object == null)
			return Double.NaN;

		if (object instanceof Number)
			return ((Number) object).doubleValue();

		try {
			NumberFormat nf = NumberFormat.getInstance(locale);
			Number number = nf.parse(String.valueOf(object));
			return number != null ? number.doubleValue() : Double.NaN;
		} catch (ParseException e) {
			if (printStackTrace)
				e.printStackTrace();
			return Double.NaN;
		}
	}

	/**
	 * Validates if a string can be parsed to Double
	 */
	public boolean canParse(String input) {
		Double result = apply(input);
		return result != null && !result.isNaN();
	}
}
