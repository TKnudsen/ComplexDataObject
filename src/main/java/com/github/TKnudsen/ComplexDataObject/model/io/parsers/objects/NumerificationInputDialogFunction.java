package com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.github.TKnudsen.ComplexDataObject.model.tools.Threads;

/**
 * @deprecated first version of a NumerificationFunction that was bond to Double
 *             output
 * @param <T>
 */
public class NumerificationInputDialogFunction implements IObjectParser<Double>, INumerificationInput<Object> {

	private Map<Object, Double> numerificationLookup = new HashMap<Object, Double>();
	private DoubleParser doubleParser;

	/**
	 * allows to proceed an automatic process of no user input arrives after some
	 * time. Default: 15sec.
	 */
	private final long maxWaitTimeUntilDialogKill;

	@SuppressWarnings("unused")
	private NumerificationInputDialogFunction() {
		this(false, 15000);
	}

	/**
	 * 
	 * @param dotMeansThousands
	 * @param maxWaitTimeUntilDialogKill in milliseconds
	 */
	public NumerificationInputDialogFunction(boolean dotMeansThousands, long maxWaitTimeUntilDialogKill) {
		doubleParser = new DoubleParser(dotMeansThousands);

		this.maxWaitTimeUntilDialogKill = maxWaitTimeUntilDialogKill;
	}

	@Override
	public Double apply(Object t) {
		if (t == null)
			return Double.NaN;

		if (numerificationLookup.get(t) == null) {
			System.out.println("NumerificationInputDialogFunction: number needed for category " + t);
			numerificationLookup.put(t, retrieveNumber(t));
		}

		return numerificationLookup.get(t);
	}

	private Double retrieveNumber(Object t) {
		long start = System.currentTimeMillis();

		DialogRunnable dialogRunnable = new DialogRunnable(t);
		Thread thread = new Thread(dialogRunnable);
		thread.start();

		while (!dialogRunnable.isFinished() && System.currentTimeMillis() - start < maxWaitTimeUntilDialogKill) {
			// dialog does not work because the thread does not finish through sleep
			Threads.sleep(250);
		}

		double d = dialogRunnable.getValue();
		thread.interrupt();
		return d;
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
			JFrame frame = new JFrame();
			frame.setAlwaysOnTop(true);

			String inputValue = JOptionPane.showInputDialog(frame, "User input required for object [" + t
					+ "]. Please input a numerical value; 0,5 for zero point five.");

			if (inputValue != null) {
				inputValue = inputValue.replace(".", ",");
				d = doubleParser.apply(inputValue);
			}

			finished = true;

//	SwingUtilities.invokeLater(new Runnable() {
//		@Override
//		public void run() {
//			String inputValue = JOptionPane.showInputDialog(frame, "User input required for object [" + t
//					+ "]. Please input a numerical value; 0,5 for zero point five.");
//			if (inputValue != null) {
//				inputValue = inputValue.replace(".", ",");
//				d = doubleParser.apply(inputValue);
//			}
//			finished = true;
//		}
//	});
		}

		public double getValue() {
			return d;
		}

		public boolean isFinished() {
			return finished;
		}

	}

	@Override
	public Double addNumerification(Object object, double value) {
		return numerificationLookup.put(object, value);
	}

}