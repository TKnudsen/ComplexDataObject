package com.github.TKnudsen.ComplexDataObject.tools;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.github.TKnudsen.ComplexDataObject.data.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.ComplexDataObject;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class WekaConversion {

	public static Instances getInstances(ComplexDataContainer container) {
		if (container == null)
			return null;

		int dims = container.getAttributeNames().size();

		FastVector attrs = new FastVector(dims);
		Map<String, Attribute> attributeMap = new HashMap<>();

		for (Iterator<String> iterator = container.getAttributeNames().iterator(); iterator.hasNext();) {
			String string = iterator.next();

			Attribute a = null;
			if (container.isNumeric(string))
				a = new Attribute(string);
			else if (container.isBoolean(string))
				a = new Attribute(string);
			else
				a = new Attribute(string, (List<String>) null);
			attrs.addElement(a);
			attributeMap.put(string, a);
		}

		Instances instances = new Instances("ComplexDataContainer " + container.toString(), attrs, container.size());

		// create instance objects
		for (ComplexDataObject object : container) {
			Instance instance = new DenseInstance(dims);

			Iterator<String> attNames = object.iterator();
			while (attNames.hasNext()) {
				String attName = attNames.next();
				Attribute attribute = attributeMap.get(attName);

				Object value = object.get(attName);
				if (value instanceof String)
					instance.setValue(attribute, (String) value);
				else if (value instanceof Boolean) {
					Integer i = ((Boolean) value).booleanValue() ? 1 : 0;
					instance.setValue(attribute, ((Number) i).doubleValue());
				} else if (Number.class.isAssignableFrom(value.getClass()))
					instance.setValue(attribute, ((Number) value).doubleValue());
			}

			instances.add(instance);
		}

		return instances;
	}
}
