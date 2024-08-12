package com.github.TKnudsen.ComplexDataObject.model.scoring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

import javax.swing.event.ChangeListener;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.data.entry.EntryWithComparableKey;
import com.github.TKnudsen.ComplexDataObject.data.ranking.Ranking;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IObjectParser;
import com.github.TKnudsen.ComplexDataObject.model.scoring.functions.AttributeScoringFunction;
import com.github.TKnudsen.ComplexDataObject.model.scoring.functions.AttributeScoringFunctions;
import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;
import com.github.TKnudsen.ComplexDataObject.model.transformations.normalization.LinearNormalizationFunction;

public final class AttributeScoringModel implements AttributeScoringFunctionChangeListener {

	private List<AttributeScoringFunction<?>> attributeScoringFunctions = new ArrayList<>();
	private Map<String, AttributeScoringFunction<?>> attributeScoringFunctionsLookup = new HashMap<>();

	private LinearNormalizationFunction normalizationFunction = null;

	private Boolean validationMode = false;

	@JsonIgnore
	private List<ChangeListener> changeListeners = new ArrayList<>();

	/**
	 * is not stored internally as the objects to be ranked may differ from the ones
	 * that have been used for the initialization of the scoring functions. Just as
	 * well it is not the aim to store the data (objects) for the calculation in the
	 * model itself
	 * 
	 * @param container
	 * @return
	 */
	public Ranking<EntryWithComparableKey<Double, ComplexDataObject>> calculateRanking(ComplexDataContainer container) {

		if (container == null)
			return null;

		Ranking<EntryWithComparableKey<Double, ComplexDataObject>> cdoRanking = new Ranking<>();

		for (ComplexDataObject cdo : container) {
			double score = getScore(cdo);

			cdoRanking.add(new EntryWithComparableKey<Double, ComplexDataObject>(score, cdo));
		}

		if (validationMode)
			for (EntryWithComparableKey<Double, ComplexDataObject> entry : cdoRanking)
				System.out.println(MathFunctions.round(entry.getKey(), 3) + ":\t" + entry.getValue().getName() + ":\t"
						+ entry.getValue().getAttribute("ISIN"));

		normalizationFunction = new LinearNormalizationFunction(cdoRanking.getFirst().getKey(),
				cdoRanking.getLast().getKey());

		handleItemRankingChangeEvent(new ItemRankingChangeEvent(this, cdoRanking));

		return cdoRanking;
	}

	public Double getScore(ComplexDataObject cdo) {
		double score = 0.0;
		for (AttributeScoringFunction<?> attributeScoringFunction : attributeScoringFunctions) {
			Double v = attributeScoringFunction.apply(cdo);

			v *= attributeScoringFunction.getWeight();

			if (v != null && !Double.isNaN(v))
				score += v;
		}

		return score;
	}

	public Double getScoreRelative(ComplexDataObject cdo) {
		double score = getScore(cdo);

		return (normalizationFunction != null) ? normalizationFunction.apply(Double.valueOf(score)).doubleValue() : 0.0;
	}

	public Double getUncertainty(ComplexDataObject cdo) {
		double u = 0.0;
		for (AttributeScoringFunction<?> attributeScoringFunction : attributeScoringFunctions) {
			if (attributeScoringFunction.getUncertaintyFunction() == null)
				continue;

			Double v = attributeScoringFunction.getUncertaintyFunction().apply(cdo);

			if (v != null && !Double.isNaN(v))
				u += v;
			else
				u += 0.5;
		}

		return u;
	}

	public void addAttributeScoringFunction(ComplexDataContainer container, String attribute) {
		this.addAttributeScoringFunction(container, attribute, null);
	}

	public AttributeScoringFunction<?> addAttributeScoringFunction(ComplexDataContainer container, String attribute,
			Function<ComplexDataObject, Double> uncertaintyFunction) {
		Objects.requireNonNull(container);
		Objects.requireNonNull(attribute);

		AttributeScoringFunction<?> f = AttributeScoringFunctions.create(container, attribute, uncertaintyFunction);

		addAttributeScoringFunction(f);

		return f;
	}

	public AttributeScoringFunction<?> addAttributeScoringFunction(ComplexDataContainer container, String attribute,
			Function<ComplexDataObject, Double> uncertaintyFunction, IObjectParser<Double> toDoubleParser) {
		Objects.requireNonNull(container);
		Objects.requireNonNull(attribute);

		AttributeScoringFunction<?> f = AttributeScoringFunctions.create(container, attribute, uncertaintyFunction,
				toDoubleParser);

		addAttributeScoringFunction(f);

		return f;
	}

	public void addAttributeScoringFunction(AttributeScoringFunction<?> attributeScoringFunction) {
		this.attributeScoringFunctions.add(attributeScoringFunction);
		this.attributeScoringFunctionsLookup.put(attributeScoringFunction.getAttribute(), attributeScoringFunction);
		attributeScoringFunction.addAttributeScoringChangeListener(this);

		AttributeScoringFunctionChangeEvent event = new AttributeScoringFunctionChangeEvent(this,
				attributeScoringFunction.getAttribute(), attributeScoringFunction);
		handleAttributeScoringChangeEvent(event);
	}

	public void addAttributeScoringFunctions(List<AttributeScoringFunction<?>> attributeScoringFunctions) {
		this.attributeScoringFunctions.addAll(attributeScoringFunctions);
		for (AttributeScoringFunction<?> attributeScoringFunction : this.attributeScoringFunctions) {
			this.attributeScoringFunctionsLookup.put(attributeScoringFunction.getAttribute(), attributeScoringFunction);
			attributeScoringFunction.addAttributeScoringChangeListener(this);
		}

		AttributeScoringFunctionChangeEvent event = new AttributeScoringFunctionChangeEvent(this, null, null);
		handleAttributeScoringChangeEvent(event);
	}

	public void removeAttributeScoringFunction(String attribute) {
		AttributeScoringFunction<?> f = null;

		for (int i = 0; i < attributeScoringFunctions.size(); i++)
			if (attributeScoringFunctions.get(i).getAttribute().equals(attribute)) {
				f = attributeScoringFunctions.remove(i);
				i--;
			}
		this.attributeScoringFunctionsLookup.remove(attribute);

		AttributeScoringFunctionChangeEvent event = new AttributeScoringFunctionChangeEvent(this, attribute, f);
		handleAttributeScoringChangeEvent(event);
	}

	public void clearAttributeScoringFunctions() {
		attributeScoringFunctions.clear();
		attributeScoringFunctionsLookup.clear();

		AttributeScoringFunctionChangeEvent event = new AttributeScoringFunctionChangeEvent(this, null, null);
		handleAttributeScoringChangeEvent(event);
	}

	public boolean containsAttributeScoringFunction(String attribute) {
		return getAttributeScoringFunction(attribute) != null;
	}

	public AttributeScoringFunction<?> getAttributeScoringFunction(String attribute) {
//		for (int i = 0; i < attributeScoringFunctions.size(); i++)
//			if (attributeScoringFunctions.get(i).getAttribute().equals(attribute))
//				return attributeScoringFunctions.get(i);
		return attributeScoringFunctionsLookup.get(attribute);
//		return null;
	}

	public List<AttributeScoringFunction<?>> getAttributeScoringFunctions() {
		return new CopyOnWriteArrayList<AttributeScoringFunction<?>>(attributeScoringFunctions);
	}

	/**
	 * @deprecated use AttributeScoringModels.getAttributeScoringCorrelation
	 * @param container
	 * @param f1
	 * @param f2
	 * @param minimumSize default: 2 (leading to a perfect correlation though)
	 * @return
	 */
	public double getAttributeScoringCorrelation(ComplexDataContainer container, AttributeScoringFunction<?> f1,
			AttributeScoringFunction<?> f2, int minimumSize) {
		return AttributeScoringModels.getAttributeScoringCorrelation(container, f1, f2, true, false, minimumSize);

//		Collection<Double> values1 = new ArrayList<>();
//		Collection<Double> values2 = new ArrayList<>();
//
//		for (ComplexDataObject cdo : container) {
//			double v1 = f1.apply(cdo);
//			double v2 = f2.apply(cdo);
//
//			if (!Double.isNaN(v1) && !Double.isNaN(v2)) {
//				values1.add(v1);
//				values2.add(v2);
//			}
//		}
//
//		PearsonsCorrelation pc = new PearsonsCorrelation();
//		double[] xArray = DataConversion.toPrimitives(values1);
//		double[] yArray = DataConversion.toPrimitives(values2);
//
//		if (xArray.length < 2 || yArray.length < 2)
//			return 0.0;
//
//		return pc.correlation(xArray, yArray);
	}

	public List<String> getAttributes() {
		List<String> attributeList = new ArrayList<>();

		for (AttributeScoringFunction<?> attributeScoringFunction : getAttributeScoringFunctions()) {
			attributeList.add(attributeScoringFunction.getAttribute());
		}

		Collections.sort(attributeList);

		return attributeList;
	}

	public void setAttributeScoringInformationQuantile(String attribute, Boolean quantile) {
		AttributeScoringFunction<?> f = getAttributeScoringFunction(attribute);

		if (f == null)
			throw new IllegalArgumentException("AttributeSorter: attribute does not exist");

		f.setQuantileBased(quantile);

		AttributeScoringFunctionChangeEvent event = new AttributeScoringFunctionChangeEvent(this, attribute, f);
		handleAttributeScoringChangeEvent(event);
	}

	public void setAttributeScoringInformationHighIsGood(String attribute, Boolean highIsGood) {
		AttributeScoringFunction<?> f = getAttributeScoringFunction(attribute);

		if (f == null)
			throw new IllegalArgumentException("AttributeSorter: attribute does not exist");

		f.setHighIsGood(highIsGood);

		AttributeScoringFunctionChangeEvent event = new AttributeScoringFunctionChangeEvent(this, attribute, f);
		handleAttributeScoringChangeEvent(event);
	}

	public void setAttributeScoringInformationWeight(String attribute, Double weight) {
		AttributeScoringFunction<?> f = getAttributeScoringFunction(attribute);

		if (f == null)
			throw new IllegalArgumentException("AttributeSorter: attribute does not exist");

		f.setWeight(weight);

		AttributeScoringFunctionChangeEvent event = new AttributeScoringFunctionChangeEvent(this, attribute, f);
		handleAttributeScoringChangeEvent(event);
	}

	public void addChangeListener(ChangeListener listener) {
		changeListeners.remove(listener);

		changeListeners.add(listener);
	}

	private final void handleAttributeScoringChangeEvent(AttributeScoringFunctionChangeEvent event) {
		normalizationFunction = null;

		for (ChangeListener listener : changeListeners)
			listener.stateChanged(event);
	}

	private final void handleItemRankingChangeEvent(ItemRankingChangeEvent event) {
		for (ChangeListener listener : changeListeners)
			listener.stateChanged(event);
	}

	public Boolean isValidationMode() {
		return validationMode;
	}

	public void setValidationMode(Boolean validationMode) {
		this.validationMode = validationMode;
	}

	@Override
	public void attributeScoringFunctionChanged(AttributeScoringFunctionChangeEvent event) {
		handleAttributeScoringChangeEvent(event);
	}
}
