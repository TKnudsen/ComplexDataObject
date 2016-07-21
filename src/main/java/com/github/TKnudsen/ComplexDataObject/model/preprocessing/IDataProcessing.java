package com.github.TKnudsen.ComplexDataObject.model.preprocessing;

import java.util.List;

import com.github.TKnudsen.ComplexDataObject.data.interfaces.IDObject;

/**
 * <p>
 * Title: IDataProcessing
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2011-2016
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.04
 */
public interface IDataProcessing<D extends IDObject> {

	public void process(List<D> data);

	public DataProcessingCategory getPreprocessingCategory();
}
