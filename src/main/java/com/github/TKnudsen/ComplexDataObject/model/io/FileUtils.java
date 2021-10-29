package com.github.TKnudsen.ComplexDataObject.model.io;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.List;

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
 * Copyright: (c) 2016-2020 Juergen Bernard,
 * https://github.com/TKnudsen/ComplexDataObject
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.03
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

	/**
	 * recursive method that retrieves all files in a given directory.
	 * 
	 * @param directoryName   where to look at
	 * @param files           target list that is filled
	 * @param filenameFilter  some filter for specific files matching the filter
	 * @param querySubfolders as to whether or not all sub-folders shall be
	 *                        traversed as well
	 */
	public static void listFilesOfDirectoryAndSubdirectories(String directoryName, List<File> files,
			FilenameFilter filenameFilter, boolean querySubfolders) {
		listFilesOfDirectoryAndSubdirectories(directoryName, files, filenameFilter, querySubfolders, false);
	}

	/**
	 * recursive method that retrieves all files in a given directory.
	 * 
	 * @param directoryName   where to look at
	 * @param files           target list that is filled
	 * @param filenameFilter  some filter for specific files matching the filter
	 * @param querySubfolders as to whether or not all sub-folders shall be
	 *                        traversed as well
	 * @param showStatus      creates a dot for every 100 files if true
	 */
	public static void listFilesOfDirectoryAndSubdirectories(String directoryName, List<File> files,
			FilenameFilter filenameFilter, boolean querySubfolders, boolean showStatus) {
		File directory = new File(directoryName);

		File[] fList = directory.listFiles();

		if (fList != null)
			for (File file : fList) {
				if (file.isFile()) {
					if (filenameFilter == null)
						files.add(file);
					else if (filenameFilter.accept(directory, file.getName()))
						files.add(file);
					if (showStatus && files.size() % 100 == 0)
						System.out.print(".");
				} else if (file.isDirectory() && querySubfolders) {
					listFilesOfDirectoryAndSubdirectories(file.getAbsolutePath(), files, filenameFilter,
							querySubfolders, showStatus);
				}
			}
	}

	/**
	 * file name is compatible to Windows file systems
	 * 
	 * @param raw
	 * @return
	 */
	public static String createFileNameString(String raw) {
		String ret = raw;

		List<String> evilChars = evilCharsForFileNames();
		for (String s : evilChars)
			ret = ret.replace(s, "_");

		ret = ret.replace("é", "e");

		ret = ret.replaceAll("/", "_");

		return ret;
	}

	/**
	 * directory name is compatible to Windows file systems
	 * 
	 * @param raw
	 * @return
	 */
	public static String createDirectoryNameString(String raw) {
		String ret = raw;

		List<String> evilChars = evilCharsForDirectoryNames();
		for (String s : evilChars)
			ret = ret.replace(s, "_");

		ret = ret.replace("é", "e");

		return ret;
	}

	private static List<String> evilCharsForFileNames() {
		return Arrays.asList("#", "<", "$", "+", "%", ">", "!", "`", "&", "*", "‘", "|", "{", "?", "“", "=", "}", ":",
				"\\", "@"); // "/",
	}

	private static List<String> evilCharsForDirectoryNames() {
		return Arrays.asList("#", "<", "$", "+", "%", ">", "!", "`", "&", "*", "‘", "|", "{", "?", "“", "=", "}", // ":"
				"\\", "@"); // "/",
	}
}
