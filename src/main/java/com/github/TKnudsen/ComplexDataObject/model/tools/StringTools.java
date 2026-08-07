package com.github.TKnudsen.ComplexDataObject.model.tools;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Static helpers for common {@link String} operations.
 *
 * <p>
 * This class is intentionally named {@code StringTools} rather than
 * {@code StringUtils} to avoid import ambiguity with
 * {@link org.apache.commons.lang3.StringUtils}.
 * </p>
 *
 * @version 2.0 revised March 2026
 * @since 2017
 */
public final class StringTools {

	private StringTools() {
	}

	// ==================== SIMILARITY ====================

	/**
	 * Computes a directional substring-window hit ratio from {@code query} to
	 * {@code target}.
	 *
	 * <p>
	 * Slides a window of {@code length} characters across {@code query} and counts
	 * how many windows occur anywhere in {@code target}. The result is the ratio of
	 * matching windows to total windows.
	 * </p>
	 *
	 * <p>
	 * This measure is asymmetric: {@code substringSimilarity(a, b, n)} is not
	 * necessarily equal to {@code substringSimilarity(b, a, n)}. Repeated windows
	 * in {@code query} are counted repeatedly.
	 * </p>
	 *
	 * @param query  the string whose windows are tested; null or empty returns 0.0
	 * @param target the string to search within; null or empty returns 0.0
	 * @param length the sliding-window length; must be positive
	 * @return similarity in [0.0, 1.0], or 0.0 for invalid input
	 */
	public static double substringSimilarity(String query, String target, int length) {
		if (query == null || target == null || query.isEmpty() || target.isEmpty() || length <= 0)
			return 0.0;

		int kernel = Math.min(length, Math.min(query.length(), target.length()));
		int totalWindows = query.length() - kernel + 1;

		int matches = 0;
		for (int i = 0; i <= query.length() - kernel; i++) {
			String window = query.substring(i, i + kernel);
			if (target.indexOf(window) >= 0)
				matches++;
		}

		return totalWindows > 0 ? (double) matches / totalWindows : 0.0;
	}

	/**
	 * @deprecated Use {@link #substringSimilarity(String, String, int)} instead.
	 */
	@Deprecated
	public static double subStringSimilarity(String query, String target, int length) {
		return substringSimilarity(query, target, length);
	}

	// ==================== COUNTING ====================

	/**
	 * Counts the number of occurrences of {@code sub} within {@code s}.
	 *
	 * <p>
	 * If {@code allowOverlaps} is {@code false}, non-overlapping matches are
	 * counted and the search advances by {@code sub.length()} after each match. If
	 * {@code allowOverlaps} is {@code true}, the search advances by 1 after each
	 * match, allowing overlapping matches to be counted.
	 * </p>
	 *
	 * <p>
	 * For {@code sub.length() == 1}, overlap is irrelevant and a direct character
	 * scan is used.
	 * </p>
	 *
	 * <p>
	 * For the non-overlapping case,
	 * {@link org.apache.commons.lang3.StringUtils#countMatches(String, String)} is
	 * equivalent.
	 * </p>
	 *
	 * @param s             the string to search in; null returns 0
	 * @param sub           the substring to search for; null or empty returns 0
	 * @param allowOverlaps whether overlapping matches should be counted
	 * @return the number of occurrences
	 */
	public static int countSubstring(String s, String sub, boolean allowOverlaps) {
		if (s == null || sub == null || sub.isEmpty())
			return 0;

		final int n = s.length();
		final int m = sub.length();
		if (m > n)
			return 0;

		if (m == 1) {
			final char c = sub.charAt(0);
			int count = 0;
			for (int i = 0; i < n; i++)
				if (s.charAt(i) == c)
					count++;
			return count;
		}

		int count = 0;
		int idx = 0;
		final int step = allowOverlaps ? 1 : m;

		while ((idx = s.indexOf(sub, idx)) >= 0) {
			count++;
			idx += step;
		}

		return count;
	}

	// ==================== TOKENIZING ====================

	/**
	 * Splits {@code text} by the given literal {@code separator} and returns all
	 * tokens, including empty ones between consecutive separators.
	 *
	 * <p>
	 * Unlike {@link String#split(String)}, the separator is treated as a literal
	 * string, not a regular expression.
	 * </p>
	 *
	 * <p>
	 * Examples: {@code tokenize("a,,b", ",")} returns {@code ["a", "", "b"]}.
	 * {@code tokenize("abc", ",")} returns {@code ["abc"]}.
	 * </p>
	 *
	 * @param text      the string to split; null returns an empty list
	 * @param separator the literal separator; must not be null or empty
	 * @return list of tokens, never null
	 * @throws NullPointerException     if {@code separator} is null
	 * @throws IllegalArgumentException if {@code separator} is empty
	 */
	public static List<String> tokenize(String text, String separator) {
		Objects.requireNonNull(separator, "separator must not be null");
		if (separator.isEmpty())
			throw new IllegalArgumentException("StringTools.tokenize: separator must not be empty");

		List<String> tokens = new ArrayList<>();
		if (text == null)
			return tokens;

		int start = 0;
		int index;
		while ((index = text.indexOf(separator, start)) >= 0) {
			tokens.add(text.substring(start, index));
			start = index + separator.length();
		}
		tokens.add(text.substring(start));
		return tokens;
	}

	// ==================== FORMATTING ====================

	/**
	 * Formats a double value to the given number of decimal places by truncating
	 * toward zero.
	 *
	 * <p>
	 * This method does not round. It uses {@link BigDecimal}, so scientific
	 * notation is handled correctly.
	 * </p>
	 *
	 * <p>
	 * Truncation uses {@link RoundingMode#DOWN}, which truncates toward zero for
	 * both positive and negative values. For example, {@code -1.29} with 1 decimal
	 * becomes {@code -1.2}.
	 * </p>
	 *
	 * @param value    the value to format
	 * @param decimals the number of decimal places; must not be negative
	 * @return plain decimal string representation, truncated toward zero
	 * @throws IllegalArgumentException if {@code decimals} is negative
	 */
	public static String truncateDouble(double value, int decimals) {
		if (decimals < 0)
			throw new IllegalArgumentException(
					"StringTools.truncateDouble: decimals must not be negative, got " + decimals);

		return BigDecimal.valueOf(value).setScale(decimals, RoundingMode.DOWN).toPlainString();
	}

	/**
	 * Right-pads or truncates a string to exactly {@code width} characters.
	 *
	 * <p>
	 * {@code null} is treated as {@code "null"}. If {@code width <= 0}, an empty
	 * string is returned. If the input is longer than {@code width}, it is
	 * truncated.
	 * </p>
	 *
	 * @param s     the input string; null is accepted
	 * @param width the desired character width
	 * @return string of exactly {@code width} characters, or {@code ""} if
	 *         {@code width <= 0}
	 */
	public static String padRight(String s, int width) {
		if (width <= 0)
			return "";

		String value = s == null ? "null" : s;
		int len = value.length();

		if (len == width)
			return value;
		if (len > width)
			return value.substring(0, width);

		StringBuilder b = new StringBuilder(width);
		b.append(value);
		for (int i = len; i < width; i++)
			b.append(' ');
		return b.toString();
	}

	/**
	 * Left-pads or truncates a string to exactly {@code width} characters.
	 *
	 * <p>
	 * See {@link #padRight(String, int)} for the full behavior.
	 * </p>
	 *
	 * @param s     the input string; null is accepted
	 * @param width the desired character width
	 * @return string of exactly {@code width} characters, or {@code ""} if
	 *         {@code width <= 0}
	 */
	public static String padLeft(String s, int width) {
		if (width <= 0)
			return "";

		String value = s == null ? "null" : s;
		int len = value.length();

		if (len == width)
			return value;
		if (len > width)
			return value.substring(0, width);

		StringBuilder b = new StringBuilder(width);
		for (int i = len; i < width; i++)
			b.append(' ');
		b.append(value);
		return b.toString();
	}

	/**
	 * Returns a string consisting of {@code count} repetitions of {@code c}.
	 *
	 * @param c     the character to repeat
	 * @param count the number of repetitions; zero or negative returns {@code ""}
	 * @return repeated-character string
	 */
	public static String repeatChar(char c, int count) {
		if (count <= 0)
			return "";
		char[] arr = new char[count];
		Arrays.fill(arr, c);
		return new String(arr);
	}

	// ==================== CLEANING ====================

	/**
	 * Replaces non-ASCII and control characters in the given text.
	 *
	 * <p>
	 * Three passes are applied in order:
	 * </p>
	 * <ol>
	 * <li>All non-ASCII characters ({@code [^\x00-\x7F]})</li>
	 * <li>ASCII control characters except CR, LF, and tab</li>
	 * <li>Unicode category {@code \p{C}}</li>
	 * </ol>
	 *
	 * <p>
	 * This means that printable non-ASCII characters are also replaced. For
	 * example, accented letters, Greek letters, and emoji are removed too. The
	 * final result is trimmed.
	 * </p>
	 *
	 * @param text      the text to clean; must not be null
	 * @param replaceBy replacement string; must not be null
	 * @return cleaned and trimmed text
	 * @throws NullPointerException if {@code text} or {@code replaceBy} is null
	 */
	public static String stripNonAsciiAndControlChars(String text, String replaceBy) {
		Objects.requireNonNull(text, "text must not be null");
		Objects.requireNonNull(replaceBy, "replaceBy must not be null");

		text = text.replaceAll("[^\\x00-\\x7F]", replaceBy);
		text = text.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", replaceBy);
		text = text.replaceAll("\\p{C}", replaceBy);
		return text.trim();
	}

	/**
	 * @deprecated Use {@link #stripNonAsciiAndControlChars(String, String)}
	 *             instead.
	 */
	@Deprecated
	public static String cleanNonASCIIContent(String text, String replaceBy) {
		return stripNonAsciiAndControlChars(text, replaceBy);
	}

	/**
	 * 
	 */
	public static String stackTraceToString(Throwable e) {
		if (e == null)
			return "";
		return org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(e);
	}
}