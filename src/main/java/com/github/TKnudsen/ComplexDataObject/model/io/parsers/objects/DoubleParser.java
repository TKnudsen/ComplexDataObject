package com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2016-2019
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.04
 */

public class DoubleParser implements IObjectParser<Double> {

	private boolean dotMeansThousands = true;

	private boolean printStackTrace = false;

	@Override
	public Double apply(Object object) {
		if (object instanceof Double)
			return new Double((double) object);

		String stringValue = String.valueOf(object).toLowerCase();

		if (stringValue.endsWith(";"))
			stringValue = stringValue.substring(0, stringValue.length() - 1);

		stringValue = stringValue.replace("%", "");
		while (stringValue.contains(" "))
			stringValue = stringValue.replace(" ", "");

		if (dotMeansThousands || isDotForThousandsNotation(stringValue)) {
			NumberFormat nf = NumberFormat.getInstance(Locale.GERMAN);
			try {
				Number number = nf.parse(stringValue);
				if (number != null)
					return number.doubleValue();
			} catch (ParseException e) {
				if (printStackTrace)
					e.printStackTrace();
			}
		}

		stringValue = stringValue.trim();
		stringValue = stringValue.replace(",", ".");
		try {
			return Double.parseDouble(stringValue);
		} catch (Exception e) {
			if (stringValue.contains("/")) {
				try {
					double d1 = Double.parseDouble(stringValue.substring(0, stringValue.indexOf("/")));
					double d2 = Double
							.parseDouble(stringValue.substring(stringValue.indexOf("/") + 1, stringValue.length()));
					return d1 / d2;
				} catch (Exception e2) {
					return Double.NaN;
				}
			}
			return Double.NaN;
		}
	}

	protected boolean isDotForThousandsNotation(String string) {
		if (string == null)
			return false;

		int dot = string.indexOf(".");
		int comma = string.indexOf(",");

		if (dot > -1 && comma > -1)
			if (dot < comma)
				return true;

		return false;
	}

	@Override
	public Class<Double> getOutputClassType() {
		return Double.class;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}

	public boolean isDotMeansThousands() {
		return dotMeansThousands;
	}

	public void setDotMeansThousands(boolean dotMeansThousands) {
		this.dotMeansThousands = dotMeansThousands;
	}

	public boolean isPrintStackTrace() {
		return printStackTrace;
	}

	public void setPrintStackTrace(boolean printStackTrace) {
		this.printStackTrace = printStackTrace;
	}

}
