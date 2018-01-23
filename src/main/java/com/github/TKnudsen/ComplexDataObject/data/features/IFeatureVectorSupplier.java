package com.github.TKnudsen.ComplexDataObject.data.features;

import java.util.List;
import java.util.function.Supplier;

/**
 * <p>
 * Title: IFeatureVectorSupplier
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2017
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 * 
 * TODO_GENERICS No longer specific for feature vectors, may become obsolete or a "ListSupplier"
 */
public interface IFeatureVectorSupplier<FV> extends Supplier<List<FV>> {

}
