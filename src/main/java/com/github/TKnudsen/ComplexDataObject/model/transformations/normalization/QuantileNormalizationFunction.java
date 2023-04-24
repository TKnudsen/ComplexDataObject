package com.github.TKnudsen.ComplexDataObject.model.transformations.normalization;

import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;

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
 * Copyright: Copyright (c) 2016-2023
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.04
 */
public class QuantileNormalizationFunction extends NormalizationFunction {

	private Ranking<Double> valueRanking;

	// TODO replace by interpolation search
	private SortedMap<Double, Integer> rankingLookup;

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

	public QuantileNormalizationFunction(StatisticsSupport statisticsSupport) {
		super(statisticsSupport);

		initializeRanking(statisticsSupport.getValues());
	}

	public QuantileNormalizationFunction(StatisticsSupport statisticsSupport, boolean limitToBounds) {
		super(statisticsSupport, limitToBounds);

		initializeRanking(statisticsSupport.getValues());
	}

	private void initializeRanking(Collection<? extends Number> values) {
		valueRanking = new Ranking<>();

		for (Number value : values)
			valueRanking.add(value.doubleValue());

		refreshRankingLookup();
	}

	private void initializeRanking(double[] values) {
		valueRanking = new Ranking<>();

		for (double value : values)
			valueRanking.add(value);

		refreshRankingLookup();
	}

	private final void refreshRankingLookup() {
		rankingLookup = new TreeMap<Double, Integer>();

		for (int i = 0; i < valueRanking.size(); i += 50)
			rankingLookup.put(valueRanking.get(i), i);
	}

	@Override
	public void setGlobalMin(Number globalMin) {
		super.setGlobalMin(globalMin);

		valueRanking.add(globalMin.doubleValue());

		// crop values outside bounds to the new value interval
		for (int i = 0; i < valueRanking.size(); i++)
			if (valueRanking.get(i) < getGlobalMin().doubleValue()) {
				valueRanking.remove(i);
				valueRanking.add(getGlobalMin().doubleValue());
				i--;
			} else
				break;
	}

	@Override
	public void setGlobalMax(Number globalMax) {
		super.setGlobalMax(globalMax);

		valueRanking.add(globalMax.doubleValue());

		// crop values outside bounds to the new value interval
		for (int i = valueRanking.size() - 1; i >= 0; i--)
			if (valueRanking.get(i) > getGlobalMax().doubleValue()) {
				valueRanking.remove(i);
				valueRanking.add(getGlobalMax().doubleValue());
				i++;
			} else
				break;
	}

	@Override
	public Number apply(Number t) {
		// check bounds
		if (t.doubleValue() <= getGlobalMin().doubleValue())
			return 0.0;
		else if (t.doubleValue() >= getGlobalMax().doubleValue())
			return 1.0;
		if (Double.isNaN(t.doubleValue()))
			return Double.NaN;

//		int interpolationSearch = Rankings.interpolationSearch(t, valueRanking, Double::doubleValue);
		int binarySearch = Rankings.binarySearch(t, valueRanking, Double::doubleValue);

		if (binarySearch == -1) {
			System.err.println("QuantileNormalizationFunction: unable to find quantile for value " + t);
			return Double.NaN;
		}

		double res = binarySearch / (double) (valueRanking.size() - 1);
		return res;

//		// iterate the ranking
//		double q = 1.0 / (double) (valueRanking.size() - 1);
//
//		// speedup: lookup the index range
//		Integer startIndex = 0;
//		for (Double d : rankingLookup.keySet())
//			if (d <= t.doubleValue())
//				startIndex = rankingLookup.get(d);
//			else
//				break;
//
//		for (int i = startIndex; i < valueRanking.size(); i++)
//			if (t.doubleValue() < valueRanking.get(i))
//				return (double) (i - 1) / (double) (valueRanking.size() - 1) + q * 0.5;
//			else if (t.doubleValue() == valueRanking.get(i)) {
//				double lower = (double) (i) / (double) (valueRanking.size() - 1);
//				// check how many equal values exist
//				double upper = lower;
//				for (int j = i + 1; j < valueRanking.size(); j++)
//					if (t.doubleValue() == valueRanking.get(j))
//						upper = (double) (j) / (double) (valueRanking.size() - 1);
//					else
//						break;
//				return (lower + upper) * 0.5;
//			}
//
//		return Double.NaN;
	}

}
