package test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import data.ComplexDataContainer;
import data.ComplexDataObject;
import tools.ComplexDataObjectFactory;

public class ComplexDataContainerTest {

	public static void main(String[] args) {

		ComplexDataObject a = ComplexDataObjectFactory.createObject("Att A", new Double(2.0), "Att B", "asdf");
		ComplexDataObject b = ComplexDataObjectFactory.createObject("Att A", new Double(3.0), "Att B", "jklö");

		List<ComplexDataObject> objects = new ArrayList<>(Arrays.asList(a, b));

		ComplexDataContainer complexDataContainer = new ComplexDataContainer(objects);
		for (ComplexDataObject complexDataObject : complexDataContainer) {
			System.out.println(complexDataObject);
		}
	}
}
