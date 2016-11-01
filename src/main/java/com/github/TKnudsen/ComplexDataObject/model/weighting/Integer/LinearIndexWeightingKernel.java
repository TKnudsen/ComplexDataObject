package com.github.TKnudsen.ComplexDataObject.model.weighting.Integer;

public class LinearIndexWeightingKernel implements IIntegerWeightingKernel {

	private Integer reference;
	private Integer interval;

	public LinearIndexWeightingKernel(Integer interval) {
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

		if (interval == 0)
			return 1.0;

		return 1.0 - (Math.abs(reference - t) / (double) interval);
	}

	@Override
	public Integer getReference() {
		return reference;
	}

	@Override
	public void setReference(Integer t) {
		this.reference = t.intValue();
	}
}
