package com.github.TKnudsen.ComplexDataObject.model.io.sql.complexDataObject;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.model.io.sql.SQLTableDeleter;
import com.github.TKnudsen.ComplexDataObject.model.io.sql.SQLUtils;

public class SQLTableDeleters {

	/**
	 * Identifies the primary key attributes of a table, checks for the existence of
	 * duplicates and duplicate rows. Relevant prior to INSERT calls when using
	 * postgresql.
	 * 
	 * @param conn
	 * @param schema
	 * @param tableName
	 * @param dataContainer
	 * @throws SQLException
	 */
	public static int removeDuplicateRows(Connection conn, String schema, String tableName,
			ComplexDataContainer dataContainer) throws SQLException {

		System.out.print("SQLTableDeleters.removeDuplicateRows... ");

		long l = System.currentTimeMillis();

		List<String> pks = SQLUtils.primaryKeysForTable(conn, schema, tableName);

		Map<ComplexDataObject, Boolean> rowsExist = SQLUtils.rowsExist(conn, schema, tableName, pks, dataContainer);

		System.out.print("duplicate check done in " + (System.currentTimeMillis() - l) + " ms... ");
		l = System.currentTimeMillis();

		int removals = 0;
		for (ComplexDataObject cdo : dataContainer) {
			if (rowsExist != null && rowsExist.get(cdo)) {
				List<String> columns = new ArrayList<>();
				List<String> queryObjects = new ArrayList<>();
				for (String a : pks) {
					columns.add(a);
					queryObjects.add(cdo.getAttribute(a) != null ? cdo.getAttribute(a).toString() : null);
				}
				SQLTableDeleter.deleteTableRow(conn, schema, tableName, columns, queryObjects, true);
				removals++;
				System.out.print(".");
			}
		}
		System.out.println();
		System.out.println("SQLTableDeleters.removeDuplicateRows... removal of " + removals
				+ " duplicates done in another " + (System.currentTimeMillis() - l) + " ms");

		return removals;
	}

}
