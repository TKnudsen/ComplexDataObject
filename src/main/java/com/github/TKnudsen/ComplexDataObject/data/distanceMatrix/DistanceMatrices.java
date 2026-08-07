package com.github.TKnudsen.ComplexDataObject.data.distanceMatrix;

import java.util.List;
import java.util.function.ToDoubleBiFunction;

/**
 * <p>
 * Static helper methods for computing pairwise distance matrices from a list
 * of elements and a distance measure (optionally exploiting symmetry for
 * performance), and for comparing two distance matrices for equality within a
 * given tolerance, e.g. in tests.
 * </p>
 */
public class DistanceMatrices {

	/**
	 * pairwise distances between elements calculated with a distance measure
	 * represented by a biFunction. Assumes that the pairwise distances are not
	 * symmetric.
	 * 
	 * @param <T>             The element type
	 * @param elements        The elements
	 * @param distanceMeasure The distance measure
	 * @return pairwise distances represented as a matrix of doubles
	 */
	public static <T> double[][] distanceMatrix(List<? extends T> elements,
			ToDoubleBiFunction<? super T, ? super T> distanceMeasure) {

		return distanceMatrix(elements, distanceMeasure, false);
	}

	/**
	 * pairwise distances between elements calculated with a distance measure
	 * represented by a biFunction.
	 * 
	 * @param <T>              The element type
	 * @param elements         The elements
	 * @param distanceMeasure  The distance measure
	 * @param symmetricMeasure determine whether apply(a,b) and apply(b,a) will
	 *                         always lead to the same result. if yes this will
	 *                         speedup the process by factor two.
	 * @return pairwise distances represented as a matrix of doubles
	 */
	public static <T> double[][] distanceMatrix(List<? extends T> elements,
			ToDoubleBiFunction<? super T, ? super T> distanceMeasure, boolean symmetricMeasure) {

		DistanceMatrixBlockedParallel<T> distanceMatrix2 = new DistanceMatrixBlockedParallel<T>(elements,
				distanceMeasure, symmetricMeasure, true);

		return distanceMatrix2.getDistanceMatrix();
	}

	/**
	 * Compares two distance matrices element-wise using absolute tolerance.
	 *
	 * @param expected     expected matrix (double[n][m])
	 * @param actual       actual matrix (double[n][m])
	 * @param epsilon      max allowed absolute difference for finite values
	 * @param nanEqualsNan if true, NaN equals NaN at the same position
	 *
	 * @throws AssertionError if matrices differ
	 */
	public static void assertDistanceMatrixEquals(double[][] expected, double[][] actual, double epsilon,
			boolean nanEqualsNan) {

		requireNonNull(expected, "expected");
		requireNonNull(actual, "actual");

		if (expected.length != actual.length) {
			throw new AssertionError("Row count differs: expected=" + expected.length + ", actual=" + actual.length);
		}

		for (int i = 0; i < expected.length; i++) {
			requireNonNull(expected[i], "expected[" + i + "]");
			requireNonNull(actual[i], "actual[" + i + "]");

			if (expected[i].length != actual[i].length) {
				throw new AssertionError("Column count differs at row " + i + ": expected=" + expected[i].length
						+ ", actual=" + actual[i].length);
			}

			for (int j = 0; j < expected[i].length; j++) {
				double e = expected[i][j];
				double a = actual[i][j];

				if (nanEqualsNan && Double.isNaN(e) && Double.isNaN(a)) {
					continue;
				}

				// Infinities must match exactly (+Inf != -Inf)
				if (Double.isInfinite(e) || Double.isInfinite(a)) {
					if (Double.doubleToLongBits(e) != Double.doubleToLongBits(a)) {
						throw mismatch(i, j, e, a, epsilon);
					}
					continue;
				}

				// Normal finite comparison (also catches NaN mismatch if nanEqualsNan == false)
				double diff = Math.abs(e - a);
				if (!(diff <= epsilon)) {
					throw mismatch(i, j, e, a, epsilon);
				}
			}
		}
	}

	private static AssertionError mismatch(int i, int j, double expected, double actual, double epsilon) {
		double diff = Math.abs(expected - actual);
		return new AssertionError("Mismatch at [" + i + "][" + j + "]: expected=" + expected + ", actual=" + actual
				+ ", |diff|=" + diff + ", epsilon=" + epsilon);
	}

	private static void requireNonNull(Object o, String name) {
		if (o == null) {
			throw new AssertionError(name + " is null");
		}
	}

	/**
	 * avoid instantiation
	 */
	private DistanceMatrices() {
	}
}
