package com.github.TKnudsen.ComplexDataObject.model.scoring.functions;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IObjectParser;
import com.github.TKnudsen.ComplexDataObject.model.scoring.AttributeScoringChangeEvent;
import com.github.TKnudsen.ComplexDataObject.model.scoring.AttributeScoringChangeListener;
import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;
import com.github.TKnudsen.ComplexDataObject.model.transformations.normalization.LinearNormalizationFunction;
import com.github.TKnudsen.ComplexDataObject.model.transformations.normalization.NormalizationFunction;
import com.github.TKnudsen.ComplexDataObject.model.transformations.normalization.QuantileNormalizationFunction;

public abstract class AttributeScoringFunction<T> implements Function<ComplexDataObject, Double> {

	private final IObjectParser<T> parser;

	private final ComplexDataContainer container;

	private final String attribute;
	private String abbreviation;
	private boolean quantileBased;
	private boolean highIsGood;
	private double weight;

	private Function<ComplexDataObject, Double> uncertaintyFunction = null;
	private NormalizationFunction uncertaintyNormalizationFunction = null;

	private double scoreForMissingObjects = 0.25;

	private List<AttributeScoringChangeListener> listeners = new ArrayList<AttributeScoringChangeListener>();

	public AttributeScoringFunction(ComplexDataContainer container, IObjectParser<T> parser, String attribute) {
		this(container, parser, attribute, null, false, true, 1.0);
	}

	public AttributeScoringFunction(ComplexDataContainer container, IObjectParser<T> parser, String attribute,
			String abbreviation, boolean quantileBased, boolean highIsGood, double weight) {

		this(container, parser, attribute, abbreviation, quantileBased, highIsGood, weight, null);
	}

	public AttributeScoringFunction(ComplexDataContainer container, IObjectParser<T> parser, String attribute,
			String abbreviation, boolean quantileBased, boolean highIsGood, double weight,
			Function<ComplexDataObject, Double> uncertaintyFunction) {

		this.container = container;
		this.parser = parser;
		this.attribute = attribute;
		if (abbreviation != null)
			this.abbreviation = abbreviation;
		else
			this.abbreviation = StringUtils.abbreviate(attribute, 10);
		this.quantileBased = quantileBased;
		this.highIsGood = highIsGood;
		this.weight = weight;

		setUncertaintyFunction(uncertaintyFunction);
	}

	protected abstract void refreshScoringFunction();

	protected abstract Double applyValue(T value);

	@Override
	public Double apply(ComplexDataObject cdo) {

		Object o = cdo.getAttribute(attribute);
		if (o == null)
			return getScoreForMissingObjects();

		T value = parser.apply(o);

		double s = applyValue(value);

		// uncertainty information. this has the result that the scoring function does
		// not necessarily produce at least once the score 1.0
		if (getUncertaintyFunction() != null && uncertaintyNormalizationFunction != null) {
			Double u = getUncertaintyFunction().apply(cdo);
			Double u2 = uncertaintyNormalizationFunction.apply(u).doubleValue();
			return s * (1 - u2);
		}

		return s;
	}

	protected final void initializeUncertaintyNormalizationFunction() {
		if (uncertaintyFunction == null) {
			uncertaintyNormalizationFunction = null;
			return;
		}

		List<Double> doubleValues = new ArrayList<>();

		for (ComplexDataObject cdo : container)
			doubleValues.add(uncertaintyFunction.apply(cdo));

		StatisticsSupport statisticsSupport = new StatisticsSupport(doubleValues);

		if (isQuantileBased())
			uncertaintyNormalizationFunction = new QuantileNormalizationFunction(statisticsSupport, true);
		else
			uncertaintyNormalizationFunction = new LinearNormalizationFunction(statisticsSupport, true);
	}

	public void addAttributeScoringChangeListener(AttributeScoringChangeListener listener) {
		listeners.remove(listener);
		listeners.add(listener);
	}

	protected final void notifyListeners(AttributeScoringChangeEvent event) {
		for (AttributeScoringChangeListener listener : listeners)
			listener.attributeScoringFunctionChanged(event);
	}

	public String getAbbreviation() {
		return abbreviation;
	}

	public final void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;

		// not necessarily needed but consistent
		refreshScoringFunction();

		AttributeScoringChangeEvent event = new AttributeScoringChangeEvent(this, attribute, this);

		notifyListeners(event);
	}

	public boolean isHighIsGood() {
		return highIsGood;
	}

	public final void setHighIsGood(boolean highIsGood) {
		this.highIsGood = highIsGood;

		refreshScoringFunction();

		AttributeScoringChangeEvent event = new AttributeScoringChangeEvent(this, attribute, this);

		notifyListeners(event);
	}

	public boolean isQuantileBased() {
		return quantileBased;
	}

	public void setQuantileBased(boolean quantileBased) {
		this.quantileBased = quantileBased;

		refreshScoringFunction();

		AttributeScoringChangeEvent event = new AttributeScoringChangeEvent(this, attribute, this);

		notifyListeners(event);
	}

	public double getScoreForMissingObjects() {
		return scoreForMissingObjects;
	}

	public final void setScoreForMissingObjects(double scoreForMissingObjects) {
		this.scoreForMissingObjects = scoreForMissingObjects;

		refreshScoringFunction();

		AttributeScoringChangeEvent event = new AttributeScoringChangeEvent(this, attribute, this);

		notifyListeners(event);
	}

	public Function<ComplexDataObject, Double> getUncertaintyFunction() {
		return uncertaintyFunction;
	}

	public final void setUncertaintyFunction(Function<ComplexDataObject, Double> uncertaintyFunction) {
		this.uncertaintyFunction = uncertaintyFunction;

		refreshScoringFunction();

		initializeUncertaintyNormalizationFunction();

		AttributeScoringChangeEvent event = new AttributeScoringChangeEvent(this, attribute, this);

		notifyListeners(event);
	}

	public double getWeight() {
		return weight;
	}

	public final void setWeight(double weight) {
		this.weight = weight;

		refreshScoringFunction();

		AttributeScoringChangeEvent event = new AttributeScoringChangeEvent(this, attribute, this);

		notifyListeners(event);
	}

	public String getAttribute() {
		return attribute;
	}

	public ComplexDataContainer getContainer() {
		return container;
	}

	public IObjectParser<T> getParser() {
		return parser;
	}

}
