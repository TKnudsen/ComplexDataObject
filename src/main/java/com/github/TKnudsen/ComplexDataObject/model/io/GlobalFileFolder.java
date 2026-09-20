package com.github.TKnudsen.ComplexDataObject.model.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import com.github.TKnudsen.ComplexDataObject.model.tools.FileTools;

/**
 * Resolves the machine-specific deploy directory used as the root for all
 * framework file access operations.
 *
 * <p>
 * The deploy directory is a single folder on each machine that holds all data,
 * configuration, and credential files consumed by the framework. It lives
 * outside any git project folder, so its contents are never accidentally
 * committed and different machines can point to different locations.
 *
 * <h2>Per-project deployment folders</h2>
 * <p>
 * Each project that needs its own independent deploy directory should subclass
 * this class, pass a project-specific deploy location filename to the
 * constructor, and expose static convenience methods that delegate to a private
 * singleton instance. The instance methods in this base class are all prefixed
 * with {@code resolve} to avoid the Java restriction that prohibits static
 * methods in subclasses from hiding instance methods of the same signature in
 * the superclass.
 *
 * <pre>
 * public class GlobalStockFileFolder extends GlobalFileFolder {
 * 	private static final GlobalStockFileFolder INSTANCE = new GlobalStockFileFolder();
 *
 * 	private GlobalStockFileFolder() {
 * 		super("deploy_locations_stocks.txt");
 * 	}
 *
 * 	public static String getGlobalResourcesDir() {
 * 		return INSTANCE.resolveGlobalResourcesDir();
 * 	}
 * 	// ... etc.
 * }
 * </pre>
 *
 * <h2>How the directory is determined</h2>
 * <p>
 * The path is persisted in a small text file (whose name is given to the
 * constructor) in the working directory. On first access, if that file does not
 * exist or contains no valid path, a Swing directory chooser is shown and the
 * selected path is appended to the file for future runs. The file may contain
 * multiple lines - the first line whose path actually exists on the current
 * machine is used, which allows the same file to be shared across machines with
 * different drive layouts.
 * 
 * @version 2.0 added here in February 2026
 */
public class GlobalFileFolder {

	private static final Logger LOG = Logger.getLogger(GlobalFileFolder.class.getName());

	private final String deployLocationFile;
	private String globalResourcesDir = null;

	/**
	 * Creates a new instance backed by the given deploy location file.
	 *
	 * @param deployLocationFile name of the text file (in the working directory)
	 *                           that persists the deploy directory path; must not
	 *                           be null or blank
	 */
	public GlobalFileFolder(String deployLocationFile) {
		if (deployLocationFile == null || deployLocationFile.trim().isEmpty())
			throw new IllegalArgumentException("GlobalFileFolder: deployLocationFile must not be null or blank");
		this.deployLocationFile = deployLocationFile;
	}

	// -------------------------------------------------------------------------
	// Instance API - prefixed with "resolve" to allow subclasses to expose
	// static methods of the more natural names without triggering the Java
	// "static method cannot hide instance method" compiler error.
	// -------------------------------------------------------------------------

	/**
	 * Returns the machine-specific deploy directory, resolving it on first call.
	 *
	 * @return the absolute path to the deploy directory; never null
	 * @throws IllegalStateException if the directory cannot be determined
	 */
	public String resolveGlobalResourcesDir() {
		if (globalResourcesDir != null)
			return globalResourcesDir;

		if (!new File(deployLocationFile).exists())
			queryForFolder();

		try {
			String path = load();
			if (path != null) {
				globalResourcesDir = path;
				return globalResourcesDir;
			}

			queryForFolder();
			path = load();
			if (path != null) {
				globalResourcesDir = path;
				return globalResourcesDir;
			}
		} catch (IOException e) {
			throw new IllegalStateException(
					"GlobalFileFolder.resolveGlobalResourcesDir: failed to read " + deployLocationFile, e);
		}

		throw new IllegalStateException("GlobalFileFolder.resolveGlobalResourcesDir: unable to identify"
				+ " deploy folder. Delete " + deployLocationFile + " and restart to re-select the folder.");
	}

	/**
	 * Forces a fresh directory-chooser prompt and re-resolves the deploy
	 * directory from scratch, regardless of any previously cached or persisted
	 * value. Intended for callers that resolved a directory successfully (it
	 * exists on disk) but then discovered it's actually the wrong one -- e.g. a
	 * stale entry left over from before the deploy folder was moved -- and want
	 * to self-heal by asking the user for the current location instead of just
	 * failing. The newly picked path is written ahead of every existing entry
	 * (see {@link #write(String)}), so it takes priority over stale entries on
	 * this and future runs without discarding the file's history.
	 *
	 * @return the freshly selected deploy directory, or {@code null} if the user
	 *         cancelled the chooser dialog
	 */
	public String resolveReselectedGlobalResourcesDir() {
		globalResourcesDir = null;
		queryForFolder();

		try {
			String path = load();
			globalResourcesDir = path;
			return path;
		} catch (IOException e) {
			throw new IllegalStateException(
					"GlobalFileFolder.resolveReselectedGlobalResourcesDir: failed to read " + deployLocationFile, e);
		}
	}

	public InputStream resolveResourceAsStream(String resourceName) throws IOException {
		return new FileInputStream(resolveResourceFile(resourceName));
	}

	public URL resolveResource(String resourceName) throws IOException {
		File file = resolveResourceFile(resourceName);
		if (!file.exists())
			throw new FileNotFoundException(
					"GlobalFileFolder.resolveResource: resource not found: " + file.getAbsolutePath());
		return file.toURI().toURL();
	}

	public File resolveFile(String fileName) throws IOException {
		return resolveResourceFile(fileName);
	}

	public String resolveFileName(String fileName) {
		return resolveGlobalResourcesDir() + File.separator + fileName;
	}

	// -------------------------------------------------------------------------
	// Private helpers
	// -------------------------------------------------------------------------

	private void queryForFolder() {
		JFrame frame = new JFrame();
		frame.setAlwaysOnTop(true);
		frame.setVisible(true);

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setDialogTitle("Choose the Data Folder");

		int result = fileChooser.showOpenDialog(frame);
		frame.dispose();

		if (result == JFileChooser.APPROVE_OPTION) {
			String path = fileChooser.getSelectedFile().getAbsolutePath();
			LOG.info("GlobalFileFolder: selected deploy directory: " + path);
			write(path);
		} else {
			LOG.warning("GlobalFileFolder: no directory selected in chooser dialog.");
		}
	}

	private String load() throws IOException {
		List<String> lines = FileTools.readLines(deployLocationFile.toString());
	    for (String line : lines) {
	        String trimmed = line.trim();
	        if (!trimmed.isEmpty() && Files.isDirectory(Paths.get(trimmed)))
	            return trimmed;
	    }
	    return null;
	}

	/**
	 * Writes {@code folderPath} as the new first line of {@link
	 * #deployLocationFile}, ahead of whatever lines are already there. {@link
	 * #load()} always returns the first existing directory it finds, so a freshly
	 * confirmed selection must win over any older entries that may still happen
	 * to exist on disk (e.g. a stale mirror/backup folder from before a move) --
	 * appending to the end would otherwise leave a stale-but-still-present entry
	 * in permanent control of resolution. Existing lines are kept, not discarded,
	 * so the file still doubles as a per-machine history of prior locations.
	 */
	private void write(String folderPath) {
		try {
			List<String> existingLines;
			try {
				existingLines = FileTools.readLines(deployLocationFile.toString());
			} catch (IOException e) {
				existingLines = java.util.Collections.emptyList();
			}

			StringBuilder content = new StringBuilder(folderPath);
			for (String line : existingLines)
				if (!line.trim().equals(folderPath))
					content.append(System.lineSeparator()).append(line);

			FileTools.writeString(deployLocationFile, content.toString(), false);
			LOG.info("GlobalFileFolder: deploy path written to " + deployLocationFile);
		} catch (UncheckedIOException e) {
			LOG.log(Level.WARNING, "GlobalFileFolder: failed to write deploy path to " + deployLocationFile, e);
		}
	}

	private File resolveResourceFile(String fileName) throws IOException {
		return new File(resolveGlobalResourcesDir(), fileName);
	}
}