package com.github.TKnudsen.ComplexDataObject.model.tools;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

/**
 * <p>
 * Strict numeric range extraction utilities.
 *
 * Guarantees: returns only finite and usable ranges. Throws if: - no finite
 * values exist - minimum/maximum are not finite (before or after bounds) -
 * final range is invalid or degenerate (min == max -- thrown as
 * {@link DegenerateRangeException}, a dedicated type so callers can catch
 * this specific, well-understood condition deliberately)
 * </p>
 */
public final class NumericRangeTools {

	private NumericRangeTools() {
	}

	public static <T> NumericRange computeFiniteRangeStrict(Collection<? extends T> data,
			Function<? super T, ? extends Number> worldToNumberMapping, Number minGlobal, Number maxGlobal) {

		Objects.requireNonNull(data, "data must not be null");
		Objects.requireNonNull(worldToNumberMapping, "worldToNumberMapping must not be null");

		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;

		int finiteCount = 0;

		// Scan only finite values
		for (T t : data) {
			if (t == null)
				continue;

			Number n = worldToNumberMapping.apply(t);
			if (n == null)
				continue;

			double v = n.doubleValue();
			if (!Double.isFinite(v))
				continue;

			finiteCount++;
			if (v < min)
				min = v;
			if (v > max)
				max = v;
		}

		if (finiteCount == 0) {
			throw new IllegalStateException(
					"NumericRangeTools.computeFiniteRangeStrict: no finite numeric values in data.");
		}

		// At this point minimum/maximum must be finite due to finiteCount > 0
		if (!Double.isFinite(min) || !Double.isFinite(max)) {
			throw new IllegalStateException(
					"NumericRangeTools.computeFiniteRangeStrict: internal error, min/max are non-finite after scan.");
		}

		// Apply bounds: must be finite too (strict rule)
		if (minGlobal != null) {
			double gMin = minGlobal.doubleValue();
			if (!Double.isFinite(gMin)) {
				throw new IllegalArgumentException(
						"NumericRangeTools.computeFiniteRangeStrict: minGlobal must be finite, got: " + gMin);
			}
			if (gMin < min)
				min = gMin;
		}

		if (maxGlobal != null) {
			double gMax = maxGlobal.doubleValue();
			if (!Double.isFinite(gMax)) {
				throw new IllegalArgumentException(
						"NumericRangeTools.computeFiniteRangeStrict: maxGlobal must be finite, got: " + gMax);
			}
			if (gMax > max)
				max = gMax;
		}

		// Validate final range
		if (!Double.isFinite(min) || !Double.isFinite(max)) {
			throw new IllegalStateException(
					"NumericRangeTools.computeFiniteRangeStrict: final range is non-finite: min=" + min + ", max="
							+ max);
		}
		if (min > max) {
			throw new IllegalStateException(
					"NumericRangeTools.computeFiniteRangeStrict: invalid range: min=" + min + " > max=" + max);
		}
		if (min == max) {
			throw new DegenerateRangeException(
					"NumericRangeTools.computeFiniteRangeStrict: degenerate range (min==max==" + min
							+ "). Ensure variance or widen bounds.");
		}

		return new NumericRange(min, max, finiteCount);
	}

	public static <T> NumericRange computeFiniteRangeStrict(Collection<? extends T> data,
			Function<? super T, ? extends Number> worldToNumberMapping) {

		return computeFiniteRangeStrict(data, worldToNumberMapping, null, null);
	}

	/**
	 * Same as {@link #computeFiniteRangeStrict(Collection, Function, Number, Number)},
	 * except a degenerate (min == max) range does not throw
	 * {@link DegenerateRangeException} -- it re-derives the single repeated
	 * (finite) mapped value and returns a {@code [value, value]} range instead.
	 * Useful for callers that render/lay out a single-value (or otherwise
	 * zero-variance) dataset and must not crash on it, e.g. a distribution/
	 * histogram widget over a column whose values are currently all identical
	 * (or all still at a default such as 0.0).
	 */
	public static <T> NumericRange computeFiniteRangeTolerant(Collection<? extends T> data,
			Function<? super T, ? extends Number> worldToNumberMapping, Number minGlobal, Number maxGlobal) {
		try {
			return computeFiniteRangeStrict(data, worldToNumberMapping, minGlobal, maxGlobal);
		} catch (DegenerateRangeException e) {
			double value = data.stream().map(worldToNumberMapping)
					.filter(v -> v != null && Double.isFinite(v.doubleValue())).map(Number::doubleValue).findFirst()
					.orElse(0d);
			return new NumericRange(value, value, data.size());
		}
	}

	public static <T> NumericRange computeFiniteRangeTolerant(Collection<? extends T> data,
			Function<? super T, ? extends Number> worldToNumberMapping) {

		return computeFiniteRangeTolerant(data, worldToNumberMapping, null, null);
	}
}