package com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects;

public class BooleanParser implements IObjectParser<Boolean> {

	@Override
	public Boolean apply(Object object) {
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
			return Boolean.TRUE;
		default:
			System.out.println("Object" + object + " could not be parsed to Boolean");
			return null;
		}
	}
}
