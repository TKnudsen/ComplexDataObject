package com.github.TKnudsen.ComplexDataObject.model.io.parsers;

import java.io.IOException;
import java.util.List;

import com.github.TKnudsen.ComplexDataObject.data.ComplexDataObject;

/**
 * <p>
 * Title: ComplexDataObjectParser
 * </p>
 * 
 * <p>
 * Description: Interface for parsers to parse files for ComplexDataObjects.
 * Every line of the file is meant to include a single ComplexDataObject.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2015
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.0
 */
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
