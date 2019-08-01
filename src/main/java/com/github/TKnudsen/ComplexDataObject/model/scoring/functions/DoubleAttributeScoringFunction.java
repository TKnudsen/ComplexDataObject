package com.github.TKnudsen.ComplexDataObject.model.scoring.functions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.DoubleParser;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IObjectParser;
import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;
import com.github.TKnudsen.ComplexDataObject.model.transformations.normalization.LinearNormalizationFunction;
import com.github.TKnudsen.ComplexDataObject.model.transformations.normalization.NormalizationFunction;
import com.github.TKnudsen.ComplexDataObject.model.transformations.normalization.QuantileNormalizationFunction;

public class DoubleAttributeScoringFunction extends AttributeScoringFunction<Double> {

	private StatisticsSupport statisticsSupport;
	private NormalizationFunction normalizationFunction;

	public DoubleAttributeScoringFunction(ComplexDataContainer container, String attribute) {
		this(container, new DoubleParser(), attribute, null, false, true, 1.0, null);
	}

	public DoubleAttributeScoringFunction(ComplexDataContainer container, IObjectParser<Double> parser,
			String attribute, String abbreviation, boolean quantileBased, boolean highIsGood, double weight) {
		this(container, parser, attribute, abbreviation, quantileBased, highIsGood, weight, null);
	}

	public DoubleAttributeScoringFunction(ComplexDataContainer container, IObjectParser<Double> parser,
			String attribute, String abbreviation, boolean quantileBased, boolean highIsGood, double weight,
			Function<ComplexDataObject, Double> uncertaintyFunction) {
		super(container, parser, attribute, abbreviation, quantileBased, highIsGood, weight, uncertaintyFunction);

		refreshScoringFunction();
	}

	@Override
	protected void refreshScoringFunction() {
		Map<Long, Object> attributeValues = getContainer().getAttributeValues(getAttribute());

		Collection<Object> values = attributeValues.values();

		List<Double> doubleValues = new ArrayList<>();

		for (Object o : values)
			doubleValues.add(getParser().apply(o));

		statisticsSupport = new StatisticsSupport(doubleValues);

		if (isQuantileBased())
			normalizationFunction = new QuantileNormalizationFunction(statisticsSupport, true);
		else
			normalizationFunction = new LinearNormalizationFunction(statisticsSupport, true);
	}

	@Override
	public Double applyValue(Double value) {
		if (value == null || Double.isNaN(value))
			return getScoreForMissingObjects();

		double output = normalizationFunction.apply(value).doubleValue();

		if (!isHighIsGood())
			output = 1 - output;

		// decision: weight should be applied externally. This, the relative value
		// domain is preserved and guaranteed internally.
		return output; // * getWeight();
	}

	public StatisticsSupport getStatisticsSupport() {
		return statisticsSupport;
	}

}
