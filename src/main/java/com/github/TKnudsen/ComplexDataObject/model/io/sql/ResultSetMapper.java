package com.github.TKnudsen.ComplexDataObject.model.io.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;

/**
 * <p>
 * Final utility class that consumes a ResultSet together with pre-built
 * column extractors from ResultSetInterpreter and materializes the rows
 * either as a list of LinkedHashMaps or directly as ComplexDataObjects,
 * using an "ID" column when present to assign object identifiers.
 * </p>
 */
public final class ResultSetMapper {

	/**
	 * Converts a ResultSet into a list of LinkedHashMaps<String,Object>. Keeps the
	 * generic representation (slow but flexible).
	 */
	public static List<LinkedHashMap<String, Object>> asListOfMaps(ResultSet rs, String[] columnNames,
			ResultSetInterpreter.ColumnExtractor[] extractors) throws SQLException {

		List<LinkedHashMap<String, Object>> list = new ArrayList<>();
		final int len = columnNames.length;

		while (rs.next()) {
			LinkedHashMap<String, Object> map = new LinkedHashMap<>(len * 4 / 3);
			for (int i = 0; i < len; i++)
				map.put(columnNames[i], extractors[i].extract(rs, i + 1));

			list.add(map);
		}
		return list;
	}

	/**
	 * Converts a ResultSet directly into ComplexDataObjects, skipping LinkedHashMap
	 * creation. If an "ID" column exists, its value is used as the object ID.
	 */
	public static List<ComplexDataObject> asListOfComplexObjects(ResultSet rs, String[] columnNames,
			ResultSetInterpreter.ColumnExtractor[] extractors) throws SQLException {

		List<ComplexDataObject> list = new ArrayList<>(1024);
		final int len = columnNames.length;

		// Try to find the "ID" column index once (case-insensitive)
		Integer idIndex = null;
		for (int i = 0; i < len; i++)
			if ("ID".equalsIgnoreCase(columnNames[i])) {
				idIndex = i;
				break;
			}

		while (rs.next()) {
			Long idValue = null;

			// Only attempt to extract ID if column exists
			if (idIndex != null)
				try {
					// Use JDBC findColumn + getLong for consistency with your version
					int idCol = rs.findColumn(columnNames[idIndex]);
					long val = rs.getLong(idCol);
					if (!rs.wasNull()) {
						idValue = val;
					}
				} catch (SQLException ignore) {
					// ID column not found or not readable, skip silently
				}

			// Construct ComplexDataObject with or without ID
			ComplexDataObject cdo = (idValue == null) ? new ComplexDataObject(len)
					: new ComplexDataObject(len, idValue);

			// Add all attributes directly (including ID column if present)
			for (int i = 0; i < len; i++) {
				String attr = columnNames[i];
				Object val = extractors[i].extract(rs, i + 1);
				cdo.add(attr, val);
			}

			list.add(cdo);
		}

		return list;
	}
}
