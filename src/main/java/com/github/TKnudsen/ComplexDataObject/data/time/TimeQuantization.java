package com.github.TKnudsen.ComplexDataObject.data.time;

import java.io.Serializable;

/**
 * <p>
 * Represents the different granularities of time, per Aigner et al.
 * </p>
 *
 * @version 1.11
 * @since 2011
 */
public enum TimeQuantization implements Serializable {

	MILLISECONDS("milliseconds"), SECONDS("seconds"), MINUTES("minutes"), HOURS("hours"), DAYS("days"), WEEKS("weeks"), MONTHS("months"), QUARTER("quarter"), YEARS("years"), DECADES("decades");

	private String name;

	private TimeQuantization(String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}
}