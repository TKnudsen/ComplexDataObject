package com.github.TKnudsen.ComplexDataObject.model.distanceMeasure;

import java.util.List;

/**
 * <p>
 * Title: WeightedDistanceMeasure
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2017
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */
public abstract class WeightedDistanceMeasure<T> implements IWeightedDistanceMeasure<T> {
	private List<Double> weights;

	public WeightedDistanceMeasure(List<Double> weights) {
		setWeights(weights);
	}

	public double applyAsDouble(T t, T u) {
		return getDistance(t, u);
	}

	@Override
	public List<Double> getWeights() {
		return weights;
	}

	public void setWeights(List<Double> weights) {
		this.weights = weights;
	}

}
