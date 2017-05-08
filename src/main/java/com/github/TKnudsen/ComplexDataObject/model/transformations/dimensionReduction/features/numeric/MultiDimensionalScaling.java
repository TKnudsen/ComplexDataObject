package com.github.TKnudsen.ComplexDataObject.model.transformations.dimensionReduction.features.numeric;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.math3.ml.distance.EuclideanDistance;

import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeatureVector;
import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeatureVectorFactory;
import com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.featureVector.EuclideanDistanceMeasure;
import com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.featureVector.INumericalFeatureVectorDistanceMeasure;
import com.github.TKnudsen.ComplexDataObject.model.processors.complexDataObject.DataTransformationCategory;

/**
 * <p>
 * Title: MultiDimensionalScaling
 * </p>
 * 
 * <p>
 * Description: Multi-dimensional Scaling (MDS) is a non-linear dimension
 * reduction algorithm proposed by Joseph B. KRUSKAL in 1964:
 * "Multidimensional scaling by optimizing goodness of fit to a nonmetric hypothesis"
 * In Psychometrika 29, 1 (1964), 1–27.
 * 
 * The principal idea is to iteratively optimize the low-dimensional (often 2D)
 * positions of objects with respect to pairwise distances in the original space
 * and a stress-minimization function.
 * 
 * <p>
 * Copyright: Copyright (c) 2012-2017 Jürgen Bernard,
 * https://github.com/TKnudsen/ComplexDataObject
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */
public class MultiDimensionalScaling implements IDimensionReduction {

	/**
	 * either a distance matrix is provided with the constructor, or the
	 * distance matrix will be calculated "online".
	 */
	private INumericalFeatureVectorDistanceMeasure distanceMeasure = new EuclideanDistanceMeasure();

	/**
	 * Euclidean distance metric for double[]
	 */
	private EuclideanDistance ed = new EuclideanDistance();

	/**
	 * distance matrix, internally used for the low-dimensional manifold
	 * optimization
	 */
	private double[][] distanceMatrix;

	// TODO integrate alternative termination criterion
	private int maxIterations = 1000;

	private boolean printProgress = false;

	/**
	 * the dimensionality of the manifold to be learned
	 */
	private int outputDimensionality;

	private Map<NumericalFeatureVector, NumericalFeatureVector> mapping;

	public MultiDimensionalScaling(INumericalFeatureVectorDistanceMeasure distanceMeasure, int outputDimensionality) {
		if (distanceMeasure == null)
			throw new IllegalArgumentException("Distance measure was null");

		this.distanceMeasure = distanceMeasure;
		this.outputDimensionality = outputDimensionality;
	}

	public MultiDimensionalScaling(double[][] distanceMatrix, int outputDimensionality, int maxIterations, double stopStressFactor) {
		if (distanceMatrix == null || distanceMatrix.length == 0 || distanceMatrix.length != distanceMatrix[0].length)
			throw new IllegalArgumentException("Multidimensional Scaling: distance matrix is ill-defined");

		this.distanceMatrix = distanceMatrix;
		this.outputDimensionality = outputDimensionality;
		this.maxIterations = maxIterations;
	}

	@Override
	public List<NumericalFeatureVector> transform(NumericalFeatureVector input) {
		return Arrays.asList(new NumericalFeatureVector[] { mapping.get(input) });
	}

	@Override
	public List<NumericalFeatureVector> transform(List<NumericalFeatureVector> inputObjects) {
		mapping = new HashMap<>();

		if (distanceMatrix == null || distanceMatrix.length != inputObjects.size() || distanceMatrix[0].length != inputObjects.size())
			calculateDistanceMatrix(inputObjects);

		// initialize points of the low-dimensional embedding
		List<double[]> lowDimensionalPoints = initializeLowDimensionalPoints(outputDimensionality, inputObjects.size());

		for (int iteration = 0; iteration < maxIterations; iteration++) {
			double[][] pointDistances = calculatePointDistances(lowDimensionalPoints);

			// calculate Kruskal's stress for every pair of objects
			if (printProgress) {
				double stress = calculateStress(distanceMatrix, pointDistances);
				System.out.println("Multidimensional Scaling - iteration " + (iteration + 1) + ": Kruskal's stress = " + stress);
			}

			// optimize low-dimensional embedding
			double[][] newPoints = new double[lowDimensionalPoints.size()][outputDimensionality];
			for (int i = 0; i < lowDimensionalPoints.size(); i++) {
				double[] newPointCoordinates = new double[outputDimensionality];
				for (int d = 0; d < outputDimensionality; d++) {
					for (int j = 0; j < lowDimensionalPoints.size(); j++) {
						if (j == i)
							continue;
						if (distanceMatrix[i][j] == 0)
							continue;
						newPointCoordinates[d] += lowDimensionalPoints.get(i)[d] + (pointDistances[i][j] - distanceMatrix[i][j]) * (lowDimensionalPoints.get(j)[d] - lowDimensionalPoints.get(i)[d]);
					}
					newPointCoordinates[d] /= (lowDimensionalPoints.size() - 1);
				}
				newPoints[i] = newPointCoordinates;
			}

			// assign the new coordinates
			for (int i = 0; i < lowDimensionalPoints.size(); i++)
				for (int j = 0; j < lowDimensionalPoints.size(); j++)
					lowDimensionalPoints.set(i, newPoints[i]);
		}

		List<NumericalFeatureVector> output = new ArrayList<>();

		for (int i = 0; i < inputObjects.size(); i++) {
			NumericalFeatureVector fv = NumericalFeatureVectorFactory.createNumericalFeatureVector(lowDimensionalPoints.get(i), inputObjects.get(i).getName(), inputObjects.get(i).getDescription());
			Iterator<String> attributeIterator = inputObjects.get(i).iterator();
			while (attributeIterator.hasNext()) {
				String attribute = attributeIterator.next();
				fv.add(attribute, inputObjects.get(i).getAttribute(attribute));
			}

			mapping.put(inputObjects.get(i), fv);
			output.add(fv);
		}

		return output;
	}

	/**
	 * sum of squared errors between distance matrix and pointDistances.
	 * 
	 * @param distanceMatrix
	 * @param pointDistances
	 * @return
	 */
	private double calculateStress(double[][] distanceMatrix, double[][] pointDistances) {
		double stress = 0;
		for (int i = 0; i < pointDistances.length; i++) {
			for (int j = 0; j < pointDistances[0].length; j++) {
				double dij = pointDistances[i][j];
				double Dij = distanceMatrix[i][j];
				stress += Math.pow(Dij - dij, 2);
			}
		}
		return stress;
	}

	/**
	 * calculates pairwise distances of the given NumericFeatureVectors. These
	 * distances are used to assess the distance relations of the original data.
	 * 
	 * @param fvs
	 * @return
	 */
	private void calculateDistanceMatrix(List<NumericalFeatureVector> fvs) {
		distanceMatrix = new double[fvs.size()][fvs.size()];

		for (int x = 0; x < fvs.size(); x++) {
			distanceMatrix[x][x] = 0;
			for (int y = 0; y < x; y++) {
				double distance = distanceMeasure.getDistance(fvs.get(x), fvs.get(y));
				distanceMatrix[x][y] = distance;
				distanceMatrix[y][x] = distance;
			}
		}
	}

	/**
	 * calculates the distances between points in the low-dimensional manifold.
	 * 
	 * @param lowDimensionalPoints
	 * @return
	 */
	private double[][] calculatePointDistances(List<double[]> lowDimensionalPoints) {
		double[][] distanceMatrix = new double[lowDimensionalPoints.size()][lowDimensionalPoints.size()];

		for (int x = 0; x < lowDimensionalPoints.size(); x++) {
			distanceMatrix[x][x] = 0;
			for (int y = 0; y < x; y++) {
				double distance = ed.compute(lowDimensionalPoints.get(x), lowDimensionalPoints.get(y));
				distanceMatrix[x][y] = distance;
				distanceMatrix[y][x] = distance;
			}
		}

		return distanceMatrix;
	}

	/**
	 * initialize points of the low-dimensional embedding
	 * 
	 * @param dimensionality
	 * @return
	 */
	private List<double[]> initializeLowDimensionalPoints(int dimensionality, int count) {
		List<double[]> lowDimensionalPoints = new ArrayList<>(count);

		Random random = new Random();

		for (int i = 0; i < count; i++) {
			double[] point = new double[dimensionality];
			for (int dim = 0; dim < dimensionality; dim++)
				point[dim] = random.nextDouble();
			lowDimensionalPoints.add(point);
		}

		return lowDimensionalPoints;
	}

	@Override
	public DataTransformationCategory getDataTransformationCategory() {
		return DataTransformationCategory.DIMENSION_REDUCTION;
	}

	@Override
	public Map<NumericalFeatureVector, NumericalFeatureVector> getMapping() {
		return mapping;
	}

	public int getOutputDimensionality() {
		return outputDimensionality;
	}

	public void setOutputDimensionality(int outputDimensionality) {
		this.outputDimensionality = outputDimensionality;
	}

	public int getMaxIterations() {
		return maxIterations;
	}

	public void setMaxIterations(int maxIterations) {
		this.maxIterations = maxIterations;
	}

	public boolean isPrintProgress() {
		return printProgress;
	}

	public void setPrintProgress(boolean printProgress) {
		this.printProgress = printProgress;
	}
}
