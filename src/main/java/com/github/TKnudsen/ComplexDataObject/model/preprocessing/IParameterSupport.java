package com.github.TKnudsen.ComplexDataObject.model.preprocessing;

import java.util.List;

import com.github.TKnudsen.ComplexDataObject.data.interfaces.IDObject;

public interface IParameterSupport<O extends IDObject> {

	public List<IDataProcessor<O>> getAlternativeParameterizations(int count);
}
