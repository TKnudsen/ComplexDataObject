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
import com.github.TKnudsen.ComplexDataObject.data.features.FeatureContainer;
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

				Object value = cdo.getAttribute(attName);
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
	 * @deprecated use
	 */
	public static <O extends Object, FV extends AbstractFeatureVector<O, ? extends Feature<O>>> Instances getInstances(List<FV> fvs) {
		if (fvs == null || fvs.size() == 0)
			return null;

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

	/**
	 * 
	 * @param fvs
	 * @return
	 * @deprecated use
	 */
	public static <O extends Object, F extends Feature<O>, FV extends AbstractFeatureVector<O, F>> Instances getInstances(FeatureContainer<O, ?, FV> featureContainer) {

		// FeatureContainer<O, ?, FV> featureContainer = new
		// FeatureContainer<>(fvs);

		List<Attribute> attrs = new ArrayList<Attribute>(featureContainer.getFeatureNames().size());
		for (String featureName : featureContainer.getFeatureNames()) {
			Attribute a = null;
			if (featureContainer.isNumeric(featureName))
				a = new Attribute(featureName);
			else
				a = new Attribute(featureName, (List<String>) null);
			attrs.add(a);
		}

		Instances instances = new Instances("asdf", (ArrayList<Attribute>) attrs, featureContainer.size());
		addInstances(featureContainer, instances);
		return instances;
	}

	/**
	 * 
	 * @param fvs
	 * @param stringToNominal
	 *            decides whether string values are represented as nominal
	 *            values (with a concrete alphabet of observations)
	 * @return
	 */
	public static <O extends Object, FV extends AbstractFeatureVector<O, ? extends Feature<O>>> Instances getInstances(List<FV> fvs, boolean stringToNominal) {
		// TODO
		System.out.println("WekaConversion.getInstances: unimplemented method. there is a difference between strings and nominals in WEKA which has to be considered in future");
		System.exit(-1);

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
		if (fvs == null || fvs.size() == 0)
			return;

		int dim = fvs.get(0).getVectorRepresentation().size();
		for (FV fv : fvs) {
			int length = fv.getVectorRepresentation().size();
			if (dim != length)
				throw new IllegalArgumentException("List of input FV has different features.");

			data.add(new DenseInstance(length));
			Instance ins = data.get(data.size() - 1);

			List<? extends Feature<O>> vectorRepresentation = fv.getVectorRepresentation();
			for (int i = 0; i < length; i++) {
				if (vectorRepresentation.get(i).getFeatureType() == FeatureType.DOUBLE)
					ins.setValue(i, (Double) vectorRepresentation.get(i).getFeatureValue());
				else if (vectorRepresentation.get(i).getFeatureType() == FeatureType.STRING) {
					String str = vectorRepresentation.get(i).getFeatureValue().toString();
					try {
						ins.setValue(i, str.toString());
					} catch (Exception e) {
						ins.setValue(i, "");
					}
				} else if (vectorRepresentation.get(i).getFeatureType() == FeatureType.BOOLEAN) {
					Boolean b = (Boolean) vectorRepresentation.get(i).getFeatureValue();
					ins.setValue(i, b.toString());
				} else {
					System.out.println("");
				}
				// TODO check whether WEKA automatically maps string to nominal.
			}
		}
	}

	public static <O extends Object, FV extends AbstractFeatureVector<O, ? extends Feature<O>>> void addInstances(FeatureContainer<O, ?, FV> featureContainer, Instances instances) {
		if (featureContainer == null || featureContainer.size() == 0)
			return;

		for (FV fv : featureContainer) {

			instances.add(new DenseInstance(fv.getDimensions()));
			Instance ins = instances.get(instances.size() - 1);

			for (String featureName : featureContainer.getFeatureNames()) {
				Feature<?> feature = fv.getFeature(featureName);
				Attribute attribute = instances.attribute(featureName);
				if (feature.getFeatureType() == FeatureType.DOUBLE) {
					Number n = (Number) feature.getFeatureValue();
					ins.setValue(attribute, n.doubleValue());
				} else if (feature.getFeatureType() == FeatureType.STRING) {
					String string = feature.getFeatureValue().toString();
					try {
						ins.setValue(attribute, string);
					} catch (Exception e) {
						ins.setValue(attribute, "");
					}
				} else if (feature.getFeatureType() == FeatureType.BOOLEAN) {
					Boolean b = (Boolean) feature.getFeatureValue();
					ins.setValue(attribute, b.toString());
				} else {
					System.out.println("");
				}
				// TODO check whether WEKA automatically maps string to nominal.
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
			if (fvs.get(i).getAttribute(classAttribute) instanceof String)
				labels.add((String) fvs.get(i).getAttribute(classAttribute));
			else
				labels.add(fvs.get(i).getAttribute(classAttribute).toString());

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
			if (fvs.get(i).getAttribute(classAttribute) == null)
				throw new IllegalArgumentException("WekaConverter.getLabeledInstances: classAttribute not found for given FeatureVector.");
			else if (fvs.get(i).getAttribute(classAttribute) instanceof String)
				labels.add((String) fvs.get(i).getAttribute(classAttribute));
			else
				labels.add(fvs.get(i).getAttribute(classAttribute).toString());

		Instances insances = getInstances(fvs);

		if (insances != null && weights != null && weights.size() == insances.size())
			addWeightsToInstances(insances, weights);

		return addLabelsToInstances(insances, labels);
	}

	/**
	 * Uses WEKAs ability to assign weights to Instances.
	 * 
	 * @param instances
	 * @param weights
	 * @return
	 */
	private static Instances addWeightsToInstances(Instances instances, List<Double> weights) {
		if (instances == null || weights == null)
			return instances;

		if (instances.size() != weights.size())
			throw new IllegalArgumentException();

		for (int i = 0; i < weights.size(); i++) {
			double w = weights.get(i);
			if (Double.isNaN(w))
				w = 0;
			instances.instance(i).setWeight(w);
		}

		return instances;

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

	public static Instances addLabelAttributeToInstance(Instances instances, List<String> labels) {
		List<String> distinctLabels = distinctListCreator(labels);

		Attribute classAtt = new Attribute("class", distinctLabels);

		instances.insertAttributeAt(classAtt, instances.numAttributes());
		instances.setClass(classAtt);
		instances.setClassIndex(instances.numAttributes() - 1);

		return instances;
	}

	public static Instances addNumericLabelAttributeToInstance(Instances insances) {
		Attribute classAtt = new Attribute("num");

		insances.insertAttributeAt(classAtt, insances.numAttributes());
		insances.setClass(classAtt);
		insances.setClassIndex(insances.numAttributes() - 1);

		return insances;

	}

	private static Instances addLabelsToInstances(Instances instances, List<String> labels) {
		if (instances == null)
			return null;

		Instances inst2 = addLabelAttributeToInstance(instances, labels);

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
