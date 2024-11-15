package com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects;

import java.util.Date;

/**
 * 
 * @author Juergern Bernard
 *
 */
public class Parsers {
	private static DoubleParser doubleParser = new DoubleParser();
	private static IntegerParser integerParser = new IntegerParser();
	private static LongParser longParser = new LongParser();
	private static BooleanParser booleanParser = new BooleanParser();
	private static DateParser dateParser = new DateParser();

	public static synchronized Double parseDouble(Object o) {
		return doubleParser.apply(o);
	}

	public static synchronized Integer parseInteger(Object o) {
		return integerParser.apply(o);
	}

	public static synchronized Long parseLong(Object o) {
		return longParser.apply(o);
	}

	public static synchronized Boolean booleanParser(Object o) {
		return booleanParser.apply(o);
	}

	public static synchronized Date parseDate(Object o) {
		return dateParser.apply(o);
	}

}
