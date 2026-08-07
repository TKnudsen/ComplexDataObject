package com.github.TKnudsen.ComplexDataObject.model.tools;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Utility class providing conversion methods for common Java data structures.
 *
 * <p>
 * Covers the following conversion categories:
 * </p>
 * <ul>
 * <li><b>Primitive arrays</b>: {@code double[]}, {@code int[]},
 * {@code Double[]}, {@code double[]} to/from {@link List}, {@link Collection}</li>
 * <li><b>Collection conversions</b>: {@link Collection} to {@link List},
 * {@link Set} to {@link List}, {@link List} to array, 2D array to
 * {@link List}</li>
 * <li><b>Frequency utilities</b>: occurrence counting, ratio computation,
 * frequency map expansion</li>
 * <li><b>Type classification</b>: numerical, boolean, and categorical type
 * detection for statistical analysis</li>
 * </ul>
 *
 * <p>
 * <b>Null handling:</b> all methods throw {@link NullPointerException} for null
 * collection or array inputs unless explicitly documented otherwise. Null
 * <em>elements</em> within collections are generally permitted and preserved.
 * </p>
 *
 * <p>
 * <b>Mutability:</b> returned lists are mutable {@link ArrayList} instances
 * unless documented as fixed-size (e.g. {@link #doublePrimitivesToList}).
 * </p>
 *
 * <p>
 * <b>Thread safety:</b> all methods are stateless and safe for concurrent use.
 * </p>
 *
 * @version 2.0 revised in February 2026
 * @since 2017
 */

public final class DataConversion {

	private DataConversion() {
	}

	/**
	 * Converts a collection of {@link Number} values to a {@code double[]} array of
	 * primitives.
	 *
	 * <p>
	 * Uses a single pre-allocated array pass -- faster than a stream pipeline for
	 * large collections since it avoids internal buffer resizing and per-element
	 * boxing overhead.
	 * </p>
	 *
	 * <p>
	 * The collection must not contain {@code null} elements; a
	 * {@code NullPointerException} will be thrown during iteration if it does.
	 * </p>
	 *
	 * @param values the collection to convert; must not be null
	 * @return a {@code double[]} of the same size and order as the input
	 * @throws NullPointerException if values is null or contains null elements
	 */
	public static double[] toPrimitives(Collection<? extends Number> values) {
		Objects.requireNonNull(values, "values must not be null");

		double[] result = new double[values.size()];
		int i = 0;
		for (Number n : values)
			result[i++] = n.doubleValue();

		return result;
	}

	/**
	 * Converts a {@link Double} object array to a {@code double[]} primitive array.
	 *
	 * <p>
	 * Delegates to {@link ArrayUtils#toPrimitive(Double[])}. The array must not
	 * contain {@code null} elements; a {@code NullPointerException} will be thrown
	 * if it does.
	 * </p>
	 *
	 * @param values the array to convert; must not be null
	 * @return a {@code double[]} of the same size and order as the input
	 * @throws NullPointerException if values is null or contains null elements
	 */
	public static double[] toPrimitives(Double[] values) {
		Objects.requireNonNull(values, "values must not be null");

		return ArrayUtils.toPrimitive(values);
	}

	/**
	 * Converts a collection of {@link Number} values to an {@code int[]} array of
	 * primitives using {@link Number#intValue()} on each element.
	 *
	 * <p>
	 * Uses a single pre-allocated array pass -- faster than a stream pipeline for
	 * large collections since it avoids internal buffer resizing and per-element
	 * boxing overhead.
	 * </p>
	 *
	 * <p>
	 * Note: {@code intValue()} truncates floating-point values toward zero (e.g.
	 * {@code 3.9} becomes {@code 3}). If truncation is not intended, consider
	 * rounding before conversion.
	 * </p>
	 *
	 * <p>
	 * The collection must not contain {@code null} elements; a
	 * {@code NullPointerException} will be thrown during iteration if it does.
	 * </p>
	 *
	 * @param values the collection to convert; must not be null
	 * @return an {@code int[]} of the same size and order as the input
	 * @throws NullPointerException if values is null or contains null elements
	 */
	public static int[] toIntPrimitives(Collection<? extends Number> values) {
		Objects.requireNonNull(values, "values must not be null");

		int[] result = new int[values.size()];
		int i = 0;
		for (Number n : values)
			result[i++] = n.intValue();

		return result;
	}

	/**
	 * Converts a {@code double[]} primitive array to a {@link Double} object array.
	 *
	 * <p>
	 * Delegates to {@link ArrayUtils#toObject(double[])}. The result never contains
	 * {@code null} elements.
	 * </p>
	 *
	 * @param values the array to convert; must not be null
	 * @return a {@link Double}{@code []} of the same size and order as the input
	 * @throws NullPointerException if values is null
	 */
	public static Double[] doubleToArray(double[] values) {
		Objects.requireNonNull(values, "values must not be null");

		return ArrayUtils.toObject(values);
	}

	/**
	 * Converts a {@code double[]} primitive array to a mutable {@link List} of
	 * {@link Double} objects.
	 *
	 * <p>
	 * The returned list is a fully mutable {@link ArrayList} -- elements may be
	 * added, removed, and replaced.
	 * </p>
	 *
	 * @param values the array to convert; must not be null
	 * @return a mutable {@link List} of the same size and order as the input
	 * @throws NullPointerException if values is null
	 */
	public static List<Double> doubleToList(double[] values) {
		Objects.requireNonNull(values, "values must not be null");

		List<Double> result = new ArrayList<>(values.length);
		for (double v : values)
			result.add(v);

		return result;
	}

	/**
	 * Converts a {@code double[]} primitive array to a mutable {@link List} of
	 * {@link Number} objects.
	 *
	 * <p>
	 * The returned list is a fully mutable {@link ArrayList} -- elements may be
	 * added, removed, and replaced.
	 * </p>
	 *
	 * @param values the array to convert; must not be null
	 * @return a mutable {@link List} of the same size and order as the input
	 * @throws NullPointerException if values is null
	 */
	public static List<Number> doubleToNumberList(double[] values) {
		Objects.requireNonNull(values, "values must not be null");

		List<Number> result = new ArrayList<>(values.length);
		for (double v : values)
			result.add(v);

		return result;
	}

	/**
	 * Converts an {@code int[]} primitive array to a mutable {@link List} of
	 * {@link Integer} objects.
	 *
	 * <p>
	 * The returned list is a fully mutable {@link ArrayList} -- elements may be
	 * added, removed, and replaced.
	 * </p>
	 *
	 * @param values the array to convert; must not be null
	 * @return a mutable {@link List} of the same size and order as the input
	 * @throws NullPointerException if values is null
	 */
	public static List<Integer> intPrimitivesToList(int[] values) {
		Objects.requireNonNull(values, "values must not be null");

		List<Integer> result = new ArrayList<>(values.length);
		for (int v : values)
			result.add(v);

		return result;
	}

	/**
	 * Converts a {@code T[]} array to a mutable {@link List} of the same type.
	 *
	 * <p>
	 * The returned list is a fully mutable {@link ArrayList} -- elements may be
	 * added, removed, and replaced.
	 * </p>
	 *
	 * <p>
	 * The array may contain {@code null} elements; these are preserved as-is in the
	 * returned list.
	 * </p>
	 *
	 * @param <T>    the element type
	 * @param values the array to convert; must not be null
	 * @return a mutable {@link List} of the same size and order as the input
	 * @throws NullPointerException if values is null
	 */
	public static <T> List<T> arrayToList(T[] values) {
		Objects.requireNonNull(values, "values must not be null");

		return new ArrayList<>(Arrays.asList(values));
	}

	/**
	 * Flattens a 2D array into a mutable {@link List} in row-major order.
	 *
	 * <p>
	 * The returned list is a fully mutable {@link ArrayList} -- elements may be
	 * added, removed, and replaced.
	 * </p>
	 *
	 * <p>
	 * Elements may be {@code null}; these are preserved as-is in the returned list.
	 * Rows must not be {@code null}.
	 * </p>
	 *
	 * @param <T>    the element type
	 * @param values the 2D array to flatten; must not be null, rows must not be
	 *               null
	 * @return a mutable {@link List} containing all elements in row-major order
	 * @throws NullPointerException if values is null or any row is null
	 */
	public static <T> List<T> array2DToList(T[][] values) {
		Objects.requireNonNull(values, "values must not be null");

		int totalSize = 0;
		for (T[] row : values) {
			Objects.requireNonNull(row, "row in values must not be null");
			totalSize += row.length;
		}
		List<T> list = new ArrayList<>(totalSize);
		for (T[] row : values)
			for (T item : row)
				list.add(item);

		return list;
	}

	/**
	 * Converts a {@link Collection} to a {@code T[]} array.
	 *
	 * <p>
	 * Delegates to {@link #collectionToList(Collection)} followed by
	 * {@link #listToArray(List, Class)}.
	 * </p>
	 *
	 * <p>
	 * Elements may be {@code null}; these are preserved as-is in the returned
	 * array.
	 * </p>
	 *
	 * @param <T>        the element type
	 * @param collection the collection to convert; must not be null
	 * @param classType  the component type of the target array; must not be null
	 * @return a {@code T[]} of the same size and order as the input collection
	 * @throws NullPointerException if collection or classType is null
	 */
	public static <T> T[] collectionToArray(Collection<T> collection, Class<T> classType) {
		Objects.requireNonNull(collection, "collection must not be null");
		Objects.requireNonNull(classType, "classType must not be null");

		return listToArray(collectionToList(collection), classType);
	}

	/**
	 * Converts a {@link Collection} to a {@link List}.
	 *
	 * <p>
	 * If the collection is already a {@link List}, it is returned directly without
	 * copying -- the caller and the original collection share the same backing list.
	 * If a defensive copy is required, wrap the result in
	 * {@code new ArrayList<>(result)}.
	 * </p>
	 *
	 * <p>
	 * If the collection is not a {@link List}, a new mutable {@link ArrayList} is
	 * returned containing all elements in iteration order.
	 * </p>
	 *
	 * <p>
	 * Elements may be {@code null}; these are preserved as-is.
	 * </p>
	 *
	 * @param <T>        the element type
	 * @param collection the collection to convert; must not be null
	 * @return the collection as a {@link List}; may be the same instance if the
	 *         input is already a {@link List}
	 * @throws NullPointerException if collection is null
	 */
	public static <T> List<T> collectionToList(Collection<T> collection) {
		Objects.requireNonNull(collection, "collection must not be null");

		if (collection instanceof List)
			return (List<T>) collection;

		return new ArrayList<>(collection);
	}

	/**
	 * Filters and casts a collection of objects to those assignable to {@code type}
	 * and returns them as a typed {@link List}, casting each matching element in a
	 * single pass.
	 *
	 * <p>
	 * Elements that are {@code null} or not an instance of {@code type} are
	 * silently skipped. No {@link ClassCastException} can occur since
	 * {@link Class#isInstance} is used before the cast.
	 * </p>
	 *
	 * <p>
	 * Example:
	 * </p>
	 * 
	 * <pre>
	 * List&lt;Object&gt; mixed = Arrays.asList("hello", 42, "world", 3.14);
	 * List&lt;String&gt; strings = filterAndCast(mixed, String.class);
	 * // to ["hello", "world"]
	 * </pre>
	 *
	 * @param <T>    the target element type
	 * @param values the source collection; must not be null
	 * @param type   the class to filter and cast to; must not be null
	 * @return a mutable {@link List} containing only elements assignable to
	 *         {@code type}, cast to {@code T}; never null
	 * @throws NullPointerException if values or type is null
	 */
	public static <T> List<T> collectionToList(Collection<?> objects, Class<T> type) {
		Objects.requireNonNull(objects, "objects must not be null");
		Objects.requireNonNull(type, "type must not be null");

		List<T> result = new ArrayList<>();
		for (Object o : objects)
			if (type.isInstance(o))
				result.add(type.cast(o));

		return result;
	}

	/**
	 * Converts a {@link Set} to a mutable {@link ArrayList}.
	 *
	 * <p>
	 * The iteration order of the returned list reflects the iteration order of the
	 * input set -- deterministic for {@link java.util.LinkedHashSet} and
	 * {@link java.util.TreeSet}, unspecified for {@link java.util.HashSet}.
	 * </p>
	 *
	 * <p>
	 * Elements may be {@code null} if the set permits them; these are preserved
	 * as-is in the returned list.
	 * </p>
	 *
	 * @param <T> the element type
	 * @param set the set to convert; must not be null
	 * @return a mutable {@link List} containing all elements of the set
	 * @throws NullPointerException if set is null
	 */
	public static <T> List<T> setToList(Set<T> set) {
		Objects.requireNonNull(set, "set must not be null");

		return new ArrayList<>(set);
	}

	/**
	 * Widens a {@link List} of {@link Double} values to a {@link List} of
	 * {@link Number}.
	 *
	 * <p>
	 * Required because Java generics are invariant -- {@code List<Double>} cannot be
	 * assigned to {@code List<Number>} directly. Each element is preserved as a
	 * {@link Double} instance; no conversion or copying of the values occurs.
	 * </p>
	 *
	 * <p>
	 * Elements may be {@code null}; these are preserved as-is.
	 * </p>
	 *
	 * @param values the list to widen; must not be null
	 * @return a new mutable {@link List} of the same size and order as the input
	 * @throws NullPointerException if values is null
	 */
	public static List<Number> doubleListToNumberList(List<Double> values) {
		Objects.requireNonNull(values, "values must not be null");

		List<Number> numbers = new ArrayList<>(values.size());
		for (Double d : values)
			numbers.add(d);

		return numbers;
	}

	/**
	 * Converts a collection of {@link Number} values to a mutable {@link List} of
	 * {@link Double} by calling {@link Number#doubleValue()} on each element.
	 *
	 * <p>
	 * Useful when working with mixed {@link Number} subtypes ({@link Integer},
	 * {@link Float}, {@link Long}, etc.) that need to be uniformly represented as
	 * {@link Double} for statistical computation.
	 * </p>
	 *
	 * <p>
	 * The collection must not contain {@code null} elements; a
	 * {@code NullPointerException} will be thrown during iteration if it does.
	 * </p>
	 *
	 * @param values the collection to convert; must not be null
	 * @return a mutable {@link List} of the same size and order as the input
	 * @throws NullPointerException if values is null or contains null elements
	 */
	public static List<Double> listWithNumbersToDoubleList(Collection<? extends Number> values) {
		Objects.requireNonNull(values, "values must not be null");

//		List<Double> result = new ArrayList<>(values.size());
//		for (Number n : values)
//			result.add(n.doubleValue());
//
//		return result;
		
		return values.stream().map(Number::doubleValue).collect(Collectors.toList());
	}
	
	/**
	 * Converts a {@link List} to a {@code T[]} array of the specified component
	 * type.
	 *
	 * <p>
	 * Uses reflection via {@link Array#newInstance} to create a typed array, then
	 * delegates to {@link List#toArray(Object[])} for the copy.
	 * </p>
	 *
	 * <p>
	 * Elements may be {@code null}; these are preserved as-is in the returned
	 * array.
	 * </p>
	 *
	 * @param <T>       the element type
	 * @param list      the list to convert; must not be null
	 * @param classType the component type of the target array; must not be null
	 * @return a {@code T[]} of the same size and order as the input list
	 * @throws NullPointerException if list or classType is null
	 */
	public static <T> T[] listToArray(List<T> list, Class<T> classType) {
		Objects.requireNonNull(list, "list must not be null");
		Objects.requireNonNull(classType, "classType must not be null");

		@SuppressWarnings("unchecked")
		T[] array = (T[]) Array.newInstance(classType, list.size());

		return list.toArray(array);
	}

	/**
	 * Zips two lists of equal size into a {@link Map} by pairing each key at index
	 * {@code i} with the value at index {@code i}.
	 *
	 * <p>
	 * If duplicate keys exist in {@code keys}, later values overwrite earlier ones
	 * -- consistent with standard {@link Map} semantics.
	 * </p>
	 *
	 * <p>
	 * Both keys and values may be {@code null} if the underlying {@link Map}
	 * permits them.
	 * </p>
	 *
	 * <p>
	 * Example:
	 * </p>
	 * 
	 * <pre>
	 * List&lt;String&gt; keys = Arrays.asList("a", "b", "c");
	 * List&lt;Integer&gt; values = Arrays.asList(1, 2, 3);
	 * Map&lt;String, Integer&gt; map = listPairToMap(keys, values);
	 * // to {a=1, b=2, c=3}
	 * </pre>
	 *
	 * @param <K>    the key type
	 * @param <V>    the value type
	 * @param keys   the list of keys; must not be null
	 * @param values the list of values; must not be null, must be the same size as
	 *               {@code keys}
	 * @return a mutable {@link LinkedHashMap} preserving the iteration order of the
	 *         input lists
	 * @throws NullPointerException     if keys or values is null
	 * @throws IllegalArgumentException if keys and values differ in size
	 */
	public static <K, V> Map<K, V> listPairToMap(List<K> keys, List<V> values) {
		Objects.requireNonNull(keys, "keys must not be null");
		Objects.requireNonNull(values, "values must not be null");

		if (keys.size() != values.size())
			throw new IllegalArgumentException("DataConversion.zip: keys and values must be the same size ("
					+ keys.size() + " vs " + values.size() + ")");

		Map<K, V> result = new LinkedHashMap<>((int) (keys.size() / 0.75f) + 1);
		for (int i = 0; i < keys.size(); i++)
			result.put(keys.get(i), values.get(i));

		return result;
	}

	/**
	 * Creates a mutable {@link List} of {@code count} elements all set to
	 * {@code t}.
	 *
	 * <p>
	 * Delegates to {@link Collections#nCopies} wrapped in a new {@link ArrayList}
	 * to ensure mutability.
	 * </p>
	 *
	 * <p>
	 * {@code t} may be {@code null} -- useful for pre-allocating a list of null
	 * sentinels to be filled in later.
	 * </p>
	 *
	 * @param <T>   the element type
	 * @param t     the constant value to repeat; may be null
	 * @param count the number of elements; must be > 0
	 * @return a mutable {@link List} of {@code count} copies of {@code t}
	 * @throws IllegalArgumentException if count is negative
	 */
	public static <T> List<T> constantValueList(T t, int count) {
		if (count < 0)
			throw new IllegalArgumentException("DataConversion.constantValueList: count must be >= 0, got " + count);

		return new ArrayList<>(Collections.nCopies(count, t));
	}

	/**
	 * Counts the frequency of each string value in the collection.
	 *
	 * <p>
	 * Returns a {@link LinkedHashMap} sorted by frequency in descending order (most
	 * frequent first). Entries with equal frequency retain their natural encounter
	 * order.
	 * </p>
	 *
	 * <p>
	 * {@code null} elements are counted as a distinct key if present in the
	 * collection.
	 * </p>
	 *
	 * @param values the collection of strings to count; must not be null
	 * @return a {@link LinkedHashMap} mapping each unique value to its occurrence
	 *         count, sorted by descending frequency
	 * @throws NullPointerException if values is null
	 */
	public static Map<String, Integer> countOccurrences(Collection<String> values) {
		Objects.requireNonNull(values, "values must not be null");

		// count into LinkedHashMap to preserve first-seen order for stable tie-breaking
		Map<String, Integer> counts = new LinkedHashMap<>();
		for (String value : values)
			counts.merge(value, 1, Integer::sum);

		LinkedHashMap<String, Integer> sorted = new LinkedHashMap<>();
		counts.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
				.forEach(e -> sorted.put(e.getKey(), e.getValue()));

		return sorted;
	}

	/**
	 * Counts the frequency of each string value in the collection and returns the
	 * {@code mostFrequent} most common entries.
	 *
	 * <p>
	 * Uses a min-heap of size {@code mostFrequent} -- O(n + u log k) where {@code u}
	 * is the number of distinct values and {@code k = mostFrequent}. Significantly
	 * faster than full sort when {@code mostFrequent << u}.
	 * </p>
	 *
	 * <p>
	 * The returned {@link LinkedHashMap} is sorted by frequency in descending
	 * order. Ties are broken by first-seen order -- earlier first-seen entries are
	 * preferred over later ones.
	 * </p>
	 *
	 * <p>
	 * {@code null} elements are counted as a distinct key if present.
	 * </p>
	 *
	 * @param values       the collection of strings to count; must not be null
	 * @param mostFrequent the maximum number of entries to return; must be &ge; 0
	 * @return a {@link LinkedHashMap} of at most {@code mostFrequent} entries,
	 *         sorted by descending frequency; ties broken by first-seen order
	 * @throws NullPointerException     if values is null
	 * @throws IllegalArgumentException if mostFrequent is negative
	 */
	public static Map<String, Integer> countOccurrences(Collection<String> values, int mostFrequent) {
		Objects.requireNonNull(values, "values must not be null");

		if (mostFrequent < 0)
			throw new IllegalArgumentException(
					"DataConversion.countOccurrences: mostFrequent must be >= 0, got " + mostFrequent);
		if (mostFrequent == 0)
			return Collections.emptyMap();

		// count occurrences and record first-seen index for tie-breaking
		Map<String, Integer> counts = new LinkedHashMap<>();
		Map<String, Integer> firstIndex = new LinkedHashMap<>();
		int idx = 0;
		for (String v : values) {
			if (!firstIndex.containsKey(v))
				firstIndex.put(v, idx++);
			counts.merge(v, 1, Integer::sum);
		}

		// min-heap: worst entry at head -- lower count loses;
		// for equal counts, later first-seen loses
		Comparator<String> worstFirst = (a, b) -> {
			int ca = counts.get(a);
			int cb = counts.get(b);
			if (ca != cb)
				return Integer.compare(ca, cb);
			return Integer.compare(firstIndex.get(b), firstIndex.get(a));
		};

		PriorityQueue<String> heap = new PriorityQueue<>(worstFirst);
		for (String key : counts.keySet()) {
			heap.offer(key);
			if (heap.size() > mostFrequent)
				heap.poll(); // evict lowest-count / latest-first-seen entry
		}

		// sort top-k best-first: higher count first; ties by earlier first-seen
		List<String> topKeys = new ArrayList<>(heap);
		topKeys.sort((a, b) -> {
			int ca = counts.get(a);
			int cb = counts.get(b);
			if (ca != cb)
				return Integer.compare(cb, ca);
			return Integer.compare(firstIndex.get(a), firstIndex.get(b));
		});

		LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
		for (String key : topKeys)
			result.put(key, counts.get(key));

		return result;
	}

	/**
	 * Expands a frequency map into a list where each element appears according to
	 * its count.
	 *
	 * <p>
	 * For example, given {@code {A=2, B=3}}, returns {@code [A, A, B, B, B]} (order
	 * reflects the map's iteration order).
	 * </p>
	 *
	 * <p>
	 * Pre-allocates the list to the exact required capacity to avoid resizing. Time
	 * and space complexity are both O(n) where n is the sum of all counts. Uses a
	 * plain loop to avoid per-key list allocation from {@link Collections#nCopies}.
	 * </p>
	 *
	 * @param <T>    the type of elements in the map keys
	 * @param counts a map of elements to their frequencies; must not be null,
	 *               values must be non-null and non-negative; null keys are
	 *               permitted if the underlying collection supports them
	 * @return a mutable {@link List} containing each key repeated by its count;
	 *         empty if the map is empty or all counts are zero
	 * @throws NullPointerException     if counts is null or any count value is null
	 * @throws IllegalArgumentException if any count value is negative
	 */
	public static <T> List<T> toList(Map<T, Integer> counts) {
		Objects.requireNonNull(counts, "counts must not be null");

		int totalSize = 0;
		for (Integer count : counts.values()) {
			if (count == null || count < 0)
				throw new IllegalArgumentException("Count values must be non-null and non-negative, found: " + count);
			totalSize += count;
		}

		List<T> data = new ArrayList<>(totalSize);
		for (Map.Entry<T, Integer> entry : counts.entrySet()) {
			T element = entry.getKey();
			int count = entry.getValue();
			for (int i = 0; i < count; i++)
				data.add(element);
		}

		return data;
	}

	/**
	 * Converts a frequency map to a map of percentage shares, where each value
	 * represents the percentage of the total count (0.0 to 100.0).
	 *
	 * <p>
	 * Null or negative count values are treated as zero. If the total of all counts
	 * is zero, all ratios are returned as {@code Double.NaN} since the result is
	 * undefined.
	 * </p>
	 *
	 * <p>
	 * Returns an empty map if the input is empty.
	 * </p>
	 *
	 * @param counts a map of string keys to their frequencies; must not be null
	 * @return a {@link Map} of the same keys to their percentage shares in [0.0,
	 *         100.0], or {@code Double.NaN} if total is zero
	 * @throws NullPointerException if counts is null
	 */
	public static Map<String, Double> countsToPercentages(Map<String, Integer> counts) {
		Objects.requireNonNull(counts, "counts must not be null");

		if (counts.isEmpty())
			return Collections.emptyMap();

		// sum non-null, non-negative counts
		long total = 0L;
		for (Integer v : counts.values())
			if (v != null && v > 0)
				total += v;

		// initial capacity sized to avoid rehashing (Java 8 compatible)
		Map<String, Double> result = new LinkedHashMap<>((int) (counts.size() / 0.75f) + 1);

		for (Map.Entry<String, Integer> e : counts.entrySet()) {
			int c = (e.getValue() == null || e.getValue() < 0) ? 0 : e.getValue();
			result.put(e.getKey(), total == 0 ? Double.NaN : (c * 100.0) / total);
		}

		return result;
	}

	/**
	 * Determines the attribute type classification of a Java class for statistical
	 * analysis and reporting.
	 *
	 * <p>
	 * Classifies into one of three mutually exclusive types:
	 * </p>
	 * <ul>
	 * <li><b>Numerical</b>: quantitative types supporting arithmetic operations
	 * (primitive numeric types, wrapper classes, {@link Number} subclasses)</li>
	 * <li><b>Boolean</b>: binary true/false types, treated as a distinct category
	 * requiring specialized binary statistics</li>
	 * <li><b>Categorical</b>: qualitative types (strings, characters, enums,
	 * complex objects) -- also the default when {@code clazz} is null</li>
	 * </ul>
	 *
	 * <p>
	 * Classification order: Boolean is checked before Numerical; everything else,
	 * including null, is Categorical.
	 * </p>
	 *
	 * @param clazz the {@code Class} to classify; {@code null} returns
	 *              {@code "Categorical"} as a safe default
	 * @return {@code "Numerical"}, {@code "Boolean"}, or {@code "Categorical"};
	 *         never {@code null}
	 * @see #isBoolean(Class)
	 * @see #isNumerical(Class)
	 */
	public static String attributeType(Class<?> clazz) {
		if (clazz == null)
			return "Categorical";
		if (isBoolean(clazz))
			return "Boolean";
		if (isNumerical(clazz))
			return "Numerical";
		return "Categorical";
	}

	/**
	 * Returns {@code true} if {@code clazz} represents the primitive
	 * {@code boolean} type or its wrapper {@link Boolean}.
	 *
	 * @param clazz the class to check; may be {@code null}
	 * @return {@code true} for {@code boolean} or {@link Boolean}; {@code false}
	 *         otherwise, including for {@code null}
	 * @see #isNumerical(Class)
	 * @see #attributeType(Class)
	 */
	public static boolean isBoolean(Class<?> clazz) {
		return clazz == Boolean.class || clazz == boolean.class;
	}

	/**
	 * Returns {@code true} if {@code clazz} represents a numerical type suitable
	 * for quantitative statistical analysis.
	 *
	 * <p>
	 * Recognized as numerical:
	 * </p>
	 * <ul>
	 * <li>Primitive numeric types: {@code byte}, {@code short}, {@code int},
	 * {@code long}, {@code float}, {@code double}</li>
	 * <li>Any class assignable from {@link Number}, including all wrapper classes
	 * ({@link Integer}, {@link Double}, etc.), {@link java.math.BigInteger},
	 * {@link java.math.BigDecimal}, and custom {@link Number} subclasses</li>
	 * </ul>
	 *
	 * @param clazz the class to check; may be {@code null}
	 * @return {@code true} for primitive numeric types or {@link Number}
	 *         subclasses; {@code false} otherwise, including for {@code null}
	 * @see #isBoolean(Class)
	 * @see #attributeType(Class)
	 */
	public static boolean isNumerical(Class<?> clazz) {
		if (clazz == null)
			return false;
		if (clazz == byte.class || clazz == short.class || clazz == int.class || clazz == long.class
				|| clazz == float.class || clazz == double.class)
			return true;
		return Number.class.isAssignableFrom(clazz);
	}
}
