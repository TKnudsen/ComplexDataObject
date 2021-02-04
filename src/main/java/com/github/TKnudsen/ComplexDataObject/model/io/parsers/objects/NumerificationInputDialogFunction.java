package com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import com.github.TKnudsen.ComplexDataObject.model.tools.Threads;

public class NumerificationInputDialogFunction implements IObjectParser<Double> {

	private Map<Object, Double> numerificationLookup = new HashMap<Object, Double>();
	private DoubleParser doubleParser;

	@SuppressWarnings("unused")
	private NumerificationInputDialogFunction() {
		this(false);
	}

	public NumerificationInputDialogFunction(boolean dotMeansThousands) {
		doubleParser = new DoubleParser(dotMeansThousands);
	}

	@Override
	public Double apply(Object t) {
		if (t == null)
			return Double.NaN;

		if (numerificationLookup.get(t) == null)
			numerificationLookup.put(t, retrieveNumber(t));

		return numerificationLookup.get(t);
	}

	private Double retrieveNumber(Object t) {
		long start = System.currentTimeMillis();

		DialogRunnable dialogRunnable = new DialogRunnable(t);
		Thread thread = new Thread(dialogRunnable);
		thread.start();

		while (!dialogRunnable.isFinished() && System.currentTimeMillis() - start < 10000) {
			Threads.sleep(250);
//			System.out.println("NumerificationInputDialogFunction.retrieveNumber: waiting for user input");
		}

		double d = dialogRunnable.getValue();
		thread.interrupt();
		return d;
	}

	public Double addNumerification(Object object, Double value) {
		return numerificationLookup.put(object, value);
	}

	@Override
	public Class<Double> getOutputClassType() {
		return Double.class;
	}

	private class DialogRunnable implements Runnable {
		private final Object t;
		private double d = Double.NaN;
		private boolean finished = false;

		DialogRunnable(Object t) {
			this.t = t;
		}

		@Override
		public void run() {
			String inputValue = JOptionPane.showInputDialog("User input required for object [" + t
					+ "]. Please input a numerical value; 0,5 for zero point five.");

			if (inputValue != null) {
				inputValue = inputValue.replace(".", ",");
				d = doubleParser.apply(inputValue);
			}

			finished = true;
		}

		public double getValue() {
			return d;
		}

		public boolean isFinished() {
			return finished;
		}

	}

}
