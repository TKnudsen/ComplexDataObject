package com.github.TKnudsen.ComplexDataObject.model.descriptors.numericalFeatures.bufferedImage;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeature;
import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeatureVector;
import com.github.TKnudsen.ComplexDataObject.model.descriptors.INumericFeatureVectorDescriptor;

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
					int color = image.getRGB(x, y);
					int R = (color >>> 16) & 0xFF;
					int G = (color >>> 8) & 0xFF;
					int B = (color >>> 0) & 0xFF;
					double luminance = (0.2126 * R + 0.7152 * G + 0.0722 * B) / 255.0;
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
