package com.github.TKnudsen.ComplexDataObject.model.io.parsers;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import com.github.TKnudsen.ComplexDataObject.data.keyValueObject.KeyValueObject;

/**
 * <p>
 * Interface for parsers to parse files towards KeyValueObjects.
 * </p>
 *
 * @version 1.02
 * @since 2015
 */
public interface IKeyValueObjectParser<KV extends KeyValueObject> {

	/**
	 * Parses a data file and returns an List of KeyValueObjects.
	 * 
	 * @param filename
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	public abstract List<KV> parse(String filename) throws IOException, ParseException;
}
