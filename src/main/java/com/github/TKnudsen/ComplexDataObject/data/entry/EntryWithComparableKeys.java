package com.github.TKnudsen.ComplexDataObject.data.entry;

import java.util.ArrayList;
import java.util.List;

import com.github.TKnudsen.ComplexDataObject.data.ranking.Ranking;

/**
 * Utility class providing factory and extraction methods for
 * {@link EntryWithComparableKey} instances and ranked collections thereof.
 *
 * <p>
 * All methods are static; this class is not meant to be instantiated.
 *
 * <p>
 * Example usage:
 * 
 * <pre>{@code
 * EntryWithComparableKey<Integer, String> entry = EntryWithComparableKeys.create(42, "hello");
 *
 * List<Integer> keys = EntryWithComparableKeys.keys(ranking);
 * List<String> values = EntryWithComparableKeys.values(ranking);
 * }</pre>
 *
 * @see EntryWithComparableKey
 * @see Ranking
 * @version 2.0 Revised in June 2026
 */
public final class EntryWithComparableKeys {

	private EntryWithComparableKeys() {
		// utility class -- do not instantiate
	}

	/**
	 * Creates a new {@link EntryWithComparableKey} for the given key-value pair.
	 *
	 * @param <K>   the key type, which must be {@link Comparable} with itself
	 * @param <V>   the value type
	 * @param key   the key; must not be {@code null}
	 * @param value the value; may be {@code null}
	 * @return a new {@code EntryWithComparableKey} holding {@code key} and
	 *         {@code value}
	 */
	public static <K extends Comparable<K>, V> EntryWithComparableKey<K, V> create(K key, V value) {
		return new EntryWithComparableKey<>(key, value);
	}

	/**
	 * Extracts the keys from a {@link Ranking} of {@link EntryWithComparableKey}
	 * objects, preserving ranking order.
	 *
	 * @param <K>     the key type, which must be {@link Comparable} with itself
	 * @param <V>     the value type
	 * @param ranking the ranked entries; may be {@code null}
	 * @return an ordered {@link List} of keys matching the ranking order, or
	 *         {@code null} if {@code ranking} is {@code null}
	 */
	public static <K extends Comparable<K>, V> List<K> keys(Ranking<EntryWithComparableKey<K, V>> ranking) {
		if (ranking == null)
			return null;

		List<K> keys = new ArrayList<>(ranking.size());
		for (EntryWithComparableKey<K, V> entry : ranking)
			keys.add(entry.getKey());

		return keys;
	}

	/**
	 * Extracts the values from a {@link Ranking} of {@link EntryWithComparableKey}
	 * objects, preserving ranking order.
	 *
	 * @param <K>     the key type, which must be {@link Comparable} with itself
	 * @param <V>     the value type
	 * @param ranking the ranked entries; may be {@code null}
	 * @return an ordered {@link List} of values matching the ranking order, or
	 *         {@code null} if {@code ranking} is {@code null}
	 */
	public static <K extends Comparable<K>, V> List<V> values(Ranking<EntryWithComparableKey<K, V>> ranking) {
		if (ranking == null)
			return null;

		List<V> values = new ArrayList<>(ranking.size());
		for (EntryWithComparableKey<K, V> entry : ranking)
			values.add(entry.getValue());

		return values;
	}
}