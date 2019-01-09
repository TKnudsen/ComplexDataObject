package com.github.TKnudsen.ComplexDataObject.data.complexDataObject.events;

import java.util.EventListener;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;

public interface IComplexDataObjectListener extends EventListener {

	void attributeValueChanged(ComplexDataObject cdo, String attribute);

	void attributeRemoved(ComplexDataObject cdo, String attribute);

}
