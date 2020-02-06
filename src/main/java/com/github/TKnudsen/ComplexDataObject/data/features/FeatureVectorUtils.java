package com.github.TKnudsen.ComplexDataObject.data.features;

import java.util.Set;

import com.github.TKnudsen.ComplexDataObject.data.interfaces.IFeatureVectorObject;

public class FeatureVectorUtils {
	/**
	 * Create a sensible string representation of the given feature vector
	 * 
	 * @param featureVector The feature vector
	 * @return The string
	 */
	public static String createString(IFeatureVectorObject<?, ?> featureVector) {
		StringBuilder sb = new StringBuilder();
		sb.append("FeatureVector[(");
		for (int i = 0; i < featureVector.sizeOfFeatures(); i++) {
			if (i > 0) {
				sb.append(", ");
			}
			Feature<?> feature = featureVector.getFeature(i);
			sb.append(feature.getFeatureName());
			sb.append("=");
			sb.append(feature.getFeatureValue());
		}
		sb.append(")");
		Set<String> attributeKeySet = featureVector.keySet();
		if (!attributeKeySet.isEmpty()) {
			sb.append(", {");
			int counter = 0;
			for (String attributeKey : attributeKeySet) {
				if (counter > 0) {
					sb.append(", ");
				}
				sb.append(attributeKey);
				sb.append("=");
				sb.append(featureVector.getAttribute(attributeKey));
				counter++;
			}
			sb.append("}");
		}
		sb.append("]");
		return sb.toString();
	}

}
