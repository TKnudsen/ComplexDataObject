package com.github.TKnudsen.ComplexDataObject.model.tools;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

/**
 * Central utility for date formatting and parsing throughout the framework.
 *
 * <p>
 * All operations use a single explicit time zone ({@link #ZONE}) and immutable,
 * thread-safe {@link DateTimeFormatter} instances. This replaces the previous
 * approach of shared {@code SimpleDateFormat} fields, which were not
 * thread-safe and caused sporadic off-by-one date errors when implicit time
 * zone handling mixed UTC and local time.
 *
 * <p>
 * Two distinct formats are supported:
 * <ul>
 * <li><b>Date format</b> -- {@code yyyy-MM-dd} (ISO 8601). Used for all domain
 * data: currency rates, stock prices, time series keys. This is the canonical
 * data format for the entire framework. See {@link #pruneDate(Date)},
 * {@link #parse(String)}, {@link #formatDate(Date)}.</li>
 * <li><b>Datetime format</b> -- {@code yyyy-MM-dd HH-mm-ss-SSS}. Used
 * exclusively for file system artifacts such as export filenames and snapshot
 * folders. Dashes replace colons in the time part to produce strings that are
 * legal as filenames on all platforms including Windows. See
 * {@link #formatDateAndTime(Date)}, {@link #parseDateAndTime(String)}.</li>
 * </ul>
 *
 * <p>
 * <b>Timezone policy:</b> {@link #ZONE} is set to the JVM system default, which
 * is expected to be {@code Europe/Zurich} or {@code Europe/Berlin} in
 * production. Demo and test environments running in other timezones are
 * acceptable -- the system default will be used consistently throughout, so
 * behaviour remains internally coherent regardless of location. All methods in
 * this class use {@link #ZONE} explicitly -- it is never implicit.
 *
 * <p>
 * This class is not instantiable.
 */
public class DateFormatTools {

	/**
	 * The canonical time zone for all date operations in this framework.
	 *
	 * <p>
	 * Expected to be {@code Europe/Zurich} or {@code Europe/Berlin} in production.
	 * Uses the JVM system default so that demo and test environments in other time
	 * zones continue to work without configuration changes.
	 */
	public static final ZoneId ZONE = ZoneId.systemDefault();

	/**
	 * The canonical date formatter: {@code yyyy-MM-dd} (ISO 8601).
	 *
	 * <p>
	 * Immutable and thread-safe. This is the single definition of the date format
	 * string for the entire framework. All domain data (currency rates, stock
	 * prices, time series keys) uses this format.
	 */
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

	/**
	 * Formatter for file system date-time strings. Pattern:
	 * {@code yyyy-MM-dd HH-mm-ss-SSS}.
	 *
	 * <p>
	 * Dashes are used throughout -- including as time-part separators -- to
	 * produce strings that are legal as filenames on all platforms, including
	 * Windows where colons are not permitted in path components.
	 *
	 * <p>
	 * {@code SSS} = three-digit milliseconds (ISO 8601 standard precision).
	 *
	 * <p>
	 * {@link Locale#ROOT} is used to ensure machine-readable, locale-independent
	 * output regardless of the JVM locale setting.
	 *
	 * <p>
	 * Immutable and thread-safe.
	 */
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss-SSS",
			Locale.ROOT);

	private DateFormatTools() {
		// utility class
	}

	// -------------------------------------------------------------------------
	// Date to String (date format)
	// -------------------------------------------------------------------------

	/**
	 * Formats a {@link Date} to its canonical {@code yyyy-MM-dd} string
	 * representation, interpreted in {@link #ZONE}.
	 *
	 * <p>
	 * Within-day information is always truncated. This method never rounds up to
	 * the next day.
	 *
	 * @param date the date to format; returns {@code null} if {@code date} is null
	 * @return the formatted date string, e.g. {@code "2026-02-23"}
	 */
	public static String pruneDate(Date date) {
		if (date == null)
			return null;

		if (date instanceof java.sql.Date sqlDate)
			return sqlDate.toLocalDate().format(DATE_FORMATTER);

		return date.toInstant().atZone(ZONE).toLocalDate().format(DATE_FORMATTER);
	}

	/**
	 * Normalizes a date string to the canonical {@code yyyy-MM-dd} format.
	 *
	 * <p>
	 * Parses the input and reformats it, ensuring consistent output regardless of
	 * minor input variations (e.g. {@code "2026-2-3"} to {@code "2026-02-03"}).
	 *
	 * @param date a date string parseable as {@code yyyy-MM-dd}; must not be null
	 * @return the normalized date string
	 * @throws IllegalArgumentException if the input cannot be parsed
	 */
	public static String pruneDate(String date) {
		Objects.requireNonNull(date, "date must not be null");

		try {
			return LocalDate.parse(date, DATE_FORMATTER).format(DATE_FORMATTER);
		} catch (DateTimeParseException e) {
			throw new IllegalArgumentException("DateFormatTools: unparseable date string: '" + date + "'", e);
		}
	}

	/**
	 * Returns today's date as a {@code yyyy-MM-dd} string in {@link #ZONE}.
	 *
	 * @return today's date string, e.g. {@code "2026-02-24"}
	 */
	public static String today() {
		return LocalDate.now(ZONE).format(DATE_FORMATTER);
	}

	// -------------------------------------------------------------------------
	// String to Date (date format)
	// -------------------------------------------------------------------------

	/**
	 * Parses a {@code yyyy-MM-dd} string into a {@link Date} at midnight in
	 * {@link #ZONE}.
	 *
	 * @param dateString a date string in {@code yyyy-MM-dd} format; must not be
	 *                   null
	 * @return a {@link Date} representing midnight at the start of the given day
	 * @throws IllegalArgumentException if the string cannot be parsed
	 */
	public static Date parse(String dateString) {
		Objects.requireNonNull(dateString, "dateString must not be null");

		try {
			return Date.from(LocalDate.parse(dateString, DATE_FORMATTER).atStartOfDay(ZONE).toInstant());
		} catch (DateTimeParseException e) {
			throw new IllegalArgumentException("DateFormatTools: unparseable date string: '" + dateString + "'", e);
		}
	}

	// -------------------------------------------------------------------------
	// Date to Date (truncation)
	// -------------------------------------------------------------------------

	/**
	 * Truncates a {@link Date} to midnight at the start of its day in
	 * {@link #ZONE}.
	 *
	 * <p>
	 * Within-day information is always discarded -- this method never rounds up to
	 * the next day.
	 *
	 * @param date the date to truncate; returns {@code null} if {@code date} is
	 *             null
	 * @return a new {@link Date} at midnight on the same calendar day in
	 *         {@link #ZONE}
	 */
	public static Date formatDate(Date date) {
		if (date == null)
			return null;
		return Date.from(date.toInstant().atZone(ZONE).toLocalDate().atStartOfDay(ZONE).toInstant());
	}

	// -------------------------------------------------------------------------
	// Date to String (date-time format, file system use only)
	// -------------------------------------------------------------------------

	/**
	 * Formats a {@link Date} to a file-system-safe date-time string using
	 * {@link #ZONE}.
	 *
	 * <p>
	 * Pattern: {@code yyyy-MM-dd HH-mm-ss-SSS}. Intended for use in filenames and
	 * folder names only -- not for domain data. See class Javadoc for why dashes
	 * replace colons in the time part.
	 *
	 * @param date the date to format; must not be null
	 * @return the formatted datetime string, e.g. {@code "2026-02-24 13-45-07-123"}
	 */
	public static String formatDateAndTime(Date date) {
		Objects.requireNonNull(date, "date must not be null");
		return date.toInstant().atZone(ZONE).format(DATE_TIME_FORMATTER);
	}

	// -------------------------------------------------------------------------
	// String to Date (date-time format, file system use only)
	// -------------------------------------------------------------------------

	/**
	 * Parses a file-system date-time string back into a {@link Date}.
	 *
	 * <p>
	 * Pattern: {@code yyyy-MM-dd HH-mm-ss-SSS}. Intended for reading back strings
	 * previously written by {@link #formatDateAndTime(Date)}.
	 *
	 * @param dateString a datetime string matching the pattern; must not be null
	 * @return the parsed {@link Date}
	 * @throws IllegalArgumentException if the string cannot be parsed
	 */
	public static Date parseDateAndTime(String dateString) {
		Objects.requireNonNull(dateString, "dateString must not be null");
		try {
			return Date.from(LocalDateTime.parse(dateString, DATE_TIME_FORMATTER).atZone(ZONE).toInstant());
		} catch (DateTimeParseException e) {
			throw new IllegalArgumentException("DateFormatTools: unparseable datetime string: '" + dateString + "'", e);
		}
	}

}