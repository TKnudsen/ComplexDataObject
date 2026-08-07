package com.github.TKnudsen.ComplexDataObject.model.tools;

import java.io.Serializable;

/**
 * <p>
 * Immutable numeric range (minimum/maximum) with basic diagnostics.
 * </p>
 */
public final class NumericRange implements Serializable {

	private static final long serialVersionUID = 1L;

	private final double min;
	private final double max;

	/** Number of finite values observed in the scan */
	private final int validValueCount;

	public NumericRange(double min, double max, int validValueCount) {
		this.min = min;
		this.max = max;
		this.validValueCount = validValueCount;
	}

	public double getMin() {
		return min;
	}

	public double getMax() {
		return max;
	}

	public boolean isFinite() {
		return Double.isFinite(min) && Double.isFinite(max);
	}

	public int getValidValueCount() {
		return validValueCount;
	}

	public double getSpan() {
		return max - min;
	}

	public boolean isDegenerate() {
		return min == max;
	}

	public boolean contains(double v) {
		return v >= min && v <= max;
	}

	@Override
	public String toString() {
		return "NumericRange[min=" + min + ", max=" + max + ", validValueCount=" + validValueCount + "]";
	}
}