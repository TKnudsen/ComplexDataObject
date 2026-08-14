package com.github.TKnudsen.ComplexDataObject.model.tools;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.data.time.TimeDuration;
import com.github.TKnudsen.ComplexDataObject.data.time.TimeQuantization;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.Parsers;

/**
 * <p>
 * Little helpers for dealing with {@link Date} objects.
 * </p>
 *
 * @version 1.02
 * @since 2016
 */
public class DateTools {

	/**
	 * a standard year, not a leap year
	 */
	public static long YEAR_IN_MILLISECONDS = new TimeDuration(TimeQuantization.YEARS, 1).getDuration();

	public static long YEAR_IN_MILLISECONDS_EXACT = 31556952000L;

	/**
	 * Retrieves whether or not a date is within a leap year.
	 * 
	 * @param y y
	 * @return boolean
	 */
	public static boolean isLeapYear(int y) {
		return ((y % 4 == 0) && ((y % 100 != 0) || (y % 400 == 0)));
	}

	/**
	 * whether a date is on weekend.
	 * 
	 * @param date date
	 * @return boolean
	 */
	public static boolean isWeekend(Date date) {
		Objects.requireNonNull(date);

		LocalDate ld = new java.sql.Date(date.getTime()).toLocalDate();

		DayOfWeek dayOfWeek = DayOfWeek.of(ld.get(ChronoField.DAY_OF_WEEK));
		switch (dayOfWeek) {
		case SATURDAY:
			return true;
		case SUNDAY:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Subtracts by day, to the most recent weekday on or before the given date.
	 * Does not truncate to midnight in {@link #ZONE}, use
	 * DateFormatTools.formatDate(result) to do so.
	 *
	 * <p>
	 * Intended to resolve the last day on which the stock market was open.
	 *
	 * @param date the reference date; must not be null
	 * @return the most recent weekday
	 */
	public static Date lastWeekDay(Date date) {
		Objects.requireNonNull(date, "date must not be null");

		Date result = new Date(date.getTime());
		while (DateTools.isWeekend(result))
			result = DateTools.addDateOrTime(result, Calendar.DATE, -1);
		return result;
	}

	/**
	 * Returns the number of days in the given month of the given year, correctly
	 * accounting for leap years.
	 *
	 * @param year  the full calendar year (e.g. {@code 2024})
	 * @param month 0-based month index (0 = January … 11 = December)
	 * @return the number of days in that month, between 28 and 31 inclusive
	 * @throws IllegalArgumentException if {@code month} is outside [0, 11]
	 */
	public static int lastDayOfMonth(int year, int month) {
		if (month < 0 || month > 11)
			throw new IllegalArgumentException("month must be in [0, 11], got: " + month);

		// YearMonth handles leap-year-aware February automatically
		return java.time.YearMonth.of(year, month + 1).lengthOfMonth();
	}

	/**
	 * Creates a Date object with the given arguments. Please consider argument
	 * constraints.
	 * 
	 * @param year  the actual year. no -1900 needed.
	 * @param month from 0-11
	 * @param day   from 1-31
	 * @return date
	 */
	public static Date createDate(int year, int month, int day) {
		return createDate(year, month, day, 0, 0, 0, 0);
	}

	/**
	 * Creates a Date object with the given arguments. Please consider argument
	 * constraints.
	 * 
	 * @param year        the actual year. no -1900 needed.
	 * @param month       from 0-11
	 * @param day         from 1-31
	 * @param hour        from 0-23
	 * @param minute      from 0-59
	 * @param second      from 0-59
	 * @param milliSecond milliSecond
	 * @return date
	 */
	public static Date createDate(int year, int month, int day, int hour, int minute, int second, int milliSecond) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.DATE, day);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, second);
		cal.set(Calendar.MILLISECOND, milliSecond);

		return cal.getTime();
	}

	/**
	 * 
	 * @param date     date
	 * @param calendar with respect to the Calendar Enum. Example: Calendar.DATE
	 *                 refers to the date within the month[1-31]. Example:
	 *                 DateTools.addDateOrTime(new Date(), Calendar.DATE, -10);
	 * @param value    number of days +-
	 * @return new adapted date
	 */
	public static Date addDateOrTime(Date date, int calendar, int value) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(calendar, value);
		return cal.getTime();
	}

	/**
	 * Rounds down a date object with respect to a given time quantization.
	 * Attention: weeks is an anomaly in the calendar an is not supported.
	 * 
	 * @param date             date
	 * @param timeQuantization quant
	 * @return dateo
	 */
	public static Date roundDown(Date date, TimeQuantization timeQuantization) {
		if (timeQuantization.equals(TimeQuantization.WEEKS))
			throw new IllegalArgumentException("rounding weeks not supported");

		Calendar val = Calendar.getInstance();
		val.setTime(date);

		val.set(Calendar.MILLISECOND, 0);
		if (timeQuantization.equals(TimeQuantization.MILLISECONDS))
			return val.getTime();

		if (timeQuantization.equals(TimeQuantization.SECONDS))
			return val.getTime();

		val.set(Calendar.SECOND, 0);

		if (timeQuantization.equals(TimeQuantization.MINUTES))
			return val.getTime();

		val.set(Calendar.MINUTE, 0);

		if (timeQuantization.equals(TimeQuantization.HOURS))
			return val.getTime();

		val.set(Calendar.HOUR, 0);

		if (timeQuantization.equals(TimeQuantization.DAYS))
			return val.getTime();

		val.set(Calendar.DAY_OF_MONTH, 1);

		if (timeQuantization.equals(TimeQuantization.MONTHS))
			return val.getTime();

		val.set(Calendar.MONTH, 0);

		if (timeQuantization.equals(TimeQuantization.YEARS))
			return val.getTime();

		return val.getTime();
	}

	/**
	 * Rounds up a date object with respect to a given time quantization. Attention:
	 * weeks is an anomaly in the calendar an is not supported.
	 * 
	 * @param date             date
	 * @param timeQuantization quant
	 * @return date
	 */
	public static Date roundUp(Date date, TimeQuantization timeQuantization) {
		if (timeQuantization.equals(TimeQuantization.WEEKS))
			throw new IllegalArgumentException("rounding weeks not supported");

		Calendar val = Calendar.getInstance();
		val.setTime(roundDown(date, timeQuantization));

		if (timeQuantization.equals(TimeQuantization.MILLISECONDS))
			val.set(Calendar.MILLISECOND, val.get(Calendar.MILLISECOND) + 1);
		else if (timeQuantization.equals(TimeQuantization.SECONDS))
			val.set(Calendar.SECOND, val.get(Calendar.SECOND + 1) + 1);
		else if (timeQuantization.equals(TimeQuantization.MINUTES))
			val.set(Calendar.MINUTE, val.get(Calendar.MINUTE) + 1);
		else if (timeQuantization.equals(TimeQuantization.HOURS))
			val.set(Calendar.HOUR, val.get(Calendar.HOUR) + 1);
		else if (timeQuantization.equals(TimeQuantization.DAYS))
			val.set(Calendar.DAY_OF_MONTH, val.get(Calendar.DAY_OF_MONTH) + 1);
		else if (timeQuantization.equals(TimeQuantization.MONTHS))
			val.set(Calendar.MONTH, val.get(Calendar.MONTH) + 1);
		else if (timeQuantization.equals(TimeQuantization.YEARS))
			val.set(Calendar.YEAR, val.get(Calendar.YEAR) + 1);

		return val.getTime();
	}

	/**
	 * Retrieves the age of a date.
	 * 
	 * @param birthday birthday
	 * @param today    today
	 * @return int
	 */
	public static int getAge(Date birthday, Date today) {
		GregorianCalendar birthd = new GregorianCalendar();
		birthd.setTime(birthday);

		GregorianCalendar today_ = new GregorianCalendar();
		today_.setTime(today);

		int year = today_.get(Calendar.YEAR) - birthd.get(Calendar.YEAR);

		if (today_.get(Calendar.MONTH) < birthd.get(Calendar.MONTH)) {
			year -= 1;
		} else if (today_.get(Calendar.MONTH) == birthd.get(Calendar.MONTH)) {
			if (today_.get(Calendar.DATE) < birthd.get(Calendar.DATE)) {
				year -= 1;
			}
		}

		if (year < 0)
			throw new IllegalArgumentException("invalid age: " + year);

		return year;
	}

	/**
	 * 
	 * @return year
	 */
	public static int getCurrentYear() {
		return Calendar.getInstance().get(Calendar.YEAR);
	}

	public static int getYear(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.get(Calendar.YEAR);
	}

	/**
	 * calculates the absolute difference between two dates. The time quantization
	 * can be chosen freely.
	 * 
	 * @param date1    date
	 * @param date2    date
	 * @param timeUnit time unit
	 * @return long
	 */
	public static long diff(Date date1, Date date2, TimeUnit timeUnit) {
		return diff(date1, date2, timeUnit, true);
	}

	/**
	 * Calculates the difference between two dates by subtracting the second from
	 * the first. In other words, if the second is is more recent, the result is
	 * negative.
	 * 
	 * It is also possible to receive the absolute value.
	 * 
	 * @param date1
	 * @param date2
	 * @param timeUnit
	 * @param abs
	 * @return
	 */
	public static long diff(Date date1, Date date2, TimeUnit timeUnit, boolean abs) {
		long diff = date1.getTime() - date2.getTime();

		long convert = timeUnit.convert(Math.abs(diff), TimeUnit.MILLISECONDS);

		return diff > 0 ? convert : -convert;
	}

	/**
	 * returns the day of the year.
	 * 
	 * @param date date
	 * @return day
	 */
	public static int dayOfTheYear(Date date) {
		Objects.requireNonNull(date);

		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		return cal.get(Calendar.DAY_OF_YEAR);
	}

	/**
	 * Extract the most recent Parse Date from ComplexDataObjects. Uses the given
	 * attribute as identifier/key for the date attribute.
	 */
	public static Date mostRecentDate(Iterable<ComplexDataObject> stored, String dateAttribute) {
		ComplexDataObject cdo = mostRecentObject(stored, dateAttribute);

		if (cdo != null)
			try {
				return Parsers.parseDate(cdo.getAttribute(dateAttribute));
			} catch (Exception ignored) {
				// ignore invalid parse dates
			}

		return null;
	}

	/**
	 * Extract the most recent ComplexDataObject from ComplexDataObjects. Uses the
	 * given attribute as identifier/key for the date attribute.
	 */
	public static ComplexDataObject mostRecentObject(Iterable<ComplexDataObject> stored, String dateAttribute) {
		Date best = null;
		ComplexDataObject ret = null;

		for (ComplexDataObject cdo : stored) {
			if (cdo == null)
				continue;

			Object parseDateObj = cdo.getAttribute(dateAttribute);
			if (parseDateObj == null)
				continue;

			Date d = null;
			try {
				d = Parsers.parseDate(parseDateObj);
			} catch (Exception ignored) {
				// ignore invalid parse dates
			}

			if (d == null)
				continue;

			if (best == null || d.getTime() > best.getTime()) {
				best = d;
				ret = cdo;
			}
		}

		return ret;
	}
}
