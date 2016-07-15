package com.github.TKnudsen.ComplexDataObject.model.preprocessing.utility;

import java.util.List;

public interface IItemSplitter {
	
	public List<? extends Object> split(Object toSplit);
}
