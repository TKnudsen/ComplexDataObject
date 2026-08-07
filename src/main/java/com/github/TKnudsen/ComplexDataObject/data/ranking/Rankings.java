package com.github.TKnudsen.ComplexDataObject.data.ranking;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import com.github.TKnudsen.ComplexDataObject.data.entry.EntryWithComparableKey;

/**
 * <p>
 * Static helper methods for Ranking instances: interpolation and binary
 * search for the index of a key, fast ordered insertion, extraction of keys
 * and values from key-value rankings, sub-ranking extraction, printing or
 * saving ranking entries (optionally with a label mapping) to a stream or
 * file, and inverting a key-value ranking into a value-to-key map.
 * </p>
 */
public class Rankings {

	/**
	 * retrieves the index for a given key. In case that no exact match is needed
	 * and not existing the index left smaller is returned.
	 * 
	 * @param key the value to be searched
	 * @return index where a key value would fit. Returns -1 if key is smaller than
	 *         the global minimum. Returns size() if value is larger than global
	 *         maximum.
	 * @throws IllegalArgumentException
	 */
	public static <E extends Comparable<E>> int interpolationSearch(Number key, Ranking<E> ranking,
			Function<E, Double> entryToDoubleFunction) throws IllegalArgumentException {
		return interpolationSearch(0, ranking.size() - 1, key, ranking, entryToDoubleFunction);
	}

	/**
	 * retrieves the index for a given key. In case that no exact match is needed
	 * and not existing the index left smaller is returned.
	 * 
	 * @param indexStart
	 * @param indexEnd
	 * @param key        the value to be searched
	 * @return index where a key value would fit. Returns -1 if key is smaller than
	 *         the global minimum. Returns size() if value is larger than global
	 *         maximum.
	 * @throws IllegalArgumentException
	 */
	public static <E extends Comparable<E>> int interpolationSearch(int indexStart, int indexEnd, Number key,
			Ranking<E> ranking, Function<E, Double> toDouble) throws IllegalArgumentException {

		if (indexStart > indexEnd)
			throw new IllegalArgumentException("Rankings: given value does not exist");

		if (ranking.isEmpty() || toDouble.apply(ranking.getFirst()) >= key.doubleValue())
			return -1;
		if (toDouble.apply(ranking.getLast()) <= key.doubleValue())
			return ranking.size();

		if (indexStart == indexEnd)
			return indexStart;

		if (indexEnd - indexStart == 1)
			if (toDouble.apply(ranking.get(indexStart)) == key)
				return indexStart;
			else
				return indexEnd;

		// interpolate appropriate index
		Double d1 = toDouble.apply(ranking.get(indexStart));
		Double d2 = toDouble.apply(ranking.get(indexEnd));

		if (d1 == key)
			return indexStart;

		if (d2 == key)
			return indexEnd;

		double delta = (d2 - d1);
		double deltaBelow = key.doubleValue() - d1;
		double deltaIndex = indexEnd - indexStart;
		int newSplitIndex = indexStart + Math.max(1, (int) (deltaIndex * deltaBelow / delta));
		if (newSplitIndex == indexEnd)
			newSplitIndex--;

		Number interpolated = toDouble.apply(ranking.get(newSplitIndex));

		if (interpolated == key)
			return newSplitIndex;
		else if (interpolated.doubleValue() > key.doubleValue())
			return interpolationSearch(indexStart, newSplitIndex, key, ranking, toDouble);
		else
			return interpolationSearch(newSplitIndex, indexEnd, key, ranking, toDouble);
	}

	public static <E extends Comparable<E>> int binarySearch(Number key, Ranking<E> ranking,
			Function<E, Double> toDouble) throws IllegalArgumentException {
		return binarySearch(0, ranking.size() - 1, key, ranking, toDouble);
	}

	private static <E extends Comparable<E>> int binarySearch(int low, int high, Number key, Ranking<E> ranking,
			Function<E, Double> toDouble) throws IllegalArgumentException {

		Objects.requireNonNull(ranking);
		Objects.requireNonNull(key);

		if (low > high)
			throw new IllegalArgumentException("Rankings.binarySearch: lower index larger than higher index");

		if (low == high)
			if (key.doubleValue() <= toDouble.apply(ranking.get(low)))
				return low;
			else
				return low + 1;

		if (high - low == 1)
			if (key.doubleValue() <= toDouble.apply(ranking.get(low)))
				return low;
			else
				return high;

		int middle = low + ((high - low) / 2);

		if (high < low)
			return -1;

		if (key.doubleValue() == toDouble.apply(ranking.get(middle))) {
			return middle;
		} else if (key.doubleValue() < toDouble.apply(ranking.get(middle))) {
			return binarySearch(low, middle, key, ranking, toDouble); // middle-1 removed
		} else {
			return binarySearch(middle, high, key, ranking, toDouble); // middle+1 removed
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <V, T extends Comparable<T>> boolean addFast(Ranking<T> ranking, T element) {
		if (element instanceof EntryWithComparableKey<?, ?>) {
			EntryWithComparableKey<?, ?> entry = (EntryWithComparableKey<?, ?>) element;
			if (entry.getKey() instanceof Number) {
				if (Double.isNaN(((Number) entry.getKey()).doubleValue()))
					System.err.println("Rankings.addFast: NaN entry identified for " + entry.getValue() + "; ignored");
				else
					addFast((Ranking) ranking, (EntryWithComparableKey) entry);
				return true;
			}
		}
		return false;
	}

	/**
	 *
	 * @param <K>
	 * @param <V>
	 * @param ranking
	 * @param entry
	 */
	public static <K extends Number & Comparable<K>, V> void addFast(Ranking<EntryWithComparableKey<K, V>> ranking,
			EntryWithComparableKey<K, V> entry) {

		Objects.requireNonNull(ranking);
		Objects.requireNonNull(entry);

		int index = -1;
		if (ranking.isEmpty() || ranking.getFirst().getKey().doubleValue() > entry.getKey().doubleValue())
			index = 0;
		else if (ranking.isEmpty() || ranking.getFirst().getKey().doubleValue() == entry.getKey().doubleValue()
				&& ranking.getFirst().getKey().doubleValue() != -0.0)// -0.0 vs. 0.0 comparison
			index = 0;
		else if (ranking.getLast().getKey().doubleValue() <= entry.getKey().doubleValue())
			index = ranking.size();
		else {
			index = binarySearch(entry.getKey(), ranking, e -> e.getKey().doubleValue());
		}

		if (index == -1)
			throw new IllegalArgumentException(
					"Rankings.addFast: problems with adding entry with key " + entry.getKey());

		ranking.add(index, entry);
	}

	public static <K extends Comparable<K>, V> Collection<K> keys(Ranking<EntryWithComparableKey<K, V>> ranking) {
		Collection<K> keys = new ArrayList<>();

		for (int i = ranking.size() - 1; i >= 0; i--)
			keys.add(ranking.get(i).getKey());

		return keys;
	}

	public static <K extends Comparable<K>, V> Collection<V> values(Ranking<EntryWithComparableKey<K, V>> ranking) {
		Collection<V> values = new ArrayList<>();

		for (int i = ranking.size() - 1; i >= 0; i--)
			values.add(ranking.get(i).getValue());

		return values;
	}

	/**
	 * Convenience method that takes a Ranking<EntryWithComparableKey<C, V>> plus a
	 * value V, traverses the ranking efficiently, and returns the corresponding key
	 * C.
	 * 
	 * @param <K>
	 * @param <V>
	 * @param ranking
	 * @param value
	 * @return
	 */
	public static <K extends Comparable<K>, V> K valueOf(Ranking<EntryWithComparableKey<K, V>> ranking, V value) {
		Objects.requireNonNull(ranking, "ranking must not be null");

		for (EntryWithComparableKey<K, V> entry : ranking)
			if (Objects.equals(entry.getValue(), value))
				return entry.getKey();

		return null;
	}

	/**
	 * Returns a new {@link Ranking} containing the elements between {@code start}
	 * (inclusive) and {@code end} (exclusive), analogous to
	 * {@link String#substring(int, int)}.
	 *
	 * @param ranking source ranking
	 * @param start   index of the first element, inclusive
	 * @param end     index of the last element, exclusive
	 * @return a new ranking with the elements in range [start, end)
	 * @throws IndexOutOfBoundsException if {@code start < 0}, {@code end >
	 *                                    ranking.size()}, or {@code start > end}
	 */
	public static <T extends Comparable<T>> Ranking<T> subRanking(Ranking<T> ranking, int start, int end) {
		Objects.requireNonNull(ranking, "ranking must not be null");

		if (start < 0 || end > ranking.size() || start > end)
			throw new IndexOutOfBoundsException("Rankings.subRanking: invalid range [" + start + ", " + end
					+ ") for ranking of size " + ranking.size());

		Ranking<T> subRanking = new Ranking<>();
		for (int i = start; i < end; i++)
			subRanking.add(ranking.get(i));

		return subRanking;
	}

	// -------------------------------------------------------------------------
	// Print / save
	// -------------------------------------------------------------------------

	/**
	 * Prints the top entries of a ranking to a {@link PrintStream}, one entry per
	 * line, prefixed with the rank number.
	 *
	 * <p>
	 * The ranking is traversed from highest to lowest (best first). {@code maxRank
	 * <= 0} prints all entries.
	 *
	 * @param ranking   ranking to print
	 * @param formatter converts one ranking element to a display string
	 * @param maxRank   maximum number of entries to print; 0 or negative prints all
	 * @param out       target print stream (e.g. {@code System.out})
	 */
	public static <T extends Comparable<T>> void print(Ranking<T> ranking, Function<T, String> formatter, int maxRank,
			PrintStream out) {
		Objects.requireNonNull(ranking, "ranking must not be null");
		Objects.requireNonNull(formatter, "formatter must not be null");
		Objects.requireNonNull(out, "out must not be null");

		int limit = maxRank <= 0 ? ranking.size() : maxRank;
		int rank = 1;
		for (int i = ranking.size() - 1; i >= 0 && rank <= limit; i--, rank++)
			out.println(rank + ".\t" + formatter.apply(ranking.get(i)));
	}

	/**
	 * Prints the top entries of a ranking to {@code System.out} using each
	 * element's {@link Object#toString()}.
	 *
	 * @param ranking ranking to print
	 * @param maxRank maximum number of entries to print; 0 or negative prints all
	 */
	public static <T extends Comparable<T>> void print(Ranking<T> ranking, int maxRank) {
		print(ranking, Object::toString, maxRank, System.out);
	}

	/**
	 * Prints the top entries of a {@link Ranking} of {@link EntryWithComparableKey}
	 * to a {@link PrintStream}.
	 *
	 * <p>
	 * Each line is formatted as {@code "rank.\tkey\tlabel"}, where the label is
	 * produced by {@code labelProvider} applied to the entry's value. This covers
	 * the dominant pattern in the codebase where the value is an opaque ID (e.g.
	 * ISIN, author ID) and the label is a human-readable description.
	 *
	 * @param ranking       ranking to print
	 * @param labelProvider maps the entry value (typically an ID) to a display
	 *                      label
	 * @param maxRank       maximum number of entries to print; 0 or negative prints
	 *                      all
	 * @param out           target print stream
	 */
	public static <K extends Comparable<K>, V> void printWithLabel(Ranking<EntryWithComparableKey<K, V>> ranking,
			Function<V, String> labelProvider, int maxRank, PrintStream out) {
		Objects.requireNonNull(labelProvider, "labelProvider must not be null");
		print(ranking, e -> e.getKey() + "\t" + labelProvider.apply(e.getValue()), maxRank, out);
	}

	/**
	 * Saves the top entries of a ranking to a UTF-8 text file, one entry per line,
	 * prefixed with the rank number.
	 *
	 * <p>
	 * Parent directories are created if they do not exist. An existing file is
	 * truncated. {@code maxRank <= 0} writes all entries.
	 *
	 * @param ranking   ranking to save
	 * @param formatter converts one ranking element to a line of text
	 * @param maxRank   maximum number of entries to write; 0 or negative writes all
	 * @param file      target file path
	 * @throws IOException if the file cannot be written
	 */
	public static <T extends Comparable<T>> void save(Ranking<T> ranking, Function<T, String> formatter, int maxRank,
			Path file) throws IOException {
		Objects.requireNonNull(ranking, "ranking must not be null");
		Objects.requireNonNull(formatter, "formatter must not be null");
		Objects.requireNonNull(file, "file must not be null");

		if (file.getParent() != null)
			Files.createDirectories(file.getParent());

		int limit = maxRank <= 0 ? ranking.size() : maxRank;
		int rank = 1;
		try (var writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
				StandardOpenOption.TRUNCATE_EXISTING)) {
			for (int i = ranking.size() - 1; i >= 0 && rank <= limit; i--, rank++) {
				writer.write(rank + ".\t" + formatter.apply(ranking.get(i)));
				writer.newLine();
			}
		}
	}

	/**
	 * Saves the top entries of a {@link Ranking} of {@link EntryWithComparableKey}
	 * to a UTF-8 text file.
	 *
	 * <p>
	 * Each line is formatted as {@code "rank.\tkey\tlabel"}. This is the
	 * file-output counterpart to
	 * {@link #printWithLabel(Ranking, Function, int, PrintStream)} for the dominant
	 * ID-keyed ranking pattern.
	 *
	 * @param ranking       ranking to save
	 * @param labelProvider maps the entry value (typically an ID) to a display
	 *                      label
	 * @param maxRank       maximum number of entries to write; 0 or negative writes
	 *                      all
	 * @param file          target file path
	 * @throws IOException if the file cannot be written
	 */
	public static <K extends Comparable<K>, V> void saveWithLabel(Ranking<EntryWithComparableKey<K, V>> ranking,
			Function<V, String> labelProvider, int maxRank, Path file) throws IOException {
		Objects.requireNonNull(labelProvider, "labelProvider must not be null");
		save(ranking, e -> e.getKey() + "\t" + labelProvider.apply(e.getValue()), maxRank, file);
	}

	/**
	 * Convenience method that takes a Ranking<EntryWithComparableKey<C, V>> and
	 * inverts it into a map. Assumes that values are unique and do not collide in
	 * the resulting map.
	 *
	 * @param <K>
	 * @param <V>
	 * @param ranking
	 * @return mapping from non-colliding values to keys
	 */
	public static <K extends Comparable<K>, V> Map<V, K> valueToKeyMap(Ranking<EntryWithComparableKey<K, V>> ranking) {
		Objects.requireNonNull(ranking, "ranking must not be null");

		HashMap<V, K> map = new HashMap<>();
		Iterator<EntryWithComparableKey<K, V>> iterator = ranking.iterator();

		while (iterator.hasNext()) {
			EntryWithComparableKey<K, V> entry = iterator.next();
			map.put(entry.getValue(), entry.getKey());
		}

		return map;
	}
}
