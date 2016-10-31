package com.github.TKnudsen.ComplexDataObject.model.tools;

import java.util.ArrayList;
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

		FastVector attrs = new FastVector<>();
		Map<String, Attribute> attributeMap = new HashMap<>();

		int dims = container.getAttributeNames().size();
		if (!container.getAttributeNames().contains("Name")) {
			dims++;
			Attribute a = new Attribute("Name", (List<String>) null);
			attrs.addElement(a);
			attributeMap.put("Name", a);
		}
		if (!container.getAttributeNames().contains("Description")) {
			dims++;
			Attribute a = new Attribute("Description", (List<String>) null);
			attrs.addElement(a);
			attributeMap.put("Description", a);
		}

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
		for (ComplexDataObject cdo : container) {
			Instance instance = new DenseInstance(dims);

			Iterator<String> attNames = cdo.iterator();
			while (attNames.hasNext()) {
				String attName = attNames.next();
				Attribute attribute = attributeMap.get(attName);

				Object value = cdo.get(attName);
				if (container.isNumeric(attName)) {
					if (value != null)
						instance.setValue(attribute, ((Number) value).doubleValue());
				} else if (container.isBoolean(attName)) {
					if (value != null) {
						Integer i = ((Boolean) value).booleanValue() ? 1 : 0;
						instance.setValue(attribute, ((Number) i).doubleValue());
					}
				} else if (value != null)
					instance.setValue(attribute, value.toString());
			}

			Attribute nameAttribute = attributeMap.get("Name");
			instance.setValue(nameAttribute, cdo.getName());

			Attribute descripptionAttribute = attributeMap.get("Description");
			instance.setValue(descripptionAttribute, cdo.getDescription());

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

	public static Instances getLabeledInstances(List<NumericalFeatureVector> fvs, List<String> labels) {

		Instances inst = getInstances(fvs);

		List<String> destinctList = destinctListCreator(labels);

		FastVector fastV = new FastVector();

		for (int i = 0; i < destinctList.size(); i++)
			fastV.addElement(destinctList.get(i));

		Attribute classAtt = new Attribute("class", fastV);

		inst.insertAttributeAt(classAtt, inst.numAttributes());
		inst.setClass(classAtt);
		inst.setClassIndex(inst.numAttributes() - 1);

		for (int i = 0; i < labels.size(); i++)
			inst.instance(i).setClassValue(labels.get(i));

		inst.setClass(classAtt);
		inst.setClassIndex(inst.numAttributes() - 1);

		return inst;
	}

	public static List<String> destinctListCreator(List<String> list) {
		List<String> destinctList = new ArrayList<String>();

		for (String str : list)
			if (!destinctList.contains(str))
				destinctList.add(str);

		return destinctList;
	}
}
