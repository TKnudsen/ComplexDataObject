package com.github.TKnudsen.ComplexDataObject.model.processors.utility;

import java.util.List;

/**
 * <p>
 * Splits an arbitrary object into a list of sub-items.
 * </p>
 */
public interface IItemSplitter {
	
	public List<?> split(Object toSplit);
}
