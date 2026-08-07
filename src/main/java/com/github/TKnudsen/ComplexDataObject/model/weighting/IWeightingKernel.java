package com.github.TKnudsen.ComplexDataObject.model.weighting;

/**
 * <p>
 * Behavior of kernel functions to determine the weight of given
 * objects, or indices, respectively.
 * </p>
 *
 * @version 1.02
 * @since 2016
 */
public interface IWeightingKernel<T extends Number> {

	public double getWeight(T t);

	public T getReference();

	public void setReference(T t);

	/**
	 * Value range where the kernel provides weights
	 * 
	 * @return
	 */
	public T getInterval();

	public void setInterval(T interval);

}
