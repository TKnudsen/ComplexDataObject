package com.github.TKnudsen.ComplexDataObject.model.io.parsers.numerification;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IObjectParser;
import com.github.TKnudsen.ComplexDataObject.model.tools.Threads;

public abstract class NumerificationInputDialogFunction<T extends Number>
		implements IObjectParser<T>, INumerificationInput<Object, T> {

	protected Map<Object, T> numerificationLookup = new HashMap<Object, T>();

	/**
	 * allows to proceed an automatic process of no user input arrives after some
	 * time. Default: 15sec.
	 */
	private final long maxWaitTimeUntilDialogKill;

	@SuppressWarnings("unused")
	private NumerificationInputDialogFunction() {
		this(15000);
	}

	/**
	 * 
	 * @param dotMeansThousands
	 * @param maxWaitTimeUntilDialogKill in milliseconds
	 */
	public NumerificationInputDialogFunction(long maxWaitTimeUntilDialogKill) {
		this.maxWaitTimeUntilDialogKill = maxWaitTimeUntilDialogKill;
	}

	protected abstract T missingValueIdentifier();

	@Override
	public T apply(Object t) {
		if (t == null)
			return missingValueIdentifier();

		if (numerificationLookup.get(t) == null) {
			System.out.println("NumerificationInputDialogFunction: number needed for category " + t);
			numerificationLookup.put(t, retrieveNumber(t));
		}

		return numerificationLookup.get(t);
	}

	private T retrieveNumber(Object t) {
		long start = System.currentTimeMillis();

		DialogRunnable dialogRunnable = new DialogRunnable(t);
		Thread thread = new Thread(dialogRunnable);
		thread.start();

		while (!dialogRunnable.isFinished() && System.currentTimeMillis() - start < maxWaitTimeUntilDialogKill) {
			// dialog does not work because the thread does not finish through sleep
			Threads.sleep(250);
		}

		T d = dialogRunnable.getValue();
		thread.interrupt();
		return d;
	}

	protected abstract T parseValue(Object o);

	private class DialogRunnable implements Runnable {
		private final Object t;
		private T n;
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
				n = parseValue(inputValue);
			}

			finished = true;

		}

		public T getValue() {
			return n;
		}

		public boolean isFinished() {
			return finished;
		}
	}

	@Override
	public T addNumerification(Object object, T value) {
		return numerificationLookup.put(object, value);
	}

}
