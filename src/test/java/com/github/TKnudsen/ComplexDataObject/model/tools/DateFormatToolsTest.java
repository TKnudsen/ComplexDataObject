package com.github.TKnudsen.ComplexDataObject.model.tools;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.zone.ZoneOffsetTransition;
import java.time.zone.ZoneRules;
import java.util.Date;

import org.junit.Test;

/**
 * Tests {@link DateFormatTools#pruneDate(Date)} / {@link DateFormatTools#formatDate(Date)}
 * for correct day-truncation, including across DST transitions -- historically
 * the source of off-by-one date errors when an instant near midnight gets
 * interpreted in the wrong zone.
 *
 * <p>
 * {@link DateFormatTools#ZONE} is the JVM's system default zone, which varies
 * by machine (production is expected to run {@code Europe/Zurich}, but the
 * machine running this test may not). Every assertion here derives its
 * expected value from {@code DateFormatTools.ZONE} itself via {@code java.time}
 * rather than a hardcoded zone/date, so the test is meaningful -- and not
 * flaky -- regardless of where it runs. The DST-transition test is skipped
 * (not failed) on a zone with no transitions, e.g. UTC.
 */
public class DateFormatToolsTest {

	@Test
	public void pruneDate_isCorrectAtEveryHour_onARegularDay() {
		assertPruneDateMatchesLocalDateAtEveryHour(LocalDate.of(2026, 6, 15));
	}

	@Test
	public void pruneDate_isCorrectAtEveryHour_onTheZonesNextDstTransitionDay() {
		ZoneRules rules = DateFormatTools.ZONE.getRules();
		ZoneOffsetTransition transition = rules.nextTransition(Instant.parse("2026-01-01T00:00:00Z"));
		assumeTrue("zone " + DateFormatTools.ZONE + " has no DST transitions -- nothing to test here",
				transition != null);

		LocalDate transitionDay = transition.getDateTimeBefore().toLocalDate();
		assertPruneDateMatchesLocalDateAtEveryHour(transitionDay);
	}

	@Test
	public void pruneDate_flipsExactlyAtMidnight_notBeforeOrAfter() {
		LocalDate day = LocalDate.of(2026, 6, 15);

		ZonedDateTime justBeforeMidnight = day.atTime(23, 59).atZone(DateFormatTools.ZONE);
		ZonedDateTime midnight = day.plusDays(1).atStartOfDay(DateFormatTools.ZONE);

		assertThat(DateFormatTools.pruneDate(Date.from(justBeforeMidnight.toInstant()))).isEqualTo(day.toString());
		assertThat(DateFormatTools.pruneDate(Date.from(midnight.toInstant()))).isEqualTo(day.plusDays(1).toString());
	}

	@Test
	public void pruneDate_nullInput_returnsNull() {
		assertThat(DateFormatTools.pruneDate((Date) null)).isNull();
	}

	@Test
	public void formatDate_truncatesToMidnight_sameCalendarDayAsPruneDate() {
		ZonedDateTime zdt = LocalDate.of(2026, 6, 15).atTime(13, 45).atZone(DateFormatTools.ZONE);
		Date date = Date.from(zdt.toInstant());

		Date truncated = DateFormatTools.formatDate(date);

		assertThat(DateFormatTools.pruneDate(truncated)).isEqualTo(DateFormatTools.pruneDate(date));
		assertThat(truncated.toInstant().atZone(DateFormatTools.ZONE).toLocalTime()).isEqualTo(LocalTime.MIDNIGHT);
	}

	@Test
	public void formatDate_nullInput_returnsNull() {
		assertThat(DateFormatTools.formatDate(null)).isNull();
	}

	/**
	 * For every hour of {@code day} (interpreted in {@link DateFormatTools#ZONE}),
	 * asserts {@link DateFormatTools#pruneDate(Date)} returns exactly the
	 * {@link LocalDate} that instant falls on in that zone. On a DST-start hour
	 * that doesn't exist in the zone, {@code java.time} silently adjusts forward
	 * (e.g. two consecutive 03:00 instants) -- the assertion still holds because
	 * it's derived from the same adjusted {@code ZonedDateTime}, not a
	 * pre-computed expectation.
	 */
	private void assertPruneDateMatchesLocalDateAtEveryHour(LocalDate day) {
		for (int hour = 0; hour <= 23; hour++) {
			ZonedDateTime zdt = day.atTime(hour, 0).atZone(DateFormatTools.ZONE);
			Date date = Date.from(zdt.toInstant());

			assertThat(DateFormatTools.pruneDate(date)).as("hour %d of %s in zone %s", hour, day, DateFormatTools.ZONE)
					.isEqualTo(zdt.toLocalDate().toString());
		}
	}
}
