package com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.Double.probabilities;

import com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.Double.DoubleDistanceMeasure;

/**
 * <p>
 * Metric assessing distance between two probability distributions.
 * Builds up in Jensen Shannon Divergence, which, in turn, builds upon the
 * Kullback Leibler. The Jensen Shannon distances mitigates Kullback's problem
 * of infinite values (if one attribute is 0), though.
 *
 * References
 * </p>
 *
 * @version 1.01
 * @since 2018
 *
 *        TODO nice to have: unify this Jensen-Shannon computation with
 *        com.github.TKnudsen.statistics.JensenShannonDivergence once the
 *        statistics project has obtained a resolvable Maven coordinate; it is
 *        duplicated here so that ComplexDataObject, being a root project,
 *        does not depend on it.
 */
public class JensenShannonDivergenceDistance extends DoubleDistanceMeasure {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5659522174712724493L;

	private static final double KL_ZERO_REPLACEMENT = 0.00001;

	public JensenShannonDivergenceDistance() {
	}

	@Override
	public double getDistance(double[] o1, double[] o2) {
		if (o1 == null || o2 == null)
			return Double.NaN;

		if (o1.length != o2.length)
			throw new IllegalArgumentException(getName() + ": given arrays have different length");

		double jensenShannonDivergence = jensenShannonDivergence(o1, o2);

		return Math.sqrt(jensenShannonDivergence);
	}

	private static double jensenShannonDivergence(double[] a, double[] b) {
		double[] mean = new double[a.length];

		for (int i = 0; i < mean.length; i++)
			mean[i] = (a[i] + b[i]) / 2;

		double klA = getKullbackLeiblerDivergence(a, mean);
		double klB = getKullbackLeiblerDivergence(b, mean);

		return (klA + klB) * 0.5;
	}

	private static double getKullbackLeiblerDivergence(double[] o1, double[] o2) {
		double klDivergence = 0.0;

		for (int i = 0; i < o1.length; ++i) {
			double a = o1[i];
			if (a >= 0.0 && a <= KL_ZERO_REPLACEMENT)
				a = KL_ZERO_REPLACEMENT;

			double b = o2[i];
			if (b >= 0.0 && b <= KL_ZERO_REPLACEMENT)
				b = KL_ZERO_REPLACEMENT;

			klDivergence += a * Math.log(a / b);
		}

		return klDivergence;
	}

	@Override
	public String getName() {
		return "Jensen Shannon Divergence Distance";
	}

	@Override
	public String getDescription() {
		return "Measure for the distance between two probability distributions";
	}

}
