package com.github.TKnudsen.ComplexDataObject.model.processors.utility;

import java.util.Set;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;

/**
 * <p>
 * Identifies and retrieves the unique values of a given attribute within a
 * {@link ComplexDataContainer}.
 * </p>
 */
public interface IUniqueValuesIdentifier {
	
	public Set<Object> getUniqueValues();
	
	public String getAttribute();
	
	public void setAttribute(String newAttribute);
	
	public Boolean hasAttribute();
	
	public ComplexDataContainer getDataContainer();
	
	public void setDataContainer(ComplexDataContainer newContainer);
	
	public Boolean hasDataContainer();
}
