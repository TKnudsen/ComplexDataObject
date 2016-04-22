package com.github.TKnudsen.ComplexDataObject.preprocessing.utility;

import java.util.List;

public interface IItemSplitter {
	
	public List<? extends Object> split(Object toSplit);
}
