package com.github.TKnudsen.ComplexDataObject.model.distanceMeasure;

import java.util.List;

/**
 * <p>
 * Extends IDistanceMeasure by adding access to the per-dimension weights
 * used internally by the distance calculation.
 * </p>
 *
 * @version 1.01
 * @since 2017
 */
public interface IWeightedDistanceMeasure<T> extends IDistanceMeasure<T> {

	/**
	 * the weightings used by the weighting model of the distance measure.
	 * 
	 * @return
	 */
	public List<Double> getWeights();

}
