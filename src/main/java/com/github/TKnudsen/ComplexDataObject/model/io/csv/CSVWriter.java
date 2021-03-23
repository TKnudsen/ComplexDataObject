package com.github.TKnudsen.ComplexDataObject.model.io.csv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;

public class CSVWriter {

	/**
	 * 
	 * @param container
	 * @param fileName
	 * @param separator separator token. use ',' as an example for comma, other
	 *                  common separators are tab, semicolon, space, etc. Be careful
	 *                  with attributes such as "description" which may also contain
	 *                  separator tokens in free text(!)
	 * @throws IOException
	 */
	public static void writeToFile(ComplexDataContainer container, String fileName, char separator) throws IOException {
		if (container == null)
			return;

		List<Iterable<? extends Object>> linesOfObjects = new ArrayList<>();

		// header
		Collection<String> attributeNames = container.getAttributeNames();
		linesOfObjects.add(attributeNames);

		// one line per object
		for (ComplexDataObject cdo : container) {
			List<? super Object> objects = new ArrayList<>();
			for (String attribute : attributeNames)
				if (cdo.getAttribute(attribute) != null)
					if (cdo.getAttribute(attribute) instanceof String)
						objects.add(cdo.getAttribute(attribute).toString().replace(String.valueOf(separator), "_"));
					else
						objects.add(cdo.getAttribute(attribute));
				else
					objects.add("");
			linesOfObjects.add(objects);
		}

		CSVWriterTools.writeToCSV(linesOfObjects, fileName, separator);
	}

	/**
	 * 
	 * @param data
	 * @param fileName
	 * @param separator separator token. tab, comma, space, etc. Be careful with
	 *                  attributes such as "description" which may also contain
	 *                  separator tokens in free text(!)
	 * @throws IOException
	 */
	public static void writeToFile(List<ComplexDataObject> data, String fileName, char separator) throws IOException {
		writeToFile(new ComplexDataContainer(data), fileName, separator);
	}
}
