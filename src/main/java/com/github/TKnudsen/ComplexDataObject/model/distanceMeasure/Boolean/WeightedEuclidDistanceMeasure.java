package com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.Boolean;

import java.util.List;

import com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.WeightedDistanceMeasure;

/**
 * <p>
 * Title: WeightedEuclidDistanceMeasure
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
public class WeightedEuclidDistanceMeasure extends WeightedDistanceMeasure<Boolean[]> {

	public WeightedEuclidDistanceMeasure(List<Double> weights) {
		super(weights);
	}

	@Override
	public String getDescription() {
		return "WeightedEuclidDistanceMeasure";
	}

	@Override
	public double getDistance(Boolean[] o1, Boolean[] o2) {
		if (o1.length != o2.length || o1.length != getWeights().size()) {
			try {
				throw new Exception("The Arrays have different Sizes.");
			} catch (Exception e) {
				e.printStackTrace();
			}
			return Double.NaN;
		}

		int length = o1.length;
		double result = 0;

		for (int i = 0; i < length; i++) {
			int xoredValues = (o1[i] ^ o2[i]) ? 1 : 0;
			result = result + getWeights().get(i) * xoredValues;
		}

		return Math.sqrt(result);
	}

	@Override
	public String getName() {
		return "WeightedEuclidDistanceMeasure";
	}

}
