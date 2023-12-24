package com.github.TKnudsen.ComplexDataObject.model.subsetComparison;

import java.util.Collection;
import java.util.function.Function;

public interface SubsetComparison<V> {

	<T> V compare(Collection<T> referenceSet, Collection<T> targetSet, Function<T, V> toValueFunction);
}
