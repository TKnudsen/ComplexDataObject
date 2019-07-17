package com.github.TKnudsen.ComplexDataObject.model.io;

import java.io.File;

/**
 * <p>
 * ComplexDataObject
 * </p>
 * 
 * <p>
 * Little helpers when working with the file system and file-IO.
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard,
 * https://github.com/TKnudsen/ComplexDataObject
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.02
 */
public class FileUtils {

	public static boolean testFileExists(String fileName) {
		File file = new File(fileName);

		boolean exists = false;
		try {
			exists = file.exists();
		} catch (Exception e) {

		}

		return exists;
	}
}
