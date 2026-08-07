package com.github.TKnudsen.ComplexDataObject.model.io.sql;

/**
 * <p>
 * Simple immutable value holder describing a required column size fix,
 * pairing a column name with the minimum VARCHAR length that column needs to
 * be altered to.
 * </p>
 */
public class ColumnSizeFix {
	public final String columnName;
	public final int requiredLength;

	public ColumnSizeFix(String columnName, int requiredLength) {
		this.columnName = columnName;
		this.requiredLength = requiredLength;
	}
}