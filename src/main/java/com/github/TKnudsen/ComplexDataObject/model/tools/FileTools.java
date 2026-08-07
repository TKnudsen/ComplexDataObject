package com.github.TKnudsen.ComplexDataObject.model.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Utility methods for file system access and text and byte file I/O.
 *
 * <p>
 * Handles paths, files, directories, raw bytes, plain text, metadata, and safe
 * file-system operations. Does not handle structured content formats or
 * domain-specific parsing.
 * 
 * All methods are static.
 * </p>
 *
 * @version 2.1 revised and extended in March 2026
 */
public final class FileTools {

	private static final Logger LOG = Logger.getLogger(FileTools.class.getName());

	private FileTools() {
	}

	// ==================== EXISTENCE ====================

	public static boolean exists(String filePath) {
		if (filePath == null)
			return false;

		try {
			return exists(Paths.get(filePath));
		} catch (InvalidPathException e) {
			LOG.log(Level.FINE, "FileTools.exists: invalid path: " + filePath, e);
			return false;
		}
	}

	public static boolean exists(Path path) {
		if (path == null)
			return false;

		try {
			return Files.exists(path);
		} catch (SecurityException e) {
			LOG.log(Level.FINE, "FileTools.exists: access denied for path: " + path, e);
			return false;
		}
	}

	public static boolean exists(File file) {
		if (file == null)
			return false;

		try {
			return exists(file.toPath());
		} catch (UnsupportedOperationException e) {
			LOG.log(Level.FINE, "FileTools.exists: toPath not supported for file: " + file, e);
			return false;
		}
	}

	/** @deprecated Use {@link #exists(String)} instead. */
	@Deprecated
	public static boolean fileExists(String fileName) {
		return exists(fileName);
	}

	/** @deprecated Use {@link #exists(Path)} instead. */
	@Deprecated
	public static boolean fileExists(Path path) {
		return exists(path);
	}

	/** @deprecated Use {@link #exists(File)} instead. */
	@Deprecated
	public static boolean fileExists(File file) {
		return exists(file);
	}

	// ==================== CLEAR / DELETE ====================

	/**
	 * Deletes all direct child files of the given directory. Sub-directories are
	 * neither deleted nor traversed.
	 */
	public static void clearDirectory(String directoryPath) {
		if (directoryPath == null)
			return;
		clearDirectory(new File(directoryPath));
	}

	/**
	 * Deletes all direct child files of the given directory. Sub-directories are
	 * neither deleted nor traversed.
	 */
	public static void clearDirectory(File directory) {
		clearDirectory(directory, false);
	}

	/**
	 * Deletes all direct child files of the given directory. Sub-directories are
	 * neither deleted nor traversed.
	 */
	public static void clearDirectory(File directory, boolean logStatus) {
		if (directory == null || !directory.exists() || !directory.isDirectory())
			return;

		File[] entries = directory.listFiles();
		if (entries == null)
			return;

		int deleted = 0;
		int failed = 0;

		for (File f : entries) {
			if (!f.isFile())
				continue;

			if (f.delete())
				deleted++;
			else
				failed++;
		}

		if (logStatus && deleted > 0)
			LOG.info("FileTools.clearDirectory: cleared " + deleted + " files in " + directory.getName());

		if (failed > 0)
			LOG.fine("FileTools.clearDirectory: failed to delete " + failed + " files in " + directory.getName());
	}

	/** @deprecated Use {@link #clearDirectory(String)} instead. */
	@Deprecated
	public static void clearFolder(String folderName) {
		clearDirectory(folderName);
	}

	/** @deprecated Use {@link #clearDirectory(File)} instead. */
	@Deprecated
	public static void clearFolder(File folder) {
		clearDirectory(folder);
	}

	/** @deprecated Use {@link #clearDirectory(File, boolean)} instead. */
	@Deprecated
	public static void clearFolder(File folder, boolean printOut) {
		clearDirectory(folder, printOut);
	}

	/**
	 * Recursively deletes a directory and all its contents.
	 *
	 * <p>
	 * Delegates to Apache Commons IO. Returns {@code false} on failure. Returns
	 * {@code true} for null or non-existent input.
	 * </p>
	 */
	public static boolean deleteDirectory(File directory) {
		if (directory == null || !directory.exists())
			return true;

		try {
			org.apache.commons.io.FileUtils.deleteDirectory(directory);
			return true;
		} catch (IOException e) {
			LOG.log(Level.WARNING, "FileTools.deleteDirectory: failed for " + directory.getAbsolutePath(), e);
			return false;
		}
	}

	public static boolean deleteDirectory(Path path) {
		if (path == null)
			return true;

		try {
			return deleteDirectory(path.toFile());
		} catch (UnsupportedOperationException e) {
			LOG.log(Level.WARNING, "FileTools.deleteDirectory: toFile not supported for path " + path, e);
			return false;
		}
	}

	public static boolean deleteDirectory(String directoryPath) {
		if (directoryPath == null)
			return true;
		return deleteDirectory(new File(directoryPath));
	}

	// ==================== COLLECT / LIST ====================

	/**
	 * @deprecated Use {@link #listFiles(String, boolean, Predicate)} instead.
	 */
	@Deprecated
	public static void collectFiles(String directoryPath, List<File> files, FilenameFilter filter, boolean recursive) {
		collectFiles(directoryPath, files, filter, recursive, false);
	}

	/**
	 * @deprecated Use {@link #listFiles(String, boolean, Predicate)} instead.
	 */
	@Deprecated
	public static void collectFiles(String directoryPath, List<File> files, FilenameFilter filter, boolean recursive,
			boolean logStatus) {
		Objects.requireNonNull(directoryPath, "directoryPath must not be null");
		Objects.requireNonNull(files, "files must not be null");

//		File directory = new File(directoryPath);
//		File[] entries = directory.listFiles();
//		if (entries == null)
//			return;
//
//		for (File file : entries) {
//			if (file.isFile()) {
//				if (filter == null || filter.accept(directory, file.getName()))
//					files.add(file);
//
//				if (logStatus && files.size() % 100 == 0)
//					LOG.fine("FileTools.collectFiles: collected " + files.size() + " files");
//			} else if (file.isDirectory() && recursive) {
//				collectFiles(file.getAbsolutePath(), files, filter, true, logStatus);
//			}
//		}

		Predicate<File> predicate = filter == null ? null : file -> filter.accept(file.getParentFile(), file.getName());

		collectFilesInternal(new File(directoryPath), files, recursive, predicate, logStatus);
	}

	/**
	 * @deprecated Use {@link #listFiles(String, boolean, Predicate)} instead.
	 */
	@Deprecated
	public static List<File> listFiles(String directoryPath, FilenameFilter filter, boolean recursive) {
		Objects.requireNonNull(directoryPath, "directoryPath must not be null");
		return listFiles(new File(directoryPath), recursive,
				filter == null ? null : file -> filter.accept(file.getParentFile(), file.getName()));
	}

	/**
	 * @deprecated Use {@link #listFiles(File, boolean, Predicate)} instead.
	 */
	@Deprecated
	public static List<File> listFiles(File directory, FilenameFilter filter, boolean recursive) {
		Objects.requireNonNull(directory, "directory must not be null");
		return listFiles(directory, recursive,
				filter == null ? null : file -> filter.accept(file.getParentFile(), file.getName()));
	}

	/**
	 * @deprecated Use {@link #listFiles(Path, boolean, Predicate)} instead.
	 */
	@Deprecated
	public static List<File> listFiles(Path directory, FilenameFilter filter, boolean recursive) {
		Objects.requireNonNull(directory, "directory must not be null");
		return listFiles(directory.toFile(), recursive,
				filter == null ? null : file -> filter.accept(file.getParentFile(), file.getName()));
	}

	public static List<File> listFiles(String directoryPath) {
		return listFiles(directoryPath, false);
	}

	public static List<File> listFiles(String directoryPath, boolean recursive) {
		Objects.requireNonNull(directoryPath, "directoryPath must not be null");
		return listFiles(new File(directoryPath), recursive);
	}

	public static List<File> listFiles(File directory) {
		return listFiles(directory, false);
	}

	public static List<File> listFiles(File directory, boolean recursive) {
		Objects.requireNonNull(directory, "directory must not be null");
		return listFilesInternal(directory, recursive, null);
	}

	public static List<File> listFiles(Path directory) {
		return listFiles(directory, false);
	}

	public static List<File> listFiles(Path directory, boolean recursive) {
		Objects.requireNonNull(directory, "directory must not be null");
		return listFiles(directory.toFile(), recursive);
	}

	/**
	 * Returns all files in a directory that satisfy the given predicate.
	 *
	 * @param directoryPath the directory to scan; must not be null
	 * @param recursive     if {@code true}, sub-directories are scanned recursively
	 * @param filter        predicate to test each file; null accepts all files
	 * @return list of matching files, never null
	 */
	public static List<File> listFiles(String directoryPath, boolean recursive, Predicate<File> filter) {
		Objects.requireNonNull(directoryPath, "directoryPath must not be null");
		return listFiles(new File(directoryPath), recursive, filter);
	}

	/**
	 * Returns all files in a directory that satisfy the given predicate.
	 *
	 * @param directory the directory to scan; must not be null
	 * @param recursive if {@code true}, sub-directories are scanned recursively
	 * @param filter    predicate to test each file; null accepts all files
	 * @return list of matching files, never null
	 */
	public static List<File> listFiles(File directory, boolean recursive, Predicate<File> filter) {
		Objects.requireNonNull(directory, "directory must not be null");
		return listFilesInternal(directory, recursive, filter);
	}

	/**
	 * Returns all files in a directory that satisfy the given predicate.
	 *
	 * @param directory the directory to scan; must not be null
	 * @param recursive if {@code true}, sub-directories are scanned recursively
	 * @param filter    predicate to test each file; null accepts all files
	 * @return list of matching files, never null
	 */
	public static List<File> listFiles(Path directory, boolean recursive, Predicate<File> filter) {
		Objects.requireNonNull(directory, "directory must not be null");
		return listFiles(directory.toFile(), recursive, filter);
	}

	/**
	 * Returns all files in a directory with the given extension.
	 *
	 * <p>
	 * The extension is matched case-insensitively. A leading dot is stripped if
	 * present, so both {@code "png"} and {@code ".png"} are accepted.
	 * </p>
	 *
	 * @param directoryPath the directory to scan; must not be null
	 * @param extension     the file extension to match; must not be null or blank
	 * @param recursive     if {@code true}, sub-directories are scanned recursively
	 * @return list of matching files, never null
	 */
	public static List<File> listFilesByExtension(String directoryPath, String extension, boolean recursive) {
		Objects.requireNonNull(directoryPath, "directoryPath must not be null");
		String normalizedExt = normalizeExtension(extension);
		return listFiles(directoryPath, recursive,
				file -> getFileExtension(file.getName()).equalsIgnoreCase(normalizedExt));
	}

	public static List<File> listFilesByExtension(File directory, String extension, boolean recursive) {
		Objects.requireNonNull(directory, "directory must not be null");
		String normalizedExt = normalizeExtension(extension);
		return listFiles(directory, recursive,
				file -> getFileExtension(file.getName()).equalsIgnoreCase(normalizedExt));
	}

	public static List<File> listFilesByExtension(Path directory, String extension, boolean recursive) {
		Objects.requireNonNull(directory, "directory must not be null");
		return listFilesByExtension(directory.toFile(), extension, recursive);
	}

	private static List<File> listFilesInternal(File directory, boolean recursive, Predicate<File> filter) {
		List<File> files = new ArrayList<>();
		collectFilesInternal(directory, files, recursive, filter, false);
		return files;
	}

	private static void collectFilesInternal(File directory, List<File> files, boolean recursive,
			Predicate<File> filter, boolean logStatus) {
		Objects.requireNonNull(directory, "directory must not be null");
		Objects.requireNonNull(files, "files must not be null");

//		File[] entries = directory.listFiles();
//		if (entries == null)
//			return;
//
//		for (File entry : entries) {
//			if (entry.isFile()) {
//				if (filter == null || filter.test(entry))
//					files.add(entry);
//
//				if (logStatus && files.size() % 100 == 0)
//					LOG.fine("FileTools.collectFiles: collected " + files.size() + " files");
//			} else if (entry.isDirectory() && recursive) {
//				collectFilesInternal(entry, files, true, filter, logStatus);
//			}
//		}		
		Deque<File> stack = new ArrayDeque<>();
		stack.push(directory);

		while (!stack.isEmpty()) {
			File current = stack.pop();
			File[] entries = current.listFiles();
			if (entries == null)
				continue;

			for (File entry : entries) {
				if (entry.isFile()) {
					if (filter == null || filter.test(entry)) {
						files.add(entry);
						if (logStatus && files.size() % 100 == 0)
							LOG.log(Level.FINE, () -> "FileTools.collectFiles: collected " + files.size() + " files");
					}
				} else if (entry.isDirectory() && recursive) {
					stack.push(entry); // no stack frame consumed
				}
			}
		}
	}

	/**
	 * Returns the absolute paths of the given files as a list of strings.
	 * Convenient method to be more compatible to the old String-based usage forms.
	 *
	 * @param files the files to convert; must not be null
	 * @return list of absolute path strings, never null
	 */
	public static List<String> toAbsolutePaths(List<File> files) {
		Objects.requireNonNull(files, "files must not be null");
		List<String> paths = new ArrayList<>(files.size());
		for (File file : files)
			paths.add(file.getAbsolutePath());
		return paths;
	}

	/**
	 * Returns all immediate sub-directories of the given directory path.
	 *
	 * @param directoryPath the directory path; must not be null
	 * @return list of sub-directories; never null
	 */
	public static List<File> listSubDirectories(String directoryPath) {
		return listSubDirectories(directoryPath, false);
	}

	/**
	 * Returns sub-directories of the given directory path.
	 *
	 * @param directoryPath the directory path; must not be null
	 * @param recursive     if true, includes nested sub-directories
	 * @return list of sub-directories; never null
	 */
	public static List<File> listSubDirectories(String directoryPath, boolean recursive) {
		Objects.requireNonNull(directoryPath, "directoryPath must not be null");
		return listSubDirectories(new File(directoryPath), recursive);
	}

	/**
	 * Returns all immediate sub-directories of the given directory.
	 *
	 * @param directory the directory; must not be null
	 * @return list of sub-directories; never null
	 */
	public static List<File> listSubDirectories(File directory) {
		return listSubDirectories(directory, false);
	}

	/**
	 * Returns sub-directories of the given directory.
	 *
	 * @param directory the directory; must not be null
	 * @param recursive if true, includes nested sub-directories
	 * @return list of sub-directories; never null
	 */
	public static List<File> listSubDirectories(File directory, boolean recursive) {
		Objects.requireNonNull(directory, "directory must not be null");

		List<File> result = new ArrayList<>();
		collectSubDirectories(directory, result, recursive);
		return result;
	}

	/**
	 * Returns all immediate sub-directories of the given path.
	 *
	 * @param directory the directory path; must not be null
	 * @return list of sub-directories; never null
	 */
	public static List<File> listSubDirectories(Path directory) {
		return listSubDirectories(directory, false);
	}

	/**
	 * Returns sub-directories of the given path.
	 *
	 * @param directory the directory path; must not be null
	 * @param recursive if true, includes nested sub-directories
	 * @return list of sub-directories; never null
	 */
	public static List<File> listSubDirectories(Path directory, boolean recursive) {
		Objects.requireNonNull(directory, "directory must not be null");
		return listSubDirectories(directory.toFile(), recursive);
	}

	private static void collectSubDirectories(File directory, List<File> result, boolean recursive) {
		Objects.requireNonNull(directory, "directory must not be null");
		Objects.requireNonNull(result, "result must not be null");

		File[] entries = directory.listFiles();
		if (entries == null)
			return;

		for (File entry : entries) {
			if (!entry.isDirectory())
				continue;

			result.add(entry);

			if (recursive)
				collectSubDirectories(entry, result, true);
		}
	}

	/**
	 * This method lists files, not directories.
	 * 
	 * @deprecated Use {@link #listFiles(String, boolean)} or
	 *             {@link #listFiles(String, boolean, Predicate)} instead.
	 */
	@Deprecated
	public static void listFilesOfDirectoryAndSubdirectories(String directoryName, List<File> files,
			FilenameFilter filenameFilter, boolean querySubfolders) {
		collectFiles(directoryName, files, filenameFilter, querySubfolders);
	}

	/**
	 * This method lists files, not directories.
	 * 
	 * @deprecated Use {@link #listFiles(String, boolean)} or
	 *             {@link #listFiles(String, boolean, Predicate)} instead.
	 */
	@Deprecated
	public static void listFilesOfDirectoryAndSubdirectories(String directoryName, List<File> files,
			FilenameFilter filenameFilter, boolean querySubfolders, boolean showStatus) {
		collectFiles(directoryName, files, filenameFilter, querySubfolders, showStatus);
	}

	private static String normalizeExtension(String extension) {
		Objects.requireNonNull(extension, "extension must not be null");

		String normalized = extension.trim();
		if (normalized.startsWith("."))
			normalized = normalized.substring(1);

		if (normalized.isEmpty())
			throw new IllegalArgumentException("extension must not be blank");

		return normalized;
	}

	public static int countFiles(Path directoryPath, boolean countSubDirectories, int stopCriterion) {
		Objects.requireNonNull(directoryPath, "directoryPath must not be null");

		if (!Files.exists(directoryPath)) {
			LOG.fine("FileTools.countFiles: directory not found: " + directoryPath);
			return 0;
		}

		if (!Files.isDirectory(directoryPath)) {
			LOG.warning("FileTools.countFiles: path is not a directory: " + directoryPath);
			return 0;
		}
		int count = 0;
		try (Stream<Path> stream = Files.list(directoryPath)) {
			for (Path p : (Iterable<Path>) stream::iterator) {
				if (Files.isDirectory(p)) {
					if (countSubDirectories) {
						int remaining = stopCriterion <= 0 ? stopCriterion : stopCriterion - count;
						count += countFiles(p, true, remaining);
					}
					// directories are never counted as files
				} else if (Files.isRegularFile(p)) {
					count++;
				}

				if (stopCriterion > 0 && count >= stopCriterion) {
					LOG.fine("FileTools.countFiles: stop criterion (" + stopCriterion + ") reached.");
					return stopCriterion;
				}
			}
		} catch (AccessDeniedException e) {
			LOG.log(Level.FINE, "FileTools.countFiles: access denied for " + directoryPath, e);
		} catch (IOException e) {
			LOG.log(Level.WARNING, "FileTools.countFiles: error listing " + directoryPath, e);
		}

		return count;
	}

	public static int countFiles(File directory, boolean countSubDirectories, int stopCriterion) {
		Objects.requireNonNull(directory, "directory must not be null");
		return countFiles(directory.toPath(), countSubDirectories, stopCriterion);
	}

	public static int countFiles(String directoryPath, boolean countSubDirectories, int stopCriterion) {
		Objects.requireNonNull(directoryPath, "directoryPath must not be null");
		return countFiles(Paths.get(directoryPath), countSubDirectories, stopCriterion);
	}

	public static File getMostRecentlyModifiedFile(Iterable<File> files) {
		if (files == null)
			return null;
		long latestTime = Long.MIN_VALUE;
		File latestFile = null;
		for (File file : files) {
			if (file == null || !file.isFile())
				continue; // skip missing/non-files
			long lastModified = file.lastModified();
			if (lastModified == 0)
				continue; // lastModified() error sentinel
			if (lastModified > latestTime) {
				latestTime = lastModified;
				latestFile = file;
			}
		}
		return latestFile;
	}

	// ==================== NAMING ====================

	public static String createFileName(String raw) {
		Objects.requireNonNull(raw, "raw must not be null");
		return sanitizeName(raw, DISALLOWED_FILE_CHARS, true);
	}

	/** @deprecated Use {@link #createDirectoryName(String)} instead. */
	@Deprecated
	public static String createFileNameString(String raw) {
		return createFileName(raw);
	}

	public static String createDirectoryName(String raw) {
		Objects.requireNonNull(raw, "raw must not be null");
		return sanitizeName(raw, DISALLOWED_DIR_CHARS, false);
	}

	/** @deprecated Use {@link #createDirectoryName(String)} instead. */
	@Deprecated
	public static String createDirectoryNameString(String raw) {
		return createDirectoryName(raw);
	}

//	private static String sanitizeName(String raw, List<String> disallowedChars, boolean replaceSlash) {
//		String[] search = disallowedChars.toArray(new String[0]);
//		String[] replacements = new String[search.length];
//		Arrays.fill(replacements, "_");
//
//		String ret = StringUtils.replaceEach(raw, search, replacements);
//		ret = ret.replace("\u00E9", "e");
//
//		if (replaceSlash)
//			ret = ret.replace("/", "_");
//
//		return ret;
//	}

	private static String sanitizeName(String raw, String[] disallowed, boolean replaceSlash) {
		String[] replacements = Arrays.copyOf(REPLACEMENT_UNDERSCORES, disallowed.length);
		String ret = StringUtils.replaceEach(raw, disallowed, replacements);
		// Unicode normalization -- see issue #6
		ret = normalizeUnicode(ret);
		if (replaceSlash)
			ret = ret.replace("/", "_");
		return ret;
	}

	private static String normalizeUnicode(String input) {
		// Decompose accented chars (e.g. e-acute -> e + combining acute), then strip
		// combining
		// marks
		String decomposed = java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD);
		return decomposed.replaceAll("\\p{M}", ""); // strip all combining/diacritic marks
	}

//	private static List<String> evilCharsForFileNames() {
//		return Arrays.asList("#", "<", "$", "+", "%", ">", "!", "`", "&", "*", "\u2018", "|", "{", "?", "\u201C", "=",
//				"}", ":", "\\", "@");
//	}

//	private static List<String> evilCharsForDirectoryNames() {
//		return Arrays.asList("#", "<", "$", "+", "%", ">", "!", "`", "&", "*", "\u2018", "|", "{", "?", "\u201C", "=",
//				"}", "\\", "@");
//	}

	private static final String[] DISALLOWED_FILE_CHARS = { "#", "<", "$", "+", "%", ">", "!", "`", "&", "*", "\u2018",
			"|", "{", "?", "\u201C", "=", "}", ":", "\\", "@" };
	private static final String[] DISALLOWED_DIR_CHARS = { "#", "<", "$", "+", "%", ">", "!", "`", "&", "*", "\u2018",
			"|", "{", "?", "\u201C", "=", "}", "\\", "@" };
	private static final String[] REPLACEMENT_UNDERSCORES = Collections
			.nCopies(Math.max(DISALLOWED_FILE_CHARS.length, DISALLOWED_DIR_CHARS.length), "_").toArray(new String[0]);

	public static String getBaseName(String filePath) {
		Objects.requireNonNull(filePath, "filePath must not be null");
		return FilenameUtils.getBaseName(filePath);
	}

	public static String getFileNameWithoutExtension(String fileNameAndPath) {
		Objects.requireNonNull(fileNameAndPath, "filePath must not be null");

		int lastDot = fileNameAndPath.lastIndexOf('.');
		int lastSeparator = Math.max(fileNameAndPath.lastIndexOf('/'), fileNameAndPath.lastIndexOf('\\'));

		if (lastDot == -1 || lastDot < lastSeparator)
			return fileNameAndPath;

		return fileNameAndPath.substring(0, lastDot);
	}

	/**
	 * Returns the file-name component of {@code filePath}.
	 *
	 * @throws IllegalArgumentException if {@code filePath} is syntactically invalid
	 *                                  or has no file-name component (e.g. a root
	 *                                  path)
	 */
	public static String getFileName(String filePath) {
		Objects.requireNonNull(filePath, "filePath must not be null");
		try {
			Path p = Paths.get(filePath);
			Path fileName = p.getFileName();
			if (fileName == null)
				throw new IllegalArgumentException("Path has no file-name component: " + filePath);
			return fileName.toString();
		} catch (InvalidPathException e) {
			throw new IllegalArgumentException("Invalid path: " + filePath, e);
		}
	}

	public static String getFileExtension(String filePath) {
		Objects.requireNonNull(filePath, "filePath must not be null");
		return FilenameUtils.getExtension(filePath);
	}

	/**
	 * Returns the parent directory portion of {@code filePath} as a string.
	 *
	 * @param filePath the file path to inspect; must not be {@code null}
	 * @return the parent directory path, or {@code null} if {@code filePath} has no
	 *         parent component
	 * @throws NullPointerException     if {@code filePath} is {@code null}
	 * @throws IllegalArgumentException if {@code filePath} is not a valid path
	 *                                  string
	 * @see #getFileName(String)
	 */
	public static String getParentDirectory(String filePath) {
		Objects.requireNonNull(filePath, "filePath must not be null");
		try {
			Path parent = Paths.get(filePath).getParent();
			return parent != null ? parent.toString() : null;
		} catch (InvalidPathException e) {
			throw new IllegalArgumentException("FileTools.getParentDirectory: invalid path: " + filePath, e);
		}
	}

	/** @deprecated Use {@link #getParentDirectory(String)} instead. */
	@Deprecated
	public static String getDirectoryName(String fileName) {
		return getParentDirectory(fileName);
	}

	// ==================== READ ====================

	public static List<String> readLines(String filePath) throws IOException {
		Objects.requireNonNull(filePath, "filePath must not be null");
		return readLines(Paths.get(filePath));
	}

	public static List<String> readLines(File file) throws IOException {
		Objects.requireNonNull(file, "file must not be null");
		return readLines(file.toPath());
	}

	public static List<String> readLines(Path path) throws IOException {
		Objects.requireNonNull(path, "path must not be null");

		if (!Files.exists(path)) {
			LOG.fine("FileTools.readLines: file not found: " + path);
			return new ArrayList<>();
		}

		if (!Files.isRegularFile(path))
			throw new IOException("Path is not a regular file: " + path);

		return Files.readAllLines(path, StandardCharsets.UTF_8);
	}

	/** @deprecated Use {@link #readLines(String)} instead. */
	@Deprecated
	public static List<String> readFile(String fileName) throws IOException {
		return readLines(fileName);
	}

	/** @deprecated Use {@link #readLines(String)} instead. */
	@Deprecated
	public static List<String> loadFile(String fileName) throws IOException {
		return readLines(fileName);
	}

	public static String readText(String filePath) throws IOException {
		Objects.requireNonNull(filePath, "filePath must not be null");
		return readText(Paths.get(filePath));
	}

	public static String readText(File file) throws IOException {
		Objects.requireNonNull(file, "file must not be null");
		return readText(file.toPath());
	}

	public static String readText(Path path) throws IOException {
		Objects.requireNonNull(path, "path must not be null");

		if (!Files.exists(path)) {
			LOG.fine("FileTools.readText: file not found: " + path);
			return "";
		}

		if (!Files.isRegularFile(path))
			throw new IOException("Path is not a regular file: " + path);

		return Files.readString(path, StandardCharsets.UTF_8);
	}

	/** @deprecated Use {@link #readText(String)} instead. */
	@Deprecated
	public static String readFileAsString(String fileName) throws IOException {
		return readText(fileName);
	}

	public static byte[] readBytes(String filePath) throws IOException {
		Objects.requireNonNull(filePath, "filePath must not be null");
		return readBytes(Paths.get(filePath));
	}

	public static byte[] readBytes(File file) throws IOException {
		Objects.requireNonNull(file, "file must not be null");
		return readBytes(file.toPath());
	}

	public static byte[] readBytes(Path path) throws IOException {
		Objects.requireNonNull(path, "path must not be null");

		if (!Files.exists(path)) {
			LOG.fine("FileTools.readBytes: file not found: " + path);
			return new byte[0];
		}

		if (!Files.isRegularFile(path))
			throw new IOException("Path is not a regular file: " + path);

		return Files.readAllBytes(path);
	}

	// ==================== WRITE ====================

	public static void writeString(String filePath, String content) {
		writeString(filePath, content, false);
	}

	public static void writeString(File file, String content) {
		Objects.requireNonNull(file, "file must not be null");
		Objects.requireNonNull(content, "content must not be null");
		writeString(file.toPath(), content, false);
	}

	/**
	 * Writes a string to a text file using UTF-8, overwriting existing content.
	 * Parent directories are created if needed.
	 *
	 * @param path    the target path; must not be null
	 * @param content the content to write; must not be null
	 * @throws UncheckedIOException if writing fails
	 */
	public static void writeString(Path path, String content) {
		Objects.requireNonNull(path, "path must not be null");
		Objects.requireNonNull(content, "content must not be null");
		writeString(path, content, false);
	}

	/**
	 * If {@code append} is true and the file is non-empty, one line break is added
	 * before the appended content.
	 */
	public static void writeString(String filePath, String content, boolean append) {
		Objects.requireNonNull(filePath, "filePath must not be null");
		Objects.requireNonNull(content, "content must not be null");
		writeString(Paths.get(filePath), content, append);
	}

	public static void writeString(File file, String content, boolean append) {
		Objects.requireNonNull(file, "file must not be null");
		Objects.requireNonNull(content, "content must not be null");
		writeString(file.toPath(), content, append);
	}

	/**
	 * If {@code append} is true and the file is non-empty, one line break is added
	 * before the appended content.
	 * 
	 * 
	 */
	public static void writeString(Path path, String content, boolean append) {
		Objects.requireNonNull(path, "path must not be null");
		Objects.requireNonNull(content, "content must not be null");

		try {
			createParentDirectory(path);
			if (!append) {
				Files.writeString(path, content, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
						StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
				return;
			}
			try (java.nio.channels.SeekableByteChannel ch = Files.newByteChannel(path, StandardOpenOption.CREATE,
					StandardOpenOption.WRITE, StandardOpenOption.APPEND)) {
				boolean fileHadContent = ch.position() > 0;
				if (fileHadContent) {
					ch.write(java.nio.ByteBuffer.wrap(System.lineSeparator().getBytes(StandardCharsets.UTF_8)));
				}
				ch.write(java.nio.ByteBuffer.wrap(content.getBytes(StandardCharsets.UTF_8)));
			}
		} catch (IOException e) {
			LOG.log(Level.WARNING, "FileTools.writeString: failed to write " + path, e);
			throw new UncheckedIOException("Failed to write file: " + path, e);
		}
	}

	/**
	 * Writes all given lines to a text file using UTF-8, overwriting existing
	 * content. Parent directories are created if needed.
	 *
	 * @param filePath the target file path; must not be null
	 * @param lines    the lines to write; must not be null or contain null elements
	 * @throws UncheckedIOException if writing fails
	 */
	public static void writeLines(String filePath, Iterable<String> lines) {
		Objects.requireNonNull(filePath, "filePath must not be null");
		writeLines(Paths.get(filePath), lines);
	}

	/**
	 * Writes all given lines to a text file using UTF-8, overwriting existing
	 * content. Parent directories are created if needed.
	 *
	 * @param file  the target file; must not be null
	 * @param lines the lines to write; must not be null or contain null elements
	 * @throws UncheckedIOException if writing fails
	 */
	public static void writeLines(File file, Iterable<String> lines) {
		Objects.requireNonNull(file, "file must not be null");
		writeLines(file.toPath(), lines);
	}

	public static void writeLines(Path path, Iterable<String> lines) {
		Objects.requireNonNull(path, "path must not be null");
		Objects.requireNonNull(lines, "lines must not be null");

		// Collect and validate before opening the file
		List<String> validated = new ArrayList<>();
		int index = 0;
		for (String line : lines) {
			if (line == null)
				throw new NullPointerException("lines[" + index + "] must not be null");
			validated.add(line);
			index++;
		}

		try {
			createParentDirectory(path);
			try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
				boolean first = true;
				for (String line : validated) {
					if (!first)
						writer.newLine();
					writer.write(line);
					first = false;
				}
			}
		} catch (IOException e) {
			LOG.log(Level.WARNING, "FileTools.writeLines: failed to write " + path, e);
			throw new UncheckedIOException("Failed to write lines to file: " + path, e);
		}
	}

	/**
	 * Writes raw bytes to a file, overwriting existing content. Parent directories
	 * are created if needed.
	 *
	 * @param filePath the target file path; must not be null
	 * @param bytes    the bytes to write; must not be null
	 * @throws UncheckedIOException if writing fails
	 */
	public static void writeBytes(String filePath, byte[] bytes) {
		Objects.requireNonNull(filePath, "filePath must not be null");
		writeBytes(Paths.get(filePath), bytes);
	}

	/**
	 * Writes raw bytes to a file, overwriting existing content. Parent directories
	 * are created if needed.
	 *
	 * @param file  the target file; must not be null
	 * @param bytes the bytes to write; must not be null
	 * @throws UncheckedIOException if writing fails
	 */
	public static void writeBytes(File file, byte[] bytes) {
		Objects.requireNonNull(file, "file must not be null");
		writeBytes(file.toPath(), bytes);
	}

	/**
	 * Writes a string to a text file using UTF-8, overwriting existing content.
	 * Parent directories are created if needed.
	 *
	 * @param path    the target path; must not be null
	 * @param content the content to write; must not be null
	 * @throws UncheckedIOException if writing fails
	 */
	public static void writeBytes(Path path, byte[] bytes) {
		Objects.requireNonNull(path, "path must not be null");
		Objects.requireNonNull(bytes, "bytes must not be null");

		try {
			createParentDirectory(path);
			Files.write(path, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			LOG.log(Level.WARNING, "FileTools.writeBytes: failed to write " + path, e);
			throw new UncheckedIOException("Failed to write bytes to file: " + path, e);
		}
	}

	/** @deprecated Use {@link #writeString(String, String)} instead. */
	@Deprecated
	public static void writeToFile(String fileName, String content) {
		writeString(fileName, content, false);
	}

	/** @deprecated Use {@link #writeString(String, String, boolean)} instead. */
	@Deprecated
	public static void writeToFile(String fileName, String content, boolean append) {
		writeString(fileName, content, append);
	}

	/** @deprecated Use {@link #writeLines(String, Iterable)} instead. */
	@Deprecated
	public static void writeToFile(String fileName, Iterable<String> lines) {
		writeLines(fileName, lines);
	}

	/** @deprecated Use {@link #writeString(String, String, boolean)} instead. */
	@Deprecated
	public static void saveFile(String fileName, String content) {
		writeString(fileName, content, false);
	}

	/** @deprecated Use {@link #writeString(String, String, boolean)} instead. */
	@Deprecated
	public static void saveFile(String fileName, String content, boolean append) {
		writeString(fileName, content, append);
	}

	// ==================== COPY / MOVE ====================

	public static void copyFile(Path source, Path target) throws IOException {
		Objects.requireNonNull(source, "source must not be null");
		Objects.requireNonNull(target, "target must not be null");

		Path parent = target.getParent();
		if (parent != null)
			Files.createDirectories(parent);

		Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
	}

	public static void copyFile(File source, File target) throws IOException {
		Objects.requireNonNull(source, "source must not be null");
		Objects.requireNonNull(target, "target must not be null");
		copyFile(source.toPath(), target.toPath());
	}

	public static void copyFile(String sourcePath, String targetPath) throws IOException {
		Objects.requireNonNull(sourcePath, "sourcePath must not be null");
		Objects.requireNonNull(targetPath, "targetPath must not be null");
		copyFile(Paths.get(sourcePath), Paths.get(targetPath));
	}

	public static void moveFile(Path source, Path target) throws IOException {
		Objects.requireNonNull(source, "source must not be null");
		Objects.requireNonNull(target, "target must not be null");

		Path parent = target.getParent();
		if (parent != null)
			Files.createDirectories(parent);

		Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
	}

	public static void moveFile(File source, File target) throws IOException {
		Objects.requireNonNull(source, "source must not be null");
		Objects.requireNonNull(target, "target must not be null");
		moveFile(source.toPath(), target.toPath());
	}

	public static void moveFile(String sourcePath, String targetPath) throws IOException {
		Objects.requireNonNull(sourcePath, "sourcePath must not be null");
		Objects.requireNonNull(targetPath, "targetPath must not be null");
		moveFile(Paths.get(sourcePath), Paths.get(targetPath));
	}

	// ==================== DIRECTORIES ====================

	public static void createDirectory(String directoryPath) throws IOException {
		Objects.requireNonNull(directoryPath, "directoryPath must not be null");
		createDirectory(Paths.get(directoryPath));
	}

	public static void createDirectory(File directory) throws IOException {
		Objects.requireNonNull(directory, "directory must not be null");
		createDirectory(directory.toPath());
	}

	public static void createDirectory(Path path) throws IOException {
		Objects.requireNonNull(path, "path must not be null");
		Files.createDirectories(path);
	}

	public static void createParentDirectory(String filePath) throws IOException {
		Objects.requireNonNull(filePath, "filePath must not be null");
		createParentDirectory(Paths.get(filePath));
	}

	public static void createParentDirectory(File file) throws IOException {
		Objects.requireNonNull(file, "file must not be null");
		createParentDirectory(file.toPath());
	}

	public static void createParentDirectory(Path path) throws IOException {
		Objects.requireNonNull(path, "path must not be null");
		Path parentDir = path.normalize().getParent();
		if (parentDir != null)
			Files.createDirectories(parentDir);
	}

	// ==================== HASH ====================

	public static String getHash(Path path, String algorithm) {
		Objects.requireNonNull(path, "path must not be null");
		Objects.requireNonNull(algorithm, "algorithm must not be null");

		if (!Files.exists(path))
			throw new UncheckedIOException(new java.io.FileNotFoundException("File not found: " + path));
		if (!Files.isRegularFile(path))
			throw new IllegalArgumentException("Path is not a regular file: " + path);

		try (InputStream is = Files.newInputStream(path)) {
			MessageDigest md = MessageDigest.getInstance(algorithm);
			byte[] buffer = new byte[8192];
			int read;
			while ((read = is.read(buffer)) != -1)
				md.update(buffer, 0, read);

			byte[] digest = md.digest();
			StringBuilder sb = new StringBuilder(digest.length * 2);
			for (byte b : digest)
				sb.append(String.format("%02x", b & 0xFF));
			return sb.toString();

		} catch (NoSuchAlgorithmException e) {
			// Programming error -- algorithm name is wrong; fail fast
			throw new IllegalArgumentException("Hash algorithm not available: " + algorithm, e);
		} catch (IOException e) {
			LOG.log(Level.WARNING, e, () -> "FileTools.getHash: failed to read " + path);
			throw new UncheckedIOException("Failed to hash file: " + path, e);
		}
	}

	public static String getMD5Hash(String filePath) {
		Objects.requireNonNull(filePath, "filePath must not be null");
		if (filePath.trim().isEmpty())
			throw new IllegalArgumentException("filePath must not be blank");
		return getMD5Hash(Paths.get(filePath));
	}

	public static String getMD5Hash(File file) {
		Objects.requireNonNull(file, "file must not be null");
		return getMD5Hash(file.toPath());
	}

	public static String getMD5Hash(Path path) {
		return getHash(path, "MD5");
	}

	public static String getSHA256Hash(String filePath) {
		Objects.requireNonNull(filePath, "filePath must not be null");
		if (filePath.trim().isEmpty())
			throw new IllegalArgumentException("filePath must not be blank");
		return getSHA256Hash(Paths.get(filePath));
	}

	public static String getSHA256Hash(File file) {
		Objects.requireNonNull(file, "file must not be null");
		return getSHA256Hash(file.toPath());
	}

	public static String getSHA256Hash(Path path) {
		return getHash(path, "SHA-256");
	}
}