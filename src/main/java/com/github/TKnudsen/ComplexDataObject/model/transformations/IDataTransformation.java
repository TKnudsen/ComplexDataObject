package com.github.TKnudsen.ComplexDataObject.model.transformations;

import java.util.List;

import com.github.TKnudsen.ComplexDataObject.data.interfaces.IDObject;
import com.github.TKnudsen.ComplexDataObject.model.processors.complexDataObject.DataTransformationCategory;

/**
 * <p>
 * Title: IDataTransformation
 * </p>
 * 
 * <p>
 * Description: Basic transformation of an object into another 'space'.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2017
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */
public interface IDataTransformation<I extends IDObject, O extends IDObject> {

	public List<O> transform(I input);

	public List<O> transform(List<I> inputObjects);

	public DataTransformationCategory getDataTransformationCategory();
}