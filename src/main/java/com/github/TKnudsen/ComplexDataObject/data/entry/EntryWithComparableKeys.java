package com.github.TKnudsen.ComplexDataObject.data.entry;

import java.util.ArrayList;
import java.util.List;

import com.github.TKnudsen.ComplexDataObject.data.ranking.Ranking;

public class EntryWithComparableKeys {

	public static <K extends Comparable<K>, V> EntryWithComparableKey<K, V> create(K k, V v) {
		return new EntryWithComparableKey<K, V>(k, v);
	}

	/**
	 * List of the keys of the Ranking of the EntryWithComparibleKey object.
	 * 
	 * The order of the keys pertains to the ranking order.
	 * 
	 * @param <K>
	 * @param <V>
	 * @param ranking
	 * @return
	 */
	public static <K extends Comparable<K>, V> List<K> keys(Ranking<EntryWithComparableKey<K, V>> ranking) {
		if (ranking == null)
			return null;

		List<K> keys = new ArrayList<>();
		for (EntryWithComparableKey<K, V> entry : ranking)
			keys.add(entry.getKey());

		return keys;
	}

	/**
	 * List of the values of the Ranking of the EntryWithComparibleKey object.
	 * 
	 * The order of the values pertains to the ranking order.
	 * 
	 * @param <K>
	 * @param <V>
	 * @param ranking
	 * @return
	 */
	public static <K extends Comparable<K>, V> List<V> values(Ranking<EntryWithComparableKey<K, V>> ranking) {
		if (ranking == null)
			return null;

		List<V> values = new ArrayList<>();
		for (EntryWithComparableKey<K, V> entry : ranking)
			values.add(entry.getValue());

		return values;
	}
}
