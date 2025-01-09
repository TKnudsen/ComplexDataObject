package com.github.TKnudsen.ComplexDataObject.model.transformations.normalization;

import java.util.Collection;

import com.github.TKnudsen.ComplexDataObject.data.ranking.Ranking;
import com.github.TKnudsen.ComplexDataObject.data.ranking.Rankings;
import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;

/**
 * <p>
 * Title: QuantileNormalizationFunction
 * </p>
 * 
 * <p>
 * Description: scales a value into the interval [0...1]. Normalization is based
 * on a quantile normalization based on a given collection of numbers.
 * 
 * Setting new bounds is possible (even if previously calculated values will be
 * obsolete then). Reason: keeping the instance alive in value-dynamic contexts.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2016-2024
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.04
 */
public class QuantileNormalizationFunction extends NormalizationFunction {

	protected Ranking<Float> valueRanking;

	/**
	 * for serialization purposes only
	 */
	@SuppressWarnings("unused")
	private QuantileNormalizationFunction() {
		super();
	}

	public QuantileNormalizationFunction(Collection<? extends Number> values) {
		super(values);

		initializeRanking(values);
	}

	public QuantileNormalizationFunction(Collection<Number> values, boolean limitToBounds) {
		super(values, limitToBounds);

		initializeRanking(values);
	}

	public QuantileNormalizationFunction(Number globalMin, Number globalMax, Collection<Number> values,
			boolean limitToBounds) {
		super(globalMin, globalMax, limitToBounds);

		initializeRanking(values);
	}

	public QuantileNormalizationFunction(StatisticsSupport statisticsSupport) {
		super(statisticsSupport);

		initializeRanking(statisticsSupport.getValues());
	}

	public QuantileNormalizationFunction(StatisticsSupport statisticsSupport, boolean limitToBounds) {
		super(statisticsSupport, limitToBounds);

		initializeRanking(statisticsSupport.getValues());
	}

	public QuantileNormalizationFunction(Number globalMin, Number globalMax, StatisticsSupport statisticsSupport,
			boolean limitToBounds) {
		super(globalMin, globalMax, limitToBounds);

		initializeRanking(statisticsSupport.getValues());
	}

	private void initializeRanking(Collection<? extends Number> values) {
		valueRanking = new Ranking<>();

		for (Number value : values)
			valueRanking.add(value.floatValue());

	}

	private void initializeRanking(double[] values) {
		valueRanking = new Ranking<>();

		for (double value : values)
			valueRanking.add((float) value);
	}

	@Override
	public void setGlobalMin(Number globalMin) {
		super.setGlobalMin(globalMin);

		valueRanking.add(globalMin.floatValue());

		// crop values outside bounds to the new value interval
		for (int i = 0; i < valueRanking.size(); i++)
			if (valueRanking.get(i) < getGlobalMin().floatValue()) {
				valueRanking.remove(i);
				valueRanking.add(getGlobalMin().floatValue());
				i--;
			} else
				break;
	}

	@Override
	public void setGlobalMax(Number globalMax) {
		super.setGlobalMax(globalMax);

		valueRanking.add(globalMax.floatValue());

		// crop values outside bounds to the new value interval
		for (int i = valueRanking.size() - 1; i >= 0; i--)
			if (valueRanking.get(i) > getGlobalMax().floatValue()) {
				valueRanking.remove(i);
				valueRanking.add(getGlobalMax().floatValue());
				i++;
			} else
				break;
	}

	@Override
	public Number apply(Number t) {
		// check bounds
		if (t.floatValue() <= getGlobalMin().floatValue())
			return 0.0;
		else if (t.floatValue() >= getGlobalMax().floatValue())
			return 1.0;
		if (Double.isNaN(t.floatValue()))
			return Double.NaN;

		int binarySearch = Rankings.binarySearch(t, valueRanking, Float::doubleValue);

		if (binarySearch == -1) {
			System.err.println("QuantileNormalizationFunction: unable to find quantile for value " + t);
			return Double.NaN;
		}

		double res = binarySearch / (double) (valueRanking.size() - 1);
		return res;
	}

	@Override
	public String toString() {
		String s = super.toString();
		s += ("Count of Value Ranking: " + (valueRanking != null ? valueRanking.size() : "unknown"));

		return s;
	}

}
