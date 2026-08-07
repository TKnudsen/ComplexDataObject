package com.github.TKnudsen.ComplexDataObject.model.transformations.mergeAndJoin.string;

import com.github.TKnudsen.ComplexDataObject.model.transformations.mergeAndJoin.IObjectMerger;

/**
 * <p>
 * Creates a bi-gram from two given strings.
 * </p>
 *
 * @version 1.01
 * @since 2017
 */
public class BigramGenerator implements IObjectMerger<String> {

	private String connector = "->";

	public BigramGenerator() {
	}

	public BigramGenerator(String connector) {
		this.connector = connector;
	}

	@Override
	public String merge(String object1, String object2) {
		if (object1 == null && object2 == null)
			return null;

		if (object1 == null)
			return new String(object2);

		if (object2 == null)
			return new String(object1);

		return object1 + connector + object2;
	}

	public String getConnector() {
		return connector;
	}

	public void setConnector(String connector) {
		this.connector = connector;
	}

}
