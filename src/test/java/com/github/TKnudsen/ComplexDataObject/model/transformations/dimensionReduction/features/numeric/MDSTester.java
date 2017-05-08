package com.github.TKnudsen.ComplexDataObject.model.transformations.dimensionReduction.features.numeric;

import java.util.ArrayList;
import java.util.List;

import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeatureVector;
import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeatureVectorFactory;
import com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.featureVector.EuclideanDistanceMeasure;

public class MDSTester {

	public static void main(String[] args) {

		List<NumericalFeatureVector> fvs = new ArrayList<>();

		double[] vect1 = new double[] { 1.0, 1.0, 0.0 };
		double[] vect2 = new double[] { 1.0, 0.98, 0.0 };
		double[] vect3 = new double[] { 0.0, 1.0, 1.0 };
		double[] vect4 = new double[] { 0.0, 1.0, 0.98 };
		double[] vect5 = new double[] { 0.0, 1.0, 0.0 };
		fvs.add(NumericalFeatureVectorFactory.createNumericalFeatureVector(vect1));
		fvs.add(NumericalFeatureVectorFactory.createNumericalFeatureVector(vect2));
		fvs.add(NumericalFeatureVectorFactory.createNumericalFeatureVector(vect3));
		fvs.add(NumericalFeatureVectorFactory.createNumericalFeatureVector(vect4));
		fvs.add(NumericalFeatureVectorFactory.createNumericalFeatureVector(vect5));

		for (NumericalFeatureVector fv : fvs) {
			for (int i = 0; i < fv.getDimensions(); i++)
				System.out.print(fv.getVector()[i] + " ");
			System.out.println();
		}

		MultiDimensionalScaling mds = new MultiDimensionalScaling(new EuclideanDistanceMeasure(), 2);
		List<NumericalFeatureVector> transformed = mds.transform(fvs);

		for (NumericalFeatureVector fv : transformed) {
			for (int i = 0; i < fv.getDimensions(); i++)
				System.out.print(fv.getVector()[i] + " ");
			System.out.println();
		}
	}
}
