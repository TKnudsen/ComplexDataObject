package com.github.TKnudsen.ComplexDataObject.test;

import java.util.ArrayList;
import java.util.List;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.arff.WekaTools;
import com.github.TKnudsen.ComplexDataObject.model.tools.WekaConversion;

import weka.core.Instances;

public class WekaConversionTester {

	public static void main(String[] args) {

		List<ComplexDataObject> complexDataObjects = new ArrayList<>();

		ComplexDataObject o1 = new ComplexDataObject(1L);
		o1.add("Attribute 1", 1.0);
		o1.add("Attribute 2", true);
		o1.add("Attribute 3", "pete");
		complexDataObjects.add(o1);

		ComplexDataObject o2 = new ComplexDataObject(2L);
		o2.add("Attribute 1", 2.0);
		o2.add("Attribute 2", false);
		o2.add("Attribute 3", "paul");
		complexDataObjects.add(o2);

		ComplexDataContainer container = new ComplexDataContainer(complexDataObjects);
		for (ComplexDataObject o : container)
			System.out.println(o);

		Instances instances = WekaConversion.getInstances(container);
		System.out.println(instances);

		ComplexDataContainer container2 = new ComplexDataContainer(WekaTools.getComplexDataObjects(instances));
		for (ComplexDataObject o : container2)
			System.out.println(o);
	}
}
