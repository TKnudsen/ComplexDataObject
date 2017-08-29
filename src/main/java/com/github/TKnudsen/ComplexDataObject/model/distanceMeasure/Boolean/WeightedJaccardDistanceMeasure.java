package com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.Boolean;

import java.util.List;

import com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.WeightedDistanceMeasure;

/**
 * <p>
 * Title: WeightedJaccardDistanceMeasure
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
 * @author Christian Ritter, J�rgen Bernard
 * @version 1.01
 */
public class WeightedJaccardDistanceMeasure extends WeightedDistanceMeasure<Boolean[]> {

	private List<Double> weights;

	public WeightedJaccardDistanceMeasure(List<Double> weights) {
		super(weights);
	}

	@Override
	public String getDescription() {
		return "Calculates weighted Jaccard distance.";
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

		int length = o1.length;

		// similarity measure: a / (a + b + c)
		// 1 - (a / (a + b + c)) is the corresponding distance measure
		for (int i = 0; i < length; i++) {
			if (o1[i] && o2[i])
				a = a + getWeights().get(i);
			if (!o1[i] && o2[i])
				b = b + getWeights().get(i);
			if (o1[i] && !o2[i])
				c = c + getWeights().get(i);
		}

		if ((a + b + c) == 0)
			return 0;

		return 1 - (a / (a + b + c));
	}

	@Override
	public String getName() {
		return "WeightedJaccard";
	}

	@Override
	public List<Double> getWeights() {
		return weights;
	}

	public void setWeights(List<Double> weights) {
		this.weights = weights;
	}

}
