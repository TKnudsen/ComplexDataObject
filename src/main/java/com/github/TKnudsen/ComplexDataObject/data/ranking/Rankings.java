package com.github.TKnudsen.ComplexDataObject.data.ranking;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

import com.github.TKnudsen.ComplexDataObject.data.entry.EntryWithComparableKey;

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

//	/**
//	 * retrieves the index for a given key. In case that no exact match is needed
//	 * and not existing the index left smaller is returned.
//	 * 
//	 * @param key the value to be searched
//	 * @return index where a key value would fit. Returns -1 if key is smaller than
//	 *         the global minimum. Returns size() if value is larger than global
//	 *         maximum.
//	 * @throws IllegalArgumentException
//	 */
//	public static <V> int interpolationSearch(Number key, Ranking<EntryWithComparableKey<? extends Number, V>> ranking)
//			throws IllegalArgumentException {
//		return interpolationSearch(0, ranking.size() - 1, key, ranking);
//	}
//
//	/**
//	 * retrieves the index for a given key. In case that no exact match is needed
//	 * and not existing the index left smaller is returned.
//	 * 
//	 * @param indexStart
//	 * @param indexEnd
//	 * @param key        the value to be searched
//	 * @return index where a key value would fit. Returns -1 if key is smaller than
//	 *         the global minimum. Returns size() if value is larger than global
//	 *         maximum.
//	 * @throws IllegalArgumentException
//	 */
//	public static <V> int interpolationSearch(int indexStart, int indexEnd, Number key,
//			Ranking<EntryWithComparableKey<? extends Number, V>> ranking) throws IllegalArgumentException {
//
//		if (indexStart > indexEnd)
//			throw new IllegalArgumentException("Rankings: given value does not exist");
//
//		if (ranking.isEmpty() || ranking.getFirst().getKey().doubleValue() >= key.doubleValue())
//			return -1;
//		if (ranking.getLast().getKey().doubleValue() <= key.doubleValue())
//			return ranking.size();
//
//		if (indexStart == indexEnd)
//			return indexStart;
//
//		if (indexEnd - indexStart == 1)
//			if (ranking.get(indexStart).getKey() == key)
//				return indexStart;
//			else
//				return indexEnd;
//
//		// interpolate appropriate index
//		Double d1 = ranking.get(indexStart).getKey().doubleValue();
//		Double d2 = ranking.get(indexEnd).getKey().doubleValue();
//
//		if (d1 == key)
//			return indexStart;
//
//		if (d2 == key)
//			return indexEnd;
//
//		double delta = (d2 - d1);
//		double deltaBelow = key.doubleValue() - d1;
//		double deltaIndex = indexEnd - indexStart;
//		int newSplitIndex = indexStart + Math.max(1, (int) (deltaIndex * deltaBelow / delta));
//		if (newSplitIndex == indexEnd)
//			newSplitIndex--;
//
//		Number interpolated = ranking.get(newSplitIndex).getKey();
//
//		if (interpolated == key)
//			return newSplitIndex;
//		else if (interpolated.doubleValue() > key.doubleValue())
//			return interpolationSearch(indexStart, newSplitIndex, key, ranking);
//		else
//			return interpolationSearch(newSplitIndex, indexEnd, key, ranking);
//	}

	@SuppressWarnings("unchecked")
	public static <V, T extends Comparable<T>> boolean addFast(Ranking<T> ranking, T element) {
		if (element instanceof EntryWithComparableKey<?, ?>) {
			EntryWithComparableKey<?, ?> entry = (EntryWithComparableKey<?, ?>) element;
			if (entry.getKey() instanceof Number) {
				Ranking<EntryWithComparableKey<? extends Number, V>> r = (Ranking<EntryWithComparableKey<? extends Number, V>>) ranking;
				if (Double.isNaN(((Number) entry.getKey()).doubleValue()))
					System.err.println("Rankings.addFast: NaN entry identified for " + entry.getValue() + "; ignored");
				else
					addFast(r, (EntryWithComparableKey<? extends Number, V>) entry);
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param <V>
	 * @param ranking
	 * @param entry
	 */
	public static <V> void addFast(Ranking<EntryWithComparableKey<? extends Number, V>> ranking,
			EntryWithComparableKey<? extends Number, V> entry) {

		Objects.requireNonNull(ranking);
		Objects.requireNonNull(entry);

//		int index = interpolationSearch(entry.getKey(), ranking, e -> e.getKey().doubleValue());
//		if (index == -1)
//			index = 0;

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

	public static <C extends Comparable<C>, V> Collection<C> keys(Ranking<EntryWithComparableKey<C, V>> ranking) {
		Collection<C> keys = new ArrayList<>();

		for (int i = ranking.size() - 1; i >= 0; i--)
			keys.add(ranking.get(i).getKey());

		return keys;
	}

	public static <C extends Comparable<C>, V> Collection<V> values(Ranking<EntryWithComparableKey<C, V>> ranking) {
		Collection<V> values = new ArrayList<>();

		for (int i = ranking.size() - 1; i >= 0; i--)
			values.add(ranking.get(i).getValue());

		return values;
	}
}
