package com.github.TKnudsen.ComplexDataObject.data.uncertainty.range;

import java.util.Collection;

import com.github.TKnudsen.ComplexDataObject.data.uncertainty.Double.ValueUncertainty;
import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;

/**
 * <p>
 * Data model of an uncertainty that is associated with a single value and that
 * covers a range. This means that this uncertainty describes how far a value
 * may deviate from the actual value: For a given value <code>v</code> and an
 * (absolute) value uncertainty range <code>(lower,upper)</code>, the actual
 * value will be in <code>[v+lower,v+upper]</code>.
 * </p>
 * 
 * <p>
 * Copyright: (c) 2015-2018 Juergen Bernard,
 * https://github.com/TKnudsen/ComplexDataObject
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */
public class ValueUncertaintyRange extends ValueUncertainty implements IValueUncertaintyRange {

	private double upperBound;
	private double lowerBound;

	public ValueUncertaintyRange() {
		super();
	}

	public ValueUncertaintyRange(double amount) {
		super(amount);
	}

	public ValueUncertaintyRange(Collection<? extends Double> values) {
		super();

		initialize(values);
	}

	private void initialize(Collection<? extends Double> values) {
		StatisticsSupport statisticsSupport = new StatisticsSupport(values);

		this.setAmount(statisticsSupport.getMedian());
		this.lowerBound = statisticsSupport.getMin();
		this.upperBound = statisticsSupport.getMax();
	}

	@Override
	public String toString() {
		return "ValueUncertaintyRange. Amount: " + getAmount() + ", bounds: [" + getLowerBound() + ", "
				+ getUpperBound() + "]";
	}

	@Override
	public double getUpperBound() {
		return upperBound;
	}

	@Override
	public double getLowerBound() {
		return lowerBound;
	}

}
