package com.github.TKnudsen.ComplexDataObject.model.processors;

import java.util.List;
import java.util.Random;

import com.github.TKnudsen.ComplexDataObject.model.processors.complexDataObject.DataProcessingCategory;

/**
 * <p>
 * Applies sampling of a given collection of elements. Uses Random with a fixed
 * seed for replication.
 * </p>
 *
 * <p>
 * Copyright: Copyright (c) 2020
 * </p>
 *
 * @author Juergen Bernard
 * @version 1.01
 * 
 */
public class SamplingDataProcessor<T> implements IDataProcessor<T> {

	private final int seed;
	private final int targetSize;

	public SamplingDataProcessor(int targetSize) {
		this(targetSize, 42);
	}

	public SamplingDataProcessor(int targetSize, int seed) {
		this.targetSize = targetSize;
		this.seed = seed;
	}

	@Override
	public void process(List<T> data) {
		Random random = new Random(seed);

		while (data.size() > targetSize) {
			int index = random.nextInt(data.size());
			data.remove(index);
		}
	}

	@Override
	public DataProcessingCategory getPreprocessingCategory() {
		return DataProcessingCategory.DATA_REDUCTION;
	}

}
