package com.github.TKnudsen.ComplexDataObject.model.preprocessing;

import com.github.TKnudsen.ComplexDataObject.data.ComplexDataContainer;

/**
 * <p>
 * Title: IPreprocessingRoutine
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
 * @version 1.03
 */
public interface IPreprocessingRoutine {

	public void process(ComplexDataContainer container);

	public PreprocessingCategory getPreprocessingCategory();
}
