package com.github.TKnudsen.ComplexDataObject.model.io.arff;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.features.FeatureVectorContainer;
import com.github.TKnudsen.ComplexDataObject.data.interfaces.IFeatureVectorObject;
import com.github.TKnudsen.ComplexDataObject.model.tools.WekaConversion;

import java.io.IOException;

import weka.core.Instances;

/**
 * <p>
 * Title: ARFFParser
 * </p>
 * 
 * <p>
 * Writes relevant object types to ARFF.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2018
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */
public class ARFFWriter {

	public void writeToARFF(ComplexDataContainer container, String fileNameWithoutExtension) {

		Instances instances = WekaConversion.getInstances(container);

		try {
			ARFFInstancesIO.saveARFF(instances, fileNameWithoutExtension + ".arff");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void writeToARFF(FeatureVectorContainer<? extends IFeatureVectorObject<?, ?>> featureContainer,
			boolean stringToNominal, String fileNameWithoutExtension) {

		Instances instances = WekaConversion.getInstances(featureContainer, stringToNominal);

		try {
			ARFFInstancesIO.saveARFF(instances, fileNameWithoutExtension + ".arff");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
