package com.github.TKnudsen.ComplexDataObject.model.weighting.Long;

/**
 * 
 * ComplexDataObject
 *
 * Copyright: (c) 2016-2018 Juergen Bernard,
 * https://github.com/TKnudsen/ComplexDataObject<br>
 * <br>
 * 
 * Linear kernel applied on long distances.
 *
 * 
 * @author Juergen Bernard
 * 
 * @version 1.01
 */
public class LinearLongWeightingKernel implements ILongWeightingKernel {

	private Long reference;
	private Long interval;

	/**
	 * for serialization
	 */
	@SuppressWarnings("unused")
	private LinearLongWeightingKernel() {
		this(10L);
	}

	public LinearLongWeightingKernel(Long interval) {
		this.interval = interval;
	}

	@Override
	public Long getInterval() {
		return interval;
	}

	@Override
	public void setInterval(Long t) {
		this.interval = t;
	}

	@Override
	public double getWeight(Long t) {
		if (Math.abs(reference - t) > interval)
			return 0.0;

		if (interval == 0)
			return 1.0;

		return 1.0 - (Math.abs(reference - t) / (double) interval);
	}

	@Override
	public Long getReference() {
		return reference;
	}

	@Override
	public void setReference(Long t) {
		this.reference = t;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof LinearLongWeightingKernel))
			return false;

		LinearLongWeightingKernel other = (LinearLongWeightingKernel) o;

		return other.interval == interval && other.reference == reference;
	}

}
