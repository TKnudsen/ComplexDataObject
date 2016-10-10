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
 * Copyright: Copyright (c) 2016
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.00
 */
public class BufferedImageRowColumnDiagonalsDescriptor implements INumericFeatureVectorDescriptor {

	private String bufferedImageAttributeName;

	public BufferedImageRowColumnDiagonalsDescriptor(String bufferedImageAttributeName) {
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
			int height = image.getHeight();

			// columns
			double lum = 0;
			for (int x = 0; x < width; x++) {
				lum = 0;
				for (int y = 0; y < height; y++) {
					double luminance = BufferedImageTools.getLuminanceforPixel(image, x, y);
					lum += luminance;
				}
				features.add(new NumericalFeature("Luminance column " + x, lum));
			}

			// rows
			for (int y = 0; y < height; y++) {
				lum = 0;
				for (int x = 0; x < width; x++) {
					double luminance = BufferedImageTools.getLuminanceforPixel(image, x, y);
					lum += luminance;
				}
				features.add(new NumericalFeature("Luminance row " + y, lum));
			}

			// diagonals
			double lengthMin = Math.min(width, height) * 0.5;
			double lengthAkt = 0;
			int deltaX;
			int deltaY;

			// starting west, direction north east
			for (int y = 0; y < height; y++) {
				// starting point for every diagonal
				deltaX = 0;
				deltaY = y;
				lengthAkt = 0;
				lum = 0;
				while (deltaX >= 0 && deltaX < width && deltaY >= 0 && deltaY < height) {
					lum += BufferedImageTools.getLuminanceforPixel(image, deltaX, deltaY);
					lengthAkt += 1;
					// iterate
					deltaX += 1;
					deltaY -= 1;
				}

				if (lengthAkt > lengthMin)
					features.add(new NumericalFeature("Luminance, diagonal NE, start (" + 0 + ", " + y + ")", lum));
			}

			// starting south, direction north east
			for (int x = 1; x < width; x++) {
				// starting point for every diagonal
				deltaX = x;
				deltaY = height - 1;
				lengthAkt = 0;
				lum = 0;
				while (deltaX >= 0 && deltaX < width && deltaY >= 0 && deltaY < height) {
					lum += BufferedImageTools.getLuminanceforPixel(image, deltaX, deltaY);
					lengthAkt += 1;
					// iterate
					deltaX += 1;
					deltaY -= 1;
				}

				if (lengthAkt > lengthMin)
					features.add(new NumericalFeature("Luminance, diagonal NE, start (" + x + ", " + (height - 1) + ")", lum));
			}

			// starting south, direction north west
			for (int x = width - 1; x >= 0; x--) {
				// starting point for every diagonal
				deltaX = x;
				deltaY = height - 1;
				lengthAkt = 0;
				lum = 0;
				while (deltaX >= 0 && deltaX < width && deltaY >= 0 && deltaY < height) {
					lum += BufferedImageTools.getLuminanceforPixel(image, deltaX, deltaY);
					lengthAkt += 1;
					// iterate
					deltaX -= 1;
					deltaY -= 1;
				}

				if (lengthAkt > lengthMin)
					features.add(new NumericalFeature("Luminance, diagonal NW, start (" + x + ", " + (height - 1) + ")", lum));
			}

			// starting east, direction north west
			for (int y = 0; y < height-1; y++) {
				// starting point for every diagonal
				deltaX = width - 1;
				deltaY = y;
				lengthAkt = 0;
				lum = 0;
				while (deltaX >= 0 && deltaX < width && deltaY >= 0 && deltaY < height) {
					lum += BufferedImageTools.getLuminanceforPixel(image, deltaX, deltaY);
					lengthAkt += 1;
					// iterate
					deltaX -= 1;
					deltaY -= 1;
				}

				if (lengthAkt > lengthMin)
					features.add(new NumericalFeature("Luminance, diagonal NW, start (" + (width - 1) + ", " + y + ")", lum));
			}

			List<NumericalFeatureVector> featureVectors = new ArrayList<>();
		
			NumericalFeatureVector featureVector = new NumericalFeatureVector(features);
			featureVector.setMaster(complexDataObject);
			
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

}
