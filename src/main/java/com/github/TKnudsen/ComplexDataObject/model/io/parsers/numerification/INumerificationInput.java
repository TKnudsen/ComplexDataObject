package com.github.TKnudsen.ComplexDataObject.model.io.parsers.numerification;

public interface INumerificationInput<T, N extends Number> {

	public N addNumerification(T object, N value);
}
