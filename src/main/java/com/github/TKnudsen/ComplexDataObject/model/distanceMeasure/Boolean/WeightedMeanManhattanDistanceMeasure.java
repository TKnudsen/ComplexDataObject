package com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.Boolean;

import java.util.List;

import com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.WeightedDistanceMeasure;

/**
 * <p>
 * Title: WeightedMeanManhattanDistanceMeasure
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2017 Juergen Bernard
 * </p>
 * 
 * @author Christian Ritter
 * @version 1.01
 */
public class WeightedMeanManhattanDistanceMeasure extends WeightedDistanceMeasure<Boolean[]> {

	public WeightedMeanManhattanDistanceMeasure(List<Double> weights) {
		super(weights);
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

		double a = 0;
		double b = 0;
		double c = 0;
		double d = 0;

		int length = o1.length;

		// distance measure: (b + c) / (a + b + c + d)
		for (int i = 0; i < length; i++) {
			if (o1[i] && o2[i])
				a = a + getWeights().get(i);
			if (!o1[i] && o2[i])
				b = b + getWeights().get(i);
			if (o1[i] && !o2[i])
				c = c + getWeights().get(i);
			if (!o1[i] && !o2[i])
				d = d + getWeights().get(i);
		}

		return (b + c) / (a + b + c + d);
	}

	@Override
	public String getName() {
		return "WeightedMeanManhattanDistanceMeasure";
	}

}
