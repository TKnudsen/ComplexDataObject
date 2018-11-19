package com.github.TKnudsen.ComplexDataObject.model.processors;

import com.github.TKnudsen.ComplexDataObject.data.interfaces.IDObject;

import java.util.List;

public interface IParameterSupport<O extends IDObject> {

	public List<IDataProcessor<O>> getAlternativeParameterizations(int count);
}
