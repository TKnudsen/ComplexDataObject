package com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects;

/**
 * <p>
 * Title: BooleanParser
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
 * @version 1.02
 */
public class BooleanParser implements IObjectParser<Boolean> {

	@Override
	public Boolean apply(Object object) {
		if (object instanceof Boolean)
			return new Boolean((boolean) object);

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
			System.out.println("Object " + object + " could not be parsed to Boolean");
			return null;
		}
	}

	@Override
	public Class<Boolean> getOutputClassType() {
		return Boolean.class;
	}
}
