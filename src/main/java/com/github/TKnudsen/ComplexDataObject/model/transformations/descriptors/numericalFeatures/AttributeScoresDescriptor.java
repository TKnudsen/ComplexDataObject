package com.github.TKnudsen.ComplexDataObject.model.transformations.descriptors.numericalFeatures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeature;
import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeatureVector;
import com.github.TKnudsen.ComplexDataObject.model.scoring.functions.AttributeScoringFunction;
import com.github.TKnudsen.ComplexDataObject.model.transformations.descriptors.IDescriptor;

public class AttributeScoresDescriptor implements INumericFeatureVectorDescriptor<ComplexDataObject> {

	private final List<AttributeScoringFunction<?>> attributeWeightingFunctions;

	public AttributeScoresDescriptor(List<AttributeScoringFunction<?>> attributeWeightingFunctions) {
		this.attributeWeightingFunctions = attributeWeightingFunctions;
	}

	@Override
	public List<NumericalFeatureVector> transform(ComplexDataObject cdo) {
		List<NumericalFeature> features = new ArrayList<>();

		for (AttributeScoringFunction<?> asf : attributeWeightingFunctions) {
			try {
				features.add(new NumericalFeature(asf.getAttribute(), asf.apply(cdo) * asf.getWeight()));
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("AttributeScoresDescriptor.transform: unable to extract a feature for attribute "
						+ asf.getAttribute() + " for object " + cdo.getName());
				features.add(new NumericalFeature(asf.getAttribute(), 0.0));
			}
		}

		NumericalFeatureVector numericalFeatureVector = new NumericalFeatureVector(features);

		return Arrays.asList(new NumericalFeatureVector[] { numericalFeatureVector });
	}

	@Override
	public List<NumericalFeatureVector> transform(List<ComplexDataObject> inputObjects) {
		List<NumericalFeatureVector> featureVectors = new ArrayList<>();

		for (ComplexDataObject cdo : inputObjects)
			featureVectors.addAll(transform(cdo));

		return featureVectors;
	}

	@Override
	public List<IDescriptor<ComplexDataObject, NumericalFeatureVector>> getAlternativeParameterizations(int count) {
		return null;
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

	@Override
	public String getDescription() {
		return "Calculates features based on a set of attributes that have been characterized to score/rank items.";
	}

}
