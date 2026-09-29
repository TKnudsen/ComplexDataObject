package com.github.TKnudsen.ComplexDataObject.model.processors.complexDataObject.clamping;

import java.util.Optional;
import java.util.Set;

/**
 * <p>
 * Persists, per canonical attribute key, either a pair of clamp percentile
 * bounds or an explicit "do not clamp this" marker. Backs {@link
 * PercentileClampingProcessor} and is what a human-in-the-loop curation tool
 * would edit.
 * </p>
 *
 * <p>
 * "Canonical key" deliberately does not have to be the exact attribute name
 * as it appears in a container -- {@link PercentileClampingProcessor} maps a
 * concrete attribute name to a canonical key via an injectable normalizer
 * (see its constructor), so that e.g. many differently-suffixed variants of
 * one underlying metric can share one entry here.
 * </p>
 *
 * @version 1.0
 * @since 2026
 */
public interface ClampConfigurationStore {

	Optional<ClampBounds> getBounds(String canonicalKey);

	boolean isIgnored(String canonicalKey);

	void setBounds(String canonicalKey, ClampBounds bounds);

	void setIgnored(String canonicalKey);

	/** Every canonical key with either registered bounds or an ignore marker. */
	Set<String> getKnownKeys();

	/** Re-reads the backing storage, discarding any unsaved in-memory state. */
	void reload();
}
