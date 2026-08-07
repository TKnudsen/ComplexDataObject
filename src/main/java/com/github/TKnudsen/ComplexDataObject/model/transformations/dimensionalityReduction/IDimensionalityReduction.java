package com.github.TKnudsen.ComplexDataObject.model.transformations.dimensionalityReduction;

import java.util.Map;

import com.github.TKnudsen.ComplexDataObject.model.transformations.IDataTransformation;

/**
 * <p>
 * Contract for dimensionality reduction algorithms that map input objects of
 * type X onto a lower-dimensional representation of type Y. Implementations
 * compute and expose the resulting mapping as well as the achieved output
 * dimensionality.
 * </p>
 *
 * @version 1.05
 * @since 2012
 */
public interface IDimensionalityReduction<X, Y> extends IDataTransformation<X, Y> {

	public int getOutputDimensionality();

	public void calculateDimensionalityReduction();

	public Map<X, Y> getMapping();
}