package com.github.TKnudsen.ComplexDataObject.model.transformations.dimensionReduction.features;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.github.TKnudsen.ComplexDataObject.data.distanceMatrix.DistanceMatrix;
import com.github.TKnudsen.ComplexDataObject.data.distanceMatrix.IDistanceMatrix;
import com.github.TKnudsen.ComplexDataObject.data.features.AbstractFeatureVector;
import com.github.TKnudsen.ComplexDataObject.data.features.Feature;
import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeatureVector;
import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeatureVectorFactory;
import com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.IDistanceMeasure;
import com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.Double.DoubleDistanceMeasure;
import com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.Double.EuclideanDistanceMeasure;

/**
 * <p>
 * Title: MultiDimensionalScaling
 * </p>
 * 
 * <p>
 * Description: Multi-dimensional Scaling (MDS) is a non-linear dimension
 * reduction algorithm proposed by Joseph B. KRUSKAL in 1964:
 * "Multidimensional scaling by optimizing goodness of fit to a nonmetric hypothesis"
 * In Psychometrika 29, 1 (1964).
 * 
 * The principal idea is to iteratively optimize the low-dimensional (often 2D)
 * positions of objects with respect to pairwise distances in the original space
 * and a stress-minimization function.
 * 
 * <p>
 * Copyright: Copyright (c) 2012-2017 J�rgen Bernard,
 * https://github.com/TKnudsen/ComplexDataObject
 * </p>
 * 
 * @author Juergen Bernard, Christian Ritter
 * @version 1.03
 */
public class MultiDimensionalScaling<O, X extends AbstractFeatureVector<O, ? extends Feature<O>>> extends DimensionalityReduction<O, X> {

	/**
	 * Euclidean distance metric for double[]
	 */
	private final DoubleDistanceMeasure doubleDistanceMeasure = new EuclideanDistanceMeasure();

	/**
	 * distance matrix, internally used for the low-dimensional manifold
	 * optimization
	 */
	private IDistanceMatrix<X> distanceMatrix;
	
	private double dmMin;
	private double dmMax;

	// TODO integrate alternative termination criterion
	private int maxIterations = 1000;

	protected boolean printProgress = false;
	
	private double normMinMax(double val) {
		return (val - dmMin) / (dmMax - dmMin);
	}

	public MultiDimensionalScaling(IDistanceMeasure<X> distanceMeasure, int outputDimensionality) {
		if (distanceMeasure == null)
			throw new IllegalArgumentException("Distance measure was null");

		this.distanceMeasure = distanceMeasure;
		this.outputDimensionality = outputDimensionality;
	}

	public MultiDimensionalScaling(IDistanceMatrix<X> distanceMatrix, int outputDimensionality) {
		if (distanceMatrix == null)
			throw new IllegalArgumentException("Multidimensional Scaling: distance matrix is null");

		this.distanceMatrix = distanceMatrix;
		this.outputDimensionality = outputDimensionality;
	}

	@Override
	public List<NumericalFeatureVector> transform(X input) {
		if (mapping != null && mapping.get(input) != null)
			return Arrays.asList(new NumericalFeatureVector[] { mapping.get(input) });
		else {
			List<X> lst = new ArrayList<>();
			lst.add(input);
			return transform(lst);
		}
	}

	@Override
	public List<NumericalFeatureVector> transform(List<X> inputObjects) {
		mapping = new HashMap<>();

		if (distanceMatrix == null)
			calculateDistanceMatrix(inputObjects);

		if (distanceMatrix == null)
			throw new IllegalArgumentException("Multidimensional Scaling: wrong input.");

		dmMin = distanceMatrix.getMinDistance();
		dmMax = distanceMatrix.getMaxDistance();
		
		// initialize points of the low-dimensional embedding
		List<double[]> lowDimensionalPoints = initializeLowDimensionalPoints(outputDimensionality, inputObjects.size());

		for (int iteration = 0; iteration < maxIterations; iteration++) {
			double[][] pointDistances = calculatePointDistances(lowDimensionalPoints);

			// calculate Kruskal's stress for every pair of objects
			if (printProgress) {
				double stress = calculateStress(distanceMatrix, pointDistances, inputObjects);
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
						newPointCoordinates[d] += lowDimensionalPoints.get(i)[d] + (pointDistances[i][j] - normMinMax(distanceMatrix.getDistance(inputObjects.get(i), inputObjects.get(j)))) * (lowDimensionalPoints.get(j)[d] - lowDimensionalPoints.get(i)[d]);
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
	private double calculateStress(IDistanceMatrix<X> distanceMatrix, double[][] pointDistances, List<X> inputObjects) {
		double stress = 0;
		for (int i = 0; i < pointDistances.length; i++) {
			for (int j = 0; j < pointDistances[0].length; j++) {
				double dij = pointDistances[i][j];
				double Dij = normMinMax(distanceMatrix.getDistance(inputObjects.get(i), inputObjects.get(j)));
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
	private void calculateDistanceMatrix(List<X> fvs) {
		distanceMatrix = new DistanceMatrix<>(fvs, distanceMeasure);
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
				double distance = doubleDistanceMeasure.getDistance(lowDimensionalPoints.get(x), lowDimensionalPoints.get(y));
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
