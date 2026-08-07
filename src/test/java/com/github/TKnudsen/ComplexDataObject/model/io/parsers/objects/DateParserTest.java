package com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

/**
 * <p>
 * Tests {@link DateParser} across its supported date/datetime formats,
 * separators, timezone offsets, invalid input, and concurrent use (the
 * underlying {@code DateFormat}s are cached per-thread, so cross-thread
 * corruption is a real regression risk worth covering).
 * </p>
 */
public class DateParserTest {

	private final DateParser parser = new DateParser();

	@Test
	public void parsesBasicInputs() {
		assertThat(parser.apply(null)).isNull();

		Date now = new Date();
		assertThat(parser.apply(now)).isEqualTo(now);

		long timestamp = 1605787445000L; // 2020-11-19 13:04:05
		assertThat(parser.apply(timestamp)).isNotNull();
	}

	@Test
	public void parsesDateOnlyFormats() {
		assertDateParsed("20201119", 2020, 11, 19);
		assertDateParsed("19-11-2020", 2020, 11, 19);
		assertDateParsed("2020-11-19", 2020, 11, 19);
		assertDateParsed("2020-11", 2020, 11, 1);

		assertThat(parser.apply("19 Nov 2020")).isNotNull();
		assertThat(parser.apply("19 November 2020")).isNotNull();
	}

	@Test
	public void parsesDateTimeFormats() {
		assertDateTimeParsed("202011191304", 2020, 11, 19, 13, 4);
		assertDateTimeParsed("20201119 1304", 2020, 11, 19, 13, 4);
		assertDateTimeParsed("19-11-2020 13:04", 2020, 11, 19, 13, 4);
		assertDateTimeParsed("2020-11-19 13:04", 2020, 11, 19, 13, 4);

		assertThat(parser.apply("19 Nov 2020 13:04")).isNotNull();
		assertThat(parser.apply("19 November 2020 13:04")).isNotNull();
	}

	@Test
	public void parsesDateTimeSecondsFormats() {
		assertDateTimeSecondsParsed("20201119130405", 2020, 11, 19, 13, 4, 5);
		assertDateTimeSecondsParsed("20201119 130405", 2020, 11, 19, 13, 4, 5);
		assertDateTimeSecondsParsed("20201119 13:04:05", 2020, 11, 19, 13, 4, 5);
		assertDateTimeSecondsParsed("19-11-2020 13:04:05", 2020, 11, 19, 13, 4, 5);
		assertDateTimeSecondsParsed("2020-11-19 13:04:05", 2020, 11, 19, 13, 4, 5);
		assertDateTimeSecondsParsed("2020:11:19 13:04:05", 2020, 11, 19, 13, 4, 5);

		assertThat(parser.apply("19 Nov 2020 13:04:05")).isNotNull();
		assertThat(parser.apply("19 November 2020 13:04:05")).isNotNull();
	}

	@Test
	public void parsesMonthYearFormats() {
		assertDateParsed("04-1986", 1986, 4, 1);
		assertDateParsed("1986-04", 1986, 4, 1);
	}

	@Test
	public void acceptsDifferentSeparators() {
		assertDateParsed("19-11-2020", 2020, 11, 19);
		assertDateParsed("19.11.2020", 2020, 11, 19);
		assertDateParsed("19_11_2020", 2020, 11, 19);
		assertDateParsed("19/11/2020", 2020, 11, 19);
		assertDateParsed("19\\11\\2020", 2020, 11, 19);
	}

	@Test
	public void parsesIsoFormats() {
		assertDateParsed("2020-11-19", 2020, 11, 19);
		assertDateTimeParsed("2020-11-19 13:04", 2020, 11, 19, 13, 4);
		assertDateTimeSecondsParsed("2020-11-19 13:04:05", 2020, 11, 19, 13, 4, 5);
		assertThat(parser.apply("2020-11-19 13:04:05:123")).isNotNull();
		assertDateTimeParsed("2020-11-19T13:04", 2020, 11, 19, 13, 4);
	}

	@Test
	public void parsesEuropeanFormats() {
		assertDateParsed("19-11-2020", 2020, 11, 19);
		assertDateParsed("19.11.2020", 2020, 11, 19);
		assertDateParsed("19/11/2020", 2020, 11, 19);
	}

	@Test
	public void parsesJavaDateToStringFormat() {
		assertThat(parser.apply("Thu Nov 19 13:04:05 CET 2020")).isNotNull();
		assertThat(parser.apply("Thu Nov 19 13:04 CET 2020")).isNotNull();
	}

	@Test
	public void handlesEdgeCaseDates() {
		assertDateParsed("2020-02-29", 2020, 2, 29); // leap year
		assertDateParsed("2020-01-01", 2020, 1, 1);
		assertDateParsed("2020-12-31", 2020, 12, 31);
		assertDateParsed("2020-1-1", 2020, 1, 1); // single digit day/month
		assertDateParsed("1900-01-01", 1900, 1, 1);
		assertDateParsed("2100-12-31", 2100, 12, 31);
		assertDateParsed("  2020-11-19  ", 2020, 11, 19); // whitespace
	}

	@Test
	public void returnsNullForInvalidInput() {
		assertThat(parser.apply("")).isNull();
		assertThat(parser.apply("   ")).isNull();
		assertThat(parser.apply("not a date")).isNull();
		assertThat(parser.apply("2020-13-45")).isNull(); // month 13, day 45
		assertThat(parser.apply("19/19/2020")).isNull();
		assertThat(parser.apply("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).isNull();
		assertThat(parser.apply("@#$%^&*()")).isNull();
	}

	@Test
	public void handlesTimezoneOffsets() {
		assertDateParsed("2020-11-19+01:00", 2020, 11, 19);
		assertDateParsed("2020-11-19+0100", 2020, 11, 19);
		assertDateParsed("2020-11-19-05:00", 2020, 11, 19);
		assertDateTimeParsed("2020-11-19 13:04+01:00", 2020, 11, 19, 13, 4);
	}

	@Test
	public void isThreadSafeUnderConcurrentUse() throws InterruptedException {
		int threadCount = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
		int iterationsPerThread = 100;
		String testDate = "2020-11-19 13:04:05";

		AtomicInteger failures = new AtomicInteger();
		Thread[] threads = new Thread[threadCount];

		for (int i = 0; i < threadCount; i++) {
			threads[i] = new Thread(() -> {
				for (int j = 0; j < iterationsPerThread; j++)
					if (parser.apply(testDate) == null)
						failures.incrementAndGet();
			});
			threads[i].start();
		}

		for (Thread thread : threads)
			thread.join();

		assertThat(failures.get()).as("%d threads x %d iterations", threadCount, iterationsPerThread).isZero();
	}

	@Test
	public void utilityMethodsBehaveAsDocumented() {
		assertThat(parser.canParse("2020-11-19")).isTrue();
		assertThat(parser.canParse("not a date")).isFalse();
		assertThat(parser.canParse(null)).isFalse();

		assertThat(DateParser.determineDateFormat("20201119")).isEqualTo("yyyyMMdd");
		assertThat(DateParser.determineDateFormat("2020-11-19")).isEqualTo("yyyy-MM-dd");
		assertThat(DateParser.determineDateFormat("not a date")).isNull();

		assertThat(DateParser.getSupportedFormats()).isNotEmpty();
		assertThat(DateParser.getFormatExamples()).isNotEmpty();

		assertThat(parser.toString()).contains("DateParser");
		assertThat(parser.getOutputClassType()).isEqualTo(Date.class);
	}

	// ==================== HELPERS ====================

	private void assertDateParsed(String input, int year, int month, int day) {
		Date result = parser.apply(input);
		assertThat(result).as("parsing '%s'", input).isNotNull();

		Calendar cal = Calendar.getInstance();
		cal.setTime(result);

		assertThat(cal.get(Calendar.YEAR)).as("year of '%s'", input).isEqualTo(year);
		assertThat(cal.get(Calendar.MONTH)).as("month of '%s'", input).isEqualTo(month - 1);
		assertThat(cal.get(Calendar.DAY_OF_MONTH)).as("day of '%s'", input).isEqualTo(day);
	}

	private void assertDateTimeParsed(String input, int year, int month, int day, int hour, int minute) {
		Date result = parser.apply(input);
		assertThat(result).as("parsing '%s'", input).isNotNull();

		Calendar cal = Calendar.getInstance();
		cal.setTime(result);

		assertThat(cal.get(Calendar.YEAR)).as("year of '%s'", input).isEqualTo(year);
		assertThat(cal.get(Calendar.MONTH)).as("month of '%s'", input).isEqualTo(month - 1);
		assertThat(cal.get(Calendar.DAY_OF_MONTH)).as("day of '%s'", input).isEqualTo(day);
		assertThat(cal.get(Calendar.HOUR_OF_DAY)).as("hour of '%s'", input).isEqualTo(hour);
		assertThat(cal.get(Calendar.MINUTE)).as("minute of '%s'", input).isEqualTo(minute);
	}

	private void assertDateTimeSecondsParsed(String input, int year, int month, int day, int hour, int minute,
			int second) {
		Date result = parser.apply(input);
		assertThat(result).as("parsing '%s'", input).isNotNull();

		Calendar cal = Calendar.getInstance();
		cal.setTime(result);

		assertThat(cal.get(Calendar.YEAR)).as("year of '%s'", input).isEqualTo(year);
		assertThat(cal.get(Calendar.MONTH)).as("month of '%s'", input).isEqualTo(month - 1);
		assertThat(cal.get(Calendar.DAY_OF_MONTH)).as("day of '%s'", input).isEqualTo(day);
		assertThat(cal.get(Calendar.HOUR_OF_DAY)).as("hour of '%s'", input).isEqualTo(hour);
		assertThat(cal.get(Calendar.MINUTE)).as("minute of '%s'", input).isEqualTo(minute);
		assertThat(cal.get(Calendar.SECOND)).as("second of '%s'", input).isEqualTo(second);
	}
}
