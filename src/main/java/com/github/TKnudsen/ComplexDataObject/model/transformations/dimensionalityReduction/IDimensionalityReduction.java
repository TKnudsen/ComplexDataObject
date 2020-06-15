package com.github.TKnudsen.ComplexDataObject.model.transformations.dimensionalityReduction;

import java.util.Map;

import com.github.TKnudsen.ComplexDataObject.model.transformations.IDataTransformation;

/**
 * 
 * <p>
 * Copyright: Copyright (c) 2012-2020 Juergen Bernard,
 * https://github.com/TKnudsen/ComplexDataObject
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.05
 * 
 */
public interface IDimensionalityReduction<X, Y> extends IDataTransformation<X, Y> {

	public int getOutputDimensionality();

	public void calculateDimensionalityReduction();

	public Map<X, Y> getMapping();
}