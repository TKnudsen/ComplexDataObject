package com.github.TKnudsen.ComplexDataObject.data.features.numericalData;

/**
 * <p>
 * Title: NumericalFeatureVectorTools
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2016
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.0
 */

public class NumericalFeatureVectorTools {

	/**
	 * retrieves the minimum value.
	 * 
	 * @param featureVector
	 * @return
	 */
	public static Double getMin(NumericalFeatureVector featureVector) {
		if (featureVector == null)
			return Double.NaN;

		Double d = Double.MAX_VALUE - 1;
		for (NumericalFeature feature : featureVector.getVectorRepresentation())
			d = Math.min(d, feature.doubleValue());
		return d;
	}

	/**
	 * retrieves the maximum value.
	 * 
	 * @param featureVector
	 * @return
	 */
	public static Double getMax(NumericalFeatureVector featureVector) {
		if (featureVector == null)
			return Double.NaN;

		Double d = Double.MIN_VALUE + 1;
		for (NumericalFeature feature : featureVector.getVectorRepresentation())
			d = Math.max(d, feature.doubleValue());
		return d;
	}

}
