package com.github.TKnudsen.ComplexDataObject.model.io.sql;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;

import com.github.TKnudsen.ComplexDataObject.data.DataSchemaEntry;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.BooleanParser;
import com.github.TKnudsen.ComplexDataObject.model.io.sql.SQLTableSelector.Order;

/**
 * <p>
 * Collection of shared SQL helper routines used across the io.sql package:
 * mapping Java classes to SQL column types, checking table existence,
 * converting JDBC ResultSets into ComplexDataObjects/ComplexDataContainers,
 * building key-value row representations, mitigating data truncation errors,
 * checking for existing/duplicate rows, and estimating JDBC batch sizes for
 * bulk inserts.
 * </p>
 */
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

			SQLTableStatistics.clearCache();
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
	 * Checks whether a given table exists in the specified schema.
	 * <p>
	 * For PostgreSQL connections, this method uses the fast, system-cache-backed
	 * {@code to_regclass(format('%I.%I', ?, ?))} query, which avoids full catalog
	 * scans. For all other databases, it falls back to
	 * {@link DatabaseMetaData#getTables}, normalizing identifier case according to
	 * the database's storage rules.
	 * <p>
	 * All JDBC resources are properly closed using try-with-resources.
	 *
	 * <h4>Behavior</h4>
	 * <ul>
	 * <li>For PostgreSQL, the lookup is case-sensitive and schema-qualified.</li>
	 * <li>For other databases, identifier case is adjusted via metadata.</li>
	 * <li>Only real tables ({@code TABLE}) are considered; add "VIEW" etc. if
	 * needed.</li>
	 * </ul>
	 *
	 * @param conn      an open {@link Connection}; not closed by this method
	 * @param schema    the schema name (must not be {@code null})
	 * @param tableName the table name (must not be {@code null})
	 * @return {@code true} if the table exists, {@code false} otherwise
	 * @throws SQLException if a database access error occurs
	 */
	public static boolean tableExists(Connection conn, String schema, String tableName) throws SQLException {

		Objects.requireNonNull(schema);
		Objects.requireNonNull(tableName);

		boolean postgreSQL = PostgreSQL.isPostgreSQLConnection(conn);

		if (postgreSQL) {
			final String sql = "select to_regclass(format('%I.%I', ?, ?)) is not null";
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setString(1, schema);
				ps.setString(2, tableName);
				try (ResultSet rs = ps.executeQuery()) {
					return rs.next() && rs.getBoolean(1);
				}
			}

		} else {
			// Generic JDBC path
			DatabaseMetaData md = conn.getMetaData();

			String normSchema = normalizeIdentifier(md, schema);
			String normTable = normalizeIdentifier(md, tableName);

			// Limit to real tables; add "VIEW" etc. if you want those to count
			String[] types = new String[] { "TABLE" };
			try (ResultSet rs = md.getTables(conn.getCatalog(), normSchema, normTable, types)) {
				return rs.next();
			}
		}
	}

	/**
	 * Normalizes an identifier (schema or table name) to match how the database
	 * stores unquoted identifiers, using the metadata flags
	 * {@link DatabaseMetaData#storesLowerCaseIdentifiers()} and
	 * {@link DatabaseMetaData#storesUpperCaseIdentifiers()}.
	 *
	 * @param md    the {@link DatabaseMetaData} for the connection
	 * @param ident the identifier (may be {@code null})
	 * @return a normalized identifier suitable for metadata lookups, or
	 *         {@code null}
	 * @throws SQLException if metadata access fails
	 */
	private static String normalizeIdentifier(DatabaseMetaData md, String ident) throws SQLException {
		if (ident == null)
			return null;
		if (md.storesLowerCaseIdentifiers()) {
			return ident.toLowerCase(Locale.ROOT);
		} else if (md.storesUpperCaseIdentifiers()) {
			return ident.toUpperCase(Locale.ROOT);
		} else {
			return ident; // case-sensitive store
		}
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

		if (resultSet == null)
			return java.util.Collections.emptyList();

		List<ComplexDataObject> result = new ArrayList<ComplexDataObject>();

		while (resultSet.next()) {
			Long id = null;
			try {
				resultSet.findColumn("ID");
				id = resultSet.getLong("ID");
			} catch (SQLException sqlex) {
			}

			ComplexDataObject cdo = (id == null) ? new ComplexDataObject(targetAttributeCharacterization.size())
					: new ComplexDataObject(targetAttributeCharacterization.size(), id);

			for (String attribute : targetAttributeCharacterization.keySet()) {
				resultSet.findColumn(attribute);
				try {
					cdo.add(attribute, SQLUtils.mySQLTypeToJavaClass(attribute,
							targetAttributeCharacterization.get(attribute), resultSet));
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}

			if (cdo == null)
				System.err.println("SQLUtils.interpreteResultSet: null object returned.");

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
	 * @param doubleAsFloat if floats shall replace double
	 * @return
	 * @throws SQLException
	 */
	public static List<ComplexDataObject> interpreteResultSet(ResultSet resultSet, boolean doubleAsFloat)
			throws SQLException {

		if (resultSet == null)
			return java.util.Collections.emptyList();

		// one unified step:
		ResultSetInterpreter.ResultSetDescriptor desc = ResultSetInterpreter.createExtractors(resultSet, doubleAsFloat);

		// fast mapping into ComplexDataObjects:
		return ResultSetMapper.asListOfComplexObjects(resultSet, desc.getColumnNames(), desc.getExtractors());
	}

	/**
	 * Interprets a single ResultSet row (does not call next()) using the same
	 * optimized mechanism as the full ResultSet version.
	 *
	 * @param resultSet
	 * @param columnNames
	 * @param columnTypes
	 * @param doubleAsFloat
	 * @return a map of columnName to value
	 * @throws SQLException
	 */
	public static LinkedHashMap<String, Object> interpreteResultSetRow(ResultSet resultSet, String[] columnNames,
			int[] columnTypes, boolean doubleAsFloat) throws SQLException {

		ResultSetInterpreter.ResultSetDescriptor desc = ResultSetInterpreter.createExtractors(resultSet, doubleAsFloat);

		// assumes resultSet is already positioned at a valid row
		return ResultSetInterpreter.interpreteResultSetRow(resultSet, desc.getColumnNames(), desc.getExtractors());
	}

	/**
	 * does not apply next(), e.g., iteration must be triggered externally.
	 * 
	 * @param resultSet
	 * @param columnNames
	 * @param doubleAsFloat return all numeric values with double precision as float
	 *                      values.
	 * @return
	 * @throws SQLException
	 */
	public static LinkedHashMap<String, Object> interpreteResultSetRowOld(ResultSet resultSet, String[] columnNames,
			int[] columnTypes, boolean doubleAsFloat) throws SQLException {

		// Pre-size the map to reduce internal resizing and rehashing
		final int len = columnNames.length;
		final LinkedHashMap<String, Object> map = new LinkedHashMap<>(len * 4 / 3);

		boolean b;
		long l;
		float f;
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

			case Types.REAL:
			case Types.FLOAT:
				f = resultSet.getFloat(i + 1);
				if (resultSet.wasNull()) {
					map.put(attribute, null);
				} else {
					map.put(attribute, f);
				}
				break;
			case Types.DOUBLE:
				if (doubleAsFloat = false) {
					d = resultSet.getDouble(i + 1);
					if (resultSet.wasNull()) {
						map.put(attribute, null);
					} else {
						map.put(attribute, d);
					}
				} else {
					f = resultSet.getFloat(i + 1);
					if (resultSet.wasNull()) {
						map.put(attribute, null);
					} else {
						map.put(attribute, f);
					}
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
	 * Creates a list of key-value pairs (attributes and values) for all
	 * ComplexDataObjects in a container.
	 *
	 * Ensures that each resulting map has entries for every attribute present in
	 * the container. Missing attributes are assigned {@code null}, so that
	 * downstream batch insert operations in databases can rely on uniform column
	 * structure, which can be much faster.
	 *
	 * @param container the ComplexDataContainer to transform
	 * @return a list of LinkedHashMaps, each representing one row with uniform keys
	 */
	public static List<LinkedHashMap<String, Object>> createKeyValuePairs(ComplexDataContainer container) {
		Objects.requireNonNull(container, "container must not be null");

		// 1. Collect all attribute names (acts as white list and column schema)
		SortedSet<String> allAttributes = new java.util.TreeSet<>(container.getAttributes());

		// 2. Prepare output
		List<LinkedHashMap<String, Object>> list = new ArrayList<>(container.size());

		// 3. For each ComplexDataObject, fill all keys (missing ones to null)
		for (ComplexDataObject cdo : container) {
			LinkedHashMap<String, Object> row = new LinkedHashMap<>();

			// Always include ID if available
			row.put("ID", cdo.getID());

			for (String attr : allAttributes) {
				Object value = cdo.containsAttribute(attr) ? cdo.getAttribute(attr) : null;
				row.put(attr, value);
			}

			list.add(row);
		}

		return list;
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
			ComplexDataContainer dataContainer, String schema, String tableName) {
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
	 * @throws SQLException
	 */
	public static boolean rowExists(Connection conn, String schema, String tableName,
			Map<String, Object> dataPerAttribute) throws SQLException {

		if (dataPerAttribute == null || dataPerAttribute.isEmpty())
			return false;

		boolean postgreSQL = PostgreSQL.isPostgreSQLConnection(conn);
		String schemaAndTable = postgreSQL ? PostgreSQL.schemaAndTableName(schema, tableName) : tableName;

		SQLWhereClause clause = SQLWhereClause.mysql();
		int i = 0;
		for (Map.Entry<String, Object> entry : dataPerAttribute.entrySet()) {
			if (i++ > 0)
				clause.and();
			clause.eq(entry.getKey(), entry.getValue() != null ? entry.getValue().toString() : null);
		}
		String where = clause.build();

		String sql = "select exists(select 1 from `" + schemaAndTable + "` where " + where + ")";
		if (postgreSQL)
			sql = PostgreSQL.replaceMySQLQuotes(sql);

		PreparedStatement ps = conn.prepareStatement(sql);
		ResultSet rs = null;
		try {
			rs = ps.executeQuery();
			List<ComplexDataObject> result = SQLUtils.interpreteResultSet(rs, false);
			if (result != null && !result.isEmpty())
				if (result.get(0).getAttribute("exists") != null)
					return booleanParser.apply(result.get(0).getAttribute("exists"));
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException e) {
					/* ignore */ }
			ps.close();
		}

		return false;
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
	 * @param primaryKeysForTable the primary keys of the table, if known
	 * @return boolean per object in the container (e.g., supposed to become a row
	 *         in an insert workflow)
	 * @throws SQLException
	 */
	public static Map<ComplexDataObject, Boolean> rowsExist(Connection conn, String schema, String tableName,
			List<String> primaryKeysForTable, ComplexDataContainer container) throws SQLException {

		if (container == null)
			return null;

		if (primaryKeysForTable == null || primaryKeysForTable.isEmpty())
			return null;

		Map<ComplexDataObject, Boolean> existsMap = new HashMap<>();

		Collection<List<Object>> pksValues = SQLTableSelector.selectColumnsFromTable(conn, schema, tableName,
				primaryKeysForTable, null, null, Order.ASC);

		// TODO check if there exists a faster way. If need be.
		Map<Object, List<Collection<Object>>> pkValuesIndex = new HashMap<>();
		for (List<Object> pkValues : pksValues) {
			Object firstPKValue = pkValues.iterator().next();
			if (!pkValuesIndex.containsKey(firstPKValue))
				pkValuesIndex.put(firstPKValue, new ArrayList<>());
			pkValuesIndex.get(firstPKValue).add(pkValues);
		}

		for (ComplexDataObject cdo : container) {
			String firstPKAttribute = primaryKeysForTable.get(0);
			Object firstPKvalue = cdo.getAttribute(firstPKAttribute);

			if (firstPKvalue != null) {
				if (pkValuesIndex.containsKey(firstPKvalue)) {
					// check for all rows which match the first primary key value
					List<Collection<Object>> collection = pkValuesIndex.get(firstPKvalue);
					for (Collection<Object> pksCombi : collection) {
						int i = 0;
						for (Object pk : pksCombi) {
							if (!cdo.getAttribute(primaryKeysForTable.get(i)).equals(pk))
								break;
							// check if also the last criterion is met
							else if (i == primaryKeysForTable.size() - 1)
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

	/**
	 * Estimates a JDBC batch size (rows per batch) based on the number of
	 * attributes (columns) per row. Uses a 10-interval lookup table with values
	 * rounded to the nearest multiple of 50.
	 *
	 * The function decreases smoothly as the number of attributes grows, keeping
	 * the total pay-load roughly stable across different row widths.
	 *
	 * @param attributeCount number of attributes (columns) per row
	 * @return recommended batch size, always a multiple of 50
	 */
	public static int estimateBatchLimit(int attributeCount) {
		if (attributeCount <= 0)
			return 1000; // default safety fallback

		// Lookup table boundaries (in attribute counts)
		// Each interval covers a power-of-two-like expansion for smoothness
		final int[] attrThresholds = { 10, 20, 50, 100, 200, 400, 800, 1600, 3200, 6400 };

		// Corresponding batch sizes (rows per batch), all multiples of 50
		final int[] batchValues = { 5000, 4000, 3000, 2000, 1250, 800, 500, 300, 150, 100 };

		// Find first threshold >= attributeCount
		for (int i = 0; i < attrThresholds.length; i++)
			if (attributeCount <= attrThresholds[i])
				return batchValues[i];

		// Beyond the largest interval to minimal safe batch size
		return 50;
	}

	/**
	 * Estimate batch size from row bytes. Samples rows, uses p90 size, targets
	 * ~6MB/batch.
	 *
	 * @param rows                    rows to insert (uniform schema)
	 * @param postgreSQL              whether the connection is PostgreSQL (affects
	 *                                minor overhead assumptions)
	 * @param useFloatInsteadOfDouble numeric mapping hint
	 * @param targetBytesPerBatch     desired total pay-load per executeBatch (e.g.
	 *                                6MB)
	 * @return batch size, clamped [50..5000], multiple of 50
	 */
	public static int estimateBatchLimitByBytes(java.util.List<LinkedHashMap<String, Object>> rows, boolean postgreSQL,
			boolean useFloatInsteadOfDouble, int targetBytesPerBatch) {

		if (rows == null || rows.isEmpty())
			return 1000;

		int sampleSize = 100;

		// Sample up to first sampleSize rows; cheap and stable
		int sampleN = Math.min(sampleSize, rows.size());
		int[] sizes = new int[sampleN];
		for (int i = 0; i < sampleN; i++)
			sizes[i] = estimateRowBytes(rows.get(i), postgreSQL, useFloatInsteadOfDouble);

		java.util.Arrays.sort(sizes);
		int p90 = sizes[(int) Math.ceil(sampleN * 0.90) - 1];
		if (p90 <= 0)
			p90 = Math.max(estimateRowBytes(rows.get(0), postgreSQL, useFloatInsteadOfDouble), 1);

		// Target pay load / p90 row size = rows per batch
		long raw = Math.max(1L, targetBytesPerBatch / Math.max(1, p90));

		// Clamp and quantize
		if (raw > 5000)
			raw = 5000;
		int rounded = roundToNearest50((int) raw);
		return rounded;
	}

	private static int roundToNearest50(int x) {
		int r = ((x + 25) / 50) * 50;
		return (r < 50) ? 50 : r;
	}

	/**
	 * Roughly estimates the serialized byte size of a single database row, based on
	 * its column values. The estimate is used for adaptive batching and avoids
	 * expensive per-character or per-type introspection.
	 *
	 * <p>
	 * This approximation assumes text-based transmission (e.g., JDBC text protocol
	 * or string conversion of parameters). It trades precision for speed and should
	 * be accurate within +/-20--30% for typical data sets.
	 * </p>
	 *
	 * @param row                     the row to estimate, mapping column name to
	 *                                value
	 * @param postgreSQL              unused but kept for signature consistency
	 * @param useFloatInsteadOfDouble unused but kept for signature consistency
	 * @return approximate row size in bytes (for tuning batch pay loads)
	 */
	private static int estimateRowBytes(LinkedHashMap<String, Object> row, boolean postgreSQL,
			boolean useFloatInsteadOfDouble) {
		// Per-column bookkeeping and separators overhead (commas, delimiters, etc.)
		final int PER_COLUMN_OVERHEAD = 4;
		// Placeholder bytes for null fields
		final int NULL_PLACEHOLDER = 2;

		int total = 0;

		for (Object v : row.values()) {
			if (v == null) {
				total += NULL_PLACEHOLDER + PER_COLUMN_OVERHEAD;
				continue;
			}

			// === Simplified type buckets ===
			if (v instanceof String) {
				// For performance, skip ASCII scan or getBytes() allocation.
				// Assume average UTF-8 expansion factor ~ 1.5x.
				int len = ((String) v).length();
				total += (int) (len * 1.5) + 2 /* quotes */ + PER_COLUMN_OVERHEAD;
				continue;
			}

			if (v instanceof Number) {
				// Covers int, long, float, double, BigDecimal, etc.
				// Textual representation usually 8--24 bytes to use ~16 avg.
				total += 16 + PER_COLUMN_OVERHEAD;
				continue;
			}

			if (v instanceof Boolean) {
				// "T"/"F" or 1/0 in most drivers
				total += 1 + PER_COLUMN_OVERHEAD;
				continue;
			}

			if (v instanceof java.util.Date || v instanceof java.sql.Timestamp) {
				// "YYYY-MM-DD" (10) or "YYYY-MM-DD HH:MM:SS.mmmuuu" (26)
				total += 24 + PER_COLUMN_OVERHEAD;
				continue;
			}

			if (v instanceof byte[]) {
				// Base64/text overhead ~40%
				total += (int) Math.ceil(((byte[]) v).length * 1.4) + PER_COLUMN_OVERHEAD;
				continue;
			}

			// Fallback: unknown or complex object, assume medium string representation
			total += 16 + PER_COLUMN_OVERHEAD;
		}

		// Add fixed per-row framing overhead (statement wrapper, separators, etc.)
		return total + 32;
	}

}
