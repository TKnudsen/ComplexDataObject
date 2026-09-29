package com.github.TKnudsen.ComplexDataObject.model.processors.complexDataObject.clamping;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * <p>
 * {@link ClampConfigurationStore} backed by a single human-readable JSON
 * file. A good fit for a configuration meant to be shared across machines via
 * a synchronized folder, without needing a database.
 * </p>
 *
 * <p>
 * Loads eagerly on construction; every mutating call persists the whole file
 * immediately, so the file on disk is never more than one call stale. Not
 * safe for concurrent writers -- meant for a single human curating the
 * configuration at a time, not concurrent automated writers.
 * </p>
 *
 * @version 1.0
 * @since 2026
 */
public class FileClampConfigurationStore implements ClampConfigurationStore {

	private static final Logger LOG = Logger.getLogger(FileClampConfigurationStore.class.getName());

	private static final ObjectMapper MAPPER = createMapper();

	private static ObjectMapper createMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		return mapper;
	}

	/** Plain JSON-serializable snapshot of the whole file's content. */
	private static final class FileContent {
		public Map<String, ClampBounds> entries = new LinkedHashMap<>();
		public Set<String> ignored = new LinkedHashSet<>();
	}

	private final String filePath;
	private FileContent content = new FileContent();

	public FileClampConfigurationStore(String filePath) {
		this.filePath = filePath;

		reload();
	}

	@Override
	public void reload() {
		File file = new File(filePath);
		if (!file.exists()) {
			content = new FileContent();
			return;
		}

		try {
			content = MAPPER.readValue(file, FileContent.class);
		} catch (IOException e) {
			LOG.severe("FileClampConfigurationStore: failed to read '" + filePath + "': " + e.getMessage());
			content = new FileContent();
		}
	}

	private void persist() {
		try {
			File file = new File(filePath);
			if (file.getParentFile() != null)
				file.getParentFile().mkdirs();

			// sorted alphabetically (case-insensitive), purely for findability
			// when a human opens the file directly -- built fresh on every
			// write rather than kept sorted in `content` itself, since
			// Jackson recreates the map via its no-arg constructor on
			// deserialization and would silently drop a custom comparator
			FileContent sorted = new FileContent();
			sorted.entries = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			sorted.entries.putAll(content.entries);
			sorted.ignored = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
			sorted.ignored.addAll(content.ignored);

			MAPPER.writeValue(file, sorted);
		} catch (IOException e) {
			LOG.severe("FileClampConfigurationStore: failed to write '" + filePath + "': " + e.getMessage());
		}
	}

	@Override
	public Optional<ClampBounds> getBounds(String canonicalKey) {
		return Optional.ofNullable(content.entries.get(canonicalKey));
	}

	@Override
	public boolean isIgnored(String canonicalKey) {
		return content.ignored.contains(canonicalKey);
	}

	@Override
	public void setBounds(String canonicalKey, ClampBounds bounds) {
		content.ignored.remove(canonicalKey);
		content.entries.put(canonicalKey, bounds);

		persist();
	}

	@Override
	public void setIgnored(String canonicalKey) {
		content.entries.remove(canonicalKey);
		content.ignored.add(canonicalKey);

		persist();
	}

	@Override
	public Set<String> getKnownKeys() {
		Set<String> keys = new LinkedHashSet<>(content.entries.keySet());
		keys.addAll(content.ignored);
		return keys;
	}
}
