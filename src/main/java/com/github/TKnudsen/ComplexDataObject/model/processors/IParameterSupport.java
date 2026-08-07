package com.github.TKnudsen.ComplexDataObject.model.processors;

import java.util.List;

import com.github.TKnudsen.ComplexDataObject.data.interfaces.IDObject;

/**
 * <p>
 * Implemented by data processors that can propose alternative
 * parameterizations of themselves, e.g. for parameter sweeps or sensitivity
 * analyses over {@link IDObject} data.
 * </p>
 */
public interface IParameterSupport<O extends IDObject> {

	public List<IDataProcessor<O>> getAlternativeParameterizations(int count);
}
