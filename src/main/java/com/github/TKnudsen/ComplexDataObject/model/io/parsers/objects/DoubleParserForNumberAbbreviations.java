package com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects;

/**
 * <p>
 * ComplexDataObject
 * </p>
 * 
 * <p>
 * Parses Double values that input that contains abbreviations such as MIO and
 * BIO for million and billions. Important German and English abbreviations are
 * included.
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard,
 * https://github.com/TKnudsen/ComplexDataObject
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.02
 */
public class DoubleParserForNumberAbbreviations extends DoubleParser {

	@Override
	public Double apply(Object object) {

		if (object == null)
			return Double.NaN;

		Double d = super.apply(object);

		if (d != null && !Double.isNaN(d))
			return d;

		// handle currency identification
		String s = object.toString().trim();
		if (s.isEmpty())
			return Double.NaN;

		double multiply = 1.0;
		if (s.contains("Mrd")) {
			s = s.substring(0, s.indexOf("Mrd")).trim();
			multiply = 1000000000;
		} else if (s.contains("Bio")) {
			s = s.substring(0, s.indexOf("Bio")).trim();
			multiply = 1000000000;
		} else if (s.contains("Billion")) {
			s = s.substring(0, s.indexOf("Billion")).trim();
			multiply = 1000000000;
		} else if (s.endsWith("B")) {
			s = s.substring(0, s.length() - 2).trim();
			multiply = 1000000000;
		} else if (s.contains("Mio")) {
			s = s.substring(0, s.indexOf("Mio")).trim();
			multiply = 1000000;
		} else if (s.contains("Million")) {
			s = s.substring(0, s.indexOf("Million")).trim();
			multiply = 1000000;
		} else if (s.endsWith("M")) {
			s = s.substring(0, s.length() - 2).trim();
			multiply = 1000000;
		} else if (s.endsWith("K") && s.length() > 1) {
			s = s.substring(0, s.length() - 2).trim();
			multiply = 1000;
		} else if (s.endsWith("k") && s.length() > 1) {
			s = s.substring(0, s.length() - 2).trim();
			multiply = 1000;
		} else {
			System.err.println("LargeAmountAbbreviationsDoubleParser.apply has problems with value " + s);
			return Double.NaN;
		}

		try {
			d = super.apply(s);
			d *= multiply;
		} catch (Exception e) {
			System.err.println("LargeAmountAbbreviationsDoubleParser.apply has problems with value " + s);
			d = Double.NaN;
		}

		return d;
	}
}
