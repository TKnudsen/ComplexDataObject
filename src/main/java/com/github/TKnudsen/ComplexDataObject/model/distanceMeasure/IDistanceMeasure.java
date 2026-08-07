package com.github.TKnudsen.ComplexDataObject.model.distanceMeasure;

import java.util.function.ToDoubleBiFunction;

import com.github.TKnudsen.ComplexDataObject.data.interfaces.ISelfDescription;

/**
 * <p>
 * Basic interface modeling distances between two objects of
 * identical type.
 * </p>
 *
 * @version 1.02
 * @since 2017
 */
public interface IDistanceMeasure<T> extends ToDoubleBiFunction<T, T>, ISelfDescription {

	public double getDistance(T o1, T o2);
}
