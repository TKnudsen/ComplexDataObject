package com.github.TKnudsen.ComplexDataObject.model.io.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central cache for per-table database statistics: primary keys and row counts.
 *
 * <p>
 * Replaces the scattered {@code primaryKeyAttributesPerTableAndSchema} map in
 * {@link SQLUtils} and the {@code rowCountCache} in {@link SQLTableIndex} with
 * a single, consistent, thread-safe registry.
 *
 * <h3>Primary keys</h3>
 * <p>
 * Loaded by querying {@code information_schema} once per schema. Cached for the
 * lifetime of the JVM session under the assumption that schemas, tables, and
 * primary keys do not change at runtime. Double-checked locking prevents
 * redundant DB queries under concurrent access.
 *
 * <h3>Row counts</h3>
 * <p>
 * Loaded in bulk from {@code pg_class.reltuples} (one round-trip per schema)
 * via {@link #prefetchRowCounts(Connection, String)}, or lazily on first use.
 * The bulk path is preferred at application startup for speed.
 *
 * <p>
 * <b>PostgreSQL-specific:</b> row count prefetch and estimation rely on
 * {@code pg_class}, {@code pg_namespace}, and {@code reltuples}, which are
 * PostgreSQL catalog tables. This class is not portable to other databases.
 *
 * <h3>Estimate vs exact counts</h3>
 * <p>
 * The cache stores a single {@code long} per table. After prefetch this is a
 * {@code reltuples} estimate. A call to
 * {@link #rowCount(Connection, String, String, boolean)} with
 * {@code exactRequired = true} replaces the cached value with an exact
 * {@code COUNT(*)} result. Callers that need to know whether a cached value is
 * an estimate or an exact count should use
 * {@link #rowCountCached(String, String)} and compare against their own
 * baseline, or always call with {@code exactRequired = true}.
 *
 * <h3>Thread safety</h3>
 * <p>
 * All caches use {@link ConcurrentHashMap}. Double-checked locking is used in
 * the primary key lazy-load path to prevent redundant queries. Row count
 * lookups do not use per-key locking; under concurrent access two threads may
 * both execute a DB query for the same table on a cache miss, which is harmless
 * but slightly redundant.
 *
 * <h3>Recommended startup sequence</h3>
 * 
 * <pre>
 * // Call once per schema at application startup alongside other DB init:
 * SQLTableStatistics.prefetch(conn, "SectorPrimus");
 * SQLTableStatistics.prefetch(conn, "Attributes");
 * </pre>
 * 
 * @version 2.0 revised in April 2026
 */
public class SQLTableStatistics {

	// -------------------------------------------------------------------------
	// Primary key cache
	// "schema" -> ( "tableName" -> ["pk1", "pk2", ...] )
	// -------------------------------------------------------------------------
	private static final ConcurrentHashMap<String, LinkedHashMap<String, List<String>>> primaryKeyCache = new ConcurrentHashMap<>();

	// -------------------------------------------------------------------------
	// Row count cache
	// "schema.table" -> estimated or exact row count (-1 = not yet analyzed)
	// -------------------------------------------------------------------------
	private static final ConcurrentHashMap<String, Long> rowCountCache = new ConcurrentHashMap<>();

	// -------------------------------------------------------------------------
	// Startup / bulk pre-fetch
	// -------------------------------------------------------------------------

	/**
	 * Pre-fetches both primary keys and row counts for all tables in a schema in
	 * two fast catalog queries. Call once per schema at application startup.
	 *
	 * <p>
	 * Equivalent to calling {@link #prefetchPrimaryKeys(Connection, String)} and
	 * {@link #prefetchRowCounts(Connection, String)} together.
	 *
	 * @param conn   active PostgreSQL connection
	 * @param schema schema name to pre-fetch
	 * @throws SQLException on DB error
	 */
	public static void prefetch(Connection conn, String schema) throws SQLException {
		prefetchPrimaryKeys(conn, schema);
		prefetchRowCounts(conn, schema);
	}

	/**
	 * Pre-fetches primary keys for all tables in a schema from
	 * {@code information_schema}. Results are cached permanently for this session.
	 *
	 * @param conn   active PostgreSQL connection
	 * @param schema schema name
	 * @throws SQLException on DB error
	 */
	public static void prefetchPrimaryKeys(Connection conn, String schema) throws SQLException {
		if (primaryKeyCache.containsKey(schema))
			return;

		LinkedHashMap<String, List<String>> tableToKeys = loadPrimaryKeysForSchema(conn, schema);
		primaryKeyCache.put(schema, tableToKeys);

		System.out.println("SQLTableStatistics.prefetchPrimaryKeys: cached primary keys for " + tableToKeys.size()
				+ " tables in schema '" + schema + "'.");
	}

	/**
	 * Pre-fetches estimated row counts for all base tables in a schema from
	 * {@code pg_class.reltuples} in a single catalog query.
	 *
	 * <p>
	 * <b>PostgreSQL-specific.</b> Uses {@code pg_class} and {@code pg_namespace};
	 * only plain base tables ({@code relkind = 'r'}) are included.
	 *
	 * <p>
	 * {@code reltuples} is PostgreSQL's planner statistic, maintained by
	 * {@code ANALYZE} and autovacuum. It is accurate within a few percent for
	 * regularly-analyzed tables and is returned for all tables in one round-trip.
	 * Tables that have never been ANALYZEd return {@code reltuples = -1}; those
	 * will fall back to an estimate or exact {@code COUNT(*)} on first use via
	 * {@link #rowCount(Connection, String, String)}.
	 *
	 * <p>
	 * Row counts are stored as {@code long} to correctly represent tables with more
	 * than {@link Integer#MAX_VALUE} rows without overflow or truncation.
	 *
	 * @param conn   active PostgreSQL connection
	 * @param schema schema name
	 * @throws SQLException on DB error
	 */
	public static void prefetchRowCounts(Connection conn, String schema) throws SQLException {
		String sql = "SELECT relname, GREATEST(reltuples::bigint, -1) AS est_rows " + "FROM pg_class c "
				+ "JOIN pg_namespace n ON n.oid = c.relnamespace " + "WHERE n.nspname = '" + schema
				+ "' AND c.relkind = 'r'";

		Statement stmt = null;
		ResultSet rs = null;
		int count = 0;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				String tableName = rs.getString("relname");
				long estRows = rs.getLong("est_rows");
				rowCountCache.put(schema + "." + tableName, estRows);
				count++;
			}
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException e) {
					/* ignore */ }
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
					/* ignore */ }
		}
		System.out.println("SQLTableStatistics.prefetchRowCounts: cached row counts for " + count
				+ " tables in schema '" + schema + "'.");
	}

	// -------------------------------------------------------------------------
	// Primary key API
	// -------------------------------------------------------------------------

	/**
	 * Returns the primary key columns for a specific table, loading and caching the
	 * full schema on first access.
	 *
	 * @param conn      active JDBC connection
	 * @param schema    schema name
	 * @param tableName table name
	 * @return list of primary key column names, or an empty list if none defined
	 * @throws SQLException on DB error
	 */
	public static List<String> primaryKeysForTable(Connection conn, String schema, String tableName)
			throws SQLException {
		LinkedHashMap<String, List<String>> forSchema = primaryKeysForSchema(conn, schema);
		if (forSchema != null && forSchema.containsKey(tableName))
			return forSchema.get(tableName);
		return new ArrayList<>();
	}

	/**
	 * Returns the primary key columns for all tables in a schema, loading and
	 * caching on first access.
	 *
	 * @param conn   active JDBC connection
	 * @param schema schema name
	 * @return map of tableName -> primary key columns; never null
	 * @throws SQLException on DB error
	 */
	public static LinkedHashMap<String, List<String>> primaryKeysForSchema(Connection conn, String schema)
			throws SQLException {
		LinkedHashMap<String, List<String>> cached = primaryKeyCache.get(schema);
		if (cached != null)
			return cached;

		synchronized (primaryKeyCache) {
			cached = primaryKeyCache.get(schema);
			if (cached != null)
				return cached;

			LinkedHashMap<String, List<String>> loaded = loadPrimaryKeysForSchema(conn, schema);
			primaryKeyCache.put(schema, loaded);
			return loaded;
		}
	}

	/**
	 * Returns primary keys for all tables across all schemas, loading and caching
	 * each schema lazily. Matches the signature of the old
	 * {@link SQLUtils#primaryKeys(Connection)}.
	 *
	 * @param conn active JDBC connection
	 * @return map of schema -> (tableName -> primary key columns)
	 * @throws SQLException on DB error
	 */
	public static Map<String, LinkedHashMap<String, List<String>>> primaryKeysAllSchemas(Connection conn)
			throws SQLException {
		String sql = "SELECT tab.table_schema, tab.table_name, "
				+ "string_agg(kcu.column_name, ', ' ORDER BY kcu.ordinal_position) AS key_columns "
				+ "FROM information_schema.tables tab " + "LEFT JOIN information_schema.table_constraints tco "
				+ "  ON tco.table_schema = tab.table_schema " + "  AND tco.table_name = tab.table_name "
				+ "  AND tco.constraint_type = 'PRIMARY KEY' " + "LEFT JOIN information_schema.key_column_usage kcu "
				+ "  ON kcu.constraint_name = tco.constraint_name "
				+ "  AND kcu.constraint_schema = tco.constraint_schema "
				+ "WHERE tab.table_schema NOT IN ('pg_catalog', 'information_schema') "
				+ "  AND tab.table_type = 'BASE TABLE' " + "GROUP BY tab.table_schema, tab.table_name "
				+ "ORDER BY tab.table_schema, tab.table_name";

		Map<String, LinkedHashMap<String, List<String>>> result = new HashMap<>();

		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				String sch = rs.getString("table_schema");
				String tab = rs.getString("table_name");
				String keyColumns = rs.getString("key_columns");

				result.computeIfAbsent(sch, k -> new LinkedHashMap<>()).put(tab, parseCommaSeparated(keyColumns));
			}
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException e) {
					/* ignore */ }
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
					/* ignore */ }
		}

		for (Map.Entry<String, LinkedHashMap<String, List<String>>> entry : result.entrySet())
			primaryKeyCache.putIfAbsent(entry.getKey(), entry.getValue());

		return result;
	}

	// -------------------------------------------------------------------------
	// Row count API
	// -------------------------------------------------------------------------

	/**
	 * Returns the row count for a table. Equivalent to calling
	 * {@link #rowCount(Connection, String, String, boolean)} with
	 * {@code exactRequired = false}.
	 *
	 * <p>
	 * Returns a cached {@code reltuples} estimate if one is available and
	 * non-negative. If the cache is cold or the estimate is {@code -1} (table never
	 * ANALYZEd), falls back to {@link SQLTableSelector#countRows} in estimate mode,
	 * which may trigger {@code ANALYZE} and re-read {@code reltuples}, or
	 * ultimately fall back to {@code COUNT(*)} only as a last resort.
	 *
	 * @param conn      active JDBC connection
	 * @param schema    schema name
	 * @param tableName table name
	 * @return row count as {@code long}; never negative
	 * @throws SQLException on DB error
	 */
	public static long rowCount(Connection conn, String schema, String tableName) throws SQLException {
		return rowCount(conn, schema, tableName, false);
	}

	/**
	 * Returns the row count for a table in either estimate or exact mode.
	 *
	 * <h3>Estimate path ({@code exactRequired = false})</h3>
	 * <p>
	 * Returns the cached value if present and non-negative. On a cache miss or a
	 * cached value of {@code -1}, delegates to
	 * {@link SQLTableSelector#countRows(Connection, String, String, boolean, boolean)}
	 * in estimate mode ({@code exactRequired = false}), which reads
	 * {@code reltuples}, triggers {@code ANALYZE} if needed, and falls back to
	 * {@code COUNT(*)} only as a last resort. The result is written into the cache.
	 *
	 * <h3>Exact path ({@code exactRequired = true})</h3>
	 * <p>
	 * Bypasses the cache entirely and delegates to
	 * {@link SQLTableSelector#countRows(Connection, String, String, boolean, boolean)}
	 * with {@code exactRequired = true}, executing a full {@code COUNT(*)}. The
	 * exact result is written into the cache, replacing any prior estimate. Note
	 * that subsequent estimate-path calls will then return this exact value until
	 * the cache is cleared.
	 *
	 * <p>
	 * Returns {@code long} to correctly represent tables with more than
	 * {@link Integer#MAX_VALUE} rows without overflow or truncation.
	 *
	 * @param conn          active JDBC connection
	 * @param schema        schema name
	 * @param tableName     table name
	 * @param exactRequired if {@code true}, bypasses any cached value and always
	 *                      executes an exact {@code COUNT(*)}
	 * @return row count as {@code long}; never negative
	 * @throws SQLException on DB error
	 */
	public static long rowCount(Connection conn, String schema, String tableName, boolean exactRequired)
			throws SQLException {
		String tableKey = schema + "." + tableName;

		if (exactRequired) {
			long exact = SQLTableSelector.countRows(conn, schema, tableName, false, true);
			rowCountCache.put(tableKey, exact);
			return exact;
		}

		Long cached = rowCountCache.get(tableKey);
		if (cached != null && cached >= 0L)
			return cached;

		long estimate = SQLTableSelector.countRows(conn, schema, tableName, false, false);
		rowCountCache.put(tableKey, estimate);
		return estimate;
	}

	/**
	 * Returns the cached row count for a table without hitting the database, or
	 * {@code -1L} if the table has not yet been cached.
	 *
	 * <p>
	 * Useful for callers that only need a fast non-blocking value and can tolerate
	 * a missing result, for example to skip processing on provably empty tables
	 * without incurring a round-trip.
	 *
	 * <p>
	 * The returned value may be a {@code reltuples} estimate or an exact
	 * {@code COUNT(*)} result depending on how the cache was last populated.
	 *
	 * @param schema    schema name
	 * @param tableName table name
	 * @return cached row count as {@code long}, or {@code -1L} if not available
	 */
	public static long rowCountCached(String schema, String tableName) {
		Long cached = rowCountCache.get(schema + "." + tableName);
		return cached != null ? cached : -1L;
	}

	/**
	 * Returns a snapshot of all currently cached row counts at the time of the
	 * call. Key format is {@code "schema.table"}. Values are either
	 * {@code reltuples} estimates populated by
	 * {@link #prefetchRowCounts(Connection, String)} or exact counts written by a
	 * prior {@link #rowCount(Connection, String, String, boolean)} call with
	 * {@code exactRequired = true}.
	 *
	 * <p>
	 * The returned map is an immutable copy. Subsequent cache updates are
	 * <em>not</em> reflected in the returned map.
	 *
	 * @return immutable snapshot of all cached row counts
	 */
	public static Map<String, Long> allCachedRowCounts() {
		return Collections.unmodifiableMap(new HashMap<>(rowCountCache));
	}

	// -------------------------------------------------------------------------
	// Cache management
	// -------------------------------------------------------------------------

	/**
	 * Clears all cached statistics (primary keys and row counts). Forces re-loading
	 * from the database on the next access. Call after bulk schema changes or table
	 * restructuring.
	 */
	public static void clearCache() {
		primaryKeyCache.clear();
		rowCountCache.clear();
		System.out.println("SQLTableStatistics: all caches cleared.");
	}

	/**
	 * Clears all cached statistics for a specific schema (primary keys and row
	 * counts). Useful after adding or removing tables within one schema without
	 * affecting others.
	 *
	 * @param schema schema name to evict
	 */
	public static void clearCache(String schema) {
		primaryKeyCache.remove(schema);
		String prefix = schema + ".";
		for (String key : new ArrayList<>(rowCountCache.keySet()))
			if (key.startsWith(prefix))
				rowCountCache.remove(key);
		System.out.println("SQLTableStatistics: entire cache cleared for schema '" + schema + "'.");
	}

	/**
	 * Clears only the cached row count statistics for a specific schema. Primary
	 * key statistics for the schema are not affected.
	 *
	 * <p>
	 * Useful after bulk inserts or deletes within one schema when row count
	 * estimates have become stale, without forcing a reload of primary key
	 * metadata.
	 *
	 * @param schema schema name whose row count cache entries should be evicted
	 */
	public static void clearRowCountCache(String schema) {
		String prefix = schema + ".";
		for (String key : new ArrayList<>(rowCountCache.keySet()))
			if (key.startsWith(prefix))
				rowCountCache.remove(key);
	}

	// -------------------------------------------------------------------------
	// Internal helpers
	// -------------------------------------------------------------------------

	/**
	 * Loads primary keys for all tables in a single schema from
	 * {@code information_schema}.
	 */
	private static LinkedHashMap<String, List<String>> loadPrimaryKeysForSchema(Connection conn, String schema)
			throws SQLException {
		String sql = "SELECT tab.table_name, "
				+ "string_agg(kcu.column_name, ', ' ORDER BY kcu.ordinal_position) AS key_columns "
				+ "FROM information_schema.tables tab " + "LEFT JOIN information_schema.table_constraints tco "
				+ "  ON tco.table_schema = tab.table_schema " + "  AND tco.table_name = tab.table_name "
				+ "  AND tco.constraint_type = 'PRIMARY KEY' " + "LEFT JOIN information_schema.key_column_usage kcu "
				+ "  ON kcu.constraint_name = tco.constraint_name "
				+ "  AND kcu.constraint_schema = tco.constraint_schema " + "WHERE tab.table_schema = '" + schema + "' "
				+ "  AND tab.table_type = 'BASE TABLE' " + "GROUP BY tab.table_name " + "ORDER BY tab.table_name";

		LinkedHashMap<String, List<String>> result = new LinkedHashMap<>();

		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				String tableName = rs.getString("table_name");
				String keyColumns = rs.getString("key_columns");
				result.put(tableName, parseCommaSeparated(keyColumns));
			}
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException e) {
					/* ignore */ }
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
					/* ignore */ }
		}
		return result;
	}

	/**
	 * Parses a comma-separated string of column names (as returned by
	 * {@code string_agg}) into a list. Returns an empty list for null input.
	 */
	private static List<String> parseCommaSeparated(String csv) {
		List<String> result = new ArrayList<>();
		if (csv == null || csv.trim().isEmpty())
			return result;
		for (String part : csv.split(","))
			result.add(part.trim());
		return result;
	}
}