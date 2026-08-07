package com.github.TKnudsen.ComplexDataObject.model.tools;

/**
 * Compatibility wrapper for the renamed {@link StringTools} class.
 *
 * <p>
 * This class existed before {@link StringTools} was introduced. All methods
 * delegate directly to {@link StringTools}. New code should use
 * {@link StringTools} directly.
 * </p>
 *
 * @deprecated Use {@link StringTools} instead. This wrapper will be removed in
 *             a future major release.
 */
@Deprecated
public final class StringUtils {

	private StringUtils() {
	}

	/**
	 * @deprecated Use {@link StringTools#substringSimilarity(String, String, int)}
	 *             instead.
	 */
	@Deprecated
	public static double subStringSimilarity(String query, String target, int length) {
		return StringTools.substringSimilarity(query, target, length);
	}

	/**
	 * @deprecated Use {@link StringTools#countSubstring(String, String, boolean)}
	 *             instead.
	 */
	@Deprecated
	public static int countSubstring(String s, String sub, boolean allowOverlaps) {
		return StringTools.countSubstring(s, sub, allowOverlaps);
	}

	/** @deprecated Use {@link StringTools#tokenize(String, String)} instead. */
	@Deprecated
	public static java.util.List<String> tokenize(String text, String separator) {
		return StringTools.tokenize(text, separator);
	}

	/** @deprecated Use {@link StringTools#truncateDouble(double, int)} instead. */
	@Deprecated
	public static String truncateDouble(double value, int decimals) {
		return StringTools.truncateDouble(value, decimals);
	}

	/** @deprecated Use {@link StringTools#padRight(String, int)} instead. */
	@Deprecated
	public static String padRight(String s, int width) {
		return StringTools.padRight(s, width);
	}

	/** @deprecated Use {@link StringTools#padLeft(String, int)} instead. */
	@Deprecated
	public static String padLeft(String s, int width) {
		return StringTools.padLeft(s, width);
	}

	/** @deprecated Use {@link StringTools#repeatChar(char, int)} instead. */
	@Deprecated
	public static String repeatChar(char c, int count) {
		return StringTools.repeatChar(c, count);
	}

	/**
	 * @deprecated Use
	 *             {@link StringTools#stripNonAsciiAndControlChars(String, String)}
	 *             instead.
	 */
	@Deprecated
	public static String cleanNonASCIIContent(String text, String replaceBy) {
		return StringTools.stripNonAsciiAndControlChars(text, replaceBy);
	}

	/**
	 * @deprecated Use
	 *             {@link org.apache.commons.lang3.exception.ExceptionUtils#getStackTrace(Throwable)}
	 *             instead.
	 */
	@Deprecated
	public static String stackTraceToString(Throwable e) {
		if (e == null)
			return "";
		return org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(e);
	}
}