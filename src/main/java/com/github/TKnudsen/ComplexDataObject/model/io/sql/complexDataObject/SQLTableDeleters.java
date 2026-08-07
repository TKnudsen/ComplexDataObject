package com.github.TKnudsen.ComplexDataObject.model.io.sql.complexDataObject;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.model.io.sql.SQLTableDeleter;
import com.github.TKnudsen.ComplexDataObject.model.io.sql.SQLTableStatistics;
import com.github.TKnudsen.ComplexDataObject.model.io.sql.SQLUtils;

/**
 * <p>
 * Removes duplicate rows from a SQL table that would otherwise conflict with
 * the primary-key values of a {@link ComplexDataContainer}, which is useful
 * prior to INSERT operations on PostgreSQL. Offers an efficient batched
 * variant that deletes all duplicates with a single WHERE-IN clause, plus a
 * deprecated one-by-one fallback.
 * </p>
 */
public class SQLTableDeleters {

	/**
	 * Identifies the primary key attributes of a table, checks for the existence of
	 * duplicates and duplicate rows. Relevant prior to INSERT calls when using
	 * postgresql.
	 * 
	 * The revised version creates one WHERE clause for all deletions instead of
	 * iterating over the delete-per-row loop.
	 * 
	 * @param conn
	 * @param schema
	 * @param tableName
	 * @param dataContainer
	 * @param sysout
	 * @throws SQLException
	 */
	public static int removeDuplicateRows(Connection conn, String schema, String tableName,
			ComplexDataContainer dataContainer, boolean sysout) throws SQLException {

		if (sysout)
			System.out.print("SQLTableDeleters.removeDuplicateRows... ");

		long l = System.currentTimeMillis();

		List<String> primaryKeysForTable = SQLTableStatistics.primaryKeysForTable(conn, schema, tableName);

		if (sysout)
			System.out.print("... gathering primary keys done in " + (System.currentTimeMillis() - l) + " ms... ");

		Map<ComplexDataObject, Boolean> rowsExist = SQLUtils.rowsExist(conn, schema, tableName, primaryKeysForTable,
				dataContainer);

		if (sysout)
			System.out.print("... duplicate check done in " + (System.currentTimeMillis() - l) + " ms. ");
		l = System.currentTimeMillis();

		// Collect tuples to delete
		List<List<String>> tuplesToDelete = new ArrayList<>();
		for (ComplexDataObject cdo : dataContainer) {
			if (rowsExist != null && Boolean.TRUE.equals(rowsExist.get(cdo))) {
				List<String> tuple = new ArrayList<>();
				if (primaryKeysForTable != null)
					for (String pk : primaryKeysForTable) {
						tuple.add(cdo.getAttribute(pk) != null ? cdo.getAttribute(pk).toString() : null);
					}
				tuplesToDelete.add(tuple);
			}
		}

		if (tuplesToDelete.isEmpty()) {
			if (sysout)
				System.out.println("... no duplicates found.");
			return 0;
		}

		// Build the big WHERE clause
		String whereClause = primaryKeysForTable != null ? buildWhereClause(primaryKeysForTable, tuplesToDelete) : "";
		String sql = String.format("DELETE FROM \"%s\".\"%s\" %s", schema, tableName, whereClause);

		try (Statement stmt = conn.createStatement()) {
			int removed = stmt.executeUpdate(sql);
			if (sysout)
				System.out.println("... removal of " + removed + " duplicate(s) done in "
						+ (System.currentTimeMillis() - l) + " ms");
			return removed;
		}
	}

	/**
	 * Builds a SQL WHERE clause for dynamic PK tuples.
	 */
	private static String buildWhereClause(List<String> columns, List<List<String>> tuples) {
		StringJoiner colJoiner = new StringJoiner(", ", "(", ")");
		columns.forEach(col -> colJoiner.add("\"" + col + "\""));

		StringJoiner valJoiner = new StringJoiner(", ", "WHERE " + colJoiner + " IN (", ")");
		for (List<String> tuple : tuples) {
			StringJoiner t = new StringJoiner(", ", "(", ")");
			for (String v : tuple) {
				t.add(v == null ? "NULL" : "'" + v.replace("'", "''") + "'");
			}
			valJoiner.add(t.toString());
		}
		return valJoiner.toString();
	}

	/**
	 * 
	 * Identifies the primary key attributes of a table, checks for the existence of
	 * duplicates and duplicate rows. Relevant prior to INSERT calls when using
	 * postgresql.
	 * 
	 * @deprecated legacy code. can go away. if the faster removeDuplicateRows has
	 *             proven its robust use.
	 * 
	 * @param conn
	 * @param schema
	 * @param tableName
	 * @param dataContainer
	 * @throws SQLException
	 */
	public static int removeDuplicateRowsOneByOne(Connection conn, String schema, String tableName,
			ComplexDataContainer dataContainer) throws SQLException {

		System.out.print("SQLTableDeleters.removeDuplicateRows... ");

		long l = System.currentTimeMillis();

		int removals = 0;

		List<String> primaryKeysForTable = SQLTableStatistics.primaryKeysForTable(conn, schema, tableName);

		System.out.print("... gathering primary keys done in " + (System.currentTimeMillis() - l) + " ms... ");

		Map<ComplexDataObject, Boolean> rowsExist = SQLUtils.rowsExist(conn, schema, tableName, primaryKeysForTable,
				dataContainer);

		System.out.print("... duplicate check done in " + (System.currentTimeMillis() - l) + " ms. ");
		l = System.currentTimeMillis();

		for (ComplexDataObject cdo : dataContainer) {
			if (rowsExist != null && rowsExist.get(cdo)) {
				List<String> columns = new ArrayList<>();
				List<String> queryObjects = new ArrayList<>();
				if (primaryKeysForTable != null)
					for (String a : primaryKeysForTable) {
						columns.add(a);
						queryObjects.add(cdo.getAttribute(a) != null ? cdo.getAttribute(a).toString() : null);
					}
				SQLTableDeleter.deleteTableRow(conn, schema, tableName, columns, queryObjects, false);
				removals++;
				System.out.print(".");
			}
		}

		System.out.println("... removal of " + removals + " duplicate(s) done in another "
				+ (System.currentTimeMillis() - l) + " ms");

		return removals;
	}

}
