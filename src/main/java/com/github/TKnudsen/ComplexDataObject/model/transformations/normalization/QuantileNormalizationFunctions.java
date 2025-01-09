package com.github.TKnudsen.ComplexDataObject.model.transformations.normalization;

import com.github.TKnudsen.ComplexDataObject.data.ranking.Ranking;

/**
 * <p>
 * Title: QuantileNormalizationFunctions
 * </p>
 * 
 * <p>
 * Description: Little helper functions for QuantileNormalizationFunction.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2024
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */
public class QuantileNormalizationFunctions {

	public static Ranking<Float> getValueRanking(QuantileNormalizationFunction quantileNormalizationFunction) {
		return quantileNormalizationFunction.valueRanking;
	}
}
