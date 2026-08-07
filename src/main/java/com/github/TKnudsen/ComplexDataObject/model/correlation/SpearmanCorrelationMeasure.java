package com.github.TKnudsen.ComplexDataObject.model.correlation;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

/**
 * <p>
 * measures the Spearman's rank correlation for two given arrays.
 * </p>
 *
 * @version 1.01
 * @since 2017
 */
public class SpearmanCorrelationMeasure {
	SpearmansCorrelation correlationMeasure = new SpearmansCorrelation();

	/**
	 * Measures the Spearman rank correlation for two given arrays. throws a
	 * DimensionMismatchException if the two arrays do not have the same length (and
	 * are not null).
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public double correlation(double[] a, double[] b) {
		if (a == null || b == null)
			return Double.NaN;

		if (a.length != b.length)
			throw new DimensionMismatchException(a.length, b.length);

		return correlationMeasure.correlation(a, b);
	}
}
