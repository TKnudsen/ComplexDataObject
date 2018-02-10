package com.github.TKnudsen.ComplexDataObject.view;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeatureVector;
import com.github.TKnudsen.ComplexDataObject.model.tools.DataConversion;
import com.github.TKnudsen.ComplexDataObject.model.transformations.dimensionalityReduction.DimensionalityReductionTools;
import com.github.TKnudsen.ComplexDataObject.model.transformations.dimensionalityReduction.IDimensionalityReduction;
import com.github.TKnudsen.ComplexDataObject.view.visualMappings.position.CoordinatesMappingFunction;

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
 * Copyright: Copyright (c) 2015-2018
 * </p>
 * 
 * @author Juergen Bernard
 */
public class DimensionalityReductionBasedMappingFunction extends CoordinatesMappingFunction<NumericalFeatureVector> {

	private IDimensionalityReduction<NumericalFeatureVector> dimensionalityReduction;

	public DimensionalityReductionBasedMappingFunction(
			IDimensionalityReduction<NumericalFeatureVector> dimensionalityReductionResult,
			List<NumericalFeatureVector> featureVectors) {

		this.dimensionalityReduction = dimensionalityReductionResult;

		Map<NumericalFeatureVector, NumericalFeatureVector> FVmapping = this.dimensionalityReduction.getMapping();

		// clone output to avoid data editing in original instances
		Map<NumericalFeatureVector, NumericalFeatureVector> FVmappingClone = new LinkedHashMap<>();
		for (NumericalFeatureVector fv : FVmapping.keySet())
			FVmappingClone.put(fv, FVmapping.get(fv).clone());
		DimensionalityReductionTools.normalizeMapping(FVmappingClone);

		// create mapping from high-dimensional FV to coordinates
		mappingLookup = new LinkedHashMap<>();

		for (NumericalFeatureVector fv : featureVectors) {
			NumericalFeatureVector normalizedOutputFV = FVmappingClone.get(fv);

			if (normalizedOutputFV != null) {
				Double[] array = DataConversion.doublePrimitivesToArray(normalizedOutputFV.getVector());
				mappingLookup.put(fv, array);
			} else {
				System.err.println(
						"DimensionalityReductionBasedMappingFunction: required FV was not part of the dimensionality reduction routine.");
				mappingLookup.put(fv, null);
			}

			// the following was before the coordinates were normalized.

			// List<NumericalFeatureVector> transform =
			// dimensionalityReductionResult.transform(fv);
			// if (transform != null && transform.size() != 0) {
			// NumericalFeatureVector transformed = transform.get(0);
			// if (transformed != null) {
			// Double[] array =
			// DataConversion.doublePrimitivesToArray(transformed.getVector());
			// mapping.put(fv, array);
			// } else
			// mapping.put(fv, null);
			// } else
			// mapping.put(fv, null);
		}
	}

	@Override
	protected Double[] calculateMapping(NumericalFeatureVector t) {
		return mappingLookup.get(t);
	}

	public IDimensionalityReduction<NumericalFeatureVector> getDimensionalityReduction() {
		return dimensionalityReduction;
	}
}
