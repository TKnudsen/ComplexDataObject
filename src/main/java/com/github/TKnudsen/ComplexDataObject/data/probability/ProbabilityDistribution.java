package com.github.TKnudsen.ComplexDataObject.data.probability;

import java.util.Map;
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
 * @version 1.03
 */
public class ProbabilityDistribution<I> {
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
		Double sum = 0.0;

		for (I value : probabilityDistribution.keySet()) {
			if (probabilityDistribution.get(value) > winning) {
				mostLikelyItem = value;
				winning = probabilityDistribution.get(value);
			}

			sum += probabilityDistribution.get(value);
		}

		if (sum != 1.0)
			throw new IllegalArgumentException("ProbabilityDistribution: given set of probabilites was null");
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
