package com.github.TKnudsen.ComplexDataObject.model.tools;

/**
 * Thrown by {@link NumericRangeTools#computeFiniteRangeStrict} when a
 * computed numeric range is degenerate (min == max) -- i.e. every finite
 * value in the data was identical, so there is no variance to build a
 * meaningful visual encoding (axis, position mapping, color scale, ...)
 * from.
 *
 * <p>
 * A dedicated type rather than a bare IllegalStateException so calling code
 * that wants to handle this specific, well-understood condition (e.g. skip
 * just one chart out of many in a batch, log which one, and continue with
 * the rest) can catch it deliberately, instead of either not catching
 * anything (one degenerate dataset aborts the whole batch) or catching
 * Exception broadly (which also silently swallows unrelated bugs).
 * </p>
 */
public class DegenerateRangeException extends IllegalStateException {

	private static final long serialVersionUID = 1L;

	public DegenerateRangeException(String message) {
		super(message);
	}

	public DegenerateRangeException(String message, Throwable cause) {
		super(message, cause);
	}
}
