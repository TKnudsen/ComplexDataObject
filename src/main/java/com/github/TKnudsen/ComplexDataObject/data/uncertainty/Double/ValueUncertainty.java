package com.github.TKnudsen.ComplexDataObject.data.uncertainty.Double;

import java.util.Collection;

import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;

/**
 * <p>
 * Basic interface uncertainties of numerical values modeled as Double.
 * </p>
 * 
 * <p>
 * Copyright: (c) 2015-2018 Juergen Bernard,
 * https://github.com/TKnudsen/ComplexDataObject
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.06
 */
public class ValueUncertainty implements IValueUncertainty {

	private Double amount;

	public ValueUncertainty() {
		super();
	}

	public ValueUncertainty(double amount) {
		this.setAmount(amount);
	}

	public ValueUncertainty(Collection<? extends Double> values) {
		initialize(values);
	}

	private void initialize(Collection<? extends Double> values) {
		StatisticsSupport statisticsSupport = new StatisticsSupport(values);

		this.setAmount(statisticsSupport.getMedian());
	}

	@Override
	public String toString() {
		return "ValueUncertainty. Amount: " + getAmount();
	}

	@Override
	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

}
