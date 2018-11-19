package com.github.TKnudsen.ComplexDataObject.model.transformations.descriptors.numericalFeatures.bufferedImage;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeatureVector;
import com.github.TKnudsen.ComplexDataObject.model.processors.complexDataObject.DataTransformationCategory;
import com.github.TKnudsen.ComplexDataObject.model.transformations.descriptors.IDescriptor;
import com.github.TKnudsen.ComplexDataObject.model.transformations.descriptors.numericalFeatures.INumericFeatureVectorDescriptor;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Title: BufferedImageRowColumnDiagonalsDescriptor
 * </p>
 * 
 * <p>
 * Description: Transforms BufferedImages into the numerical feature space. The
 * luminance values of rows, columns and diagonals is used to build the feature
 * space. diagonal streaks need to be at least half of min(row,column) length to
 * be considered. BufferedImage.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2016-2018
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.05
 */
public class BufferedImageRowColumnDiagonalsDescriptor
		implements INumericFeatureVectorDescriptor<ComplexDataObject> {

	private String bufferedImageAttributeName;

	/**
	 * determines which x-st row/column is used
	 */
	private int sampling = 1;

	/**
	 * determines how many pixels are skipped at each image border
	 */
	private int cropBorders = 0;

	private BufferedImageRowColumnDiagonalsRawDescriptor biDescriptor = new BufferedImageRowColumnDiagonalsRawDescriptor();

	public BufferedImageRowColumnDiagonalsDescriptor(String bufferedImageAttributeName) {
		this.bufferedImageAttributeName = bufferedImageAttributeName;
	}

	@Override
	public List<NumericalFeatureVector> transform(ComplexDataObject complexDataObject) {
		if (complexDataObject == null)
			return null;

		if (complexDataObject.getAttribute(bufferedImageAttributeName) == null)
			return null;

		List<NumericalFeatureVector> featureVectors = new ArrayList<>();

		Object object = complexDataObject.getAttribute(bufferedImageAttributeName);
		if (object instanceof BufferedImage) {
			BufferedImage image = (BufferedImage) complexDataObject.getAttribute(bufferedImageAttributeName);

			NumericalFeatureVector featureVector = biDescriptor.transform(image).get(0);

			featureVector.setMaster(complexDataObject);
			if (complexDataObject.getAttribute("Label") != null)
				featureVector.add("Label", complexDataObject.getAttribute("Label"));

			featureVectors.add(featureVector);
			return featureVectors;

		} else
			return null;
	}

	@Override
	public List<NumericalFeatureVector> transform(List<ComplexDataObject> complexDataObjects) {
		if (complexDataObjects == null)
			return null;

		List<NumericalFeatureVector> featureVectors = new ArrayList<>();

		for (ComplexDataObject complexDataObject : complexDataObjects)
			featureVectors.addAll(transform(complexDataObject));

		return featureVectors;
	}

	@Override
	public String getName() {
		return "BufferedImageRawDataDescriptor";
	}

	@Override
	public String getDescription() {
		return "Descriptor for BufferedImages returning a NumericalFeatureVector with the raw RGB pixel information";
	}

	public String getBufferedImageAttributeName() {
		return bufferedImageAttributeName;
	}

	public void setBufferedImageAttributeName(String bufferedImageAttributeName) {
		this.bufferedImageAttributeName = bufferedImageAttributeName;
	}

	public int getSampling() {
		return sampling;
	}

	public void setSampling(int sampling) {
		if (sampling < 1)
			throw new IllegalArgumentException("parameter must be >= 1");

		this.sampling = sampling;

		this.biDescriptor.setSampling(sampling);
	}

	public int getCropBorders() {
		return cropBorders;
	}

	public void setCropBorders(int cropBorders) {
		if (cropBorders < 0)
			throw new IllegalArgumentException("parameter must be >= 0");

		this.cropBorders = cropBorders;

		this.biDescriptor.setCropBorders(cropBorders);
	}

	@Override
	public List<IDescriptor<ComplexDataObject, NumericalFeatureVector>> getAlternativeParameterizations(int count) {
		return null;
	}

	@Override
	public DataTransformationCategory getDataTransformationCategory() {
		return DataTransformationCategory.DESCRIPTOR;
	}
}
