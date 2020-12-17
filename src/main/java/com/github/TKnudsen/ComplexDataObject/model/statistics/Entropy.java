package com.github.TKnudsen.ComplexDataObject.model.statistics;

import java.util.Collection;
import java.util.Map;

/**
 * Calculates the entropy for various value distributions.
 *
 * <p>
 * Copyright: Copyright (c) 2016-2020
 * </p>
 *
 * @author Juergen Bernard
 * @version 1.02
 */
public class Entropy {

	public static double calculateEntropy(Map<String, Double> labelDistribution) {
		if (labelDistribution == null || labelDistribution.size() == 0)
			return 0;

		return calculateEntropy(labelDistribution.values());
	}

//	public static double calculateEntropy(Collection<Double> distribution) {
//		if (distribution == null || distribution.size() == 0)
//			return 0;
//
//		double entropy = 0.0;
//		for (Double d : distribution)
//			if (d > 0)
//				entropy -= (d * Math.log(d));
//
//		entropy /= Math.log(2.0);
//
//		return entropy;
//	}

	public static double calculateEntropy(Collection<? extends Number> distribution) {
		if (distribution == null || distribution.size() == 0)
			return 0;

		double entropy = 0.0;
		for (Number d : distribution)
			if (d.doubleValue() > 0)
				entropy -= (d.doubleValue() * Math.log(d.doubleValue()));

		entropy /= Math.log(2.0);

		return entropy;
	}
}
