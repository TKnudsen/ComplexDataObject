package com.github.TKnudsen.ComplexDataObject.model.io.parsers.numerification;

import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.DoubleParser;

public class FloatDoubleNumerificationInputDialogFunction extends NumerificationInputDialogFunction<Float> {

	private final DoubleParser doubleParser;

	private FloatDoubleNumerificationInputDialogFunction() {
		super(15000);

		this.doubleParser = new DoubleParser();
	}

	/**
	 * 
	 * @param dotMeansThousands          specific for parsing doubles, depending on
	 *                                   where on Earth the application context is
	 * @param maxWaitTimeUntilDialogKill in milliseconds
	 */
	public FloatDoubleNumerificationInputDialogFunction(boolean dotMeansThousands, long maxWaitTimeUntilDialogKill) {
		super(maxWaitTimeUntilDialogKill);

		doubleParser = new DoubleParser(dotMeansThousands);
	}

	@Override
	protected Float missingValueIdentifier() {
		return Float.NaN;
	}

	@Override
	public Class<Float> getOutputClassType() {
		return Float.class;
	}

	@Override
	protected Float parseValue(Object o) {
		Double d = doubleParser.apply(o);
		if (d == null)
			return null;
		return d.floatValue();
	}
}
