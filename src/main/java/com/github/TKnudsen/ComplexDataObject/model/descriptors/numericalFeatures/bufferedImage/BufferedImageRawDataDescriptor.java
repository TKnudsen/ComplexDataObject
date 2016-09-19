package com.github.TKnudsen.ComplexDataObject.model.descriptors.numericalFeatures.bufferedImage;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeature;
import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeatureVector;
import com.github.TKnudsen.ComplexDataObject.model.descriptors.INumericFeatureVectorDescriptor;
import com.github.TKnudsen.ComplexDataObject.model.tools.BufferedImageTools;

/**
 * <p>
 * Title: BufferedImageRawDataDescriptor
 * </p>
 * 
 * <p>
 * Description: Basic transformation of BufferedImages into the numerical
 * feature space. The luminance of every pixel creates an inidividual dimemsion.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2016
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */
public class BufferedImageRawDataDescriptor implements INumericFeatureVectorDescriptor {

	private String bufferedImageAttributeName;

	public BufferedImageRawDataDescriptor(String bufferedImageAttributeName) {
		this.bufferedImageAttributeName = bufferedImageAttributeName;
	}

	@Override
	public List<NumericalFeatureVector> transform(ComplexDataObject complexDataObject) {
		if (complexDataObject == null)
			return null;

		if (complexDataObject.get(bufferedImageAttributeName) == null)
			return null;

		Object object = complexDataObject.get(bufferedImageAttributeName);
		if (object instanceof BufferedImage) {
			BufferedImage image = (BufferedImage) complexDataObject.get(bufferedImageAttributeName);

			List<NumericalFeature> features = new ArrayList<>();

			int width = image.getWidth();
			int heigth = image.getHeight();

			for (int x = 0; x < width; x++)
				for (int y = 0; y < heigth; y++) {
					double luminance = BufferedImageTools.getLuminanceforPixel(image, x, y);
					features.add(new NumericalFeature(x + "_" + y, luminance));
				}

			List<NumericalFeatureVector> featureVectors = new ArrayList<>();
			featureVectors.add(new NumericalFeatureVector(features));
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

}
