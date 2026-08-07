package com.github.TKnudsen.ComplexDataObject.data.attributes;

import java.util.Collection;
import java.util.Map.Entry;

import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IObjectParser;

/**
 * <p>
 * Determines the attribute type and the type of parser needed for a collection
 * of values.
 * </p>
 *
 * @version 1.01
 * @since 2019
 */
public interface AttributeTypeAndParserDetector extends AttributeTypeDetector {

	public <T> Entry<Class<T>, IObjectParser<T>> getAttributeTypeAndParserType(Collection<Object> values);
}
