package com.github.TKnudsen.ComplexDataObject.model.transformations.normalization;

import com.github.TKnudsen.ComplexDataObject.data.ranking.Ranking;

/**
 * <p>
 * Little helper functions for QuantileNormalizationFunction.
 * </p>
 *
 * @version 1.01
 * @since 2024
 */
public class QuantileNormalizationFunctions {

	public static Ranking<Float> getValueRanking(QuantileNormalizationFunction quantileNormalizationFunction) {
		return quantileNormalizationFunction.valueRanking;
	}
}
