package com.github.TKnudsen.ComplexDataObject.model.processors.features.numericalData;

import java.util.List;

import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeature;
import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeatureVector;
import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeatureVectorTools;
import com.github.TKnudsen.ComplexDataObject.model.processors.complexDataObject.DataProcessingCategory;
import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;

/**
 * <p>
 * Title: MinMaxNormalization
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2017
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.0
 */
public class MinMaxNormalization implements INumericalFeatureVectorProcessor {

	private boolean globalMinMax;

	public MinMaxNormalization() {
		this.globalMinMax = false;
	}

	public MinMaxNormalization(boolean globalMinMax) {
		this.globalMinMax = globalMinMax;
	}

	@Override
	public void process(List<NumericalFeatureVector> data) {

		double globalMin = Double.POSITIVE_INFINITY;
		double globalMax = Double.NEGATIVE_INFINITY;
		if (globalMinMax) {
			for (NumericalFeatureVector fv : data) {
				globalMin = Math.min(globalMin, NumericalFeatureVectorTools.getMin(fv));
				globalMax = Math.max(globalMax, NumericalFeatureVectorTools.getMax(fv));
			}
		}

		for (NumericalFeatureVector fv : data) {
			double min = NumericalFeatureVectorTools.getMin(fv);
			double max = NumericalFeatureVectorTools.getMax(fv);
			for (int i = 0; i < fv.getDimensions(); i++) {
				NumericalFeature feature = fv.getFeature(i);
				if (globalMinMax)
					feature.setFeatureValue(MathFunctions.linearScale(globalMin, globalMax, fv.get(i)));
				else
					feature.setFeatureValue(MathFunctions.linearScale(min, max, fv.get(i)));
			}
		}
	}

	@Override
	public DataProcessingCategory getPreprocessingCategory() {
		return DataProcessingCategory.DATA_NORMALIZATION;
	}

	public boolean isGlobalMinMax() {
		return globalMinMax;
	}

	public void setGlobalMinMax(boolean globalMinMax) {
		this.globalMinMax = globalMinMax;
	}
}