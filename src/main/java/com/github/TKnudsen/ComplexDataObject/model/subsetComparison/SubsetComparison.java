package com.github.TKnudsen.ComplexDataObject.model.subsetComparison;

import java.util.Collection;
import java.util.function.Function;

/**
 * <p>
 * Interface for comparing a target subset of items against a reference set,
 * using a function that extracts the comparison value from each item, and
 * returning a value that characterizes the difference between the two sets.
 * </p>
 */
public interface SubsetComparison<V> {

	<T> V compare(Collection<T> referenceSet, Collection<T> targetSet, Function<T, V> toValueFunction);
}
