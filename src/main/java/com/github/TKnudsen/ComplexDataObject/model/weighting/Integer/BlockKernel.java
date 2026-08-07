package com.github.TKnudsen.ComplexDataObject.model.weighting.Integer;

/**
 * <p>
 * Weighting kernel with a block/step shape: returns a constant weight of 1.0
 * for any integer value within the configured interval of the reference
 * value, and 0.0 outside that interval.
 * </p>
 *
 * @version 1.02
 * @since 2016
 */
public class BlockKernel implements IIntegerWeightingKernel {

	private Integer reference;
	private Integer interval;

	/**
	 * for serialization
	 */
	public BlockKernel() {
		this.interval = 3;
	}

	public BlockKernel(Integer interval) {
		this.interval = interval;
	}

	@Override
	public Integer getInterval() {
		return interval;
	}

	@Override
	public void setInterval(Integer t) {
		this.interval = t;
	}

	@Override
	public double getWeight(Integer t) {
		if (Math.abs(reference - t) > interval)
			return 0.0;

		return 1.0;
	}

	@Override
	public Integer getReference() {
		return reference;
	}

	@Override
	public void setReference(Integer t) {
		this.reference = t.intValue();
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof BlockKernel))
			return false;

		BlockKernel other = (BlockKernel) o;

		return other.interval == interval && other.reference == reference;
	}
}
