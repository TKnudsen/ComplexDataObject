package com.github.TKnudsen.ComplexDataObject.data.uncertainty.string;

import java.util.Map;
import java.util.Set;

import com.github.TKnudsen.ComplexDataObject.data.uncertainty.IUncertaintyQualitative;

/**
 * <p>
 * Title: LabelUncertainty
 * </p>
 * 
 * <p>
 * Description: data model for uncertainties of string data.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2015-2017
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.0
 */
public class LabelUncertainty implements IUncertaintyQualitative<String> {

	private Map<String, Double> valueDistribution;

	private String representant;

	/**
	 * constructor for reflection-based and jackson-based access.
	 */
	@SuppressWarnings("unused")
	private LabelUncertainty() {
		this(null, null);
	}

	public LabelUncertainty(Map<String, Double> valueDistribution) {
		this(valueDistribution, null);
	}

	public LabelUncertainty(Map<String, Double> valueDistribution, String representant) {
		this.valueDistribution = valueDistribution;
		this.representant = representant;

		if (representant == null)
			this.representant = calculateRepresentant();
	}

	private String calculateRepresentant() {
		String rep = null;
		Double repRatio = 0.0;

		for (String value : valueDistribution.keySet())
			if (valueDistribution.get(value) > repRatio) {
				rep = value;
				repRatio = valueDistribution.get(value);
			}

		return rep;
	}

	@Override
	public String getRepresentant() {
		return representant;
	}

	@Override
	public Map<String, Double> getValueDistribution() {
		return valueDistribution;
	}

	public Set<String> getValueSet() {
		return valueDistribution.keySet();
	}
}
