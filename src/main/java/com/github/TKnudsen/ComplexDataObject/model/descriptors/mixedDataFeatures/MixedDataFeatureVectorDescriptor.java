package com.github.TKnudsen.ComplexDataObject.model.descriptors.mixedDataFeatures;

import java.util.ArrayList;
import java.util.List;

import com.github.TKnudsen.ComplexDataObject.data.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.data.features.FeatureType;
import com.github.TKnudsen.ComplexDataObject.data.features.mixedData.MixedDataFeature;
import com.github.TKnudsen.ComplexDataObject.data.features.mixedData.MixedDataFeatureTools;
import com.github.TKnudsen.ComplexDataObject.data.features.mixedData.MixedDataFeatureVector;
import com.github.TKnudsen.ComplexDataObject.model.descriptors.IMixedDataFeatureVectorDescriptor;

/**
 * <p>
 * Title: MixedDataFeatureVectorDescriptor
 * </p>
 *
 * <p>
 * Description: creates MixedDataFeatureVector out of a given list of
 * ComplexDataObjects. Tries to transform all attributes.
 * </p>
 *
 * <p>
 * Copyright: Copyright (c) 2016
 * </p>
 *
 * @author Juergen Bernard
 * @version 1.0
 */
public class MixedDataFeatureVectorDescriptor implements IMixedDataFeatureVectorDescriptor {

	@Override
	public List<MixedDataFeatureVector> transform(ComplexDataObject complexDataObject) {
		List<MixedDataFeatureVector> mixedDataFeatureVectors = new ArrayList<>();

		List<MixedDataFeature> features = new ArrayList<>();

		for (String string : complexDataObject) {
			Object value = complexDataObject.get(string);

			try {
				FeatureType featureType = MixedDataFeatureTools.guessFeatureType(value);
				features.add(new MixedDataFeature(string, value, featureType));
			} catch (IllegalArgumentException e) {

			}
		}

		mixedDataFeatureVectors.add(new MixedDataFeatureVector(features));

		return mixedDataFeatureVectors;
	}

	@Override
	public List<MixedDataFeatureVector> transform(List<ComplexDataObject> complexDataObjects) {

		List<MixedDataFeatureVector> mixedDataFeatureVectors = new ArrayList<>();

		for (ComplexDataObject object : complexDataObjects) {
			mixedDataFeatureVectors.addAll(transform(object));
		}

		return mixedDataFeatureVectors;
	}

	@Override
	public String getName() {
		return "MixedDataFeatureVectorDescriptor";
	}

	@Override
	public String getDescription() {
		return "Creates MixedDataFeatureVector out of a given list of ComplexDataObjects. Tries to transform all attributes.";
	}
}
