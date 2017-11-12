package com.github.TKnudsen.ComplexDataObject.data.features;

import java.util.List;

/**
 * <p>
 * Title: IFeatureVectorSupplier
 * </p>
 * 
 * <p>
 * Description: use of a FV source without maintainance effort. It's the job of
 * the supplier to refresh the FVs when needed.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2017
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */
public class FeatureVectorSupplier<FV extends AbstractFeatureVector<?, ?>> implements IFeatureVectorSupplier<FV> {

	private List<FV> featureVectors;

	public FeatureVectorSupplier(List<FV> featureVectors) {
		this.featureVectors = featureVectors;
	}

	@Override
	public List<FV> get() {
		return featureVectors;
	}

}
