package com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * <p>
 * Title: DoubleParser
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2016
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */

public class DoubleParser implements IObjectParser<Double> {

	@Override
	public Double apply(Object object) {
		String stringValue = String.valueOf(object).toLowerCase();

		if (stringValue.endsWith(";"))
			stringValue = stringValue.substring(0, stringValue.length() - 1);

		stringValue = stringValue.replace("%", "");
		while (stringValue.contains(" "))
			stringValue = stringValue.replace(" ", "");

		if (isDotForThousandsNotation(stringValue)) {
			NumberFormat nf = NumberFormat.getInstance(Locale.GERMAN);
			try {
				Number number = nf.parse(stringValue);
				if (number != null)
					return number.doubleValue();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		stringValue = stringValue.trim();
		stringValue = stringValue.replace(",", ".");
		try {
			return Double.parseDouble(stringValue);
		} catch (Exception e) {
			return Double.NaN;
		}
	}

	private boolean isDotForThousandsNotation(String string) {
		if (string == null)
			return false;

		int dot = string.indexOf(".");
		int comma = string.indexOf(",");

		if (dot > -1 && comma > -1)
			if (dot < comma)
				return true;

		return false;
	}
}
