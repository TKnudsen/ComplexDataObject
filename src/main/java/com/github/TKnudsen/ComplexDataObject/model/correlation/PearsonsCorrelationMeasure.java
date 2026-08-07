package com.github.TKnudsen.ComplexDataObject.model.correlation;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

/**
 * <p>
 * measures the Pearson correlation for two given arrays.
 * </p>
 *
 * @version 1.02
 * @since 2017
 */
public class PearsonsCorrelationMeasure {
	PearsonsCorrelation correlationMeasure = new PearsonsCorrelation();

	/**
	 * Measures the Pearson correlation for two given arrays. throws a
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
