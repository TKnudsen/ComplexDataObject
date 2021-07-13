package com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects;

/**
 * <p>
 * ComplexDataObject
 * </p>
 * 
 * <p>
 * Parses values that contain abbreviations such as MIO and BIO for million and
 * billions. Important German and English abbreviations are included.
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2021 Juergen Bernard,
 * https://github.com/TKnudsen/ComplexDataObject
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.03
 */
public class DoubleParserForNumberAbbreviations extends DoubleParser {

	public DoubleParserForNumberAbbreviations(boolean dotMeansThousands) {
		super(dotMeansThousands);
	}

	@Override
	public Double apply(Object object) {
		if (object == null)
			return Double.NaN;

		if (object instanceof Double)
			return new Double((double) object);

		// handle currency identification
		String s = object.toString().trim();
		if (s.isEmpty())
			return Double.NaN;

		double multiply = 1.0;
		if (s.contains("Mrd")) {
			s = s.substring(0, s.indexOf("Mrd")).trim();
			// German
			multiply = 1000000000;
		} else if (s.contains("Bio")) {
			s = s.substring(0, s.indexOf("Bio")).trim();
			// German
			multiply = 1000000000000L;
		} else if (s.contains("Billion")) {
			s = s.substring(0, s.indexOf("Billion")).trim();
			multiply = 1000000000;
		} else if (s.endsWith("B")) {
			s = s.substring(0, s.length() - 1).trim();
			multiply = 1000000000;
		} else if (s.contains("Mio")) {
			s = s.substring(0, s.indexOf("Mio")).trim();
			// German
			multiply = 1000000;
		} else if (s.contains("Million")) {
			s = s.substring(0, s.indexOf("Million")).trim();
			multiply = 1000000;
		} else if (s.endsWith("M")) {
			s = s.substring(0, s.length() - 1).trim();
			multiply = 1000000;
		} else if (s.endsWith("K") && s.length() > 1) {
			s = s.substring(0, s.length() - 1).trim();
			multiply = 1000;
		} else if (s.endsWith("k") && s.length() > 1) {
			s = s.substring(0, s.length() - 1).trim();
			multiply = 1000;
		}

		try {
			Double d = super.apply(s);
			d *= multiply;
			return d;
		} catch (Exception e) {
			System.err.println(
					"LargeAmountDoubleParserForNumberAbbreviationsAbbreviationsDoubleParser.apply has problems with value "
							+ s);
			return Double.NaN;
		}
	}
}
