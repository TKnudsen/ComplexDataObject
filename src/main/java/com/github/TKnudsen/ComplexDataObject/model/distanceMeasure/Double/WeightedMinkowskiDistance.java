package com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.Double;

import java.util.List;

import com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.WeightedDistanceMeasure;

/**
 * <p>
 * Title: WeightedMinkowskiDistance
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
public class WeightedMinkowskiDistance extends WeightedDistanceMeasure<double[]> {

	private double exponent;

	public WeightedMinkowskiDistance(List<Double> weights) {
		this(weights, 2.0);
	}

	public WeightedMinkowskiDistance(List<Double> weights, double exponent) {
		super(weights);
		this.exponent = exponent;
	}

	@Override
	public String getDescription() {
		return "WeightedMinkowskiDistanceMeasure";
	}

	@Override
	public double getDistance(double[] o1, double[] o2) {
		// check if arrays have same length
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
			result = result + getWeights().get(i) * Math.pow(Math.abs((o1[i] - o2[i])), exponent);
		}

		return Math.pow(result, 1 / exponent);
	}

	@Override
	public String getName() {
		return "WeightedMinkowskiDistanceMeasure";
	}

}
