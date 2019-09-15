package com.github.TKnudsen.ComplexDataObject.model.io.arff;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

import weka.core.Instances;
import weka.core.converters.ArffSaver;

/**
 * <p>
 * Title: ARFFInstancesIO
 * </p>
 * 
 * <p>
 * Description: Tools for the IO if Weka Instanes to *.arff and vice versa.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2018-2019
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.02
 */
public class ARFFInstancesIO {

	public static Instances loadARFF(String arffFile) {
		if (arffFile == null)
			return null;

		BufferedReader reader;
		try {
			FileReader fileReader = new FileReader(arffFile);
			reader = new BufferedReader(fileReader);

			Instances instances = new Instances(reader);

			fileReader.close();
			reader.close();

			return instances;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			System.err.println("ARFFInstancesIO.loadARFF: unable to read file " + arffFile + " . Exception caught.");
			return null;
		}
	}

	/**
	 * simple Weka writer routine. Inspired by Weka's tutorial:
	 * https://weka.wikispaces.com/Save+Instances+to+an+ARFF+File
	 * 
	 * @param instances
	 * @param fileNameWithArffExtension
	 * @throws IOException
	 */
	public static void saveARFF(Instances instances, String fileNameWithArffExtension) throws IOException {
		// unable to avoid UTF8 char encoding problem with this strategy
		// saveARFF(instances, new File(fileNameWithArffExtension));

		// unable to avoid UTF8 char encoding problem with this strategy
		// try {
		// DataSink.write(fileName, instances);
		// } catch (Exception e) {
		// System.err.println("Failed to save data to: " + fileName);
		// e.printStackTrace();
		// }

		File parentDir = new File(fileNameWithArffExtension).getParentFile();
		if (!parentDir.exists())
			parentDir.mkdirs();

		BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(fileNameWithArffExtension), "UTF8"));
		writer.write(instances.toString());
		writer.flush();
		writer.close();
	}

	public static void saveARFF(Instances instances, File arffFile) throws IOException {
		ArffSaver saver = new ArffSaver();
		saver.setInstances(instances);
		saver.setFile(arffFile);
		saver.writeBatch();
	}
}
