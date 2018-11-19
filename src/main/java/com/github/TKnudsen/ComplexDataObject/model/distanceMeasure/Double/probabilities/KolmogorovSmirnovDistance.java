package com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.Double.probabilities;

import com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.Double.DoubleDistanceMeasure;
import com.github.TKnudsen.ComplexDataObject.model.statistics.KolmogorovSmirnovTest;

import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.util.LocalizedFormats;

/**
 * <p>
 * Title: KolmogorovSmirnovDistance
 * </p>
 * 
 * <p>
 * Description: nonparametric test of the equality of continuous,
 * one-dimensional probability distributions. Used to to compare two samples
 * (two-sample K–S test).
 * 
 * The Kolmogorov–Smirnov statistic quantifies a distance between the empirical
 * distribution function of the sample and the cumulative distribution function
 * of the reference distribution, or between the empirical distribution
 * functions of two samples.
 * 
 * Named after Andrey Kolmogorov and Nikolai Smirnov. References:
 * 
 * Kolmogorov A (1933). "Sulla determinazione empirica di una legge di
 * distribuzione". G. Ist. Ital. Attuari. 4: 83–91.
 * 
 * Smirnov N (1948). "Table for estimating the goodness of fit of empirical
 * distributions". Annals of Mathematical Statistics. 19: 279–281.
 * doi:10.1214/aoms/1177730256.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2018, https://github.com/TKnudsen/ComplexDataObject
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.02
 */
public class KolmogorovSmirnovDistance extends DoubleDistanceMeasure {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5659522174712724493L;

	public KolmogorovSmirnovDistance() {
	}

	@Override
	public double getDistance(double[] o1, double[] o2) {
		if (o1 == null || o2 == null)
			// return Double.NaN;
			throw new NullArgumentException(LocalizedFormats.NULL_NOT_ALLOWED);

		if (o1.length < 2 || o2.length < 2)
			// test will throw an exeption. return max distance.
			return 1.0;

		return KolmogorovSmirnovTest.calculateKolmogorovSmirnov(o1, o2);
	}

	@Override
	public String getName() {
		return "KolmogorovSmirnovDistance";
	}

	@Override
	public String getDescription() {
		return "Measure for the distance between two probability distributions";
	}

}
