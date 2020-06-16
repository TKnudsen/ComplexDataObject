package com.github.TKnudsen.ComplexDataObject.model.scoring.functions.Double;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IObjectParser;
import com.github.TKnudsen.ComplexDataObject.model.scoring.AttributeScoringFunctionChangeEvent;
import com.github.TKnudsen.ComplexDataObject.model.scoring.functions.AttributeScoringFunction;
import com.github.TKnudsen.ComplexDataObject.model.tools.DataConversion;
import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;
import com.github.TKnudsen.ComplexDataObject.model.transformations.normalization.LinearNormalizationFunction;
import com.github.TKnudsen.ComplexDataObject.model.transformations.normalization.NormalizationFunction;
import com.github.TKnudsen.ComplexDataObject.model.transformations.normalization.QuantileNormalizationFunction;

public class DoubleAttributeBipolarScoringFunction extends DoubleAttributeScoringFunction {

	@JsonIgnore
	private StatisticsSupport statisticsSupportPositiveRaw;
	@JsonIgnore
	private StatisticsSupport statisticsSupportPositive;

	@JsonIgnore
	private StatisticsSupport statisticsSupportNegativeRaw;
	@JsonIgnore
	private StatisticsSupport statisticsSupportNegative;

	private double neutralValue = Double.NaN;

	private NormalizationFunction normalizationFunctionPositive;
	private NormalizationFunction normalizationFunctionNegative;
	private QuantileNormalizationFunction quantileNormalizationFunctionPositive;
	private QuantileNormalizationFunction quantileNormalizationFunctionNegative;

	/**
	 * for serialization purposes
	 */
	@SuppressWarnings("unused")
	private DoubleAttributeBipolarScoringFunction() {
		super();
	}

	public DoubleAttributeBipolarScoringFunction(ComplexDataContainer container, String attribute,
			IObjectParser<Double> parser) {
		this(container, parser, attribute, null, false, true, 1.0, null);
	}

	public DoubleAttributeBipolarScoringFunction(ComplexDataContainer container, IObjectParser<Double> parser,
			String attribute, String abbreviation, boolean quantileBased, boolean highIsGood, double weight) {
		this(container, parser, attribute, abbreviation, quantileBased, highIsGood, weight, null);
	}

	public DoubleAttributeBipolarScoringFunction(ComplexDataContainer container, IObjectParser<Double> parser,
			String attribute, String abbreviation, boolean quantileBased, boolean highIsGood, double weight,
			Function<ComplexDataObject, Double> uncertaintyFunction) {
		super(container, parser, attribute, abbreviation, quantileBased, highIsGood, weight, uncertaintyFunction);
	}

	public DoubleAttributeBipolarScoringFunction(ComplexDataContainer container, IObjectParser<Double> parser,
			String attribute, String abbreviation, boolean quantileBased, boolean highIsGood, double weight,
			Function<ComplexDataObject, Double> uncertaintyFunction, double neutralValue) {
		super(container, parser, attribute, abbreviation, quantileBased, highIsGood, weight, uncertaintyFunction);

		setNeutralValue(neutralValue);
	}

	protected void initializeStdOutlierTreatment(Collection<Double> doubleValues) {
		if (Double.isNaN(neutralValue))
			super.initializeStdOutlierTreatment(doubleValues);

		else {
			Collection<Double> values = new ArrayList<>();
			for (Double d : doubleValues)
				if (d != null && !Double.isNaN(d))
					values.add(d);

			StatisticsSupport statisticsSupport = new StatisticsSupport(values);

			double mean = neutralValue;// statisticsSupport.getMean();
			double standardDeviation = statisticsSupport.getStandardDeviation();
			double min = statisticsSupport.getMin();
			double max = statisticsSupport.getMax();

			double outlierStd = (this.outlierStd != null && !Double.isNaN(this.outlierStd)) ? this.outlierStd : 10.0;
			double outlierStdTop = (this.outlierStdTop != null && !Double.isNaN(this.outlierStdTop))
					? this.outlierStdTop
					: 10.0;

			outlierPruningMinValue = (outlierPruningMinValueExternal != null) ? outlierPruningMinValueExternal
					: Math.max(min, mean - outlierStd * standardDeviation);
			outlierPruningMaxValue = (outlierPruningMaxValueExternal != null) ? outlierPruningMaxValueExternal
					: Math.min(max, mean + outlierStdTop * standardDeviation);
		}
	}

	@Override
	/**
	 * given double values must not be null or NaN!
	 */
	protected void initializeStatisticsSupport(Collection<Double> doubleValues) {
		StatisticsSupport[] initializeStatistics = initializeStatistics(doubleValues);

		statisticsSupportNegative = initializeStatistics[0];
		statisticsSupportPositive = initializeStatistics[1];
	}

	@Override
	protected void initializeRawValuesStatisticsSupport(Collection<Double> doubleValues) {
		StatisticsSupport[] initializeStatistics = initializeStatistics(doubleValues);

		statisticsSupportNegativeRaw = initializeStatistics[0];
		statisticsSupportPositiveRaw = initializeStatistics[1];
	}

	/**
	 * 
	 * @param doubleValues
	 * @return StatisticsSupport[0] is negative StatisticsSupport[1] is positive
	 */
	private final StatisticsSupport[] initializeStatistics(Collection<Double> doubleValues) {
		// build positive and negative collections
		Collection<Double> negative = new ArrayList<>();
		Collection<Double> positive = new ArrayList<>();

		for (Double value : doubleValues)
			if (value != null && !Double.isNaN(value))
				if (value >= neutralValue)
					positive.add(value);
				else
					negative.add(value);

		return new StatisticsSupport[] { new StatisticsSupport(negative), new StatisticsSupport(positive) };
	}

	@Override
	protected void initializeNormalizationFunctions() {
		quantileNormalizationFunctionPositive = null;
		normalizationFunctionPositive = null;

		// if the entire value domain is NaN no normalizationFunction can be built
		if (!Double.isNaN(statisticsSupportPositiveRaw.getMean()))
			if (getQuantileNormalizationRate() > 0)
				quantileNormalizationFunctionPositive = new QuantileNormalizationFunction(statisticsSupportPositiveRaw,
						true);
		if (!Double.isNaN(statisticsSupportPositive.getMean()))
			normalizationFunctionPositive = new LinearNormalizationFunction(statisticsSupportPositive, true);
		else
			System.err.println(
					getClass().getSimpleName() + ": negative value range did not contain entries for attribute "
							+ getAttribute() + ". adjust neutral value");

		quantileNormalizationFunctionNegative = null;
		normalizationFunctionNegative = null;
		// if the entire value domain is NaN no normalizationFunction can be built
		if (!Double.isNaN(statisticsSupportNegativeRaw.getMean()) && statisticsSupportNegativeRaw.getCount() > 0)
			if (getQuantileNormalizationRate() > 0)
				quantileNormalizationFunctionNegative = new QuantileNormalizationFunction(statisticsSupportNegativeRaw,
						true);
		if (!Double.isNaN(statisticsSupportNegative.getMean()) && statisticsSupportNegative.getCount() > 0)
			normalizationFunctionNegative = new LinearNormalizationFunction(statisticsSupportNegative, true);
		else
			System.err.println(
					getClass().getSimpleName() + ": negative value range did not contain entries for attribute "
							+ getAttribute() + ". adjust neutral value");

		// close the region around 0.0
		if (quantileNormalizationFunctionPositive != null)
			quantileNormalizationFunctionPositive.setGlobalMin(neutralValue);
		if (normalizationFunctionPositive != null)
			normalizationFunctionPositive.setGlobalMin(neutralValue);
		if (quantileNormalizationFunctionNegative != null)
			quantileNormalizationFunctionNegative.setGlobalMax(neutralValue);
		if (normalizationFunctionNegative != null)
			normalizationFunctionNegative.setGlobalMax(neutralValue);
	}

	@Override
	protected double normalizeLinear(double value) {
		if (Double.isNaN(neutralValue) || value >= neutralValue)
			return normalizationFunctionPositive.apply(value).doubleValue();
		else if (normalizationFunctionNegative != null)
			return normalizationFunctionNegative.apply(value).doubleValue() - 1;

		return Double.NaN;
	}

	@Override
	protected double normalizeQuantiles(double value) {
		if (Double.isNaN(neutralValue) || value >= neutralValue)
			return quantileNormalizationFunctionPositive.apply(value).doubleValue();
		else if (quantileNormalizationFunctionNegative != null)
			return quantileNormalizationFunctionNegative.apply(value).doubleValue() - 1;

		return Double.NaN;
	}

	protected double calculateAverageScore() {
		double score = AttributeScoringFunction.calculateAverageScoreWithoutMissingValues(this, true);

		if (Double.isNaN(score))
			System.err.println(this.getClass().getSimpleName() + ": NaN value detected for the average score!");

		return score;
	}

	@Override
	/**
	 * this is expensive. is it really needed?
	 */
	public StatisticsSupport getStatisticsSupport() {
		Collection<Double> values = new ArrayList<>(
				DataConversion.doublePrimitivesToList(statisticsSupportPositive.getValues()));

		if (statisticsSupportNegative != null)
			values.addAll(DataConversion.doublePrimitivesToList(statisticsSupportNegative.getValues()));

		return new StatisticsSupport(values);
	}

	@Override
	protected Double toDouble(Double t) {
		return t;
	}

	@Override
	protected double invertScore(double output) {
		if (normalizationFunctionNegative != null)
			return -output;
		else
			return 1 - output;
	}

	public double getNeutralValue() {
		return neutralValue;
	}

	public void setNeutralValue(double neutralValue) {
		this.neutralValue = neutralValue;

		this.scoresBuffer = new HashMap<>();

		refreshScoringFunction();

		AttributeScoringFunctionChangeEvent event = new AttributeScoringFunctionChangeEvent(this, getAttribute(), this);

		notifyListeners(event);
	}

}
