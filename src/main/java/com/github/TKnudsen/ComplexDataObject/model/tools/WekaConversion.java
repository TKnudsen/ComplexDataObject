package com.github.TKnudsen.ComplexDataObject.model.tools;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeatureVector;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 * <p>
 * Title: WekaConversion
 * </p>
 *
 * <p>
 * Description:
 * </p>
 *
 * <p>
 * Copyright: Copyright (c) 2016
 * </p>
 *
 * @author Juergen Bernard
 * @version 1.01
 */
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

	public static Instances getInstances(List<NumericalFeatureVector> fvs) {
		int length = fvs.get(0).getVector().length;
		FastVector attrs = new FastVector(length);
		for (int i = 0; i < length; i++) {
			Attribute a = new Attribute(i + 1 + "");
			attrs.addElement(a);
		}

		Instances data = new Instances("NumericalFeatureVectors", attrs, fvs.size());
		addInstances(fvs, data);
		return data;
	}

	public static void addInstances(List<NumericalFeatureVector> fvs, Instances data) {
		for (NumericalFeatureVector fv : fvs) {
			double[] vector = fv.getVector();
			Instance ins = new DenseInstance(vector.length);
			for (int i = 0; i < vector.length; i++) {
				ins.setValue(i, vector[i]);
			}
			data.add(ins);
		}
	}
}
