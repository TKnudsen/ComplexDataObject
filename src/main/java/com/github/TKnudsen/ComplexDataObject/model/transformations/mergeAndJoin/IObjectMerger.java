package com.github.TKnudsen.ComplexDataObject.model.transformations.mergeAndJoin;

/**
 * <p>
 * Accepts two objects of a given type T and returns one NEW object
 * of a given type T
 * </p>
 *
 * @version 1.01
 * @since 2017
 */
public interface IObjectMerger<O> {

	public O merge(O object1, O object2);
}
