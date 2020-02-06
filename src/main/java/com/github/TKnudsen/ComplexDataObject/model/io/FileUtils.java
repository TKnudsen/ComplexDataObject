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

	/**
	 * clears all files in a the given folder. for a given filename nothing will
	 * happen. use f.getParentFile() to come from a filename to a folder name.
	 * 
	 * @param folderName
	 */
	public static void clearFolder(String folderName) {
		clearFolder(new File(folderName));
	}

	/**
	 * clears all files in a the given folder. for a given filename nothing will
	 * happen. use f.getParentFile() to come from a filename to a folder name.
	 * 
	 * @param folderName
	 */
	public static void clearFolder(File folderName) {
		clearFolder(folderName, false);
	}

	/**
	 * clears all files in a the given folder. for a given filename nothing will
	 * happen. use f.getParentFile() to come from a filename to a folder name.
	 * 
	 * @param folderName
	 */
	public static void clearFolder(File folderName, boolean printOut) {
		if (!folderName.exists())
			return;

		int deleteCount = 0;
		for (File someFile : folderName.listFiles())
			if (!someFile.isDirectory())
				if (someFile.delete())
					deleteCount++;

		if (printOut && deleteCount > 0)
			System.out.println("FileUtils.clearFolder: cleared. " + deleteCount + " files deleted in folder "
					+ folderName.getName());
	}
}
