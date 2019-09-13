package com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

public class NumerificationInputDialogFunction implements IObjectParser<Double> {

	private Map<Object, Double> numerificationLookup = new HashMap<Object, Double>();
	private DoubleParser doubleParser = new DoubleParser();

	@Override
	public Double apply(Object t) {
		if (t == null)
			return Double.NaN;

		if (numerificationLookup.get(t) == null)
			numerificationLookup.put(t, retrieveNumber(t));

		return numerificationLookup.get(t);
	}

	private Double retrieveNumber(Object t) {
		String inputValue = JOptionPane.showInputDialog(
				"User input required for object [" + t + "]. Please input a numerical value; 0,5 for zero dot five.");

		inputValue = inputValue.replace(".", ",");

		return doubleParser.apply(inputValue);
	}

	@Override
	public Class<Double> getOutputClassType() {
		return Double.class;
	}

}
