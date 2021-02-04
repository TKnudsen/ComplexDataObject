package com.github.TKnudsen.ComplexDataObject.model.scoring.functions.Double;

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
}
