package com.github.TKnudsen.ComplexDataObject.data.complexDataObject.events;

import java.util.EventListener;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;

/**
 * <p>
 * Listener interface for observing changes to a ComplexDataObject, notified
 * whenever an attribute value is changed or an attribute is removed.
 * </p>
 */
public interface IComplexDataObjectListener extends EventListener {

	void attributeValueChanged(ComplexDataObject cdo, String attribute);

	void attributeRemoved(ComplexDataObject cdo, String attribute);

}
