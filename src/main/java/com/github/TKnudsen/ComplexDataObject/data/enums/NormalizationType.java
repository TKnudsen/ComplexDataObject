package com.github.TKnudsen.ComplexDataObject.data.enums;

/**
 * <p>
 * type of normalization, e.g., needed in the feature creation
 * process.
 * </p>
 *
 * @version 1.0
 * @since 2016
 */

public enum NormalizationType {

	offsetTranslation("Offset Translation"), removeLinearTrends("Remove Linear Trends"), normalizeMinMax("Normalize Min/Max"), normalizeMinMaxGlobal("Normalize Min/Max (Global)"), normalizeMinMaxBinWise("Normalize Min/Max (binwise)"), amplitudeScaling("Amplitude Scaling"), none("none");

	private String type;

	private NormalizationType(String type) {
		this.type = type;
	}

	public String toString() {
		return type;
	}
}
