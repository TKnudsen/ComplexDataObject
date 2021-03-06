package com.github.TKnudsen.ComplexDataObject.model.io.arff;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.exception.NullArgumentException;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.data.enums.AttributeType;
import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeature;
import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeatureVector;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.ParserTools;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IntegerParser;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.LongParser;
import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

/**
 * <p>
 * Title: WekaTools
 * </p>
 * 
 * <p>
 * Description: Tools class for dealing with resources stemming from the WEKA
 * library. At heart Instance and Attribute objects are handled.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2015
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.02
 */
public class WekaTools {

	private static IntegerParser intParser = new IntegerParser();
	private static LongParser longParser = new LongParser();

	public static List<ComplexDataObject> getComplexDataObjects(Instances instances) {
		List<ComplexDataObject> data = new ArrayList<>();

		// Step1: create metaMapping
		Map<Integer, Entry<String, Class<?>>> metaMapping = WekaTools.getAttributeSchema(instances);

		// Step2: create ComplexDataObjects
		for (int zeile = 0; zeile < instances.numInstances(); zeile++) {

			Instance instance = instances.instance(zeile);

			ComplexDataObject complexDataObject = new ComplexDataObject();

			// parse columns
			for (Integer spalte = 0; spalte < instances.numAttributes(); spalte++) {

				Entry<String, ?> entry = WekaTools.assignEntry(metaMapping, instance, spalte, "?");

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
			}
			data.add(complexDataObject);
		}
		return data;
	}

	public static Map<Integer, Entry<String, Class<?>>> getAttributeSchema(Instances instances) {
		Map<Integer, Entry<String, Class<?>>> attributeSchema = new HashMap<Integer, Entry<String, Class<?>>>();

		if (instances == null)
			return null;

		for (int i = 0; i < instances.numAttributes(); i++) {
			AttributeType type = getAttributeType(instances, instances.attribute(i));
			switch (type) {
			case NUMERIC:
				attributeSchema.put((Integer) i,
						new SimpleEntry<String, Class<?>>(instances.attribute(i).name(), Double.class));
				break;
			case LONG:
				attributeSchema.put((Integer) i,
						new SimpleEntry<String, Class<?>>(instances.attribute(i).name(), Long.class));
				break;
			case INTEGER:
				attributeSchema.put((Integer) i,
						new SimpleEntry<String, Class<?>>(instances.attribute(i).name(), Integer.class));
				break;
			case BINARY:
				attributeSchema.put((Integer) i,
						new SimpleEntry<String, Class<?>>(instances.attribute(i).name(), Boolean.class));
				break;
			case CATEGORICAL:
				attributeSchema.put((Integer) i,
						new SimpleEntry<String, Class<?>>(instances.attribute(i).name(), String.class));
				break;
			default:
				break;
			}
		}

		return attributeSchema;
	}

	public static Entry<String, ?> assignEntry(Map<Integer, Entry<String, Class<?>>> attributeSchema, Instance instance,
			int spalte, String missingValueIndicator) {

		boolean missingValue = false;
		try {
			missingValue = instance.isMissing(spalte);
		} catch (IncompatibleClassChangeError changeError) {
			System.out.println("WekaTools: IncompatibleClassChangeError.");
		}

		Entry<String, ?> entry = null;

		// Long
		if (attributeSchema.get(spalte).getValue().equals(Long.class))
			if (missingValue)
				entry = new SimpleEntry<String, Long>(attributeSchema.get(spalte).getKey(), null);
			else
				try {
					entry = new SimpleEntry<String, Long>(attributeSchema.get(spalte).getKey(),
							longParser.apply(instance.value(spalte)));
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
		// Integer
		else if (attributeSchema.get(spalte).getValue().equals(Integer.class))
			if (missingValue)
				entry = new SimpleEntry<String, Integer>(attributeSchema.get(spalte).getKey(), null);
			else
				try {
					entry = new SimpleEntry<String, Integer>(attributeSchema.get(spalte).getKey(),
							intParser.apply(instance.value(spalte)));
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
		// Double //TODO check for Number?
		else if (attributeSchema.get(spalte).getValue().equals(Double.class))
			if (missingValue || String.valueOf(instance.value(spalte)).equals("")
					|| String.valueOf(instance.value(spalte)).equals(missingValueIndicator))
				entry = new SimpleEntry<String, Double>(attributeSchema.get(spalte).getKey(), Double.NaN);
			else {
				try {
					entry = new SimpleEntry<String, Double>(attributeSchema.get(spalte).getKey(),
							new Double(String.valueOf(instance.value(spalte)).replace(",", ".")));
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
		// String
		else if (attributeSchema.get(spalte).getValue().equals(String.class))
			if (missingValue)
				entry = new SimpleEntry<String, String>(attributeSchema.get(spalte).getKey(), null);
			else
				entry = new SimpleEntry<String, String>(attributeSchema.get(spalte).getKey(),
						new String(String.valueOf(instance.stringValue(spalte))));
		// Boolean
		else if (attributeSchema.get(spalte).getValue().equals(Boolean.class)) {
			if (missingValue)
				entry = new SimpleEntry<String, String>(attributeSchema.get(spalte).getKey(), null);
			else {
				String s = String.valueOf(instance.value(spalte));
				Boolean b = ParserTools.parseBoolean(s);
				entry = new SimpleEntry<String, Boolean>(attributeSchema.get(spalte).getKey(), b);
			}
		}
		// Date (real date)
		else if (attributeSchema.get(spalte).getValue().equals(Date.class))
			if (missingValue || String.valueOf(instance.value(spalte)).equals(""))
				entry = new SimpleEntry<String, Date>(attributeSchema.get(spalte).getKey(), null);
			else
				entry = new SimpleEntry<String, Date>(attributeSchema.get(spalte).getKey(),
						ParserTools.parseDate(String.valueOf(instance.value(spalte))));

		return entry;
	}

	public static AttributeType getAttributeType(Instances instances, Attribute attribute) {
		AttributeType type;
		if (attribute.isNumeric()) {
			// determine if attribute is numeric or ordinal
			if (MathFunctions.hasFloatingPointValues(instances.attributeToDoubleArray(attribute.index())))
				type = AttributeType.NUMERIC;
			else {
				// assess the number of digits
				int length = 0;
				for (double number : instances.attributeToDoubleArray(attribute.index()))
					length = Math.max(length, (int) (Math.log10(number) + 1));
				if (length > 9)
					type = AttributeType.LONG;
				else
					type = AttributeType.INTEGER;
			}
		} else {
			// get list of attribute values
			List<String> attValues = new ArrayList<>();
			for (int j = 0; j < attribute.numValues(); j++)
				attValues.add(attribute.value(j));

			// determine if attribute is categorical or binary
			if (attValues.size() == 2) {
				for (int i = 0; i < attValues.size(); i++)
					attValues.add(attValues.remove(i).toLowerCase());

				if (attValues.contains("no") && attValues.contains("yes")
						|| attValues.contains("false") && attValues.contains("true")
						|| attValues.contains("0") && attValues.contains("1"))
					type = AttributeType.BINARY;
				else
					type = AttributeType.CATEGORICAL;
			}

			else
				type = AttributeType.CATEGORICAL;
		}
		return type;
	}

}
