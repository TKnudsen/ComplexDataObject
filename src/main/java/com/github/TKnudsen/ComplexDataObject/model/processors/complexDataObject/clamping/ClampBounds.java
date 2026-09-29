package com.github.TKnudsen.ComplexDataObject.model.processors.complexDataObject.clamping;

import java.time.LocalDate;

/**
 * <p>
 * A pair of percentile bounds, in {@code [0, 100]}, used to clamp an
 * attribute's outliers: values below the {@code lowerPercentile} are raised
 * to that percentile's value, values above the {@code upperPercentile} are
 * lowered to it.
 * </p>
 *
 * <p>
 * Deliberately mutable with a no-arg constructor for straightforward JSON
 * (de)serialization; treat instances as immutable in application code and
 * use {@link #of(double, double)} to create one.
 * </p>
 *
 * @version 1.1
 * @since 2026
 */
public class ClampBounds {

	private double lowerPercentile;
	private double upperPercentile;

	// the date these bounds were last set, in ISO-8601 (e.g. "2026-09-29") --
	// lets a human curating the configuration see at a glance which entries
	// are old enough that the underlying distribution may have drifted and
	// the bounds may need revisiting; absent (null) on entries written before
	// this field existed
	private String lastUpdated;

	/** For JSON deserialization only; prefer {@link #of(double, double)}. */
	public ClampBounds() {
	}

	private ClampBounds(double lowerPercentile, double upperPercentile) {
		this.lowerPercentile = lowerPercentile;
		this.upperPercentile = upperPercentile;
		this.lastUpdated = LocalDate.now().toString();
	}

	public static ClampBounds of(double lowerPercentile, double upperPercentile) {
		if (lowerPercentile < 0 || lowerPercentile > 100 || upperPercentile < 0 || upperPercentile > 100)
			throw new IllegalArgumentException("ClampBounds: percentiles must be within [0, 100]");
		if (lowerPercentile >= upperPercentile)
			throw new IllegalArgumentException("ClampBounds: lowerPercentile must be < upperPercentile");

		return new ClampBounds(lowerPercentile, upperPercentile);
	}

	public double getLowerPercentile() {
		return lowerPercentile;
	}

	public void setLowerPercentile(double lowerPercentile) {
		this.lowerPercentile = lowerPercentile;
	}

	public double getUpperPercentile() {
		return upperPercentile;
	}

	public void setUpperPercentile(double upperPercentile) {
		this.upperPercentile = upperPercentile;
	}

	/** ISO-8601 date these bounds were last set, or null if unknown (set before this field existed). */
	public String getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(String lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	@Override
	public String toString() {
		return "ClampBounds [p" + lowerPercentile + ", p" + upperPercentile + ", lastUpdated=" + lastUpdated + "]";
	}
}
