package com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects;

/**
 * <p>
 * Parses arbitrary objects into their String representation by delegating to
 * String.valueOf, returning null for null input.
 * </p>
 *
 * @version 1.02
 * @since 2018
 */
public class StringParser implements IObjectParser<String> {

	@Override
	public String apply(Object t) {
		if (t == null)
			return null;

		return String.valueOf(t);
	}

	@Override
	public Class<String> getOutputClassType() {
		return String.class;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
}
