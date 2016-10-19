package com.github.TKnudsen.ComplexDataObject.model.io.writers;

import java.io.File;
import java.io.IOException;

import weka.core.Instances;
import weka.core.converters.ArffSaver;

/**
 * <p>
 * Title: WekaWriterTools
 * </p>
 * 
 * <p>
 * Description: Tools easing the output of Weka-associated data structures.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2016
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.0
 */
public class WekaWriterTools {

	/**
	 * simple Weka writer routine. Inspired by Weka's tutorial:
	 * https://weka.wikispaces.com/Save+Instances+to+an+ARFF+File
	 * 
	 * @param instances
	 * @param fileName
	 * @throws IOException
	 */
	public static void writeToFile(Instances instances, String fileName) throws IOException {
		ArffSaver saver = new ArffSaver();
		saver.setInstances(instances);
		saver.setFile(new File(fileName));
		saver.writeBatch();
	}
}
