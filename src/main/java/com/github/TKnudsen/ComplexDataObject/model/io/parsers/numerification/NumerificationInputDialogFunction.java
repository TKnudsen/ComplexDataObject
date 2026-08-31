package com.github.TKnudsen.ComplexDataObject.model.io.parsers.numerification;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IObjectParser;
import com.github.TKnudsen.ComplexDataObject.model.tools.Threads;

/**
 * <p>
 * Abstract IObjectParser implementation that resolves a numeric value for an
 * arbitrary (typically categorical) input object by looking it up in an
 * internal cache or, if absent, prompting the user via a Swing input dialog
 * with a configurable timeout. Resolved values are cached in a lookup map so
 * each distinct object is only asked for once.
 * </p>
 */
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
		DialogRunnable dialogRunnable = new DialogRunnable(t);

		if (SwingUtilities.isEventDispatchThread()) {
			// Already on the EDT (e.g. triggered from an interactive Swing action) --
			// showInputDialog() is modal and pumps its own nested event loop, so it is
			// safe to call directly and synchronously here. Routing this through the
			// background-thread-plus-poll path below would deadlock: the poll loop
			// would block the EDT via Thread.sleep (which does not pump events), while
			// SwingUtilities.invokeAndWait would then wait forever for that same
			// blocked EDT to process it.
			dialogRunnable.showDialog();
			return dialogRunnable.getValue();
		}

		long start = System.currentTimeMillis();

		Thread thread = new Thread(dialogRunnable);
		thread.start();

		while (!dialogRunnable.isFinished() && System.currentTimeMillis() - start < maxWaitTimeUntilDialogKill) {
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
		// volatile: written on the EDT (via showDialog), read by the polling caller
		// thread in retrieveNumber() -- without volatile there is no guaranteed
		// happens-before edge, so the poll loop could spin past the timeout without
		// ever observing completion.
		private volatile boolean finished = false;

		DialogRunnable(Object t) {
			this.t = t;
		}

		@Override
		public void run() {
			// Reached only on the background-thread path (see retrieveNumber): this
			// thread is never the EDT, so building/showing the JFrame/JOptionPane here
			// directly (the original bug) produced a dialog that was realized but never
			// properly painted or made responsive to input -- Swing components must only
			// be created and touched on the EDT. Marshal onto it instead.
			try {
				SwingUtilities.invokeAndWait(this::showDialog);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				finished = true;
			} catch (InvocationTargetException e) {
				e.getCause().printStackTrace();
				finished = true;
			}
		}

		/** Must only run on the EDT -- called directly if already there, or marshaled via invokeAndWait otherwise. */
		private void showDialog() {
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

	public Map<Object, T> getNumerificationLookup() {
		return Collections.unmodifiableMap(numerificationLookup);
	}
}
