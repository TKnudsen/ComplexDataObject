package com.github.TKnudsen.ComplexDataObject.model.preprocessing;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.TKnudsen.ComplexDataObject.data.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.ComplexDataObject;

/**
 * Used to delete a given list of (extreme) values from each {@link ComplexDataObject} 
 * in the {@link ComplexDataContainer}.
 * 
 * @author Robert Heimbach
 *
 * @param <T>
 */
public class RemovingValues<T extends Object> implements IPreprocessingRoutine {

	private String attribute;
	private Set<T> valuesToRemove;

	public RemovingValues(String attribute, List<T> valuesToRemove){
		this.attribute = attribute;
		this.valuesToRemove = new HashSet<T>(valuesToRemove);
	}
	
	@Override
	public void process(ComplexDataContainer container) {
		
		for(ComplexDataObject complexDataObject : container){
			
			Object value = complexDataObject.get(attribute);
		
			if(value != null && valuesToRemove.contains(value)){
					
					complexDataObject.remove(attribute);
			}
		}
	}
}
