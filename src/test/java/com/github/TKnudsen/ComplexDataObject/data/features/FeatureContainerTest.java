package com.github.TKnudsen.ComplexDataObject.data.features;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.github.TKnudsen.ComplexDataObject.data.features.mixedData.MixedDataFeature;
import com.github.TKnudsen.ComplexDataObject.data.features.mixedData.MixedDataFeatureContainer;
import com.github.TKnudsen.ComplexDataObject.data.features.mixedData.MixedDataFeatureVector;

public class FeatureContainerTest {

	@Test
	public void testStuff() {
		List<MixedDataFeature> mixedDataFeatures1 = new ArrayList<>();
		mixedDataFeatures1.add(new MixedDataFeature("Feature 1", true, FeatureType.BOOLEAN));
		mixedDataFeatures1.add(new MixedDataFeature("Feature 2", 1.33, FeatureType.DOUBLE));
		mixedDataFeatures1.add(new MixedDataFeature("Feature 3", "Peter", FeatureType.STRING));
		MixedDataFeatureVector vector1 = new MixedDataFeatureVector(mixedDataFeatures1);

		List<MixedDataFeature> mixedDataFeatures2 = new ArrayList<>();
		mixedDataFeatures2.add(new MixedDataFeature("Feature 1", false, FeatureType.BOOLEAN));
		mixedDataFeatures2.add(new MixedDataFeature("Feature 2", 1.34, FeatureType.DOUBLE));
		mixedDataFeatures2.add(new MixedDataFeature("Feature 3", "Paul", FeatureType.STRING));
		MixedDataFeatureVector vector2 = new MixedDataFeatureVector(mixedDataFeatures2);

		List<MixedDataFeatureVector> objects = new ArrayList<>();
		objects.add(vector1);
		objects.add(vector2);
		FeatureVectorContainer container = new MixedDataFeatureContainer(objects);

		container.addFeature(new MixedDataFeature("TestFeature", false, FeatureType.BOOLEAN));
		container.getFeatureValues("Feature 2");
	}
}
