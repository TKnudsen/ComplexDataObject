package com.github.TKnudsen.ComplexDataObject.model.io.parsers.numerification;

import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.DoubleParser;

public class DoubleNumerificationInputDialogFunction extends NumerificationInputDialogFunction<Double> {

	private final DoubleParser doubleParser;

	/**
	 * 
	 * @param dotMeansThousands          specific for parsing doubles, depending on
	 *                                   where on Earth the application context is
	 * @param maxWaitTimeUntilDialogKill in milliseconds
	 */
	public DoubleNumerificationInputDialogFunction(boolean dotMeansThousands, long maxWaitTimeUntilDialogKill) {
		super(maxWaitTimeUntilDialogKill);

		doubleParser = new DoubleParser(dotMeansThousands);
	}

	@Override
	protected Double missingValueIdentifier() {
		return Double.NaN;
	}

	@Override
	public Class<Double> getOutputClassType() {
		return Double.class;
	}

	@Override
	protected Double parseValue(Object o) {
		return doubleParser.apply(o);
	}
}
