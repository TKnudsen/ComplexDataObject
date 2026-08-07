package com.github.TKnudsen.ComplexDataObject.model.scoring.functions.Double;

/**
 * <p>
 * Static utility class with factory methods to create a
 * DoubleAttributeBipolarScoringFunction from an existing
 * DoubleAttributeScoringFunction configuration, or to clone an existing
 * DoubleAttributeBipolarScoringFunction while carrying over its
 * normalization, outlier, and uncertainty settings.
 * </p>
 */
public class DoubleAttributeBipolarScoringFunctions {

	public static DoubleAttributeBipolarScoringFunction create(DoubleAttributeScoringFunction scoringFunction,
			double neutralValue) {
		DoubleAttributeBipolarScoringFunction bipolarScoringFunction = new DoubleAttributeBipolarScoringFunction(
				scoringFunction.getContainer(), scoringFunction.getParser(), scoringFunction.getAttribute(),
				scoringFunction.getAbbreviation(), scoringFunction.isQuantileBased(), scoringFunction.isHighIsGood(),
				scoringFunction.getWeight(), scoringFunction.getUncertaintyFunction(), neutralValue);

		bipolarScoringFunction.setQuantileNormalizationRate(scoringFunction.getQuantileNormalizationRate());

		return bipolarScoringFunction;

	}

	public static DoubleAttributeBipolarScoringFunction clone(DoubleAttributeBipolarScoringFunction scoringFunction) {
		DoubleAttributeBipolarScoringFunction bipolarScoringFunction = new DoubleAttributeBipolarScoringFunction(
				scoringFunction.getContainer(), scoringFunction.getParser(), scoringFunction.getAttribute(),
				scoringFunction.getAbbreviation(), scoringFunction.isQuantileBased(), scoringFunction.isHighIsGood(),
				scoringFunction.getWeight(), scoringFunction.getUncertaintyFunction(),
				scoringFunction.getNeutralValue());

		bipolarScoringFunction.setAbbreviation(scoringFunction.getAbbreviation());

		bipolarScoringFunction.setLinearTransitionOfQuantileNormalizationRates(
				scoringFunction.isLinearTransitionOfQuantileNormalizationRates());

		bipolarScoringFunction.setMissingValueAvgScoreRatio(scoringFunction.getMissingValueAvgScoreRatio());

		bipolarScoringFunction.setOutlierStd(scoringFunction.getOutlierStd());
		bipolarScoringFunction.setOutlierStdTop(scoringFunction.getOutlierStdTop());
		bipolarScoringFunction.setOutlierPruningMinValue(scoringFunction.getOutlierPruningMinValue());
		bipolarScoringFunction.setOutlierPruningMaxValue(scoringFunction.getOutlierPruningMaxValue());

		bipolarScoringFunction.setQuantileNormalizationRate(scoringFunction.getQuantileNormalizationRate());

		bipolarScoringFunction.setUncertaintyConsideration(scoringFunction.getUncertaintyConsideration());

		return bipolarScoringFunction;
	}
}
