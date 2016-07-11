package com.github.TKnudsen.ComplexDataObject.data.features.mixedData;

import java.util.Map;

import com.github.TKnudsen.ComplexDataObject.data.DataContainer;
import com.github.TKnudsen.ComplexDataObject.data.DataSchema;

/**
 * <p>
 * Title: MixedDataContainer
 * </p>
 * 
 * <p>
 * Description: MixedDataContainer stores and manages MixedDataVectors. A
 * DataSchema contains all keys of the MixedDataFeatures.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2016
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.03
 */
public class MixedDataContainer extends DataContainer<MixedDataVector> {

	public MixedDataContainer(DataSchema dataSchema) {
		super(dataSchema);
	}

	public MixedDataContainer(Map<Long, MixedDataVector> objectsMap) {
		super(objectsMap);
	}

	public MixedDataContainer(Iterable<MixedDataVector> objects) {
		super(objects);
	}

	public Boolean isNumeric(String attribute) {
		if (Number.class.isAssignableFrom(dataSchema.getType(attribute)))
			return true;
		return false;
	}

	public Boolean isBoolean(String attribute) {
		if (Boolean.class.isAssignableFrom(dataSchema.getType(attribute)))
			return true;
		return false;
	}
}
