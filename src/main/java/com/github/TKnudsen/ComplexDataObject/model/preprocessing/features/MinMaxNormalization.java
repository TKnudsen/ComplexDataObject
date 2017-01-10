package com.github.TKnudsen.ComplexDataObject.model.preprocessing.features;

import java.util.List;

import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeature;
import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeatureVector;
import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeatureVectorTools;
import com.github.TKnudsen.ComplexDataObject.model.preprocessing.complexDataObject.DataProcessingCategory;
import com.github.TKnudsen.ComplexDataObject.model.preprocessing.features.numericalData.INumericalFeatureVectorProcessor;

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
					feature.setFeatureValue(normalize(globalMin, globalMax, fv.get(i)));
				else
					feature.setFeatureValue(normalize(min, max, fv.get(i)));
			}
		}
	}

	private double normalize(double min, double max, double value) {
		if (!Double.isNaN(value))
			if (max != min)
				return (value - min) / (max - min);
			else if (max != 0)
				return (value - min) / (max);
			else
				return 1.0;
		else
			return Double.NaN;
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