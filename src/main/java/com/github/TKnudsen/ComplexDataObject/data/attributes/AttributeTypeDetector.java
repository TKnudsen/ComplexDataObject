package com.github.TKnudsen.ComplexDataObject.data.attributes;

import java.util.Collection;

/**
 * <p>
 * determines the type of an attribute (column of a table)
 * </p>
 *
 * @version 1.01
 * @since 2018
 */
public interface AttributeTypeDetector {

	public Class<?> getAttributeType(Collection<Object> values);
}
