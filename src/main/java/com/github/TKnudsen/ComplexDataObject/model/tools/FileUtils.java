package com.github.TKnudsen.ComplexDataObject.model.tools;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Delegation facade over {@link FileTools}.
 *
 * <p>
 * This class exists for API compatibility only. All implementations live in
 * {@link FileTools}. New code should use {@link FileTools} directly.
 * </p>
 *
 * @version 2.1 delegating facade, March 2026
 * @see FileTools
 */
public final class FileUtils {

	private FileUtils() {
	}

	// ==================== EXISTENCE ====================

	public static boolean exists(String filePath) {
		return FileTools.exists(filePath);
	}

	public static boolean exists(Path path) {
		return FileTools.exists(path);
	}

	public static boolean exists(File file) {
		return FileTools.exists(file);
	}

	/** @deprecated Use {@link FileTools#exists(String)} instead. */
	@Deprecated
	public static boolean fileExists(String fileName) {
		return FileTools.exists(fileName);
	}

	/** @deprecated Use {@link FileTools#exists(Path)} instead. */
	@Deprecated
	public static boolean fileExists(Path path) {
		return FileTools.exists(path);
	}

	/** @deprecated Use {@link FileTools#exists(File)} instead. */
	@Deprecated
	public static boolean fileExists(File file) {
		return FileTools.exists(file);
	}

	/** @deprecated Use {@link FileTools#exists(String)} instead. */
	@Deprecated
	public static boolean testFileExists(String fileName) {
		return FileTools.exists(fileName);
	}

	// ==================== CLEAR / DELETE ====================

	public static void clearDirectory(String directoryPath) {
		FileTools.clearDirectory(directoryPath);
	}

	public static void clearDirectory(File directory) {
		FileTools.clearDirectory(directory);
	}

	public static void clearDirectory(File directory, boolean logStatus) {
		FileTools.clearDirectory(directory, logStatus);
	}

	/** @deprecated Use {@link FileTools#clearDirectory(String)} instead. */
	@Deprecated
	public static void clearFolder(String folderName) {
		FileTools.clearDirectory(folderName);
	}

	/** @deprecated Use {@link FileTools#clearDirectory(File)} instead. */
	@Deprecated
	public static void clearFolder(File folder) {
		FileTools.clearDirectory(folder);
	}

	/** @deprecated Use {@link FileTools#clearDirectory(File, boolean)} instead. */
	@Deprecated
	public static void clearFolder(File folder, boolean printOut) {
		FileTools.clearDirectory(folder, printOut);
	}

	public static boolean deleteDirectory(File directory) {
		return FileTools.deleteDirectory(directory);
	}

	public static boolean deleteDirectory(Path path) {
		return FileTools.deleteDirectory(path);
	}

	public static boolean deleteDirectory(String directoryPath) {
		return FileTools.deleteDirectory(directoryPath);
	}

	// ==================== COLLECT / LIST ====================

	public static void collectFiles(String directoryPath, List<File> files, FilenameFilter filter, boolean recursive) {
		FileTools.collectFiles(directoryPath, files, filter, recursive);
	}

	public static void collectFiles(String directoryPath, List<File> files, FilenameFilter filter, boolean recursive,
			boolean logStatus) {
		FileTools.collectFiles(directoryPath, files, filter, recursive, logStatus);
	}

	public static List<File> listFiles(String directoryPath, FilenameFilter filter, boolean recursive) {
		return FileTools.listFiles(directoryPath, filter, recursive);
	}

	public static List<File> listFiles(File directory, FilenameFilter filter, boolean recursive) {
		return FileTools.listFiles(directory, filter, recursive);
	}

	public static List<File> listFiles(Path directory, FilenameFilter filter, boolean recursive) {
		return FileTools.listFiles(directory, filter, recursive);
	}

	/**
	 * @deprecated Use
	 *             {@link FileTools#collectFiles(String, List, FilenameFilter, boolean)}
	 *             instead.
	 */
	@Deprecated
	public static void listFilesOfDirectoryAndSubdirectories(String directoryName, List<File> files,
			FilenameFilter filenameFilter, boolean querySubfolders) {
		FileTools.collectFiles(directoryName, files, filenameFilter, querySubfolders);
	}

	/**
	 * @deprecated Use
	 *             {@link FileTools#collectFiles(String, List, FilenameFilter, boolean, boolean)}
	 *             instead.
	 */
	@Deprecated
	public static void listFilesOfDirectoryAndSubdirectories(String directoryName, List<File> files,
			FilenameFilter filenameFilter, boolean querySubfolders, boolean showStatus) {
		FileTools.collectFiles(directoryName, files, filenameFilter, querySubfolders, showStatus);
	}

	public static List<File> listSubDirectories(String directoryPath) {
		return FileTools.listFiles(directoryPath);
	}

	public static List<File> listSubDirectories(String directoryPath, boolean recursive) {
		return FileTools.listFiles(directoryPath, recursive);
	}

	public static List<File> listSubDirectories(File directory) {
		return FileTools.listFiles(directory);
	}

	public static List<File> listSubDirectories(File directory, boolean recursive) {
		return FileTools.listFiles(directory, recursive);
	}

	public static List<File> listSubDirectories(Path directory) {
		return FileTools.listFiles(directory);
	}

	public static List<File> listSubDirectories(Path directory, boolean recursive) {
		return FileTools.listFiles(directory, recursive);
	}

	public static int countFiles(Path directoryPath, boolean countSubDirectories, int stopCriterion) {
		return FileTools.countFiles(directoryPath, countSubDirectories, stopCriterion);
	}

	public static int countFiles(File directory, boolean countSubDirectories, int stopCriterion) {
		return FileTools.countFiles(directory, countSubDirectories, stopCriterion);
	}

	public static int countFiles(String directoryPath, boolean countSubDirectories, int stopCriterion) {
		return FileTools.countFiles(directoryPath, countSubDirectories, stopCriterion);
	}

	public static File getMostRecentlyModifiedFile(Iterable<File> files) {
		return FileTools.getMostRecentlyModifiedFile(files);
	}

	// ==================== NAMING ====================

	/** @deprecated Use {@link FileTools#createFileName(String)} instead. */
	@Deprecated
	public static String createFileNameString(String raw) {
		return FileTools.createFileName(raw);
	}

	public static String createDirectoryName(String raw) {
		return FileTools.createDirectoryName(raw);
	}

	/** @deprecated Use {@link FileTools#createDirectoryName(String)} instead. */
	@Deprecated
	public static String createDirectoryNameString(String raw) {
		return FileTools.createDirectoryName(raw);
	}

	public static String getBaseName(String filePath) {
		return FileTools.getBaseName(filePath);
	}

	/** @deprecated Use {@link FileTools#getBaseName(String)} instead. */
	@Deprecated
	public static String getFileNameWithoutExtension(String fileName) {
		return FileTools.getFileNameWithoutExtension(fileName);
	}

	/** @deprecated Use {@link FileTools#getBaseName(String)} instead. */
	@Deprecated
	public static String getFilenameNameWithoutExtension(String fileName) {
		return FileTools.getFileNameWithoutExtension(fileName);
	}

	public static String getFileName(String filePath) {
		return FileTools.getFileName(filePath);
	}

	public static String getFileExtension(String filePath) {
		return FileTools.getFileExtension(filePath);
	}

	public static String getParentDirectory(String filePath) {
		return FileTools.getParentDirectory(filePath);
	}

	/** @deprecated Use {@link FileTools#getParentDirectory(String)} instead. */
	@Deprecated
	public static String getDirectoryName(String fileName) {
		return FileTools.getParentDirectory(fileName);
	}

	// ==================== READ ====================

	public static List<String> readLines(String filePath) throws IOException {
		return FileTools.readLines(filePath);
	}

	public static List<String> readLines(File file) throws IOException {
		return FileTools.readLines(file);
	}

	public static List<String> readLines(Path path) throws IOException {
		return FileTools.readLines(path);
	}

	/** @deprecated Use {@link FileTools#readLines(String)} instead. */
	@Deprecated
	public static List<String> readFile(String fileName) throws IOException {
		return FileTools.readLines(fileName);
	}

	/** @deprecated Use {@link FileTools#readLines(String)} instead. */
	@Deprecated
	public static List<String> loadFile(String fileName) throws IOException {
		return FileTools.readLines(fileName);
	}

	public static String readText(String filePath) throws IOException {
		return FileTools.readText(filePath);
	}

	public static String readText(File file) throws IOException {
		return FileTools.readText(file);
	}

	public static String readText(Path path) throws IOException {
		return FileTools.readText(path);
	}

	/** @deprecated Use {@link FileTools#readText(String)} instead. */
	@Deprecated
	public static String readFileAsString(String fileName) throws IOException {
		return FileTools.readText(fileName);
	}

	// ==================== WRITE ====================

	public static void writeString(String filePath, String content) {
		FileTools.writeString(filePath, content);
	}

	public static void writeString(String filePath, String content, boolean append) {
		FileTools.writeString(filePath, content, append);
	}

	public static void writeString(File file, String content, boolean append) {
		FileTools.writeString(file, content, append);
	}

	public static void writeString(Path path, String content, boolean append) {
		FileTools.writeString(path, content, append);
	}

	public static void writeLines(String filePath, Iterable<String> lines) {
		FileTools.writeLines(filePath, lines);
	}

	public static void writeLines(File file, Iterable<String> lines) {
		FileTools.writeLines(file, lines);
	}

	public static void writeLines(Path path, Iterable<String> lines) {
		FileTools.writeLines(path, lines);
	}

	/** @deprecated Use {@link FileTools#writeString(String, String)} instead. */
	@Deprecated
	public static void writeToFile(String fileName, String content) {
		FileTools.writeString(fileName, content);
	}

	/**
	 * @deprecated Use {@link FileTools#writeString(String, String, boolean)}
	 *             instead.
	 */
	@Deprecated
	public static void writeToFile(String fileName, String content, boolean append) {
		FileTools.writeString(fileName, content, append);
	}

	/** @deprecated Use {@link FileTools#writeLines(String, Iterable)} instead. */
	@Deprecated
	public static void writeToFile(String fileName, Iterable<String> lines) {
		FileTools.writeLines(fileName, lines);
	}

	/**
	 * @deprecated Use {@link FileTools#writeString(String, String, boolean)}
	 *             instead.
	 */
	@Deprecated
	public static void saveFile(String fileName, String content) {
		FileTools.writeString(fileName, content, false);
	}

	/**
	 * @deprecated Use {@link FileTools#writeString(String, String, boolean)}
	 *             instead.
	 */
	@Deprecated
	public static void saveFile(String fileName, String content, boolean append) {
		FileTools.writeString(fileName, content, append);
	}

	// ==================== COPY / MOVE ====================

	public static void copyFile(Path source, Path target) throws IOException {
		FileTools.copyFile(source, target);
	}

	public static void copyFile(File source, File target) throws IOException {
		FileTools.copyFile(source, target);
	}

	public static void copyFile(String sourcePath, String targetPath) throws IOException {
		FileTools.copyFile(sourcePath, targetPath);
	}

	public static void moveFile(Path source, Path target) throws IOException {
		FileTools.moveFile(source, target);
	}

	public static void moveFile(File source, File target) throws IOException {
		FileTools.moveFile(source, target);
	}

	public static void moveFile(String sourcePath, String targetPath) throws IOException {
		FileTools.moveFile(sourcePath, targetPath);
	}

	// ==================== DIRECTORIES ====================

	public static void createDirectory(String directoryPath) throws IOException {
		FileTools.createDirectory(directoryPath);
	}

	public static void createDirectory(File directory) throws IOException {
		FileTools.createDirectory(directory);
	}

	public static void createDirectory(Path path) throws IOException {
		FileTools.createDirectory(path);
	}

	public static void createParentDirectory(String filePath) throws IOException {
		FileTools.createParentDirectory(filePath);
	}

	public static void createParentDirectory(File file) throws IOException {
		FileTools.createParentDirectory(file);
	}

	public static void createParentDirectory(Path path) throws IOException {
		FileTools.createParentDirectory(path);
	}

	// ==================== HASH ====================

	public static String getHash(Path path, String algorithm) {
		return FileTools.getHash(path, algorithm);
	}

	public static String getMD5Hash(String filePath) {
		return FileTools.getMD5Hash(filePath);
	}

	public static String getMD5Hash(File file) {
		return FileTools.getMD5Hash(file);
	}

	public static String getMD5Hash(Path path) {
		return FileTools.getMD5Hash(path);
	}

	public static String getSHA256Hash(String filePath) {
		return FileTools.getSHA256Hash(filePath);
	}

	public static String getSHA256Hash(File file) {
		return FileTools.getSHA256Hash(file);
	}

	public static String getSHA256Hash(Path path) {
		return FileTools.getSHA256Hash(path);
	}
}