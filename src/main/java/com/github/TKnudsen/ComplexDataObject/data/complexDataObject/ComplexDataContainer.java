package com.github.TKnudsen.ComplexDataObject.data.complexDataObject;

import java.util.Map;

import com.github.TKnudsen.ComplexDataObject.data.DataContainer;
import com.github.TKnudsen.ComplexDataObject.data.DataSchema;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.events.IComplexDataObjectListener;

/**
 * <p>
 * Title: ComplexDataContainer
 * </p>
 * 
 * <p>
 * Description: ComplexDataContainer stores and manages ComplexDataObjects. A
 * DataSchema contains all keys of the ComplexDataObjects.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2015-2019
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.03
 */
public class ComplexDataContainer extends DataContainer<ComplexDataObject> implements IComplexDataObjectListener {

	public ComplexDataContainer(DataSchema dataSchema) {
		super(dataSchema);
	}

	public ComplexDataContainer(Map<Long, ComplexDataObject> objectsMap) {
		super(objectsMap);

		for (ComplexDataObject cdo : objectsMap.values())
			cdo.addComplexDataObjectListener(this);
	}

	public ComplexDataContainer(Iterable<ComplexDataObject> objects) {
		super(objects);

		for (ComplexDataObject cdo : objects)
			cdo.addComplexDataObjectListener(this);
	}

	/**
	 * adds a new object. updates data schema and attribute values.
	 * 
	 * @param object
	 * @return
	 */
	public boolean add(ComplexDataObject object) {
		Boolean b = super.add(object);

		object.addComplexDataObjectListener(this);

		return b;
	}

	@Override
	public void attributeValueChanged(ComplexDataObject cdo, String attribute) {
		if (attributeValues.get(attribute) != null)
			this.attributeValues.get(attribute).put(cdo.getID(), cdo.getAttribute(attribute));
		else
			extendDataSchema(cdo);
	}

	@Override
	public void attributeRemoved(ComplexDataObject cdo, String attribute) {
		if (attributeValues.get(attribute) != null)
			if (this.attributeValues.get(attribute).get(cdo.getID()) != null)
				this.attributeValues.get(attribute).remove(cdo.getID());
	}

}
