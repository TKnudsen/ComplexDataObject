package com.github.TKnudsen.ComplexDataObject.model.subsetComparison;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * returns the most interesting category of a subset compared to the reference
 * set. Interestingness is defined by the most differing ratio of occurrences.
 * 
 * Throws an exception, if target subset contains a category that is not
 * contained in the reference set.
 * 
 * @author Juergern Bernard
 *
 */
public class CategoricalSubsetComparison implements SubsetComparison<String> {

	@Override
	public <T> String compare(Collection<T> referenceSet, Collection<T> targetSet,
			Function<T, String> toValueFunction) {

		Objects.requireNonNull(referenceSet);
		Objects.requireNonNull(targetSet);
		Objects.requireNonNull(toValueFunction);

		if (referenceSet.isEmpty() || targetSet.isEmpty())
			throw new IllegalArgumentException("CategoricalSubsetComparison.compare: set empty");

		Map<String, Double> referenceCountsRel = countAndNormalize(referenceSet, toValueFunction);
		Map<String, Double> targetCountsRel = countAndNormalize(targetSet, toValueFunction);

		// check for missing category
		for (String r : referenceCountsRel.keySet())
			if (!referenceCountsRel.containsKey(r))
				throw new IllegalArgumentException(
						"CategoricalSubsetComparison.compare: targetSet contains category that is not in the reference set: "
								+ r);

		// sub
		for (String r : referenceCountsRel.keySet()) {
			if (targetCountsRel.containsKey(r))
				referenceCountsRel.put(r, targetCountsRel.get(r) - referenceCountsRel.get(r));
		}

		String ret = null;
		double max = 0.0;
		for (String r : referenceCountsRel.keySet()) {
			double d = Math.abs(referenceCountsRel.get(ret));
			if (max < d) {
				ret = r;
				max = d;
			}
		}

		return ret;
	}

	private <T> Map<String, Double> countAndNormalize(Collection<T> set, Function<T, String> toValueFunction) {
		List<Double> counts = new ArrayList<>();
		Map<String, Integer> indices = new HashMap<>();
		double count = 0.0;
		for (T t : set) {
			String s = toValueFunction.apply(t);
			if (s != null) {
				if (!indices.containsKey(s)) {
					indices.put(s, indices.size());
					counts.add(0.0);
				}
				counts.set(indices.get(s), counts.get(indices.get(s)) + 1);
				count++;
			}
		}

		// normalize
		for (int i = 0; i < counts.size(); i++)
			counts.set(i, counts.get(i) / count);

		Map<String, Double> ret = new HashMap<>();
		for (String s : indices.keySet())
			ret.put(s, counts.get(indices.get(s)) / count);
		return ret;
	}
}
