package com.github.TKnudsen.ComplexDataObject.model.io.sql;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.github.TKnudsen.ComplexDataObject.data.DataSchemaEntry;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.PrimaryKeyDataContainer;

public class SQLUtils {

	public static String columnQuote = "`";
	public static String valueQuote = "'";

	/**
	 * creates a new database / schema if not exists
	 * 
	 * @param conn
	 * @param database
	 * @return
	 */
	public static boolean createDatabase(Connection conn, String database) {

		Statement stmt = null;

		System.out.print("SQLUtils.createSchema: Schema " + database + "...");
		try {
			stmt = conn.createStatement();
			String sql = "CREATE DATABASE IF NOT EXISTS " + database;
			stmt.executeUpdate(sql);
			System.out.println("done");
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}

		try {
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * @param conn
	 * @param schema
	 * @param tableName
	 * @return
	 * @throws SQLException
	 */
	public static boolean tableExists(Connection conn, String schema, String tableName) throws SQLException {

		Objects.requireNonNull(schema);
		Objects.requireNonNull(tableName);

		DatabaseMetaData dbm = conn.getMetaData();

		ResultSet resultSet = dbm.getTables(null, schema, tableName, null);

		try {
			while (resultSet.next()) {
				String cat = resultSet.getString("TABLE_CAT");
				String schem = resultSet.getString("TABLE_SCHEM");
				String name = resultSet.getString("TABLE_NAME");
				// String type = resultSet.getString("TABLE_TYPE");
				// String remarks = resultSet.getString("REMARKS");

				if (schema.toLowerCase().equals(cat)
						|| schema.toLowerCase().equals(schem) && tableName.toLowerCase().equals(name))
					return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (resultSet != null)
				resultSet.close();
		}

		return false;
	}

	public static String classToMySQLType(Class<?> javaClass, boolean primaryKey) {
		return classToMySQLType(javaClass, null, primaryKey);
	}

	public static String classToMySQLType(Class<?> javaClass, Collection<Object> values, boolean primaryKey) {
		Objects.requireNonNull(javaClass);

		String c = javaClass.getSimpleName().toLowerCase();
		String sql = null;

		switch (c) {
		case "boolean":
			sql = "BIT(1)";
			break;
		case "byte":
			sql = "BIT(8)";
			break;
		case "int":
			sql = "INT";
			break;
		case "integer":
			sql = "INT";
			break;
		case "long":
			sql = "BIGINT(19)";
			break;
		case "date":
			sql = "DATE";
			break;
		case "float":
			sql = "FLOAT";
			break;
		case "double":
			sql = "DOUBLE";
			break;
		case "bigdecimal":
			sql = "DECIMAL";
			break;
		case "string":
			if (values == null)
				sql = "BLOB";
			else {
				int maxCount = 0;
				for (Object v : values)
					if (v != null)
						maxCount = Math.max(maxCount, v.toString().length());
				if (maxCount < 128)
					sql = "VARCHAR (" + (int) (maxCount * 1.33) + ")";
				else
					sql = "BLOB";
			}
			break;
		default:
			throw new IllegalArgumentException(
					"Unable to suggest MySQL tye for object of class" + javaClass.getSimpleName());
		}

		if (primaryKey && sql.equals("BLOB"))
			return "VARCHAR (255)";
		else
			return sql;
	}

	/**
	 * iterates a ResultSet and converts it to ComplexDataObjects using a given
	 * target attribute characterization.
	 * 
	 * @param resultSet
	 * @param targetAttributeCharacterization
	 * @return
	 * @throws SQLException
	 */
	public static List<ComplexDataObject> interpreteResultSet(ResultSet resultSet,
			Map<String, Class<?>> targetAttributeCharacterization) throws SQLException {

		if (targetAttributeCharacterization == null || targetAttributeCharacterization.isEmpty())
			throw new IllegalArgumentException(
					"MySQLUtils.interpreteResultSet: targetAttributeCharacterization was null/empty. use interpreteResultSet without the characterization in such a case.");

		List<ComplexDataObject> result = new ArrayList<ComplexDataObject>();

		while (resultSet.next()) {
			Long id = null;
			try {
				resultSet.findColumn("ID");
				id = resultSet.getLong("ID");
			} catch (SQLException sqlex) {
			}

			ComplexDataObject cdo = (id == null) ? new ComplexDataObject() : new ComplexDataObject(id);

			for (String attribute : targetAttributeCharacterization.keySet()) {
				resultSet.findColumn(attribute);
				try {
					cdo.add(attribute, SQLUtils.mySQLTypeToJavaClass(attribute,
							targetAttributeCharacterization.get(attribute), resultSet));
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}

			result.add(cdo);
		}

		return result;
	}

	/**
	 * interprets a ResultSet without any given schema. Simply uses the information
	 * provided with the ResultSet. Inspired by the ResultSetSerializer class in
	 * jackson-databind.
	 * 
	 * @param resultSet
	 * @return
	 * @throws SQLException
	 */
	public static List<ComplexDataObject> interpreteResultSet(ResultSet resultSet) throws SQLException {

		List<ComplexDataObject> result = new ArrayList<ComplexDataObject>();

		try {
			ResultSetMetaData rsmd = resultSet.getMetaData();
			String[] columnNames = new String[rsmd.getColumnCount()];
			int[] columnTypes = new int[rsmd.getColumnCount()];

			for (int i = 0; i < columnNames.length; i++) {
				columnNames[i] = rsmd.getColumnLabel(i + 1);
				columnTypes[i] = rsmd.getColumnType(i + 1);
			}

			while (resultSet.next()) {
				Long id = null;
				try {
					resultSet.findColumn("ID");
					id = resultSet.getLong("ID");
				} catch (SQLException sqlex) {
				}

				ComplexDataObject cdo = (id == null) ? new ComplexDataObject() : new ComplexDataObject(id);

				LinkedHashMap<String, Object> map = interpreteResultSetRow(resultSet, columnNames, columnTypes);
				for (String attribute : map.keySet())
					if (attribute.equals("ID"))
						continue;
					else
						cdo.add(attribute, map.get(attribute));

				result.add(cdo);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * does not apply next(), e.g., iteration must be triggered externally.
	 * 
	 * @param resultSet
	 * @param columnNames
	 * @return
	 * @throws SQLException
	 */
	public static LinkedHashMap<String, Object> interpreteResultSetRow(ResultSet resultSet, String[] columnNames,
			int[] columnTypes) throws SQLException {
		LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();

		boolean b;
		long l;
		double d;

		for (int i = 0; i < columnNames.length; i++) {

			String attribute = columnNames[i];
			
			switch (columnTypes[i]) {

			case Types.INTEGER:
				l = resultSet.getInt(i + 1);
				if (resultSet.wasNull()) {
					map.put(attribute, null);
				} else {
					map.put(attribute, l);
				}
				break;

			case Types.BIGINT:
				l = resultSet.getLong(i + 1);
				if (resultSet.wasNull()) {
					map.put(attribute, null);
				} else {
					map.put(attribute, l);
				}
				break;

			case Types.DECIMAL:
			case Types.NUMERIC:
				map.put(attribute, resultSet.getBigDecimal(i + 1));
				break;

			case Types.FLOAT:
			case Types.REAL:
			case Types.DOUBLE:
				d = resultSet.getDouble(i + 1);
				if (resultSet.wasNull()) {
					map.put(attribute, null);
				} else {
					map.put(attribute, d);
				}
				break;

			case Types.NVARCHAR:
			case Types.VARCHAR:
			case Types.LONGNVARCHAR:
			case Types.LONGVARCHAR:
				map.put(attribute, resultSet.getString(i + 1));
				break;

			case Types.BOOLEAN:
			case Types.BIT:
				b = resultSet.getBoolean(i + 1);
				if (resultSet.wasNull()) {
					map.put(attribute, null);
				} else {
					map.put(attribute, b);
				}
				break;

			case Types.BINARY:
			case Types.VARBINARY:
			case Types.LONGVARBINARY:
				byte[] bytes = resultSet.getBytes(i + 1);
				if (bytes == null) {
					map.put(attribute, null);
					break;
				}
				try {
					String text = new String(bytes, "UTF-8");
					map.put(attribute, text);
					break;
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				map.put(attribute, resultSet.getBytes(i + 1));
				break;

			case Types.TINYINT:
			case Types.SMALLINT:
				l = resultSet.getShort(i + 1);
				if (resultSet.wasNull()) {
					map.put(attribute, null);
				} else {
					map.put(attribute, l);
				}
				break;

			case Types.DATE:
				java.sql.Date date = resultSet.getDate(i + 1);
				map.put(attribute, ((date == null) ? null : new Date(date.getTime())));
				break;

			case Types.TIMESTAMP:
				map.put(attribute, resultSet.getTime(i + 1));
				break;

			case Types.BLOB:
				Blob blob = resultSet.getBlob(i);
				map.put(attribute, blob.getBinaryStream());
				blob.free();
				break;

			case Types.CLOB:
				Clob clob = resultSet.getClob(i);
				map.put(attribute, clob.getCharacterStream());
				clob.free();
				break;

			case Types.ARRAY:
				throw new RuntimeException("SQLUtils.interpreteResultSet: not yet implemented for SQL type ARRAY");

			case Types.STRUCT:
				throw new RuntimeException("SQLUtils.interpreteResultSet: not yet implemented for SQL type STRUCT");

			case Types.DISTINCT:
				throw new RuntimeException("SQLUtils.interpreteResultSet: not yet implemented for SQL type DISTINCT");

			case Types.REF:
				throw new RuntimeException("SQLUtils.interpreteResultSet: not yet implemented for SQL type REF");

			case Types.JAVA_OBJECT:
			default:
				map.put(attribute, resultSet.getObject(i + 1));
				break;
			}
		}

		return map;
	}

	public static Object mySQLTypeToJavaClass(DataSchemaEntry<?> entry, ResultSet resultSet) throws SQLException {
		return mySQLTypeToJavaClass(entry.getName(), entry.getType(), resultSet);
	}

	@SuppressWarnings("unchecked")
	public static <T> T mySQLTypeToJavaClass(String attributeName, Class<T> attributeType, ResultSet resultSet)
			throws SQLException {
		Objects.requireNonNull(attributeName);
		Objects.requireNonNull(attributeType);
		Objects.requireNonNull(resultSet);

		if (attributeType.equals(String.class))
			return (T) parseString(attributeName, resultSet);
		if (attributeType.equals(Boolean.class))
			return (T) parseBoolean(attributeName, resultSet);
		if (attributeType.equals(Integer.class))
			return (T) parseInt(attributeName, resultSet);
		if (attributeType.equals(Float.class))
			return (T) parseFloat(attributeName, resultSet);
		if (attributeType.equals(Double.class))
			return (T) parseDouble(attributeName, resultSet);
		if (attributeType.equals(Date.class))
			return (T) parseDate(attributeName, resultSet);

		throw new IllegalArgumentException("SQLUtils.mySQLTypeToJavaClass: unable to cast MySQL class type: "
				+ attributeType + ", attribute name:" + attributeName);
	}

	private static String parseString(String attributeName, ResultSet resultSet) throws SQLException {
		String s = resultSet.getString(attributeName);
		if (resultSet.wasNull())
			return (String) null;
		else
			return s;
	}

	private static Boolean parseBoolean(String attributeName, ResultSet resultSet) throws SQLException {
		Boolean bool = resultSet.getBoolean(attributeName);
		if (resultSet.wasNull())
			return (Boolean) null;
		else
			return bool;
	}

	private static Integer parseInt(String attributeName, ResultSet resultSet) throws SQLException {
		int integer = resultSet.getInt(attributeName);// getBigDecimal(attributeName);
		if (resultSet.wasNull())
			return (Integer) null;
		else
			return (Integer) (integer);
	}

	private static Float parseFloat(String attributeName, ResultSet resultSet) throws SQLException {
		BigDecimal bigDecimalInt = resultSet.getBigDecimal(attributeName);
		if (resultSet.wasNull())
			return (Float) null;
		else
			return (Float) ((bigDecimalInt == null) ? null : bigDecimalInt.floatValue());
	}

	private static Double parseDouble(String attributeName, ResultSet resultSet) throws SQLException {
		BigDecimal bigDecimalD = resultSet.getBigDecimal(attributeName);
		if (resultSet.wasNull())
			return (Double) null;
		else
			return (Double) ((bigDecimalD == null) ? null : bigDecimalD.doubleValue());
	}

	private static Date parseDate(String attributeName, ResultSet resultSet) throws SQLException {
		java.sql.Date date = resultSet.getDate(attributeName);
		if (resultSet.wasNull())
			return (Date) null;
		else
			return (Date) ((date == null) ? null : new Date(date.getTime()));
	}

	/**
	 * creates a list of key value pairs (attributes and values) for
	 * ComplexDataObjects.
	 * 
	 * Part of the generalization process.
	 * 
	 * @param cdos
	 * @param attributeList attributes that will be ignored
	 * @param blackList     determines if the attributeList is a black list
	 *                      (attributes will be ignored) or a whiteLit (only those
	 *                      attributes will be considered)
	 * @return
	 */
	public static List<LinkedHashMap<String, Object>> createKeyValuePairs(Iterable<ComplexDataObject> cdos,
			Set<String> attributeList, boolean blackList) {
		List<LinkedHashMap<String, Object>> listOfMapWithKeyValuePairs = new ArrayList<LinkedHashMap<String, Object>>();

		for (ComplexDataObject cdo : cdos)
			listOfMapWithKeyValuePairs.add(createKeyValuePairs(cdo, attributeList, blackList));

		return listOfMapWithKeyValuePairs;
	}

	/**
	 * creates a list of key value pairs (attributes and values) for a container
	 * with ComplexDataObjects.
	 * 
	 * Part of the generalization process.
	 * 
	 * @param container
	 * @return
	 */
	public static List<LinkedHashMap<String, Object>> createKeyValuePairs(ComplexDataContainer container) {
		List<LinkedHashMap<String, Object>> listOfMapWithKeyValuePairs = new ArrayList<LinkedHashMap<String, Object>>();

		SortedSet<String> whiteList = new TreeSet<>(container.getAttributeNames());

		for (ComplexDataObject cdo : container)
			listOfMapWithKeyValuePairs.add(createKeyValuePairs(cdo, whiteList, false));

		return listOfMapWithKeyValuePairs;
	}

	/**
	 * creates key value pairs (attributes and values) for a ComplexDataObject.
	 * 
	 * Part of the generalization process.
	 * 
	 * @param cdo
	 * @param attributeList attributes that will be ignored. can be null
	 * @param blackList     determines if the list is a black list (true) or a white
	 *                      list (false)
	 * @return
	 */
	public static LinkedHashMap<String, Object> createKeyValuePairs(ComplexDataObject cdo, Set<String> attributeList,
			boolean blackList) {

		LinkedHashMap<String, Object> keyValuePairs = new LinkedHashMap<>();
		keyValuePairs.put("ID", cdo.getID());

		Iterator<String> iterator = cdo.iterator();
		while (iterator.hasNext()) {
			String attribute = iterator.next();

			if (attributeList != null)
				if (blackList)
					if (attributeList.contains(attribute))
						continue;
					else {// let pass
					}
				else // white list
				if (!attributeList.contains(attribute))
					continue;
				else {// let pass
				}

			keyValuePairs.put(attribute, cdo.getAttribute(attribute));
		}

		return keyValuePairs;

	}

	/**
	 * creates key value pairs (attributes and values) for a ComplexDataObject.
	 * 
	 * Part of the generalization process.
	 * 
	 * @param cdo
	 * @return
	 */
	public static LinkedHashMap<String, Object> createKeyValuePairs(ComplexDataObject cdo) {

		LinkedHashMap<String, Object> keyValuePairs = new LinkedHashMap<>();
		keyValuePairs.put("ID", cdo.getID());

		Iterator<String> iterator = cdo.iterator();
		while (iterator.hasNext()) {
			String attribute = iterator.next();
			keyValuePairs.put(attribute, cdo.getAttribute(attribute));
		}

		return keyValuePairs;
	}

	/**
	 * can be applied when a data truncation error caused an SQLException. The
	 * method identifies the attribute that was too short, identifies the necessary
	 * size of the attribute/column (using the data container) and modifies the
	 * target SQL table, respectively.
	 * 
	 * It is recommended to apply recursion and try the upstream operation again.
	 * Sometimes it happens that multiple data truncations happen (different
	 * columns).
	 * 
	 * @param e             SQLException possibly including the message of the
	 *                      exception including "Data truncation: Data too long for
	 *                      column '"
	 * @param dataContainer data used to define the new size of the table column
	 * @param schema        target Schema
	 * @param tableName     target table name
	 * @return column name that is extended in the database
	 */
	public static String mitigateDataTruncationError(Connection conn, SQLException e,
			PrimaryKeyDataContainer dataContainer, String schema, String tableName) {
		return mitigateDataTruncationError(conn, e, createKeyValuePairs(dataContainer), schema, tableName);
	}

	/**
	 * can be applied when a data truncation error caused an SQLException. The
	 * method identifies the attribute that was too short, identifies the necessary
	 * size of the attribute/column (using the keyValuePairs) and modifies the
	 * target SQL table, respectively.
	 * 
	 * It is recommended to apply recursion and try the upstream operation again.
	 * Sometimes it happens that multiple data truncations happen (different
	 * columns).
	 * 
	 * @param e             SQLException possibly including the message of the
	 *                      exception including "Data truncation: Data too long for
	 *                      column '"
	 * @param keyValuePairs data used to define the new size of the table column
	 * @param schema        target Schema
	 * @param tableName     target table name
	 * @return column name that is extended in the database
	 */
	public static String mitigateDataTruncationError(Connection conn, SQLException e,
			List<LinkedHashMap<String, Object>> keyValuePairs, String schema, String tableName) {

		String column = e.getLocalizedMessage();

		if (column.contains("Data truncation: Data too long for column '")) {
			column = column.replace("Data truncation: Data too long for column '", "");
			column = column.substring(0, column.indexOf("' at row"));
			column = column.trim();

			int length = 20;
			for (LinkedHashMap<String, Object> row : keyValuePairs)
				if (row.get(column) != null)
					length = Math.max(length, row.get(column).toString().length());

			try {
				System.err.println(
						"SQLUtils: Data truncation (column size too short). Trying to modify column and upload the data again");
				SQLTableAlternator.modifyColumnVarCharSize(conn, schema.toString(), tableName, column, length, true);

				return column;
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}

		return null;
	}

}
