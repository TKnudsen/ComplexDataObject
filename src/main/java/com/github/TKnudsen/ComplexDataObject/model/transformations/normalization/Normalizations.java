package com.github.TKnudsen.ComplexDataObject.model.transformations.normalization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import com.github.TKnudsen.ComplexDataObject.model.tools.DataConversion;

/**
 * <p>
 * Little helper so that locally there is no need to iterate through a
 * distribution of values to normalize.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2022
 * </p>
 * 
 * @author Juergen Bernard
 *
 */
public class Normalizations {

	public static Collection<? extends Number> normalize(Collection<? extends Number> values,
			Function<Number, Number> function) {

		List<Number> output = new ArrayList<>();
		for (Number n : values)
			output.add(function.apply(n));

		return output;
	}

}
