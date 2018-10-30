package com.github.TKnudsen.ComplexDataObject.model.io.arff;

import java.io.IOException;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.features.FeatureVectorContainer;
import com.github.TKnudsen.ComplexDataObject.data.interfaces.IFeatureVectorObject;
import com.github.TKnudsen.ComplexDataObject.model.tools.WekaConversion;

import weka.core.Instances;

/**
 * <p>
 * Title: ARFFParser
 * </p>
 * 
 * <p>
 * Description: Parses ComplexDataObjects from an ARFF file. Note: this parser
 * is not part of the persistence layer. In fact, it gathers new
 * ComplexDataObjects from a given file.
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
