package com.github.TKnudsen.ComplexDataObject.model.processors.features.numericalData;

import java.util.List;

import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeatureVector;
import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeatureVectorContainer;
import com.github.TKnudsen.ComplexDataObject.model.processors.complexDataObject.DataProcessingCategory;
import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;
import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;

/**
 * <p>
 * Title: OutlierTreatment
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
 * @version 1.01
 */
public class OutlierTreatment implements INumericalFeatureVectorProcessor {

	private double outlierquantile;

	public OutlierTreatment() {
		this(5.0);
	}

	/**
	 * percentage in x.0: example: 5% = 5.0.
	 * 
	 * @param outlierquantile
	 */
	public OutlierTreatment(double outlierquantile) {
		this.outlierquantile = outlierquantile;
	}

	@Override
	public void process(List<NumericalFeatureVector> data) {
		process(new NumericalFeatureVectorContainer(data));

	}

	@Override
	public DataProcessingCategory getPreprocessingCategory() {
		return DataProcessingCategory.DATA_CLEANING;
	}

	@Override
	public void process(NumericalFeatureVectorContainer container) {
		// TODO for every feature:

		StatisticsSupport statistics = new StatisticsSupport(featureValues);
		double minNew = statistics.getPercentile(outlierquantile);
		double maxNew = statistics.getPercentile(100 - outlierquantile);
		// TODO for every value:
		value = MathFunctions.linearScale(minNew, maxNew, value, true);

	}

}
