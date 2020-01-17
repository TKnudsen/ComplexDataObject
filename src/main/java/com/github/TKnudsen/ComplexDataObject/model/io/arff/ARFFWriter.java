package com.github.TKnudsen.ComplexDataObject.model.io.arff;

import java.io.IOException;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.features.FeatureVectorContainer;
import com.github.TKnudsen.ComplexDataObject.data.interfaces.IFeatureVectorObject;
import com.github.TKnudsen.ComplexDataObject.model.tools.WekaConversion;

import weka.core.Instances;

/**
 * <p>
 * Writes relevant data contained in ComplexDataContainer to ARFF.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2018-2020
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.02
 */
public class ARFFWriter {

	/**
	 * no file ending (arff) required
	 * 
	 * @param container
	 * @param fileNameWithoutExtension
	 */
	public void writeToARFF(ComplexDataContainer container, String fileNameWithoutExtension) {

		Instances instances = WekaConversion.getInstances(container);

		try {
			ARFFInstancesIO.saveARFF(instances, fileNameWithoutExtension + ".arff");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * no file ending (arff) required
	 * 
	 * @param container
	 * @param fileNameWithoutExtension
	 * @param relationName
	 */
	public void writeToARFF(ComplexDataContainer container, String fileNameWithoutExtension, String relationName) {

		Instances instances = WekaConversion.getInstances(container);
		instances.setRelationName(relationName);

		try {
			ARFFInstancesIO.saveARFF(instances, fileNameWithoutExtension + ".arff");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * no file ending (arff) required
	 * 
	 * @param featureContainer
	 * @param stringToNominal
	 * @param fileNameWithoutExtension
	 */
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
