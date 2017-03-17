package com.github.TKnudsen.ComplexDataObject.view;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.github.TKnudsen.ComplexDataObject.data.interfaces.IDObject;

/**
 * <p>
 * Title: VisualMapping
 * </p>
 * 
 * <p>
 * Description: provides the basic functionality for mapping abstract data to
 * the visual space.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2015-2017
 * </p>
 * 
 * @author Juergen Bernard
 * @TODO: define visual variable data structure
 */
public abstract class VisualMappingFunction<T extends IDObject, M extends Object> implements Function<T, M> {

	protected Map<T, M> mappingLookup = new HashMap<T, M>();

	protected abstract M calculateMapping(T t);

	@Override
	public M apply(T t) {
		if (mappingLookup.get(t) == null)
			mappingLookup.put(t, calculateMapping(t));
		return mappingLookup.get(t);
	}
}
