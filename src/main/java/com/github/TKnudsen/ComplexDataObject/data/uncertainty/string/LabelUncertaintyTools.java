package com.github.TKnudsen.ComplexDataObject.data.uncertainty.string;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;

/**
 * <p>
 * Static helper methods for LabelUncertainty instances, currently offering a
 * method to merge a collection of LabelUncertainty objects into one by
 * averaging their per-label value distributions.
 * </p>
 *
 * @version 1.01
 * @since 2015
 */
public class LabelUncertaintyTools {

	/**
	 * merges a series of LabelUncertaintys.
	 * 
	 * @param labelUncertainties
	 * @return
	 */
	public static LabelUncertainty mergeLabelUncertainties(Collection<LabelUncertainty> labelUncertainties) {
		if (labelUncertainties == null)
			return null;

		Map<String, List<Double>> valueDistributions = new LinkedHashMap<>();

		for (LabelUncertainty labelUncertainty : labelUncertainties) {
			if (labelUncertainty == null)
				continue;

			for (String label : labelUncertainty.getLabelSet()) {
				if (valueDistributions.get(label) == null)
					valueDistributions.put(label, new ArrayList<>());

				valueDistributions.get(label).add(labelUncertainty.getValueDistribution().get(label));
			}
		}

		Map<String, Double> valueDistribution = new LinkedHashMap<>();

		for (String label : valueDistributions.keySet())
			valueDistribution.put(label, MathFunctions.getMean(valueDistributions.get(label)));

		return new LabelUncertainty(valueDistribution);
	}
}
