package com.github.TKnudsen.ComplexDataObject.model.attribute;

/**
 * <p>
 * Filter interface used to decide whether a given attribute (identified by
 * name) should be accepted, e.g. when selecting a subset of attributes from
 * a data object or schema.
 * </p>
 */
public interface IAttributeFilter {

	public boolean accept(String attribute);
}
