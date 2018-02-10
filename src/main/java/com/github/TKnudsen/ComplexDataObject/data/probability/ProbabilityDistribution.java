package com.github.TKnudsen.ComplexDataObject.data.probability;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * <p>
 * Title: ProbabilityDistribution
 * </p>
 * 
 * <p>
 * Description: Stores the probability distribution of a given set of items.
 * Probabilities must add up to 100%
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2016-2018
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.04
 */
public class ProbabilityDistribution<I> {

	private static double EPSILON = 1e-8;

	private Map<I, Double> probabilityDistribution;

	private I mostLikelyItem;

	@SuppressWarnings("unused")
	private ProbabilityDistribution() {
		this(null);
	}

	public ProbabilityDistribution(Map<I, Double> probabilityDistribution) {
		this.probabilityDistribution = probabilityDistribution;

		if (probabilityDistribution == null)
			throw new NullPointerException("ProbabilityDistribution: given set of probabilites was null");

		calculateRepresentant();
	}

	private void calculateRepresentant() {
		if (probabilityDistribution == null) {
			mostLikelyItem = null;
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
			throw new IllegalArgumentException("ProbabilityDistribution: sum of given set of probabilites was != 100%");
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
