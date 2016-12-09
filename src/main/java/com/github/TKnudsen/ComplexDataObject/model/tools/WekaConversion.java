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
 * Description: helper tools that ease the use of WEKA data structures, i.e.,
 * Instances and Instance objects.
 * </p>
 *
 * <p>
 * Copyright: Copyright (c) 2016
 * </p>
 *
 * @author Juergen Bernard
 * @version 1.04
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
			Attribute a = null;
			if (fvs.get(0).getFeature(i).getFeatureType().equals(FeatureType.DOUBLE))
				a = new Attribute(i + 1 + "");
			else
				a = new Attribute(i + 1 + "", (List<String>) null);
			attrs.add(a);
		}

		Instances data = new Instances(fvs.get(0).getClass().getName(), (ArrayList<Attribute>) attrs, fvs.size());
		addInstances(fvs, data);
		return data;
	}

	public static <O extends Object, FV extends AbstractFeatureVector<O, ? extends Feature<O>>> void addInstances(List<FV> fvs, Instances data) {
		for (FV fv : fvs) {
			int length = fv.getVectorRepresentation().size();
			data.add(new DenseInstance(length));
			Instance ins = data.get(data.size() - 1);

			for (int i = 0; i < length; i++) {
				if (fv.getVectorRepresentation().get(i).getFeatureType() == FeatureType.DOUBLE)
					ins.setValue(i, (Double) fv.getVectorRepresentation().get(i).getFeatureValue());
				else if (fv.getVectorRepresentation().get(i).getFeatureType() == FeatureType.STRING) {
					String str = (String) fv.getVectorRepresentation().get(i).getFeatureValue();
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

		Instances insances = getInstances(fvs);

		return addLabelsToInstances(insances, labels);
	}

	/**
	 *
	 * @param fvs
	 * @param classAttribute
	 * @return
	 */
	public static <O extends Object, FV extends AbstractFeatureVector<O, ? extends Feature<O>>> Instances getLabeledInstances(List<FV> fvs, String classAttribute) {

		return getLabeledInstances(fvs, null, classAttribute);
	}

	/**
	 * creates instances with weights for a given List of FVs.
	 * 
	 * @param fvs
	 * @param weights
	 * @param classAttribute
	 * @return
	 */
	public static <O extends Object, FV extends AbstractFeatureVector<O, ? extends Feature<O>>> Instances getLabeledInstances(List<FV> fvs, List<Double> weights, String classAttribute) {
		List<String> labels = new ArrayList<>();
		for (int i = 0; i < fvs.size(); i++)
			if (fvs.get(i).get(classAttribute) instanceof String)
				labels.add((String) fvs.get(i).get(classAttribute));
			else
				labels.add(fvs.get(i).get(classAttribute).toString());

		Instances insances = getInstances(fvs);

		if (weights != null && weights.size() == insances.size())
			addWeightsToInstances(insances, weights);

		return addLabelsToInstances(insances, labels);
	}

	/**
	 * Uses WEKAs ability to assign weights to Instances.
	 * 
	 * @param insances
	 * @param weights
	 * @return
	 */
	private static Instances addWeightsToInstances(Instances insances, List<Double> weights) {
		if (insances == null || weights == null)
			return insances;

		for (int i = 0; i < weights.size(); i++) {
			double w = weights.get(i);
			if (Double.isNaN(w))
				w = 0;
			insances.instance(i).setWeight(w);
		}

		return insances;

	}

	public static Instances getLabeledInstances(List<NumericalFeatureVector> fvs, List<String> labels) {
		Instances insances = getInstances(fvs);

		return addLabelsToInstances(insances, labels);
	}

	public static Instances getNumericLabeledMixInstances(List<MixedDataFeatureVector> mfvs, List<Double> numLabels) {
		Instances insances = getInstances(mfvs);

		return addNumericLabelsToInstances(insances, numLabels);
	}

	private static Instances addNumericLabelsToInstances(Instances insances, List<Double> numLabels) {
		Attribute classAtt = new Attribute("num");

		insances.insertAttributeAt(classAtt, insances.numAttributes());
		insances.setClassIndex(insances.numAttributes() - 1);

		for (int i = 0; i < numLabels.size(); i++)
			insances.instance(i).setValue(insances.numAttributes() - 1, numLabels.get(i));

		return insances;

	}

	public static Instances addLabelAttributeToInstance(Instances insances, List<String> labels) {
		List<String> distinctLabels = distinctListCreator(labels);

		Attribute classAtt = new Attribute("class", distinctLabels);

		insances.insertAttributeAt(classAtt, insances.numAttributes());
		insances.setClass(classAtt);
		insances.setClassIndex(insances.numAttributes() - 1);

		return insances;

	}

	public static Instances addNumericLabelAttributeToInstance(Instances insances) {
		Attribute classAtt = new Attribute("num");

		insances.insertAttributeAt(classAtt, insances.numAttributes());
		insances.setClass(classAtt);
		insances.setClassIndex(insances.numAttributes() - 1);

		return insances;

	}

	private static Instances addLabelsToInstances(Instances insances, List<String> labels) {
		Instances inst2 = addLabelAttributeToInstance(insances, labels);

		for (int i = 0; i < labels.size(); i++)
			inst2.instance(i).setClassValue(labels.get(i));

		return inst2;

	}

	/**
	 * Improve this piece of code!
	 * 
	 * @param list
	 * @return
	 */
	public static List<String> distinctListCreator(List<String> list) {
		List<String> distinctList = new ArrayList<String>();

		if (list == null)
			return distinctList;

		for (String str : list)
			if (!distinctList.contains(str))
				distinctList.add(str);

		return distinctList;
	}
}
