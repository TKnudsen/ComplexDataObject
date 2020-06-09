package com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects;

/**
 * <p>
 * Copyright: Copyright (c) 2016-2020
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.03
 */
public class BooleanParser implements IObjectParser<Boolean> {

	private boolean printWarnings = false;

	public BooleanParser() {
		this(false);
	}

	public BooleanParser(boolean printWarnings) {
		this.printWarnings = printWarnings;
	}

	@Override
	public Boolean apply(Object object) {
		if (object instanceof Boolean)
			return new Boolean((boolean) object);

		if (object instanceof Integer)
			if ((Integer) object == 0)
				return Boolean.FALSE;
			else if ((Integer) object == 1)
				return Boolean.TRUE;

		final String stringRepresentation = String.valueOf(object).toLowerCase();
		switch (stringRepresentation) {
		case "false":
		case "FALSE":
		case "nein":
		case "Nein":
		case "0":
		case "0.0":
		case "n":
		case "no":
		case "No":
		case "NO":
			return Boolean.FALSE;
		case "true":
		case "TRUE":
		case "j":
		case "ja":
		case "Ja":
		case "1":
		case "1.0":
		case "y":
		case "yes":
		case "Yes":
		case "YES":
			return Boolean.TRUE;
		default:
			if (printWarnings)
				System.out.println("Object " + object + " could not be parsed to Boolean");
			return null;
		}
	}

	@Override
	public Class<Boolean> getOutputClassType() {
		return Boolean.class;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}

	public boolean isPrintWarnings() {
		return printWarnings;
	}

	public void setPrintWarnings(boolean printWarnings) {
		this.printWarnings = printWarnings;
	}
}
