package com.github.TKnudsen.ComplexDataObject.model.scoring.functions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.BooleanParser;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IObjectParser;
import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;
import com.github.TKnudsen.ComplexDataObject.model.transformations.normalization.LinearNormalizationFunction;
import com.github.TKnudsen.ComplexDataObject.model.transformations.normalization.NormalizationFunction;
import com.github.TKnudsen.ComplexDataObject.model.transformations.normalization.QuantileNormalizationFunction;

public class BooleanAttributeScoringFunction extends AttributeScoringFunction<Boolean> {

	@JsonIgnore
	private StatisticsSupport statisticsSupport;

	private NormalizationFunction normalizationFunction;
	private QuantileNormalizationFunction quantileNormalizationFunction;

	/**
	 * for serialization purposes
	 */
	@SuppressWarnings("unused")
	private BooleanAttributeScoringFunction() {
		super();
	}

	public BooleanAttributeScoringFunction(ComplexDataContainer container, String attribute) {
		this(container, new BooleanParser(), attribute, null, false, true, 1.0, null);
	}

	public BooleanAttributeScoringFunction(ComplexDataContainer container, IObjectParser<Boolean> parser,
			String attribute, String abbreviation, boolean quantileBased, boolean highIsGood, double weight) {
		this(container, parser, attribute, abbreviation, quantileBased, highIsGood, weight, null);
	}

	public BooleanAttributeScoringFunction(ComplexDataContainer container, IObjectParser<Boolean> parser,
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

		for (Object o : values) {
			Boolean b = getParser().apply(o);
			if (b == null)
				continue;

			doubleValues.add(toDouble(b));
		}

		statisticsSupport = new StatisticsSupport(doubleValues);

		if (getQuantileNormalizationRate() > 0)
			quantileNormalizationFunction = new QuantileNormalizationFunction(statisticsSupport, true);

		normalizationFunction = new LinearNormalizationFunction(0.0, 1.0, true);

		scoreAverageWithoutMissingValues = AttributeScoringFunctions.calculateAverageScoreWithoutMissingValues(this,
				false);

		// clear scoresBuffer as it contains old missing value data now
		scoresBuffer.clear();

		if (Double.isNaN(scoreAverageWithoutMissingValues))
			System.err.println(
					this.getClass().getSimpleName() + ": NaN value detected for the scoreAverageWithoutMissingValues!");

		Double missingValueAvgScoreRatio = getMissingValueAvgScoreRatio();
		if (missingValueAvgScoreRatio == null || Double.isNaN(missingValueAvgScoreRatio))
			scoreForMissingObjects = scoreAverageWithoutMissingValues * 0.5;
		else
			scoreForMissingObjects = scoreAverageWithoutMissingValues * missingValueAvgScoreRatio;
	}

	@Override
	protected Double toDouble(Boolean t) {
		Objects.requireNonNull(t);

		if (t)
			return 1.0;
		return 0.0;
	}

	@Override
	protected double invertScore(double output) {
		return output = 1 - output;
	}

	@Override
	protected double normalizeLinear(double value) {
		return normalizationFunction.apply(value).doubleValue();
	}

	@Override
	protected double normalizeQuantiles(double value) {
		return quantileNormalizationFunction.apply(value).doubleValue();
	}

}
