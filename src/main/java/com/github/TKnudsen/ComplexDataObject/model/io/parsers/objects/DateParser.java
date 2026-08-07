package com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * <p>
 * Thread-safe date parser supporting multiple formats. Focus on European date
 * notation (dd-MM-yyyy). US format (MM/dd/yyyy) not supported.
 *
 * In line with the solution to parse many dates programatically.
 * https://stackoverflow.com/questions/3389348/parse-any-date-in-java
 *
 * To reduce complexity the parser replaces "\\", "_", "/", and "." by "-";
 *
 * Older versions of the parser defined a series of date formats. This is
 * applied if new patterns do not match
 * </p>
 *
 * @version 2.0 (optimized)
 */
public class DateParser implements IObjectParser<Date> {

	// Pattern cache - compiled once for performance
	private static final Map<String, String> DATE_FORMAT_PATTERNS = buildDateFormatPatterns();
	private static final Map<Pattern, String> COMPILED_PATTERNS = compilePatterns();

	// Configuration
	private boolean printStackTrace = false;
	private int maxRecursionDepth = 2;

	/**
	 * Builds the mapping of regex patterns to date formats
	 */
	private static LinkedHashMap<String, String> buildDateFormatPatterns() {
		LinkedHashMap<String, String> patterns = new LinkedHashMap<String, String>();

		// Date only formats
		patterns.put("^\\d{8}$", "yyyyMMdd");
		patterns.put("^\\d{1,2}-\\d{1,2}-\\d{4}$", "dd-MM-yyyy");
		patterns.put("^\\d{4}-\\d{1,2}-\\d{1,2}$", "yyyy-MM-dd");
		patterns.put("^\\d{4}-\\d{1,2}$", "yyyy-MM");
		patterns.put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}$", "dd MMM yyyy");
		patterns.put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}$", "dd MMMM yyyy");

		// Date with time formats
		patterns.put("^\\d{12}$", "yyyyMMddHHmm");
		patterns.put("^\\d{8}\\s\\d{4}$", "yyyyMMdd HHmm");
		patterns.put("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}$", "dd-MM-yyyy HH:mm");
		patterns.put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}$", "yyyy-MM-dd HH:mm");
		patterns.put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}$", "dd MMM yyyy HH:mm");
		patterns.put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}$", "dd MMMM yyyy HH:mm");

		// NEW PATTERN: yyyy-MM-dd-HH:mm (e.g., "2020-11-19-05:00")
		patterns.put("^\\d{4}-\\d{1,2}-\\d{1,2}-\\d{1,2}:\\d{2}$", "yyyy-MM-dd-HH:mm");

		// Date with seconds formats
		patterns.put("^\\d{14}$", "yyyyMMddHHmmss");
		patterns.put("^\\d{8}\\s\\d{6}$", "yyyyMMdd HHmmss");
		patterns.put("^\\d{8}\\s\\d{1,2}:\\d{2}:\\d{2}$", "yyyyMMdd HH:mm:ss");
		patterns.put("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd-MM-yyyy HH:mm:ss");
		patterns.put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$", "yyyy-MM-dd HH:mm:ss");
		patterns.put("^\\d{4}:\\d{1,2}:\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$", "yyyy:MM:dd HH:mm:ss");
		patterns.put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd MMM yyyy HH:mm:ss");
		patterns.put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd MMMM yyyy HH:mm:ss");
		patterns.put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}-\\d{2}-\\d{2}-\\d{3}$", "yyyy-MM-dd HH-mm-ss-SSS");
		patterns.put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}-\\d{2}-\\d{2}-\\d{4}$", "yyyy-MM-dd HH-mm-ss-SSSS");

		// Month-Year formats
		patterns.put("^\\d{1,2}-\\d{4}$", "MM-yyyy");

		return patterns;
	}

	/**
	 * Pre-compiles all regex patterns for performance
	 */
	private static Map<Pattern, String> compilePatterns() {
		Map<Pattern, String> compiled = new LinkedHashMap<Pattern, String>();
		for (Map.Entry<String, String> entry : DATE_FORMAT_PATTERNS.entrySet()) {
			compiled.put(Pattern.compile(entry.getKey()), entry.getValue());
		}
		return compiled;
	}

	/**
	 * Thread-local DateFormat cache for thread safety and performance
	 */
	private static final ThreadLocal<Map<String, DateFormat>> DATE_FORMAT_CACHE = new ThreadLocal<Map<String, DateFormat>>() {
		@Override
		protected Map<String, DateFormat> initialValue() {
			return new LinkedHashMap<String, DateFormat>();
		}
	};

	/**
	 * Gets a cached DateFormat instance for the given pattern
	 */
	private DateFormat getDateFormat(String pattern) {
		Map<String, DateFormat> cache = DATE_FORMAT_CACHE.get();
		DateFormat format = cache.get(pattern);

		if (format == null) {
			format = new SimpleDateFormat(pattern, Locale.US);
			format.setLenient(false);
			cache.put(pattern, format);
		}

		return format;
	}

	/**
	 * Determines the date format pattern for a given date string
	 */
	public static String determineDateFormat(String dateString) {
		if (dateString == null || dateString.isEmpty())
			return null;

		String lowercase = dateString.toLowerCase(Locale.ROOT);

		for (Map.Entry<Pattern, String> entry : COMPILED_PATTERNS.entrySet()) {
			if (entry.getKey().matcher(lowercase).matches())
				return entry.getValue();
		}

		return null;
	}

	/**
	 * Parses an object to Date
	 */
	@Override
	public Date apply(Object object) {
		return applyInternal(object, 0);
	}

	/**
	 * Internal parsing method with recursion depth tracking
	 */
	private Date applyInternal(Object object, int recursionDepth) {
		if (recursionDepth > maxRecursionDepth)
			return null;

		if (object == null)
			return null;

		if (object instanceof Date)
			return new Date(((Date) object).getTime());

		if (object instanceof Long)
			return new Date((Long) object);

		String dateString = String.valueOf(object);
		String normalized = normalizeString(dateString);

		if (normalized.isEmpty() || normalized.length() > 50)
			return null;

		// Try pattern-based parsing first
		Date date = parseWithPatterns(normalized);
		if (date != null)
			return date;

		// Try fallback parsing by length
		date = parseByLength(normalized);
		if (date != null)
			return date;

		// Try special Java Date.toString() formats
		date = parseSpecialFormats(dateString);
		if (date != null)
			return date;

		// Handle time zone offset
		return handleTimezoneOffset(object, recursionDepth);
	}

	/**
	 * Normalizes date string - single pass
	 */
	private String normalizeString(String input) {
		if (input == null || input.isEmpty())
			return "";

		StringBuilder sb = new StringBuilder(input.length());

		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);

			if (c == 'T')
				sb.append(' ');
			else if (c == '\\' || c == '_' || c == '/' || c == '.')
				sb.append('-');
			else
				sb.append(c);
		}

		return sb.toString().trim();
	}

	/**
	 * Tries to parse using pattern matching
	 */
	private Date parseWithPatterns(String normalized) {
		String formatPattern = determineDateFormat(normalized);
		if (formatPattern == null)
			return null;

		try {
			DateFormat format = getDateFormat(formatPattern);
			return format.parse(normalized);
		} catch (ParseException e) {
			if (printStackTrace)
				e.printStackTrace();
			return null;
		}
	}

	/**
	 * Tries to parse based on string length
	 */
	private Date parseByLength(String normalized) {
		int length = normalized.length();

		try {
			switch (length) {
			case 24:
				return getDateFormat("yyyy-MM-dd HH-mm-ss-SSSS").parse(normalized);
			case 23:
				return getDateFormat("yyyy-MM-dd HH:mm:ss:SSS").parse(normalized);

			case 19:
				try {
					return getDateFormat("yyyy-MM-dd HH:mm:ss").parse(normalized);
				} catch (ParseException e) {
					return getDateFormat("yyyy:MM:dd HH:mm:ss").parse(normalized);
				}

			case 16:
				try {
					return getDateFormat("yyyy-MM-dd HH:mm").parse(normalized);
				} catch (ParseException e) {
					return getDateFormat("dd-MM-yyyy HH:mm").parse(normalized);
				}

			case 13:
				return getDateFormat("yyyy-MM-dd HH").parse(normalized);

			case 10:
				return parseLengthTen(normalized);

			case 7:
				try {
					return getDateFormat("yyyy-MM").parse(normalized);
				} catch (ParseException e) {
					return getDateFormat("MM-yyyy").parse(normalized);
				}

			default:
				return null;
			}
		} catch (ParseException e) {
			if (printStackTrace)
				e.printStackTrace();
			return null;
		}
	}

	/**
	 * Parses length-10 strings with multiple format attempts
	 */
	private Date parseLengthTen(String normalized) throws ParseException {
		String[] formats = { "yyyy-MM-dd", "dd-MM-yyyy", "MM-dd-yyyy" };

		for (String format : formats) {
			try {
				return getDateFormat(format).parse(normalized);
			} catch (ParseException e) {
				// Try next format
			}
		}

		throw new ParseException("No format matched", 0);
	}

	/**
	 * Parses special formats like Java Date.toString() NEW: Added support for "Thu
	 * Nov 19 13:04 CET 2020"
	 */
	private Date parseSpecialFormats(String original) {
		// Try standard Java Date.toString() format: "Thu Nov 19 13:04:05 CET 2020"
		try {
			DateFormat format = getDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
			return format.parse(original);
		} catch (ParseException e) {
			// Continue to next format
		}

		// NEW: Try without seconds: "Thu Nov 19 13:04 CET 2020"
		try {
			DateFormat format = getDateFormat("EEE MMM dd HH:mm zzz yyyy");
			return format.parse(original);
		} catch (ParseException e) {
			if (printStackTrace)
				e.printStackTrace();
		}

		return null;
	}

	/**
	 * Handles timezone offset by stripping it
	 */
	private Date handleTimezoneOffset(Object object, int recursionDepth) {
		String str = object.toString();

		int plusIndex = str.indexOf('+');
		if (plusIndex > 0) {
			String withoutOffset = str.substring(0, plusIndex);
			return applyInternal(withoutOffset, recursionDepth + 1);
		}

		int minusIndex = str.lastIndexOf('-');
		if (minusIndex > 10) {
			String withoutOffset = str.substring(0, minusIndex);
			return applyInternal(withoutOffset, recursionDepth + 1);
		}

		return null;
	}

	// ==================== INTERFACE METHODS ====================

	@Override
	public Class<Date> getOutputClassType() {
		return Date.class;
	}

	@Override
	public String toString() {
		return "DateParser[formats=" + DATE_FORMAT_PATTERNS.size() + "]";
	}

	// ==================== CONFIGURATION ====================

	public boolean isPrintStackTrace() {
		return printStackTrace;
	}

	public void setPrintStackTrace(boolean printStackTrace) {
		this.printStackTrace = printStackTrace;
	}

	public int getMaxRecursionDepth() {
		return maxRecursionDepth;
	}

	public void setMaxRecursionDepth(int maxRecursionDepth) {
		this.maxRecursionDepth = Math.max(0, Math.min(5, maxRecursionDepth));
	}

	// ==================== UTILITY METHODS ====================

	public static Map<String, String> getSupportedFormats() {
		return new LinkedHashMap<String, String>(DATE_FORMAT_PATTERNS);
	}

	public boolean canParse(String dateString) {
		Date result = apply(dateString);
		return result != null;
	}

	public static void clearCache() {
		DATE_FORMAT_CACHE.remove();
	}

	// ==================== TESTING ====================

	public static Map<String, String> getFormatExamples() {
		LinkedHashMap<String, String> examples = new LinkedHashMap<String, String>();

		examples.put("20201119", "yyyyMMdd");
		examples.put("19-11-2020", "dd-MM-yyyy");
		examples.put("2020-11-19", "yyyy-MM-dd");
		examples.put("2020-11", "yyyy-MM");
		examples.put("19 Nov 2020", "dd MMM yyyy");
		examples.put("19 November 2020", "dd MMMM yyyy");
		examples.put("202011191304", "yyyyMMddHHmm");
		examples.put("20201119 1304", "yyyyMMdd HHmm");
		examples.put("19-11-2020 13:04", "dd-MM-yyyy HH:mm");
		examples.put("2020-11-19 13:04", "yyyy-MM-dd HH:mm");

		// NEW EXAMPLES
		examples.put("2020-11-19-05:00", "yyyy-MM-dd-HH:mm"); // NEW

		examples.put("19 Nov 2020 13:04", "dd MMM yyyy HH:mm");
		examples.put("19 November 2020 13:04", "dd MMMM yyyy HH:mm");
		examples.put("20201119130405", "yyyyMMddHHmmss");
		examples.put("20201119 130405", "yyyyMMdd HHmmss");
		examples.put("20201119 13:04:05", "yyyyMMdd HH:mm:ss");
		examples.put("19-11-2020 13:04:05", "dd-MM-yyyy HH:mm:ss");
		examples.put("2020-11-19 13:04:05", "yyyy-MM-dd HH:mm:ss");
		examples.put("2020:11:19 13:04:05", "yyyy:MM:dd HH:mm:ss");
		examples.put("19 Nov 2020 13:04:05", "dd MMM yyyy HH:mm:ss");
		examples.put("19 November 2020 13:04:05", "dd MMMM yyyy HH:mm:ss");
		examples.put("19 November 2020 13:04:05-111", "dd MMMM yyyy HH:mm:ss-SSS");
		examples.put("19 November 2020 13:04:05-1111", "dd MMMM yyyy HH:mm:ss-SSSS");

		// NEW EXAMPLE
		examples.put("Thu Nov 19 13:04 CET 2020", "EEE MMM dd HH:mm zzz yyyy"); // NEW

		examples.put("04-1986", "MM-yyyy");

		return examples;
	}

	public static void main(String[] args) {
		DateParser parser = new DateParser();
		parser.setPrintStackTrace(true);

		System.out.println("Testing DateParser with " + DATE_FORMAT_PATTERNS.size() + " patterns\n");

		Map<String, String> examples = getFormatExamples();
		int passed = 0;
		int failed = 0;

		for (Map.Entry<String, String> entry : examples.entrySet()) {
			String sample = entry.getKey();
			String expectedPattern = entry.getValue();

			Date result = parser.apply(sample);

			if (result != null) {
				System.out.println("PASS: " + sample + " -> " + result + " (pattern: " + expectedPattern + ")");
				passed++;
			} else {
				System.out.println("FAIL: " + sample + " (expected pattern: " + expectedPattern + ")");
				failed++;
			}
		}

		System.out.println("\n" + repeatChar('=', 70));
		System.out.println("Tests passed: " + passed);
		System.out.println("Tests failed: " + failed);
		System.out.println("Total: " + (passed + failed));
	}

	private static String repeatChar(char c, int count) {
		StringBuilder sb = new StringBuilder(count);
		for (int i = 0; i < count; i++)
			sb.append(c);
		return sb.toString();
	}
}
