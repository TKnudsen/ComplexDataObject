package com.github.TKnudsen.ComplexDataObject.model.preprocessing.utility;

import java.util.Set;

import com.github.TKnudsen.ComplexDataObject.data.ComplexDataContainer;

public interface IUniqueValuesIdentifier {
	
	public Set<Object> getUniqueValues();
	
	public String getAttribute();
	
	public void setAttribute(String newAttribute);
	
	public Boolean hasAttribute();
	
	public ComplexDataContainer getDataContainer();
	
	public void setDataContainer(ComplexDataContainer newContainer);
	
	public Boolean hasDataContainer();
}
