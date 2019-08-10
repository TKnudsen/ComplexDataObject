package com.github.TKnudsen.ComplexDataObject.model.scoring.functions.Double;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.DoubleParser;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IObjectParser;
import com.github.TKnudsen.ComplexDataObject.model.tools.DataConversion;
import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;
import com.github.TKnudsen.ComplexDataObject.model.transformations.normalization.LinearNormalizationFunction;
import com.github.TKnudsen.ComplexDataObject.model.transformations.normalization.NormalizationFunction;
import com.github.TKnudsen.ComplexDataObject.model.transformations.normalization.QuantileNormalizationFunction;

public class DoubleAttributeBipolarScoringFunction extends DoubleAttributeScoringFunction {

	@JsonIgnore
	private StatisticsSupport statisticsSupportPositive;

	@JsonIgnore
	private StatisticsSupport statisticsSupportNegative;

	private NormalizationFunction normalizationFunctionPositive;
	private NormalizationFunction normalizationFunctionNegative;

	/**
	 * for serialization purposes
	 */
	@SuppressWarnings("unused")
	private DoubleAttributeBipolarScoringFunction() {
		super();
	}

	public DoubleAttributeBipolarScoringFunction(ComplexDataContainer container, String attribute) {
		this(container, new DoubleParser(), attribute, null, false, true, 1.0, null);
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

	@Override
	/**
	 * given double values must not be null or NaN!
	 */
	protected void initializeStatisticsSupport(Collection<Double> doubleValues) {

		// build positive and negative collections
		Collection<Double> negative = new ArrayList<>();
		Collection<Double> positive = new ArrayList<>();

		for (Double value : doubleValues)
			if (value != null && !Double.isNaN(value))
				if (value >= 0)
					positive.add(value);
				else
					negative.add(value);

		statisticsSupportPositive = new StatisticsSupport(positive);
		statisticsSupportNegative = new StatisticsSupport(negative);
	}

	@Override
	protected void initializeNormalizationFunctions() {
		// if the entire value domain is NaN no normalizationFunction can be built
		if (!Double.isNaN(statisticsSupportPositive.getMean()))
			if (isQuantileBased())
				normalizationFunctionPositive = new QuantileNormalizationFunction(statisticsSupportPositive, true);
			else
				normalizationFunctionPositive = new LinearNormalizationFunction(statisticsSupportPositive, true);

		// if the entire value domain is NaN no normalizationFunction can be built
		if (!Double.isNaN(statisticsSupportNegative.getMean()))
			if (statisticsSupportNegative.getCount() > 0) {
				if (isQuantileBased())
					normalizationFunctionNegative = new QuantileNormalizationFunction(statisticsSupportNegative, true);
				else {
					normalizationFunctionNegative = new LinearNormalizationFunction(statisticsSupportNegative, true);

					// close the region around 0.0
					normalizationFunctionPositive.setGlobalMin(0.0);
					normalizationFunctionNegative.setGlobalMax(0.0);
				}
			} else
				normalizationFunctionNegative = null;
	}

	@Override
	protected double normalize(double value) {
		if (value >= 0)
			return normalizationFunctionPositive.apply(value).doubleValue();
		else if (normalizationFunctionNegative != null)
			return normalizationFunctionNegative.apply(value).doubleValue() - 1;

		return Double.NaN;
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

}
