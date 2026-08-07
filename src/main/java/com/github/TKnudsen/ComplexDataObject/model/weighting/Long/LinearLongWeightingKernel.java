package com.github.TKnudsen.ComplexDataObject.model.weighting.Long;

import java.util.Objects;

/**
 * <p>
 * Linear kernel applied on long distances. Weight decays linearly from 1.0
 * at the reference value to 0.0 at the edge of the configured interval, and
 * is 0.0 beyond that interval.
 * </p>
 *
 * @version 1.02
 * @since 2016
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
		Objects.requireNonNull(interval);

		if (interval < 1)
			throw new IllegalArgumentException(
					"LinearLongWeightingKernel: interval with negative duration: " + interval);

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
