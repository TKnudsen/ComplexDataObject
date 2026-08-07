package com.github.TKnudsen.ComplexDataObject.model.io.sql;

import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedHashMap;

/**
 * <p>
 * Efficient JDBC ResultSet interpreter with pre-compiled column extractors.
 * Single entry point for building per-column readers.
 * </p>
 */
public class ResultSetInterpreter {

	@FunctionalInterface
	public interface ColumnExtractor {
		Object extract(ResultSet rs, int index) throws SQLException;
	}

	/**
	 * Combines schema (column names, types) with extractor functions.
	 */
	public static class ResultSetDescriptor {
		private final String[] columnNames;
		private final int[] columnTypes;
		private final ColumnExtractor[] extractors;

		public ResultSetDescriptor(String[] columnNames, int[] columnTypes, ColumnExtractor[] extractors) {
			this.columnNames = columnNames;
			this.columnTypes = columnTypes;
			this.extractors = extractors;
		}

		public String[] getColumnNames() {
			return columnNames;
		}

		public int[] getColumnTypes() {
			return columnTypes;
		}

		public ColumnExtractor[] getExtractors() {
			return extractors;
		}
	}

	/**
	 * Creates all extractors in one step directly from a ResultSet. This is the
	 * most efficient and convenient entry point.
	 */
	public static ResultSetDescriptor createExtractors(ResultSet rs, boolean doubleAsFloat) throws SQLException {
		ResultSetMetaData meta = rs.getMetaData();
		int len = meta.getColumnCount();

		String[] columnNames = new String[len];
		int[] columnTypes = new int[len];
		ColumnExtractor[] extractors = new ColumnExtractor[len];

		for (int i = 0; i < len; i++) {
			columnNames[i] = meta.getColumnLabel(i + 1);
			columnTypes[i] = meta.getColumnType(i + 1);

			final int type = columnTypes[i];
			final int nullable = meta.isNullable(i + 1);
			final boolean mayBeNull = (nullable != ResultSetMetaData.columnNoNulls);

			switch (type) {
			case Types.INTEGER:
				extractors[i] = (rs2, col) -> mayBeNull ? nullableNum(rs2, col, Integer.class) : rs2.getInt(col);
				break;

			case Types.BIGINT:
				extractors[i] = (rs2, col) -> mayBeNull ? nullableNum(rs2, col, Long.class) : rs2.getLong(col);
				break;

			case Types.TINYINT:
			case Types.SMALLINT:
				extractors[i] = (rs2, col) -> mayBeNull ? nullableNum(rs2, col, Short.class) : rs2.getShort(col);
				break;

			case Types.REAL:
			case Types.FLOAT:
				extractors[i] = (rs2, col) -> mayBeNull ? nullableNum(rs2, col, Float.class) : rs2.getFloat(col);
				break;

			case Types.DOUBLE:
				if (!mayBeNull) {
					extractors[i] = (rs2, col) -> {
						double v = rs2.getDouble(col);
						return doubleAsFloat ? Float.valueOf((float) v) : Double.valueOf(v);
					};
				} else {
					extractors[i] = (rs2, col) -> {
						Object o = rs2.getObject(col);
						if (o == null)
							return null;
						double v = ((Number) o).doubleValue();
						return doubleAsFloat ? (float) v : v;
					};
				}
				break;

			case Types.DECIMAL:
			case Types.NUMERIC:
				extractors[i] = (rs2, col) -> rs2.getBigDecimal(col);
				break;

			case Types.BOOLEAN:
			case Types.BIT:
				extractors[i] = (rs2, col) -> rs2.getObject(col, Boolean.class);
				break;

			case Types.VARCHAR:
			case Types.NVARCHAR:
			case Types.LONGVARCHAR:
			case Types.LONGNVARCHAR:
				extractors[i] = ResultSet::getString;
				break;

			case Types.DATE:// speedup could be to return a long, but this requires a contract
				extractors[i] = (rs2, col) -> {
					java.sql.Date d = rs2.getDate(col);
					return (d == null) ? null : new java.util.Date(d.getTime());
				};
				break;

			case Types.TIMESTAMP:
				extractors[i] = ResultSet::getTimestamp;
				break;

			case Types.BINARY:
			case Types.VARBINARY:
			case Types.LONGVARBINARY:
				extractors[i] = (rs2, col) -> {
					byte[] bytes = rs2.getBytes(col);
					return bytes == null ? null : new String(bytes, StandardCharsets.UTF_8);
				};
				break;

			default:
				extractors[i] = ResultSet::getObject;
				break;
			}
		}

		return new ResultSetDescriptor(columnNames, columnTypes, extractors);
	}

	private static Object nullableNum(ResultSet rs, int col, Class<?> type) throws SQLException {
		if (type == Integer.class) {
			int v = rs.getInt(col);
			return rs.wasNull() ? null : v;
		}
		if (type == Long.class) {
			long v = rs.getLong(col);
			return rs.wasNull() ? null : v;
		}
		if (type == Short.class) {
			short v = rs.getShort(col);
			return rs.wasNull() ? null : v;
		}
		if (type == Float.class) {
			float v = rs.getFloat(col);
			return rs.wasNull() ? null : v;
		}
		// handles any unanticipated type,which should never happen since nullableNum is
		// only called for Integer, Long, Short, and Float
		Object o = rs.getObject(col);
		return o;
	}

	/**
	 * Converts current ResultSet row to a LinkedHashMap using pre-built extractors.
	 */
	public static LinkedHashMap<String, Object> interpreteResultSetRow(ResultSet rs, String[] columnNames,
			ColumnExtractor[] extractors) throws SQLException {
		int len = columnNames.length;
		LinkedHashMap<String, Object> map = new LinkedHashMap<>(len * 4 / 3);
		for (int i = 0; i < len; i++)
			map.put(columnNames[i], extractors[i].extract(rs, i + 1));
		return map;
	}

}
