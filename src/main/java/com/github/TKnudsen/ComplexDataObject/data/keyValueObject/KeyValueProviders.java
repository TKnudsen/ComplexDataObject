package com.github.TKnudsen.ComplexDataObject.data.keyValueObject;

import java.util.List;
import java.util.Objects;

import com.github.TKnudsen.ComplexDataObject.data.interfaces.IKeyValueProvider;

/**
 * <p>
 * support tools for KeyValueProvider data.
 * </p>
 *
 * @version 1.02
 * @since 2017
 */
public class KeyValueProviders {

	public static <V, T extends IKeyValueProvider<V>> void setAttribute(String attributeName, List<? extends T> objects,
			List<? extends V> attributeValues) {
		Objects.requireNonNull(attributeName, "The attributeName may not be null");
		if (objects.size() != attributeValues.size()) {
			throw new IllegalArgumentException(
					"There are " + objects.size() + " objects but " + attributeValues.size() + " attribute values");
		}

		for (int i = 0; i < objects.size(); i++) {
			T object = objects.get(i);
			V attributeValue = attributeValues.get(i);
			object.add(attributeName, attributeValue);
		}
	}

}
