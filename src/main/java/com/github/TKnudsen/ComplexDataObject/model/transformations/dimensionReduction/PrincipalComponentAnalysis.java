package com.github.TKnudsen.ComplexDataObject.model.transformations.dimensionReduction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeature;
import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeatureVector;
import com.github.TKnudsen.ComplexDataObject.model.processors.complexDataObject.DataTransformationCategory;
import com.github.TKnudsen.ComplexDataObject.model.tools.WekaConversion;
import com.github.TKnudsen.ComplexDataObject.model.transformations.IDataTransformation;

import weka.attributeSelection.PrincipalComponents;
import weka.core.Instance;
import weka.core.Instances;

/**
 * <p>
 * Title: PrincipalComponentAnalysis
 * </p>
 * 
 * <p>
 * Description: Principal component analysis using WEKA's PrincipalComponents
 * algorithm. Default parameters:
 * 
 * -D Don't normalize input data.
 * 
 * -R Retain enough PC attributes to account for this proportion of variance in
 * the original data. (default = 0.95)
 * 
 * -O Transform through the PC space and back to the original space.
 * 
 * -A Maximum number of attributes to include in transformed attribute names.
 * (-1 = include all)
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2017
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */
public class PrincipalComponentAnalysis implements IDataTransformation<NumericalFeatureVector, NumericalFeatureVector> {

	private boolean normalize = true;
	private double minimumRemainingVariance = Double.NaN;
	private int outputDimensions = 2;
	private boolean transformThroughPCASpaceBackToOriginalSpace = false;

	public PrincipalComponentAnalysis(boolean normalize, int outputDimensions) {
		this.normalize = normalize;
		this.outputDimensions = outputDimensions;
	}

	public PrincipalComponentAnalysis(boolean normalize, double minimumRemainingVariance, int outputDimensions) {
		this.normalize = normalize;
		this.minimumRemainingVariance = minimumRemainingVariance;
		this.outputDimensions = outputDimensions;
	}

	private weka.attributeSelection.PrincipalComponents pca;

	private void initPCA() {
		List<String> parameters = new ArrayList<>();

		if (normalize)
			parameters.add("-D");

		if (!Double.isNaN(minimumRemainingVariance) && minimumRemainingVariance > 0.0 && minimumRemainingVariance <= 1.0) {
			parameters.add("-R");
			parameters.add("" + minimumRemainingVariance);
		}

		if (transformThroughPCASpaceBackToOriginalSpace)
			parameters.add("-O");

		if (outputDimensions > 0) {
			parameters.add("-A");
			parameters.add("" + -1);
		}

		String[] opts = new String[parameters.size()];
		opts = parameters.toArray(opts);

		pca = new PrincipalComponents();
		try {
			pca.setOptions(opts);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<NumericalFeatureVector> transform(NumericalFeatureVector input) {
		if (pca == null)
			return null;

		Instances instances = WekaConversion.getInstances(new ArrayList<NumericalFeatureVector>(Arrays.asList(input)), false);
		Iterator<Instance> iterator = instances.iterator();
		if (iterator.hasNext()) {
			try {
				Instance transformed = pca.convertInstance(iterator.next());
				NumericalFeatureVector outputFeatureVector = createNumericalFeatureVector(transformed);

				outputFeatureVector.setMaster(input);
				return new ArrayList<>(Arrays.asList(outputFeatureVector));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	@Override
	public List<NumericalFeatureVector> transform(List<NumericalFeatureVector> inputObjects) {
		if (pca == null)
			initPCA();

		Instances instances = WekaConversion.getInstances(inputObjects);
		try {
			pca.buildEvaluator(instances);
			Instances transformedData = pca.transformedData(instances);
			System.out.println(transformedData);

			// build new feature vectors
			List<NumericalFeatureVector> returnFVs = new ArrayList<>();
			for (int i = 0; i < transformedData.size(); i++) {
				Instance transformed = transformedData.get(i);

				NumericalFeatureVector outputFeatureVector = createNumericalFeatureVector(transformed);

				outputFeatureVector.setMaster(inputObjects.get(i));
				returnFVs.add(outputFeatureVector);
			}

			return returnFVs;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private NumericalFeatureVector createNumericalFeatureVector(Instance transformed) {
		double[] values = transformed.toDoubleArray();

		List<NumericalFeature> features = new ArrayList<>();
		for (int d = 0; d < Math.min(values.length, outputDimensions); d++)
			features.add(new NumericalFeature("Dim " + d, values[d]));

		NumericalFeatureVector outputFeatureVector = new NumericalFeatureVector(features);
		return outputFeatureVector;
	}

	@Override
	public DataTransformationCategory getDataTransformationCategory() {
		return DataTransformationCategory.DIMENSION_REDUCTION;
	}

	public int getOutputDimensions() {
		return outputDimensions;
	}

	public void setOutputDimensions(int outputDimensions) {
		this.outputDimensions = outputDimensions;
	}

	public double getMinimumRemainingVariance() {
		return minimumRemainingVariance;
	}

	public void setMinimumRemainingVariance(double minimumRemainingVariance) {
		this.minimumRemainingVariance = minimumRemainingVariance;
	}

	public boolean isNormalize() {
		return normalize;
	}

	public void setNormalize(boolean normalize) {
		this.normalize = normalize;
	}

	public boolean isTransformThroughPCASpaceBackToOriginalSpace() {
		return transformThroughPCASpaceBackToOriginalSpace;
	}

	public void setTransformThroughPCASpaceBackToOriginalSpace(boolean transformThroughPCASpaceBackToOriginalSpace) {
		this.transformThroughPCASpaceBackToOriginalSpace = transformThroughPCASpaceBackToOriginalSpace;
	}
}
