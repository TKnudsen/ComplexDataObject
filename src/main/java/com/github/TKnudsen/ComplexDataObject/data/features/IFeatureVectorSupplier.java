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
 */
public interface IFeatureVectorSupplier<FV extends AbstractFeatureVector<?, ?>> extends Supplier<List<FV>> {

}
