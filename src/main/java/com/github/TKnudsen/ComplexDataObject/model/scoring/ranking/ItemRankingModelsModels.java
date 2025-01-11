package com.github.TKnudsen.ComplexDataObject.model.scoring.ranking;

import java.util.ArrayList;
import java.util.Collection;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.model.correlation.Correlations;
import com.github.TKnudsen.ComplexDataObject.model.scoring.functions.AttributeScoringFunction;

/**
 * @deprecateds
 */
public class ItemRankingModelsModels {

	/**
	 * @deprecated use AttributeScoringFunctions.getCorrelation
	 * @param container
	 * @param f1
	 * @param f2
	 * @param pearson
	 * @param spearman
	 * @param minimumSize default: 2 (leading to a perfect correlation though)
	 * @return
	 */
	public static double getAttributeScoringCorrelation(ComplexDataContainer container, AttributeScoringFunction<?> f1,
			AttributeScoringFunction<?> f2, boolean pearson, boolean spearman, int minimumSize) {

		Collection<Double> values1 = new ArrayList<>();
		Collection<Double> values2 = new ArrayList<>();

		for (ComplexDataObject cdo : container) {
			double v1 = f1.apply(cdo);
			double v2 = f2.apply(cdo);

			if (!Double.isNaN(v1) && !Double.isNaN(v2)) {
				values1.add(v1);
				values2.add(v2);
			}
		}

		return Correlations.compute(values1, values2, pearson, spearman, minimumSize);
	}

}
