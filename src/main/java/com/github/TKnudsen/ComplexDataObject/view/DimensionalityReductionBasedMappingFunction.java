package com.github.TKnudsen.ComplexDataObject.view;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeatureVector;
import com.github.TKnudsen.ComplexDataObject.model.tools.DataConversion;
import com.github.TKnudsen.ComplexDataObject.model.transformations.dimensionalityReduction.IDimensionalityReduction;

/**
 * <p>
 * Title: DimensionalityReductionBasedMappingFunction
 * </p>
 * 
 * <p>
 * Description: provides the basic functionality for mapping abstract data to
 * the visual space. DimensionalityReduction is used to map a high-dimensional
 * FeatureVector to Double[] coordinates.
 * 
 * Note: the DimensionalityReduction has to be calculated beforehand.
 * 
 * ToDo: Later on a dimRed Result should be used in here.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2015-2017
 * </p>
 * 
 * @author Juergen Bernard
 */
public class DimensionalityReductionBasedMappingFunction extends PositionMappingFunction<NumericalFeatureVector> {

	Map<NumericalFeatureVector, Double[]> mapping;

	private IDimensionalityReduction<Double, NumericalFeatureVector> dimensionalityReduction;

	public DimensionalityReductionBasedMappingFunction(IDimensionalityReduction<Double, NumericalFeatureVector> dimensionalityReductionResult, List<NumericalFeatureVector> featureVectors) {

		this.dimensionalityReduction = dimensionalityReductionResult;

		mapping = new LinkedHashMap<>();

		for (NumericalFeatureVector fv : featureVectors) {
			List<NumericalFeatureVector> transform = dimensionalityReductionResult.transform(fv);
			if (transform != null && transform.size() != 0) {
				NumericalFeatureVector transformed = transform.get(0);
				if (transformed != null) {
					Double[] array = DataConversion.doublePrimitivesToArray(transformed.getVector());
					mapping.put(fv, array);
				} else
					mapping.put(fv, null);
			} else
				mapping.put(fv, null);
		}
	}

	@Override
	protected Double[] calculateMapping(NumericalFeatureVector t) {
		return mapping.get(t);
	}

	public IDimensionalityReduction<Double, NumericalFeatureVector> getDimensionalityReduction() {
		return dimensionalityReduction;
	}
}
