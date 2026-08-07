package com.github.TKnudsen.ComplexDataObject.data.uncertainty;

import java.util.Map;

/**
 * <p>
 * Interface for uncertainty information for qualitative data.
 * </p>
 *
 * @version 1.0
 * @since 2015
 */
public interface IUncertaintyQualitative<T> extends IUncertainty<T> {

	public Map<T, Double> getValueDistribution();

}
