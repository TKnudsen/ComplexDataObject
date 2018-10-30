package com.github.TKnudsen.ComplexDataObject.data.probability;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;

/**
 * <p>
 * Title: ProbabilityDistribution
 * </p>
 * 
 * <p>
 * Description: Stores the probability distribution of a given set of items. The
 * given set of items may be empty. If it is not empty, the associated
 * probabilities must add up to 1.0.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2016-2018
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.07
 */
public class ProbabilityDistribution<I> {

	public static double EPSILON = 1e-8;

	private final Map<I, Double> probabilityDistribution;

	private I mostLikelyItem;

	public ProbabilityDistribution(Map<I, Double> probabilityDistribution) {
		this.probabilityDistribution = Objects.requireNonNull(probabilityDistribution,
				"The probabilityDistribution may not be null");

		calculateRepresentant();
	}

	private void calculateRepresentant() {

		if (probabilityDistribution.isEmpty()) {
			return;
		}
		Double winning = 0.0;
		double sum = 0.0;
		for (I value : probabilityDistribution.keySet()) {
			if (probabilityDistribution.get(value) > winning) {
				mostLikelyItem = value;
				winning = probabilityDistribution.get(value);
			}

			sum += probabilityDistribution.get(value);
		}

		if (Math.abs(sum - 1.0) > EPSILON)
			throw new IllegalArgumentException(
					"ProbabilityDistribution: sum of given set of probabilites was != 100% (" + sum + ")");
	}

	public static boolean checkProbabilitySumMatchesHundredPercent(Collection<Double> probabilities, double epsilon,
			boolean print) {
		double sum = MathFunctions.getSum(probabilities, false);

		if (Math.abs(sum - 1.0) > EPSILON) {
			System.err.println("ProbabilityDistribution: sum of given set of probabilites was != 100% (" + sum + ")");
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		Iterator<Entry<I, Double>> iterator = probabilityDistribution.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<I, Double> probability = iterator.next();
			sb.append(probability.getKey());
			sb.append('=').append('"');
			sb.append(probability.getValue());
			sb.append('"');
			if (iterator.hasNext())
				sb.append(',').append(' ');
		}

		return sb.toString();
	}

	/**
	 * provides values in the intrinsic order of a given set. Meaningful if several
	 * distributions are to be compared.
	 * 
	 * Throws a NullPointerException if no probabilty is available for a given item.
	 * 
	 * @param items
	 * @return
	 */
	public List<Double> values(Set<I> items) {
		List<Double> list = new ArrayList<>();

		for (I i : items) {
			Double probability = getProbability(i);

			if (probability == null)
				throw new NullPointerException("ProbabilityDistribution: probability for item " + i + " was null.");

			list.add(getProbability(i));
		}

		return list;
	}

	/**
	 * little helper to normalize a map with doubles to 1.0.
	 * 
	 * @param valuesMap
	 * @return
	 */
	public static <I> Map<I, Double> normalize(Map<I, Double> valuesMap) {
		double sum = 0.0;

		for (double d : valuesMap.values())
			sum += d;

		Map<I, Double> output = new LinkedHashMap<>();

		for (I i : valuesMap.keySet())
			output.put(i, valuesMap.get(i) / sum);

		return output;
	}

	public Double getProbability(I item) {
		return probabilityDistribution.get(item);
	}

	public Map<I, Double> getProbabilityDistribution() {
		return probabilityDistribution;
	}

	public Set<I> keySet() {
		return probabilityDistribution.keySet();
	}

	public I getMostLikelyItem() {
		return mostLikelyItem;
	}
}
