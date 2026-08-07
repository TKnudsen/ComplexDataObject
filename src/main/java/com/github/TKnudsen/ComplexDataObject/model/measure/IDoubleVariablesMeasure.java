package com.github.TKnudsen.ComplexDataObject.model.measure;

import java.util.function.IntToDoubleFunction;

/**
 * <p>
 * Computes a scalar measure between two variables represented as
 * {@link IntToDoubleFunction} accessors over a given number of elements,
 * avoiding the need to materialize the variables as arrays or collections
 * beforehand.
 * </p>
 */
public interface IDoubleVariablesMeasure {

	double compute(IntToDoubleFunction x, IntToDoubleFunction y, int size);
}
