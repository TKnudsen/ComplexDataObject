package com.github.TKnudsen.ComplexDataObject.model.correlation;

import java.util.Collection;
import java.util.Objects;

import com.github.TKnudsen.ComplexDataObject.model.tools.DataConversion;
import com.github.TKnudsen.ComplexDataObject.model.transformations.normalization.Normalizations;
import com.github.TKnudsen.ComplexDataObject.model.transformations.normalization.ZScoreNormalizationFunction;

public class Correlations {

	/**
	 * computes correlations for values of collections of equal lengths, of size of
	 * at least two.
	 * 
	 * @param values1
	 * @param values2
	 * @param pearson
	 * @param spearman
	 * @param minimumSize default: 2 (leading to a perfect correlation though)
	 * @return
	 */
	public static double compute(Collection<? extends Number> values1, Collection<? extends Number> values2,
			boolean pearson, boolean spearman, int minimumSize) {

		Objects.requireNonNull(values1);
		Objects.requireNonNull(values2);
		if (values2.size() != values2.size())
			throw new IllegalArgumentException("Correlations.compute requires value collections of equal length");

		if (values1.size() <= Math.max(1, minimumSize - 1) || values2.size() <= Math.max(1, minimumSize - 1))
			return Double.NaN;

		Collection<? extends Number> normalized1 = Normalizations.normalize(values1,
				new ZScoreNormalizationFunction(values1));
		Collection<? extends Number> normalized2 = Normalizations.normalize(values2,
				new ZScoreNormalizationFunction(values2));

		double p = pearson(normalized1, normalized2);
		double s = spearman(normalized1, normalized2);

		if (!Double.isNaN(p) && Double.isNaN(s))
			return p;
		if (Double.isNaN(p) && !Double.isNaN(s))
			return s;
		if (Double.isNaN(p) && Double.isNaN(s))
			return Double.NaN;

		return (p + s) * 0.5;
	}

	/**
	 * computes correlations for values of collections of equal lengths, of size of
	 * at least two.
	 * 
	 * @param values1
	 * @param values2
	 * @return
	 */
	public static double pearson(Collection<? extends Number> values1, Collection<? extends Number> values2) {
		Objects.requireNonNull(values1);
		Objects.requireNonNull(values2);
		if (values2.size() != values2.size())
			throw new IllegalArgumentException("Correlations.pearson requires value collections of equal length");

		if (values1.size() <= 1 || values2.size() <= 1)
			return Double.NaN;

		PearsonsCorrelationMeasure pc = new PearsonsCorrelationMeasure();
		double[] xArray = DataConversion.toPrimitives(values1);
		double[] yArray = DataConversion.toPrimitives(values2);

		double correlation = 0.0;
		if (xArray.length > 1 && yArray.length > 1)
			correlation = pc.correlation(xArray, yArray);

		return correlation;
	}

	/**
	 * computes correlations for values of collections of equal lengths, of size of
	 * at least two.
	 * 
	 * @param values1
	 * @param values2
	 * @return
	 */
	public static double spearman(Collection<? extends Number> values1, Collection<? extends Number> values2) {
		Objects.requireNonNull(values1);
		Objects.requireNonNull(values2);
		if (values2.size() != values2.size())
			throw new IllegalArgumentException("Correlations.spearman requires value collections of equal length");

		if (values1.size() <= 1 || values2.size() <= 1)
			return Double.NaN;

		SpearmanCorrelationMeasure sc = new SpearmanCorrelationMeasure();
		double[] xArray = DataConversion.toPrimitives(values1);
		double[] yArray = DataConversion.toPrimitives(values2);

		double correlation = 0.0;
		if (xArray.length > 1 && yArray.length > 1)
			correlation = sc.correlation(xArray, yArray);

		return correlation;
	}
}
