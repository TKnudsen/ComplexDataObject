package com.github.TKnudsen.ComplexDataObject.data.complexDataObject;

import java.util.Arrays;
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
 * Copyright: Copyright (c) 2015-2024
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.04
 */
public class ComplexDataContainer extends DataContainer<ComplexDataObject> implements IComplexDataObjectListener {

	public ComplexDataContainer(DataSchema dataSchema) {
		super(dataSchema);
	}

	/**
	 * @deprecated Use the Iterable<ComplexDataObject> constructor instead. Storage
	 *             in the data container now works with the objects directly, IDs
	 *             have been taken out of business.
	 * @param objectsMap
	 */
	public ComplexDataContainer(Map<Long, ComplexDataObject> objectsMap) {
		super(objectsMap);

		for (ComplexDataObject cdo : objectsMap.values())
			cdo.addComplexDataObjectListener(this);
	}

	public ComplexDataContainer(ComplexDataObject object) {
		this(Arrays.asList(object), ID_ATTRIBUTE);
	}

	public ComplexDataContainer(ComplexDataObject object, String primaryKeyAttribute) {
		this(Arrays.asList(object), primaryKeyAttribute);
	}

	public ComplexDataContainer(Iterable<ComplexDataObject> objects) {
		this(objects, DataContainer.ID_ATTRIBUTE);
	}

	/**
	 * 
	 * @param objects
	 * @param primaryKeyAttribute A primary key attribute that allows managing
	 *                            objects having a primary key. The ID attribute is
	 *                            the historic default, but it can also be any other
	 *                            attribute defined here, such as the "ISIN" for
	 *                            stocks. Attention: do not use attributes with
	 *                            non-categorical values, such as continuous
	 *                            numbers.
	 */
	public ComplexDataContainer(Iterable<ComplexDataObject> objects, String primaryKeyAttribute) {
		super(objects, primaryKeyAttribute);

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
		// extend schema if attribute does not exist and meaningful(value !=null)
		if (!dataSchema.contains(attribute) && cdo.getAttribute(attribute) != null)
			extendDataSchema(cdo);

		// update attribute
		if (attributeValues.get(attribute) != null)
			this.attributeValues.get(attribute).put(cdo, cdo.getAttribute(attribute));
		// this part will not be done any more
		// attributeValues are now treated lazy
		// else
		// calculateEntities(attribute);
	}

	@Override
	public void attributeRemoved(ComplexDataObject cdo, String attribute) {
		if (attributeValues.get(attribute) != null)
			if (this.attributeValues.get(attribute).get(cdo) != null)
				this.attributeValues.get(attribute).remove(cdo);
	}

}
