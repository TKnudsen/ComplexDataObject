package com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects;

public class Parsers {
	private static DoubleParser doubleParser = new DoubleParser();
	private static IntegerParser integerParser = new IntegerParser();

	public static synchronized Double parseDouble(Object o) {
		return doubleParser.apply(o);
	}

	public static synchronized Integer parseInteger(Object o) {
		return integerParser.apply(o);
	}
}
