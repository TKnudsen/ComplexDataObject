package com.github.TKnudsen.ComplexDataObject.model.preprocessing.complexDataObject;

import com.github.TKnudsen.ComplexDataObject.data.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.model.preprocessing.IDataProcessing;

/**
 * <p>
 * Title:
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

public interface IComplexDataObjectProcessor extends IDataProcessing<ComplexDataObject> {

	public void process(ComplexDataContainer container);
}
