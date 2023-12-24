package com.github.TKnudsen.ComplexDataObject.model.io.sql;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

import com.github.TKnudsen.ComplexDataObject.data.DataSchemaEntry;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.PrimaryKeyDataContainer;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.BooleanParser;
import com.github.TKnudsen.ComplexDataObject.model.io.sql.SQLTableSelector.Order;

public class SQLUtils {

	public static String columnQuote = "`";
	public static String valueQuote = "'";

	private static BooleanParser booleanParser = new BooleanParser();

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

		boolean postgreSQL = PostgreSQL.isPostgreSQLConnection(conn);

		if (postgreSQL) {
			String sqlQuery = "			   SELECT EXISTS " + "(" + "	SELECT " + "	FROM pg_tables"
					+ "	WHERE schemaname = '" + schema + "'" + "	AND tablename = '" + tableName + "'" + ");";

			ResultSet resultSet = null;
			try {
				PreparedStatement preparedStatement = conn.prepareStatement(sqlQuery);
				resultSet = preparedStatement.executeQuery();
				List<ComplexDataObject> all = SQLUtils.interpreteResultSet(resultSet);
				for (ComplexDataObject cdo : all)
					return new BooleanParser().apply(cdo.getAttribute("exists"));

				System.out.println(all);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (resultSet != null)
					resultSet.close();
			}

		} else {
			DatabaseMetaData dbm = conn.getMetaData();

			ResultSet resultSet = dbm.getTables(null, schema, tableName, null);

			try {
				while (resultSet.next()) {
					String cat = resultSet.getString("TABLE_CAT");
					String schem = resultSet.getString("TABLE_SCHEM");
					String name = resultSet.getString("TABLE_NAME");
					// String type = resultSet.getString("TABLE_TYPE");
					// String remarks = resultSet.getString("REMARKS");

					if (!PostgreSQL.isPostgreSQLConnection(conn))
						if (schema.toLowerCase().equals(cat)
								|| schema.toLowerCase().equals(schem) && tableName.toLowerCase().equals(name))
							return true;
						else {
						}
					else
						return (schema.equals(schem) && tableName.equals(name));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (resultSet != null)
					resultSet.close();
			}
		}

		return false;
	}

	public static String classToSQLType(Class<?> javaClass, boolean primaryKey, boolean useFloatInsteadOfDouble,
			boolean postgreSQL) {
		return classToSQLType(javaClass, null, primaryKey, useFloatInsteadOfDouble, postgreSQL);
	}

	/**
	 * 
	 * @param javaClass
	 * @param values
	 * @param primaryKey
	 * @param useFloatInsteadOfDouble save space and represent double as float
	 * @param postgreSQL              some types are different compared to mysql
	 * @return
	 */
	public static String classToSQLType(Class<?> javaClass, Collection<Object> values, boolean primaryKey,
			boolean useFloatInsteadOfDouble, boolean postgreSQL) {
		Objects.requireNonNull(javaClass);

		String c = javaClass.getSimpleName().toLowerCase();
		String sql = null;

		switch (c) {
		case "boolean":
			if (!postgreSQL)
				sql = "BIT(1)";
			else
				sql = "BOOLEAN";
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
			if (!postgreSQL)
				sql = "BIGINT(19)";
			else
				sql = "BIGINT";
			break;
		case "date":
			sql = "DATE";
			break;
		case "float":
			if (!postgreSQL)
				sql = "FLOAT";
			else
				sql = "FLOAT4"; // real
			break;
		case "double":
			if (!postgreSQL)
				sql = useFloatInsteadOfDouble ? "FLOAT" : "DOUBLE";
			else
				sql = useFloatInsteadOfDouble ? "FLOAT4" : "FLOAT8"; // double precision
			break;
		case "bigdecimal":
			sql = "DECIMAL";
			break;
		case "string":
			if (values == null)
				if (!postgreSQL)
					sql = "BLOB";
				else
					sql = "TEXT";
			else {
				int maxCount = 0;
				for (Object v : values)
					if (v != null)
						maxCount = Math.max(maxCount, v.toString().length() + 1);
				if (maxCount < 128)
					sql = "VARCHAR (" + (int) (Math.max(maxCount * 1.33, 3)) + ")";
				else if (!postgreSQL)
					sql = "BLOB";
				else
					sql = "TEXT";
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

		if (resultSet != null)
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
				// map.put(attribute, resultSet.getTime(i + 1));
				map.put(attribute, resultSet.getTimestamp(i + 1));
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

	/**
	 * only tested for postgresql
	 * 
	 * @param conn
	 * @param schema
	 * @param tableName
	 * @param dataPerAttribute
	 * @return
	 */
	public static boolean rowExists(Connection conn, String schema, String tableName,
			Map<String, Object> dataPerAttribute) {

		if (dataPerAttribute == null || dataPerAttribute.isEmpty())
			return false;

		String schemaAndTable = PostgreSQL.isPostgreSQLConnection(conn)
				? PostgreSQL.schemaAndTableName(schema, tableName)
				: tableName; // here the schema is already part of the connection

		PreparedStatement preparedStatement = null;

		String where = "";
		for (String attribute : dataPerAttribute.keySet()) {
			if (!where.equals(""))
				where += " and ";
			where += (attribute + "='" + dataPerAttribute.get(attribute) + "'");
		}

		String sql = "select exists(select 1 from `" + schemaAndTable + "` where " + where + ")";

		if (PostgreSQL.isPostgreSQLConnection(conn))
			sql = PostgreSQL.replaceMySQLQuotes(sql);

		try {
			preparedStatement = conn.prepareStatement(sql);

			ResultSet resultSet = preparedStatement.executeQuery();
			List<ComplexDataObject> result = new ArrayList<ComplexDataObject>();
			result.addAll(SQLUtils.interpreteResultSet(resultSet));

			if (resultSet != null)
				resultSet.close();
			if (preparedStatement != null)
				preparedStatement.close();

			if (result != null && !result.isEmpty())
				if (result.get(0).getAttribute("exists") != null)
					return booleanParser.apply(result.get(0).getAttribute("exists"));
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Identifies duplicates by checking against values of table primary keys.
	 * 
	 * Note: only tested for postgresql.
	 * 
	 * @param conn
	 * @param schema
	 * @param tableName
	 * @param dataPerAttribute
	 * @return boolean per object in the container (e.g., supposed to become a row
	 *         in an insert workflow)
	 * @throws SQLException
	 */
	public static Map<ComplexDataObject, Boolean> rowsExist(Connection conn, String schema, String tableName,
			ComplexDataContainer container) throws SQLException {

		if (container == null)
			return null;

		List<String> pks = primaryKeysForTable(conn, schema, tableName);

		return rowsExist(conn, schema, tableName, pks, container);
	}

	/**
	 * Identifies duplicates by checking against values of table primary keys.
	 * Supports checks for multiple primary keys.
	 * 
	 * Note: only tested for postgresql.
	 * 
	 * @param conn
	 * @param schema
	 * @param tableName
	 * @param dataPerAttribute
	 * @param primaryKeys      the primary keys of the table, if known
	 * @return boolean per object in the container (e.g., supposed to become a row
	 *         in an insert workflow)
	 * @throws SQLException
	 */
	public static Map<ComplexDataObject, Boolean> rowsExist(Connection conn, String schema, String tableName,
			List<String> primaryKeys, ComplexDataContainer container) throws SQLException {

		if (container == null)
			return null;

		if (primaryKeys == null || primaryKeys.isEmpty())
			return null;

		Map<ComplexDataObject, Boolean> existsMap = new HashMap<>();

		Collection<Collection<Object>> pksValues = SQLTableSelector.selectColumnsFromTable(conn, schema, tableName,
				primaryKeys, null, null, Order.ASC);

		Map<Object, List<Collection<Object>>> pkValuesIndex = new HashMap<>();
		for (Collection<Object> pkValues : pksValues) {
			Object firstPKValue = pkValues.iterator().next();
			if (!pkValuesIndex.containsKey(firstPKValue))
				pkValuesIndex.put(firstPKValue, new ArrayList<>());
			pkValuesIndex.get(firstPKValue).add(pkValues);
		}

		for (ComplexDataObject cdo : container) {
			String firstPKAttribute = primaryKeys.get(0);
			Object firstPKvalue = cdo.getAttribute(firstPKAttribute);

			if (firstPKvalue != null) {
				if (pkValuesIndex.containsKey(firstPKvalue)) {
					// check for all rows which match the first primary key value
					List<Collection<Object>> collection = pkValuesIndex.get(firstPKvalue);
					for (Collection<Object> pksCombi : collection) {
						int i = 0;
						for (Object pk : pksCombi) {
							if (!cdo.getAttribute(primaryKeys.get(i)).equals(pk))
								break;
							// check if also the last criterion is met
							else if (i == primaryKeys.size() - 1)
								existsMap.put(cdo, true);
							i++;
						}
					}
				}
			}

			if (!existsMap.containsKey(cdo))
				existsMap.put(cdo, false);
		}

		return existsMap;
	}

	public static List<String> primaryKeysForTable(Connection conn, String schema, String tableName) {
		List<String> pks = new ArrayList<>();

		Map<String, List<String>> primaryKeysForTables = primaryKeysForTables(conn, schema);
		if (primaryKeysForTables == null)
			return pks;

		return primaryKeysForTables.get(tableName);

//		PreparedStatement preparedStatement = null;
//
//		String sql = "SELECT c.column_name, c.data_type\r\n" + "FROM information_schema.table_constraints tc \r\n"
//				+ "JOIN information_schema.constraint_column_usage AS ccu USING (constraint_schema, constraint_name) \r\n"
//				+ "JOIN information_schema.columns AS c ON c.table_schema = tc.constraint_schema\r\n"
//				+ "  AND tc.table_name = c.table_name AND ccu.column_name = c.column_name\r\n"
//				+ "WHERE constraint_type = 'PRIMARY KEY' and tc.table_name = '" + tableName + "';";
//
//		sql = "select tab.table_schema," + "       tab.table_name," + "       tco.constraint_name,"
//				+ "       string_agg(kcu.column_name, ', ') as key_columns" + "from information_schema.tables tab"
//				+ "left join information_schema.table_constraints tco"
//				+ "          on tco.table_schema = tab.table_schema" + "          and tco.table_name = tab.table_name"
//				+ "          and tco.constraint_type = 'PRIMARY KEY'"
//				+ "left join information_schema.key_column_usage kcu "
//				+ "          on kcu.constraint_name = tco.constraint_name"
//				+ "          and kcu.constraint_schema = tco.constraint_schema"
//				+ "          and kcu.constraint_name = tco.constraint_name"
//				+ "where tab.table_schema not in ('pg_catalog', 'information_schema')"
//				+ "      and tab.table_type = 'BASE TABLE'" + "group by tab.table_schema," + "         tab.table_name,"
//				+ "         tco.constraint_name" + "order by tab.table_schema," + "         tab.table_name";
//
//		try {
//			preparedStatement = conn.prepareStatement(sql);
//
//			ResultSet resultSet = preparedStatement.executeQuery();
//
//			List<ComplexDataObject> result = new ArrayList<ComplexDataObject>();
//
//			result.addAll(SQLUtils.interpreteResultSet(resultSet));
//
//			for (ComplexDataObject cdo : result) {
//				Object sch = cdo.getAttribute("table_schema");
//				Object tab = cdo.getAttribute("table_name");
//				if (sch != null && sch.equals(schema))
//					if (tab != null && tab.equals(tableName)) {
//						System.out.println(cdo);
//					}
//			}
//
////			// transform into return type
////			List<String> pks = new ArrayList<>();
////			for (ComplexDataObject cdo : result) {
////				String pk = cdo.getAttribute("column_name").toString();
////				if (!pks.contains(pk))
////					pks.add(pk);
////			}
//
//			if (resultSet != null)
//				resultSet.close();
//			if (preparedStatement != null)
//				preparedStatement.close();
//
//			return pks;
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//
//		return null;
	}

	public static Map<String, List<String>> primaryKeysForTables(Connection conn, String schema) {

		PreparedStatement preparedStatement = null;

//		String sql = "select tc.table_schema, tc.table_name, kc.column_name\r\n"
//				+ "from information_schema.table_constraints tc\r\n"
//				+ "  join information_schema.key_column_usage kc \r\n"
//				+ "    on kc.table_name = tc.table_name and kc.table_schema = tc.table_schema and kc.constraint_name = tc.constraint_name\r\n"
//				+ "where tc.constraint_type = 'PRIMARY KEY'\r\n" + "  and kc.ordinal_position is not null\r\n"
//				+ "order by tc.table_schema,\r\n" + "         tc.table_name,\r\n"
//				+ "         kc.position_in_unique_constraint;";

		String sql = "select tab.table_schema, tab.table_name, tco.constraint_name, string_agg(kcu.column_name, ', ') as key_columns from information_schema.tables tab left join information_schema.table_constraints tco on tco.table_schema = tab.table_schema and tco.table_name = tab.table_name and tco.constraint_type = 'PRIMARY KEY' left join information_schema.key_column_usage kcu on kcu.constraint_name = tco.constraint_name and kcu.constraint_schema = tco.constraint_schema and kcu.constraint_name = tco.constraint_name where tab.table_schema not in ('pg_catalog', 'information_schema') and tab.table_type = 'BASE TABLE' group by tab.table_schema, tab.table_name, tco.constraint_name order by tab.table_schema, tab.table_name";

		try {
			preparedStatement = conn.prepareStatement(sql);

			ResultSet resultSet = preparedStatement.executeQuery();

			List<ComplexDataObject> result = new ArrayList<ComplexDataObject>();

			result.addAll(SQLUtils.interpreteResultSet(resultSet));

			// transform into return type
			Map<String, List<String>> map = new LinkedHashMap<>();

			for (ComplexDataObject cdo : result) {
				Object sch = cdo.getAttribute("table_schema");
				if (sch != null && sch.equals(schema)) {
					Object tab = cdo.getAttribute("table_name");
					if (tab != null && cdo.getAttribute("key_columns") != null) {
						String pkks = cdo.getAttribute("key_columns").toString();
						List<String> pks = new ArrayList<>();
						if (pkks != null && pkks.length() > 0) {
							while (pkks.length() > 0) {
								if (pkks.contains(",")) {
									pks.add(pkks.substring(0, pkks.indexOf(",")).trim());
									pkks = pkks.substring(pkks.indexOf(",") + 1, pkks.length()).trim();
								} else {
									pks.add(pkks.trim());
									pkks = "";
								}
							}
						}
						map.put(tab.toString(), pks);
					}
				}
			}

			if (resultSet != null)
				resultSet.close();
			if (preparedStatement != null)
				preparedStatement.close();

			return map;
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

}
