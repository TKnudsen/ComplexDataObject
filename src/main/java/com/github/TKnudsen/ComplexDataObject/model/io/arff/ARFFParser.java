package com.github.TKnudsen.ComplexDataObject.model.io.arff;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.exception.NullArgumentException;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.ComplexDataObjectParser;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.ParserTools;

import weka.core.Instance;
import weka.core.Instances;

/**
 * Parses ComplexDataObjects from an ARFF file. Note: this parser is not part of
 * the persistence layer. In fact, it gathers new ComplexDataObjects from a
 * given file.
 * 
 * a flag can be set to assign IDs of ComplexDataObjects incrementally starting
 * with 0, reflecting the lines of instances within the arff file.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2015-2020
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.02
 */
public class ARFFParser implements ComplexDataObjectParser {

	private String missingValueIndicator = "?";

	private final boolean assignIncrementalIDs;

	public ARFFParser() {
		this(false);
	}

	public ARFFParser(boolean assignIncrementalIDs) {
		this.assignIncrementalIDs = assignIncrementalIDs;
	}

	@Override
	public List<ComplexDataObject> parse(String filename) throws IOException {
		Instances instances = ARFFInstancesIO.loadARFF(filename);
		if (instances == null)
			return null;

		List<ComplexDataObject> data = new ArrayList<>();

		// Step1: create metaMapping
		Map<Integer, Entry<String, Class<?>>> metaMapping = WekaTools.getAttributeSchema(instances);

		// Step2: create ComplexDataObjects
		for (int zeile = 0; zeile < instances.numInstances(); zeile++) {

			Instance instance = instances.instance(zeile);
			// new: added zeile as the ID for the ComplexDataObject.
			// This replaces the creation of a random ID
			ComplexDataObject complexDataObject = (assignIncrementalIDs) ? new ComplexDataObject(zeile)
					: new ComplexDataObject();

			// parse columns
			for (Integer spalte = 0; spalte < instances.numAttributes(); spalte++) {

				Entry<String, ?> entry = null;
				try {
					entry = WekaTools.assignEntry(metaMapping, instance, spalte, missingValueIndicator);
				} catch (Exception e) {

				}

				if (entry != null) {
					if (entry.getValue() != null && entry.getValue() instanceof String) {
						Date date = ParserTools.parseDate((String) entry.getValue());
						if (date != null)
							complexDataObject.add(entry.getKey(), date);
						else
							complexDataObject.add(entry.getKey(), entry.getValue());
					} else
						complexDataObject.add(entry.getKey(), entry.getValue());
				} else
					throw new NullArgumentException();

				if (complexDataObject.getAttribute("Name") != null) {
					complexDataObject.setName(complexDataObject.getAttribute("Name").toString());
//					complexDataObject.removeAttribute("Name");
				}

				if (complexDataObject.getAttribute("Description") != null) {
					complexDataObject.setDescription(complexDataObject.getAttribute("Description").toString());
//					complexDataObject.removeAttribute("Description");
				}
			}
			data.add(complexDataObject);
		}
		return data;
	}

	@Deprecated // use ARFFTools
	public static Instances parseARFF(String arffFile) {
		return ARFFInstancesIO.loadARFF(arffFile);
	}

	public String getMissingValueIndicator() {
		return missingValueIndicator;
	}

	@Override
	public String getName() {
		return "ARFF Parser";
	}

	@Override
	public String getDescription() {
		return getName();
	}
}
