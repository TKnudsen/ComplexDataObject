package com.github.TKnudsen.ComplexDataObject.model.scoring.functions.Double;

import java.util.Collection;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.DoubleParser;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IObjectParser;
import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;
import com.github.TKnudsen.ComplexDataObject.model.transformations.normalization.LinearNormalizationFunction;
import com.github.TKnudsen.ComplexDataObject.model.transformations.normalization.NormalizationFunction;
import com.github.TKnudsen.ComplexDataObject.model.transformations.normalization.QuantileNormalizationFunction;

public class DoubleAttributePositiveScoringFunction extends DoubleAttributeScoringFunction {

	@JsonIgnore
	private StatisticsSupport statisticsSupport;

	private NormalizationFunction normalizationFunction;

	/**
	 * for serialization purposes
	 */
	@SuppressWarnings("unused")
	private DoubleAttributePositiveScoringFunction() {
		super();
	}

	public DoubleAttributePositiveScoringFunction(ComplexDataContainer container, String attribute) {
		this(container, new DoubleParser(), attribute, null, false, true, 1.0, null);
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
	protected void initializeNormalizationFunctions() {
		// if the entire value domain is NaN no normalizationFunction can be built
		if (!Double.isNaN(statisticsSupport.getMean()))
			if (isQuantileBased())
				normalizationFunction = new QuantileNormalizationFunction(statisticsSupport, true);
			else
				normalizationFunction = new LinearNormalizationFunction(statisticsSupport, true);
	}

	@Override
	protected double normalize(double value) {
		return normalizationFunction.apply(value).doubleValue();
	}

	@Override
	public StatisticsSupport getStatisticsSupport() {
		return statisticsSupport;
	}

}
