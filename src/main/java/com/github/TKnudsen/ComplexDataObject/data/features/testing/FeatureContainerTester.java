package com.github.TKnudsen.ComplexDataObject.data.features.testing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.TKnudsen.ComplexDataObject.data.features.FeatureContainer;
import com.github.TKnudsen.ComplexDataObject.data.features.FeatureSchema;
import com.github.TKnudsen.ComplexDataObject.data.features.FeatureType;
import com.github.TKnudsen.ComplexDataObject.data.features.mixedData.MixedDataFeature;
import com.github.TKnudsen.ComplexDataObject.data.features.mixedData.MixedDataVector;

/**
 * <p>
 * Title:
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
 * @version 1.0
 */

public class FeatureContainerTester {
	public static class MixedFeatureContainer extends FeatureContainer<Object, MixedDataFeature, MixedDataVector> {

		public MixedFeatureContainer(FeatureSchema featureSchema) {
			super(featureSchema);
		}

		public MixedFeatureContainer(Map<Long, MixedDataVector> featureVectorMap) {
			super(featureVectorMap);
		}

		public MixedFeatureContainer(Iterable<MixedDataVector> objects) {
			super(objects);
		}
	}

	public static void main(String[] args) {

		List<MixedDataFeature> mixedDataFeatures1 = new ArrayList<>();
		mixedDataFeatures1.add(new MixedDataFeature("Feature 1", true, FeatureType.BOOLEAN));
		mixedDataFeatures1.add(new MixedDataFeature("Feature 2", 1.33, FeatureType.DOUBLE));
		mixedDataFeatures1.add(new MixedDataFeature("Feature 3", "Peter", FeatureType.STRING));
		MixedDataVector vector1 = new MixedDataVector(mixedDataFeatures1);

		List<MixedDataFeature> mixedDataFeatures2 = new ArrayList<>();
		mixedDataFeatures2.add(new MixedDataFeature("Feature 1", false, FeatureType.BOOLEAN));
		mixedDataFeatures2.add(new MixedDataFeature("Feature 2", 1.34, FeatureType.DOUBLE));
		mixedDataFeatures2.add(new MixedDataFeature("Feature 3", "Paul", FeatureType.STRING));
		MixedDataVector vector2 = new MixedDataVector(mixedDataFeatures2);

		List<MixedDataVector> objects = new ArrayList<>();
		objects.add(vector1);
		objects.add(vector2);
		FeatureContainer container = new MixedFeatureContainer(objects);

		container.addFeature(new MixedDataFeature("TestFeature", false, FeatureType.BOOLEAN));
		container.getFeatureValues("Feature 2");
	}
}
