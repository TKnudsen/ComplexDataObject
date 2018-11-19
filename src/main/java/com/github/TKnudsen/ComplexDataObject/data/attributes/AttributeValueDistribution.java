package com.github.TKnudsen.ComplexDataObject.data.attributes;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;

/**
 * <p>
 * Title: AttributeValueDistribution
 * </p>
 * 
 * <p>
 * Description: stores the value distribution of an attribute AND the
 * priorization of external entities w.r.t. attributes.
 * 
 * Example: Distribution of horse power (attribute) of cars. Two different
 * scoring functions A and B have different priorizations of horse power, e.g.,
 * A: 100PS, B: 200PS.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2018
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.02
 */
public class AttributeValueDistribution<T> extends ComplexDataObject implements Comparable<T> {

	private Map<String, Double> priorizations = new HashMap<>();

	private Collection<T> valueDistribution;

	private Class<T> valueType;

	private String attributeName;

	@SuppressWarnings("unused") // only for serialization purposes
	public AttributeValueDistribution() {
		super();
	}

	public AttributeValueDistribution(String attributeName, Class<T> valueType, Collection<T> valueDistribution) {
		if (!CollectionUtils.isNotEmpty(valueDistribution))
			throw new IllegalArgumentException(getName() + ": value distribution must not be null or empty.");

		this.attributeName = attributeName;
		this.valueType = valueType;

		setValueDistribution(valueDistribution);
		setName(attributeName);

		// it may be necessary to do some validity checks, e.g., whether the attribute
		// name contains a line break, etc.
	}

	public Double getPriorization(String characterOrPriorization) {
		return priorizations.get(characterOrPriorization);
	}

	/**
	 * 
	 * @param identifier
	 * @param value
	 *            must not be null. otherwise JSON reader will interpret the value
	 *            as string.
	 */
	public void setPriorization(String identifier, double value) {
		if (Double.isNaN((double) value)) {
			System.err.println(getName() + ": priorization must not be NaN. set to 0");
			this.priorizations.put(identifier, 0.0);
		} else
			this.priorizations.put(identifier, value);
	}

	public Map<String, Double> getPriorizations() {
		return priorizations;
	}

	public void setPriorizations(Map<String, Double> priorizations) {
		this.priorizations = priorizations;
	}

	public Collection<T> getValueDistribution() {
		return valueDistribution;
	}

	public void setValueDistribution(Collection<T> valueDistribution) {
		this.valueDistribution = valueDistribution;
	}

	public Class<T> getValueType() {
		return valueType;
	}

	@Override
	public int compareTo(T o) {
		if (o == null)
			return -1;

		if (equals(o))
			return 0;

		return toString().compareTo(((T) o).toString());
	}

	public String getAttributeName() {
		return this.attributeName;
	}

	@Override
	public String getDescription() {
		return "Stores information about the distribution of values of an attribute";
	}

	@Override
	public String toString() {
		return getName() + ", " + getAttributeName();
	}

}
