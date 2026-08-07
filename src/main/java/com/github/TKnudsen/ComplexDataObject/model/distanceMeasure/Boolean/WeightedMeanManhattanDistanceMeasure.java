package com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.Boolean;

import java.util.List;

import com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.WeightedDistanceMeasure;

/**
 * <p>
 * Computes a weighted mean Manhattan-style distance between two Boolean
 * arrays as the ratio of the weighted sum of differing positions to the
 * total weighted sum of all positions, with missing values contributing the
 * configured null value.
 * </p>
 *
 * @version 1.01
 * @since 2016
 */
public class WeightedMeanManhattanDistanceMeasure extends WeightedDistanceMeasure<Boolean[]> {

	public WeightedMeanManhattanDistanceMeasure(List<Double> weights) {
		super(weights);
	}

	public WeightedMeanManhattanDistanceMeasure(List<Double> weights, double nullValue) {
		super(weights, nullValue);
	}

	@Override
	public String getDescription() {
		return "WeightedMeanManhattanDistanceMeasure";
	}

	@Override
	public double getDistance(Boolean[] o1, Boolean[] o2) {
		// check if arrays have same length
		if (o1.length != o2.length || o1.length != getWeights().size()) {
			try {
				throw new Exception("The Arrays have different Sizes.");
			} catch (Exception e) {
				e.printStackTrace();
			}
			return Double.NaN;
		}

		double equal = 0;
		double unequal = 0;

		int length = o1.length;

		for (int i = 0; i < length; i++) {
			if (o1[i] == null || o2[i] == null)
				unequal += getNullValue() * getWeights().get(i);
			else if (o1[i].equals(o2[i]))
				equal += getWeights().get(i);
			else
				unequal += getWeights().get(i);
		}

		return unequal / (equal + unequal);
	}

	@Override
	public String getName() {
		return "WeightedMeanManhattanDistanceMeasure";
	}

}
