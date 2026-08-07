package com.github.TKnudsen.ComplexDataObject.model.correlation;

import java.util.Collection;
import java.util.Objects;

import com.github.TKnudsen.ComplexDataObject.model.tools.DataConversion;

/**
 * Utility class for computing Pearson and Spearman correlations between two
 * collections or arrays of equal length.
 *
 * <p>
 * Entry points:
 * </p>
 * <ul>
 * <li>{@link #compute} -- average of Pearson and Spearman, with NaN fallback to
 * whichever measure is available</li>
 * <li>{@link #pearson} -- Pearson correlation only</li>
 * <li>{@link #spearman} -- Spearman correlation only</li>
 * </ul>
 *
 * <p>
 * All methods accept either {@code Collection<? extends Number>} or
 * {@code double[]}. Callers with arrays already in hand should prefer the array
 * overloads to avoid redundant conversion.
 * </p>
 *
 * <p>
 * All methods return {@code Double.NaN} when input is too small or the result
 * is undefined. A minimum size of 2 is enforced; note that size 2 always yields
 * a perfect correlation.
 * </p>
 *
 * <p>
 * <b>Thread safety:</b> both correlation measures are held in
 * {@link ThreadLocal} instances -- one per thread, no contention.
 * </p>
 * 
 * @version 2.0 revised in February 2026
 */
public class Correlations {

	/**
	 * Minimum number of paired samples required to compute a meaningful
	 * correlation. Size 2 always yields a perfect correlation and is therefore
	 * excluded.
	 */
	public static final int MIN_CORRELATION_SAMPLE_SIZE = 3;

	/**
	 * Unsure if state-ful. Decision for ThreadLocal since it has negligible overhead
	 * and eliminates the risk entirely.
	 */
	private static final ThreadLocal<PearsonsCorrelationMeasure> PEARSON = ThreadLocal
			.withInitial(PearsonsCorrelationMeasure::new);

	/**
	 * Unsure if stateful. Decision for ThreadLocal since it has negligible overhead
	 * and eliminates the risk entirely.
	 */
	private static final ThreadLocal<SpearmanCorrelationMeasure> SPEARMAN = ThreadLocal
			.withInitial(SpearmanCorrelationMeasure::new);

	private Correlations() {
	}

	// -----------------------------------------------------------------------
	// Collection-based public API
	// -----------------------------------------------------------------------

	/**
	 * Computes the average of Pearson and Spearman correlations. Falls back to
	 * whichever measure is available if one returns {@code NaN}.
	 *
	 * @param values1     first collection; must not be null
	 * @param values2     second collection; must not be null, must equal values1 in
	 *                    size
	 * @param minimumSize minimum number of elements required; must be &ge; 2
	 * @return average correlation in [-1, 1], or {@code Double.NaN} if undefined
	 * @throws NullPointerException     if either collection is null
	 * @throws IllegalArgumentException if collections differ in size
	 */
	public static double compute(Collection<? extends Number> values1, Collection<? extends Number> values2,
			int minimumSize) {
		int n = validateSameSize(values1, values2);
		if (n < Math.max(3, minimumSize))
			return Double.NaN;

		double[] a1 = DataConversion.toPrimitives(values1);
		double[] a2 = DataConversion.toPrimitives(values2);
		return compute(a1, a2);
	}

	/**
	 * Computes a correlation selected by the {@code pearson} and {@code spearman}
	 * flags. If both flags are {@code true} (or both {@code false}), the average of
	 * Pearson and Spearman is returned. If only one flag is {@code true}, that
	 * measure alone is returned.
	 *
	 * @param values1     first collection; must not be null
	 * @param values2     second collection; must not be null, must equal values1 in
	 *                    size
	 * @param pearson     include Pearson correlation
	 * @param spearman    include Spearman correlation
	 * @param minimumSize minimum number of elements required; must be &ge; 2
	 * @return correlation in [-1, 1], or {@code Double.NaN} if undefined
	 * @throws NullPointerException     if either collection is null
	 * @throws IllegalArgumentException if collections differ in size
	 */
	public static double compute(Collection<? extends Number> values1, Collection<? extends Number> values2,
			boolean pearson, boolean spearman, int minimumSize) {
		int n = validateSameSize(values1, values2);
		if (n < Math.max(3, minimumSize))
			return Double.NaN;

		double[] a1 = DataConversion.toPrimitives(values1);
		double[] a2 = DataConversion.toPrimitives(values2);

		if (pearson && !spearman)
			return pearson(a1, a2);
		if (!pearson && spearman)
			return spearman(a1, a2);
		return compute(a1, a2);
	}

	/**
	 * Computes the Pearson correlation for two collections of equal length.
	 *
	 * @param values1 first collection; must not be null
	 * @param values2 second collection; must not be null, must equal values1 in
	 *                size
	 * @return Pearson correlation in [-1, 1], or {@code Double.NaN} if undefined
	 * @throws NullPointerException     if either collection is null
	 * @throws IllegalArgumentException if collections differ in size
	 */
	public static double pearson(Collection<? extends Number> values1, Collection<? extends Number> values2) {
		int n = validateSameSize(values1, values2);
		if (n < 3)
			return Double.NaN;

		return pearson(DataConversion.toPrimitives(values1), DataConversion.toPrimitives(values2));
	}

	/**
	 * Computes the Spearman correlation for two collections of equal length.
	 *
	 * @param values1 first collection; must not be null
	 * @param values2 second collection; must not be null, must equal values1 in
	 *                size
	 * @return Spearman correlation in [-1, 1], or {@code Double.NaN} if undefined
	 * @throws NullPointerException     if either collection is null
	 * @throws IllegalArgumentException if collections differ in size
	 */
	public static double spearman(Collection<? extends Number> values1, Collection<? extends Number> values2) {
		int n = validateSameSize(values1, values2);
		if (n < 3)
			return Double.NaN;

		return spearman(DataConversion.toPrimitives(values1), DataConversion.toPrimitives(values2));
	}

	// -----------------------------------------------------------------------
	// Array-based public API -- preferred when arrays are already available
	// -----------------------------------------------------------------------

	/**
	 * Computes the average of Pearson and Spearman correlations for two arrays.
	 * Preferred over the collection overload when arrays are already available.
	 *
	 * @param a1 first array; must not be null, must equal a2 in length
	 * @param a2 second array; must not be null, must equal a1 in length
	 * @return average correlation in [-1, 1], or {@code Double.NaN} if undefined
	 * @throws NullPointerException     if either array is null
	 * @throws IllegalArgumentException if arrays differ in length
	 */
	public static double compute(double[] a1, double[] a2) {
		validateSameLength(a1, a2);
		if (a1.length < 3)
			return Double.NaN;

		double p = pearson(a1, a2);
		double s = spearman(a1, a2);

		if (!Double.isNaN(p) && Double.isNaN(s))
			return p;
		if (Double.isNaN(p) && !Double.isNaN(s))
			return s;
		if (Double.isNaN(p))
			return Double.NaN;
		return (p + s) * 0.5;
	}

	/**
	 * Computes the Pearson correlation for two arrays of equal length.
	 *
	 * @param a1 first array; must not be null, must equal a2 in length
	 * @param a2 second array; must not be null, must equal a1 in length
	 * @return Pearson correlation in [-1, 1], or {@code Double.NaN} if undefined
	 * @throws NullPointerException     if either array is null
	 * @throws IllegalArgumentException if arrays differ in length
	 */
	public static double pearson(double[] a1, double[] a2) {
		validateSameLength(a1, a2);

		if (a1.length < 3)
			return Double.NaN;

		return PEARSON.get().correlation(a1, a2);
	}

	/**
	 * Computes the Spearman correlation for two arrays of equal length.
	 *
	 * @param a1 first array; must not be null, must equal a2 in length
	 * @param a2 second array; must not be null, must equal a1 in length
	 * @return Spearman correlation in [-1, 1], or {@code Double.NaN} if undefined
	 * @throws NullPointerException     if either array is null
	 * @throws IllegalArgumentException if arrays differ in length
	 */
	public static double spearman(double[] a1, double[] a2) {
		validateSameLength(a1, a2);

		if (a1.length < 3)
			return Double.NaN;

		return SPEARMAN.get().correlation(a1, a2);
	}

	// -----------------------------------------------------------------------
	// Validation
	// -----------------------------------------------------------------------

	/**
	 * Validates that both collections are non-null and equal in size.
	 *
	 * @return the common size
	 * @throws NullPointerException     if either collection is null
	 * @throws IllegalArgumentException if sizes differ
	 */
	private static int validateSameSize(Collection<? extends Number> values1, Collection<? extends Number> values2) {
		Objects.requireNonNull(values1, "values1 must not be null");
		Objects.requireNonNull(values2, "values2 must not be null");

		int n1 = values1.size();
		int n2 = values2.size();
		if (n1 != n2)
			throw new IllegalArgumentException(
					"Correlations: collections must be equal length (" + n1 + " vs " + n2 + ")");
		return n1;
	}

	/**
	 * Validates that both arrays are non-null and equal in length.
	 *
	 * @throws NullPointerException     if either array is null
	 * @throws IllegalArgumentException if lengths differ
	 */
	private static void validateSameLength(double[] a1, double[] a2) {
		Objects.requireNonNull(a1, "a1 must not be null");
		Objects.requireNonNull(a2, "a2 must not be null");

		if (a1.length != a2.length)
			throw new IllegalArgumentException(
					"Correlations: arrays must be equal length (" + a1.length + " vs " + a2.length + ")");
	}
}