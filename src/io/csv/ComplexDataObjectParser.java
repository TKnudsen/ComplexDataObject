package io.csv;

import java.io.IOException;
import java.util.List;

import data.ComplexDataObject;

public interface ComplexDataObjectParser {

	/**
	 * Parses a data file and returns an List of ComplexDataObjects. Every line
	 * will be a ComplexDataObject.
	 * 
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	public abstract List<ComplexDataObject> parse(String filename) throws IOException;
}
