package com.github.TKnudsen.ComplexDataObject.model.weighting.Long;

import com.github.TKnudsen.ComplexDataObject.model.weighting.IWeightingKernel;

/**
 * <p>
 * Specialization of IWeightingKernel for Long-typed reference values and
 * intervals, e.g. for weighting elements by their timestamp/duration
 * distance to a reference point in time.
 * </p>
 */
public interface ILongWeightingKernel extends IWeightingKernel<Long> {

}
