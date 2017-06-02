package com.github.TKnudsen.ComplexDataObject.model.tools;

import java.util.List;

/**
 * <p>
 * Title: DataConversion
 * </p>
 *
 * <p>
 * Description: Provides little helpers for the conversion of general data
 * structures
 * </p>
 *
 * <p>
 * Copyright: Copyright (c) 2017
 * </p>
 *
 * @author Juergen Bernard
 * @version 1.01
 */
public class DataConversion {

	public static double[] toPrimitives(List<Double> values) {
		if (values == null)
			return null;

		double[] ret = new double[values.size()];

		for (int i = 0; i < values.size(); i++)
			ret[i] = values.get(i).doubleValue();

		return ret;
	}
}
