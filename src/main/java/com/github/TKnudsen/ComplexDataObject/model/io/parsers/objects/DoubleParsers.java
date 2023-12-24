package com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects;

public class DoubleParsers {

	private static DoubleParser doubleParser = new DoubleParser();

	public static synchronized Double apply(Object o) {
		return doubleParser.apply(o);
	}
}
