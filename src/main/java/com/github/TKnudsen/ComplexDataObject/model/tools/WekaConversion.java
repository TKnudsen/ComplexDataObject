package com.github.TKnudsen.ComplexDataObject.model.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.data.features.AbstractFeatureVector;
import com.github.TKnudsen.ComplexDataObject.data.features.Feature;
import com.github.TKnudsen.ComplexDataObject.data.features.FeatureType;
import com.github.TKnudsen.ComplexDataObject.data.features.mixedData.MixedDataFeatureVector;
import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeatureVector;

import weka.core.Attribute;
import weka.core.DenseInstance;
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
 * @version 1.03
 */
public class WekaConversion {

	public static Instances getInstances(ComplexDataContainer container) {
		if (container == null)
			return null;

		List<Attribute> attrs = new ArrayList<Attribute>();
		Map<String, Attribute> attributeMap = new HashMap<>();

		int dims = container.getAttributeNames().size();
		if (!container.getAttributeNames().contains("Name")) {
			dims++;
			Attribute a = new Attribute("Name", (List<String>) null);
			attrs.add(a);
			attributeMap.put("Name", a);
		}
		if (!container.getAttributeNames().contains("Description")) {
			dims++;
			Attribute a = new Attribute("Description", (List<String>) null);
			attrs.add(a);
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
			attrs.add(a);
			attributeMap.put(string, a);
		}

		Instances instances = new Instances("ComplexDataContainer " + container.toString(), (ArrayList<Attribute>) attrs, container.size());

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

	/**
	 * 
	 * @param fvs
	 * @return
	 */
	public static <O extends Object, FV extends AbstractFeatureVector<O, ? extends Feature<O>>> Instances getInstances(List<FV> fvs) {
		int length = fvs.get(0).getDimensions();
		List<Attribute> attrs = new ArrayList<Attribute>(length);
		for (int i = 0; i < length; i++) {
			Attribute a = new Attribute(i + 1 + "");
			attrs.add(a);
		}

		Instances data = new Instances(fvs.get(0).getClass().getName(), (ArrayList<Attribute>) attrs, fvs.size());
		addInstances(fvs, data);
		return data;
	}

	public static Instances getInstancesNumerical(List<NumericalFeatureVector> fvs) {
		int length = fvs.get(0).getVector().length;
		List<Attribute> attrs = new ArrayList<Attribute>(length);
		for (int i = 0; i < length; i++) {
			Attribute a = new Attribute(i + 1 + "");
			attrs.add(a);
		}

		Instances data = new Instances("NumericalFeatureVectors", (ArrayList<Attribute>) attrs, fvs.size());
		addInstances(fvs, data);
		return data;
	}

	public static Instances getInstancesMixed(List<MixedDataFeatureVector> mfvs) {
		int length = mfvs.get(0).getVectorRepresentation().size();
		List<Attribute> attrs = new ArrayList<Attribute>(length);
		for (int i = 0; i < length; i++) {

			Attribute a;

			if (mfvs.get(0).getVectorRepresentation().get(i).getFeatureType() == FeatureType.STRING) {
				List<String> strList = new ArrayList<String>();
				for (MixedDataFeatureVector mfv : mfvs) {
					if (!strList.contains((String) mfv.getVectorRepresentation().get(i).getFeatureValue()))
						strList.add((String) mfv.getVectorRepresentation().get(i).getFeatureValue());
				}

				a = new Attribute(i + 1 + "", strList);

			} else {
				a = new Attribute(i + 1 + "");
			}

			attrs.add(a);
		}

		Instances data = new Instances("MixedFeatureVectors", (ArrayList<Attribute>) attrs, mfvs.size());
		addMixedInstances(mfvs, data);
		return data;
	}

	public static <O extends Object, FV extends AbstractFeatureVector<O, ? extends Feature<O>>> void addInstances(List<FV> fvs, Instances data) {
		for (FV mf : fvs) {
			int length = mf.getVectorRepresentation().size();

			data.add(new DenseInstance(length));

			Instance ins = data.get(data.size() - 1);

			for (int i = 0; i < length; i++) {

				if (mf.getVectorRepresentation().get(i).getFeatureType() == FeatureType.DOUBLE)
					ins.setValue(i, (Double) mf.getVectorRepresentation().get(i).getFeatureValue());
				else if (mf.getVectorRepresentation().get(i).getFeatureType() == FeatureType.STRING) {
					String str = (String) mf.getVectorRepresentation().get(i).getFeatureValue();
					ins.setValue(i, str);
				}
			}

		}
	}

	public static void addMixedInstances(List<MixedDataFeatureVector> mfvs, Instances data) {
		for (MixedDataFeatureVector mfv : mfvs) {
			int length = mfv.getVectorRepresentation().size();

			data.add(new DenseInstance(length));

			Instance ins = data.get(data.size() - 1);

			for (int i = 0; i < length; i++) {

				if (mfv.getVectorRepresentation().get(i).getFeatureType() == FeatureType.DOUBLE)
					ins.setValue(i, (Double) mfv.getVectorRepresentation().get(i).getFeatureValue());
				else if (mfv.getVectorRepresentation().get(i).getFeatureType() == FeatureType.STRING) {
					String str = (String) mfv.getVectorRepresentation().get(i).getFeatureValue();
					ins.setValue(i, str);
				}
			}

		}
	}

	public static Instances getLabeledInstancesNumerical(List<NumericalFeatureVector> fvs, String classAttribute) {
		List<String> labels = new ArrayList<>();
		for (int i = 0; i < fvs.size(); i++)
			if (fvs.get(i).get(classAttribute) instanceof String)
				labels.add((String) fvs.get(i).get(classAttribute));
			else
				labels.add(fvs.get(i).get(classAttribute).toString());

		Instances inst = getInstancesNumerical(fvs);

		return addLabelsToInstances(inst, labels);
	}

	/**
	 *
	 * @param fvs
	 * @param classAttribute
	 * @return
	 */
	public static <O extends Object, FV extends AbstractFeatureVector<O, ? extends Feature<O>>> Instances getLabeledInstances(List<FV> fvs, String classAttribute) {
		List<String> labels = new ArrayList<>();
		for (int i = 0; i < fvs.size(); i++)
			if (fvs.get(i).get(classAttribute) instanceof String)
				labels.add((String) fvs.get(i).get(classAttribute));
			else
				labels.add(fvs.get(i).get(classAttribute).toString());

		Instances inst = getInstances(fvs);

		return addLabelsToInstances(inst, labels);
	}

	public static Instances getLabeledInstances(List<NumericalFeatureVector> fvs, List<String> labels) {

		Instances inst = getInstancesNumerical(fvs);

		return addLabelsToInstances(inst, labels);
	}

	public static Instances getLabeledMixInstances(List<MixedDataFeatureVector> mfvs, List<String> labels) {

		Instances inst = getInstancesMixed(mfvs);

		return addLabelsToInstances(inst, labels);
	}

	public static Instances getNumericLabeledInstances(List<NumericalFeatureVector> fvs, List<Double> numLabels) {

		Instances inst = getInstancesNumerical(fvs);

		return addNumericLabelsToInstances(inst, numLabels);
	}

	public static Instances getNumericLabeledMixInstances(List<MixedDataFeatureVector> mfvs, List<Double> numLabels) {

		Instances inst = getInstancesMixed(mfvs);

		return addNumericLabelsToInstances(inst, numLabels);
	}

	private static Instances addNumericLabelsToInstances(Instances inst, List<Double> numLabels) {

		Attribute classAtt = new Attribute("num");

		inst.insertAttributeAt(classAtt, inst.numAttributes());
		inst.setClassIndex(inst.numAttributes() - 1);

		for (int i = 0; i < numLabels.size(); i++)
			inst.instance(i).setValue(inst.numAttributes() - 1, numLabels.get(i));

		return inst;

	}

	public static Instances addLabelAttributeToInstance(Instances inst, List<String> labels) {

		List<String> destinctList = destinctListCreator(labels);

		Attribute classAtt = new Attribute("class", destinctList);

		inst.insertAttributeAt(classAtt, inst.numAttributes());
		inst.setClass(classAtt);
		inst.setClassIndex(inst.numAttributes() - 1);

		return inst;

	}

	public static Instances addNumericLabelAttributeToInstance(Instances inst) {

		Attribute classAtt = new Attribute("num");

		inst.insertAttributeAt(classAtt, inst.numAttributes());
		inst.setClass(classAtt);
		inst.setClassIndex(inst.numAttributes() - 1);

		return inst;

	}

	private static Instances addLabelsToInstances(Instances inst, List<String> labels) {

		Instances inst2 = addLabelAttributeToInstance(inst, labels);

		for (int i = 0; i < labels.size(); i++)
			inst2.instance(i).setClassValue(labels.get(i));

		return inst2;

	}

	public static List<String> destinctListCreator(List<String> list) {
		List<String> destinctList = new ArrayList<String>();

		for (String str : list)
			if (!destinctList.contains(str))
				destinctList.add(str);

		return destinctList;
	}
}
