package com.github.TKnudsen.ComplexDataObject.model.scoring.functions.Double;

import java.util.Collection;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IObjectParser;
import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;
import com.github.TKnudsen.ComplexDataObject.model.transformations.normalization.LinearNormalizationFunction;
import com.github.TKnudsen.ComplexDataObject.model.transformations.normalization.NormalizationFunction;
import com.github.TKnudsen.ComplexDataObject.model.transformations.normalization.QuantileNormalizationFunction;

public class DoubleAttributePositiveScoringFunction extends DoubleAttributeScoringFunction {

	@JsonIgnore
	private StatisticsSupport statisticsSupport;
	@JsonIgnore
	private StatisticsSupport statisticsSupportRaw;

	private NormalizationFunction normalizationFunction;
	private QuantileNormalizationFunction quantileNormalizationFunction;

	/**
	 * for serialization purposes
	 */
	@SuppressWarnings("unused")
	private DoubleAttributePositiveScoringFunction() {
		super();
	}

	public DoubleAttributePositiveScoringFunction(ComplexDataContainer container, String attribute,
			IObjectParser<Double> parser) {
		this(container, parser, attribute, null, false, true, 1.0, null);
	}

	public DoubleAttributePositiveScoringFunction(ComplexDataContainer container, IObjectParser<Double> parser,
			String attribute, String abbreviation, boolean quantileBased, boolean highIsGood, double weight) {
		this(container, parser, attribute, abbreviation, quantileBased, highIsGood, weight, null);
	}

	public DoubleAttributePositiveScoringFunction(ComplexDataContainer container, IObjectParser<Double> parser,
			String attribute, String abbreviation, boolean quantileBased, boolean highIsGood, double weight,
			Function<ComplexDataObject, Double> uncertaintyFunction) {
		super(container, parser, attribute, abbreviation, quantileBased, highIsGood, weight, uncertaintyFunction);
	}

	@Override
	protected void initializeStatisticsSupport(Collection<Double> doubleValues) {
		statisticsSupport = new StatisticsSupport(doubleValues);
	}

	@Override
	protected void initializeRawValuesStatisticsSupport(Collection<Double> doubleValues) {
		statisticsSupportRaw = new StatisticsSupport(doubleValues);
	}

	@Override
	protected void initializeNormalizationFunctions() {
		// if the entire value domain is NaN no normalizationFunction can be built
		if (!Double.isNaN(statisticsSupport.getMean())) {
			if (getQuantileNormalizationRate() > 0)
				quantileNormalizationFunction = new QuantileNormalizationFunction(statisticsSupportRaw, true);

			normalizationFunction = new LinearNormalizationFunction(statisticsSupport, true);
		}
	}

	@Override
	protected double normalizeLinear(double value) {
		return normalizationFunction.apply(value).doubleValue();
	}

	@Override
	protected double normalizeQuantiles(double value) {
		return quantileNormalizationFunction.apply(value).doubleValue();
	}

	@Override
	protected Double toDouble(Double t) {
		return t;
	}

	@Override
	protected double invertScore(double output) {
		return 1 - output;
	}

	@Override
	public StatisticsSupport getStatisticsSupport() {
		return statisticsSupport;
	}

}
