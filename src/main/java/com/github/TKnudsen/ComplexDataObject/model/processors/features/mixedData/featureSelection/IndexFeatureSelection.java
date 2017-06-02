package com.github.TKnudsen.ComplexDataObject.model.processors.features.mixedData.featureSelection;

import java.util.Collections;
import java.util.List;

import com.github.TKnudsen.ComplexDataObject.data.features.mixedData.MixedDataFeatureContainer;
import com.github.TKnudsen.ComplexDataObject.data.features.mixedData.MixedDataFeatureVector;
import com.github.TKnudsen.ComplexDataObject.model.processors.complexDataObject.DataProcessingCategory;
import com.github.TKnudsen.ComplexDataObject.model.processors.features.mixedData.IMixedDataFeatureVectorProcessor;

/**
 * @author Christian Ritter
 *
 */
public class IndexFeatureSelection implements IMixedDataFeatureVectorProcessor {

	private List<Integer> indizesToRemove;

	public IndexFeatureSelection(List<Integer> indizesToRemove) {
		this.indizesToRemove = indizesToRemove;
	}

	@Override
	public DataProcessingCategory getPreprocessingCategory() {
		return DataProcessingCategory.DATA_REDUCTION;
	}

	@Override
	public void process(List<MixedDataFeatureVector> data) {
		process(new MixedDataFeatureContainer(data));
	}

	@Override
	public void process(MixedDataFeatureContainer container) {
		Collections.sort(indizesToRemove);
		for (MixedDataFeatureVector fv : container) {
			int count = 0;
			for (Integer i : indizesToRemove) {
				fv.removeFeature(i - count);
				count++;
			}
		}
	}

}
