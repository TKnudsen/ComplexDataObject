package com.github.TKnudsen.ComplexDataObject.model.io.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manages PostgreSQL indexes lazily and dynamically.
 *
 * <p>
 * When index-critical SELECT operations are performed, this class inspects the
 * WHERE clause to identify which columns are being filtered. For each unique
 * schema-table-column combination it has not seen before, it checks whether the
 * table qualifies for indexing (row count, cardinality, write ratio) and
 * creates an index if appropriate.
 *
 * <p>
 * The first encounter of a new schema-table-WHERE combination may take several
 * seconds (index creation on large tables). All subsequent calls are instant
 * because the combination is remembered in-memory and the index persists in the
 * database.
 *
 * <p>
 * Thread-safe: uses {@link ConcurrentHashMap}-backed sets to track
 * already-processed combinations so concurrent callers never create duplicate
 * indexes.
 *
 * <h3>WHERE clause column extraction -- supported patterns</h3>
 * <ol>
 * <li>Double-quoted identifiers: {@code "ISIN"}, {@code "Parse Date"}</li>
 * <li>Backtick-quoted identifiers: {@code `ISIN`}, {@code `Parse Date`}
 * (MySQL-style, used by legacy code paths that go through
 * {@code PostgreSQL.replaceMySQLQuotes} later)</li>
 * <li>Unquoted single-word identifiers before comparison operators:
 * {@code ISIN = 'x'}, {@code year > '2014'}</li>
 * </ol>
 *
 * <h3>Typical usage</h3>
 *
 * <pre>
 * // Before executing a WHERE query, call:
 * SQLTableIndex.ensureIndexForWhereClause(conn, "SectorPrimus", "primus", "\"ISIN\" IN ('US67066G1040')",
 * 		"Parse Date");
 * // Then run your SELECT as usual.
 * </pre>
 */
public class SQLTableIndex {

	// -------------------------------------------------------------------------
	// Thresholds for the table qualification checks (tunable)
	// -------------------------------------------------------------------------

	/** Tables with fewer rows than this will not be indexed. */
	public static int MIN_ROWS_FOR_INDEX = 10_000;

	/**
	 * Maximum fraction of total rows that a typical query value may match for a
	 * column to be worth indexing. Measures selectivity, not cardinality.
	 *
	 * <p>
	 * Computed as {@code 1.0 / distinctValues} -- i.e. the fraction of rows a single
	 * value touches on average. If that fraction exceeds this threshold, an index
	 * scan would visit too many pages and a full scan is likely faster.
	 *
	 * <p>
	 * Default 0.20: skip indexing only if a typical value matches more than 20% of
	 * rows. Examples:
	 * <ul>
	 * <li>ISIN on 1,000,000 rows with 3,200 distinct values -> selectivity 0.0003 ->
	 * well below threshold -> index created (ok)</li>
	 * <li>boolean column with 2 distinct values -> selectivity 0.5 -> above threshold
	 * -> index skipped (ok)</li>
	 * <li>status column with 3 values -> selectivity 0.33 -> above threshold -> index
	 * skipped (ok)</li>
	 * </ul>
	 * Columns with only 1 distinct value (constant column) are always skipped.
	 */
	public static double MAX_SELECTIVITY_RATIO = 0.20;

	/**
	 * If the ratio of (updates + deletes) to inserts exceeds this value the table
	 * is considered write-heavy and a warning is printed. Index creation still
	 * proceeds -- the developer can raise this or call {@link #ensureIndex} to
	 * bypass qualification checks for known hot paths.
	 */
	public static double WRITE_HEAVY_WARN_RATIO = 2.0;

	// -------------------------------------------------------------------------
	// In-memory registries -- all cached per session so DB is only hit once.
	// Key for combinations : "schema.table|col1,col2,..."
	// Key for table cache : "schema.table"
	// -------------------------------------------------------------------------
	private static final Set<String> handledCombinations = Collections
			.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

	private static final Set<String> tableQualifiedCache = Collections
			.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

	private static final Set<String> tableDisqualifiedCache = Collections
			.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

	/**
	 * Per-combinationKey lock objects, so concurrent callers for different
	 * tables don't serialize on each other while one of them is off doing a
	 * network round trip (see {@link #ensureIndexForWhereClause}). Entries are
	 * removed again once the corresponding key is handled, so this does not
	 * grow unbounded across a long-running process with many distinct tables.
	 */
	private static final ConcurrentHashMap<String, Object> keyLocks = new ConcurrentHashMap<String, Object>();

	// -------------------------------------------------------------------------
	// Column extraction patterns
	//
	// Pattern order matters: quoted patterns are tried first (most specific),
	// unquoted fallback is only used when no quoted identifiers are found.
	//
	// P1: double-quoted "Column Name" -> captures anything between " "
	// P2: backtick-quoted `Column Name` -> captures anything between ` `
	// Both cover identifiers with spaces (e.g. "Parse Date", `Parse Date`)
	// and special chars (e.g. "Score Relative of wachstumFMP [-s]")
	// P3: unquoted word before operator -> only plain [A-Za-z_][A-Za-z0-9_]*
	// Used as last resort for legacy raw strings like: ISIN = 'x'
	// -------------------------------------------------------------------------
	private static final Pattern P_DOUBLE_QUOTED = Pattern.compile("\"([^\"]+)\"");
	private static final Pattern P_BACKTICK_QUOTED = Pattern.compile("`([^`]+)`");
	private static final Pattern P_UNQUOTED = Pattern.compile(
			"\\b([A-Za-z_][A-Za-z0-9_]*)\\s*(?:=|!=|<>|<=|>=|<|>|\\bIN\\b|\\bLIKE\\b|\\bIS\\b)",
			Pattern.CASE_INSENSITIVE);

	// -------------------------------------------------------------------------
	// Public API
	// -------------------------------------------------------------------------

	/**
	 * Ensures that indexes exist for columns referenced in the given WHERE clause,
	 * plus an optional ORDER BY column. Safe to call before every query: it is a
	 * no-op for combinations that have been handled before (in this JVM session or
	 * in a previous session whose indexes still exist in the DB).
	 *
	 * @param conn           active JDBC connection (must be PostgreSQL)
	 * @param schema         schema name, e.g. {@code "SectorPrimus"}
	 * @param tableName      table name, e.g. {@code "primus"}
	 * @param whereClause    the WHERE condition without the keyword WHERE, e.g.
	 *                       {@code "\"ISIN\" IN ('US67066G1040')"}. May be null.
	 * @param orderAttribute the ORDER BY column (used as secondary index column so
	 *                       that index-only scans are possible). May be null.
	 * @throws SQLException if index creation fails for a reason other than the
	 *                      index already existing
	 */
	public static void ensureIndexForWhereClause(Connection conn, String schema, String tableName, String whereClause,
			String orderAttribute) throws SQLException {

		if (whereClause == null && orderAttribute == null)
			return;

		long t = System.currentTimeMillis();

		List<String> filterColumns = extractColumnsFromWhereClause(whereClause);

		if (filterColumns.isEmpty() && orderAttribute == null)
			return;

		List<String> allColumns = new ArrayList<String>(filterColumns);
		if (orderAttribute != null && !filterColumns.contains(orderAttribute))
			allColumns.add(orderAttribute);

		String combinationKey = buildKey(schema, tableName, allColumns);

		// Fast path: already handled in this session
		if (handledCombinations.contains(combinationKey)) {
			return;
		}

		// Per-key lock, not a single global one: the qualification check below does
		// a network round trip (row count, possibly selectivity stats), and with
		// one table per ticker essentially every combinationKey is new on a given
		// day, so a single shared lock here would serialize every concurrent
		// caller onto one queue regardless of how many DB-facing worker threads
		// they run on. Locking per key still prevents redundant duplicate work for
		// the same combinationKey without forcing unrelated keys to wait on each
		// other.
		Object keyLock = keyLocks.computeIfAbsent(combinationKey, k -> new Object());
		try {
			synchronized (keyLock) {
				// Double-checked locking
				if (handledCombinations.contains(combinationKey)) {
					return;
				}

				if (tableQualifiesForIndex(conn, schema, tableName, filterColumns))
					ensureIndexInDatabase(conn, schema, tableName, filterColumns, orderAttribute);

				// Mark as handled regardless so we don't re-check every query
				handledCombinations.add(combinationKey);
			}
		} finally {
			keyLocks.remove(combinationKey, keyLock);
		}

		long dur = (System.currentTimeMillis() - t);
		if (dur > 1000)
			System.err.println("SQLTableIndex: ensureIndexForWhereClause done in " + dur + " ms. Schema: " + schema
					+ ", table: " + tableName + ", WHERE: " + whereClause);
		else if (dur > 100)
			System.out.println("SQLTableIndex: ensureIndexForWhereClause done in " + dur + " ms. Schema: " + schema
					+ ", table: " + tableName + ", WHERE: " + whereClause);
	}

	/**
	 * Explicitly ensures an index on a fixed set of columns. Useful for known hot
	 * paths (e.g. called once at application startup). Bypasses qualification
	 * checks -- caller takes responsibility.
	 *
	 * @param conn      active JDBC connection
	 * @param schema    schema name
	 * @param tableName table name
	 * @param columns   ordered list of columns to index
	 * @throws SQLException on DB error
	 */
	public static void ensureIndex(Connection conn, String schema, String tableName, List<String> columns)
			throws SQLException {

		if (columns == null || columns.isEmpty())
			return;

		String combinationKey = buildKey(schema, tableName, columns);

		if (handledCombinations.contains(combinationKey))
			return;

		synchronized (handledCombinations) {
			if (handledCombinations.contains(combinationKey))
				return;

			ensureIndexInDatabase(conn, schema, tableName, columns, null);
			handledCombinations.add(combinationKey);
		}
	}

	/**
	 * Lists all indexes on a given table as reported by PostgreSQL's
	 * {@code pg_indexes} view.
	 *
	 * @param conn      active JDBC connection
	 * @param schema    schema name
	 * @param tableName table name
	 * @return list of index names
	 * @throws SQLException on DB error
	 */
	public static List<String> listIndexes(Connection conn, String schema, String tableName) throws SQLException {

		List<String> indexes = new ArrayList<String>();
		String sql = "SELECT indexname FROM pg_indexes WHERE schemaname = '" + schema + "' AND tablename = '"
				+ tableName + "'";

		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			while (rs.next())
				indexes.add(rs.getString("indexname"));
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
		return indexes;
	}

	/**
	 * Drops an index by name. Use with care.
	 *
	 * @param conn      active JDBC connection
	 * @param schema    schema name
	 * @param indexName exact index name as returned by {@link #listIndexes}
	 * @throws SQLException on DB error
	 */
	public static void dropIndex(Connection conn, String schema, String indexName) throws SQLException {

		String sql = "DROP INDEX IF EXISTS \"" + schema + "\".\"" + indexName + "\"";
		Statement stmt = conn.createStatement();
		try {
			stmt.execute(sql);
		} finally {
			stmt.close();
		}

		// Invalidate in-memory cache so the index will be re-created on next access
		String prefix = schema + ".";
		for (String key : new ArrayList<String>(handledCombinations))
			if (key.startsWith(prefix))
				handledCombinations.remove(key);
	}

	/**
	 * Clears all in-memory caches. Forces re-validation against the database on the
	 * next call. Useful in tests or after bulk schema changes.
	 */
	public static void clearCache() {
		handledCombinations.clear();
		tableQualifiedCache.clear();
		tableDisqualifiedCache.clear();
	}

	// -------------------------------------------------------------------------
	// Table qualification checks
	// -------------------------------------------------------------------------

	/**
	 * Returns true if the table is worth indexing based on three criteria:
	 * <ol>
	 * <li>Row count &gt;= {@link #MIN_ROWS_FOR_INDEX}</li>
	 * <li>At least one filter column has selectivity (fraction of rows per typical
	 * value) below {@link #MAX_SELECTIVITY_RATIO}</li>
	 * <li>Write ratio check -- logs a warning if write-heavy but does not block
	 * index creation</li>
	 * </ol>
	 * Results are cached per schema.table so the DB is only queried once per table
	 * per session.
	 */
	static boolean tableQualifiesForIndex(Connection conn, String schema, String tableName, List<String> filterColumns)
			throws SQLException {

		String tableKey = schema + "." + tableName;

		if (tableQualifiedCache.contains(tableKey))
			return true;
		if (tableDisqualifiedCache.contains(tableKey))
			return false;

		// --- Check 1: row count ---
		long rowCount = SQLTableStatistics.rowCount(conn, schema, tableName, false);
		if (rowCount < MIN_ROWS_FOR_INDEX) {
//			System.out.println("SQLTableIndex: table " + tableKey + " has only " + rowCount + " rows (< "
//					+ MIN_ROWS_FOR_INDEX + ")  -  skipping index.");
			tableDisqualifiedCache.add(tableKey);
			return false;
		}

		// --- Check 2: selectivity of filter columns ---
		// selectivity = 1.0 / distinctValues = fraction of rows a single value touches.
		// LOW selectivity (small number) = good for indexing (each value is rare).
		// HIGH selectivity (close to 1.0) = bad for indexing (each value matches most
		// rows).
		// ISIN on 1M rows with 3200 distinct values -> selectivity 0.0003 -> index it.
		// boolean with 2 values -> selectivity 0.5 -> skip.
		boolean anyColumnQualifies = false;
		for (String col : filterColumns) {
			double selectivity = selectivityRatio(conn, schema, tableName, col, rowCount);
			if (selectivity < 0)
				continue; // column not found or error  -  skip silently
			if (selectivity == 1.0) {
				System.out.println("SQLTableIndex: column \"" + col + "\" in " + tableKey
						+ " has only 1 distinct value  -  skipping that column.");
			} else if (selectivity <= MAX_SELECTIVITY_RATIO) {
				anyColumnQualifies = true;
			} else {
				System.out.println("SQLTableIndex: column \"" + col + "\" in " + tableKey + " has high selectivity "
						+ String.format("%.4f", selectivity) + " (> " + MAX_SELECTIVITY_RATIO
						+ ", i.e. each value matches >" + String.format("%.0f", MAX_SELECTIVITY_RATIO * 100)
						+ "% of rows)" + "  -  index unlikely to help, skipping.");
			}
		}

		if (!anyColumnQualifies && !filterColumns.isEmpty()) {
			System.out
					.println("SQLTableIndex: no filter column in " + tableKey + " qualifies for indexing  -  skipping.");
			tableDisqualifiedCache.add(tableKey);
			return false;
		}

		// --- Check 3: write-heavy warning (non-blocking) ---
		checkWriteRatio(conn, schema, tableName, tableKey);

		tableQualifiedCache.add(tableKey);

		return true;
	}

	/**
	 * Returns the selectivity of a column: the fraction of total rows that a single
	 * typical value matches on average, computed as {@code 1.0 / distinctValues}.
	 *
	 * <p>
	 * A low value (near 0) means each value is rare -- good candidate for indexing.
	 * A high value (near 1.0) means each value matches most rows -- an index scan
	 * would be slower than a full table scan.
	 *
	 * <p>
	 * <b>Performance:</b> uses {@code pg_stats.n_distinct} (PostgreSQL's planner
	 * statistics, updated by ANALYZE/autovacuum) for an instant result with no
	 * table scan. Falls back to exact {@code COUNT(DISTINCT col)} only when no
	 * statistics are available yet (table never ANALYZEd). The fallback is slow on
	 * large tables but only runs once per column per session.
	 *
	 * <p>
	 * {@code n_distinct} semantics:
	 * <ul>
	 * <li>{@code > 0} -- absolute count of distinct values</li>
	 * <li>{@code < 0} -- fraction of rows that are distinct (e.g. -1.0 = all
	 * unique)</li>
	 * <li>{@code = 0} -- no statistics yet, fall back to COUNT(DISTINCT)</li>
	 * </ul>
	 *
	 * @return selectivity in [0.0, 1.0], or -1 if the column cannot be queried
	 */
	private static double selectivityRatio(Connection conn, String schema, String tableName, String column,
			long rowCount) {

		if (rowCount <= 0)
			return -1;

		// --- Fast path: pg_stats (no table scan, updated by autovacuum) ---
		String statsSql = "SELECT n_distinct FROM pg_stats " + "WHERE schemaname = '" + schema + "' AND tablename = '"
				+ tableName + "' AND attname = '" + column + "'";
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(statsSql);
			if (rs.next()) {
				double nDistinct = rs.getDouble(1);
				if (!rs.wasNull() && nDistinct != 0) {
					long distinct;
					if (nDistinct > 0) {
						// absolute count
						distinct = (long) nDistinct;
					} else {
						// fraction: e.g. -0.003 means 0.3% of rows are distinct values
						distinct = Math.max(1L, Math.round(-nDistinct * rowCount));
					}
					if (distinct <= 0)
						return -1;
					if (distinct == 1)
						return 1.0;

					return 1.0 / distinct;
				}
				// n_distinct == 0 means no stats yet -- fall through to COUNT(DISTINCT)
			}
		} catch (SQLException e) {
			// pg_stats not accessible -- fall through
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

		// --- Slow fallback: full COUNT(DISTINCT) scan ---
		// Only reached for tables that have never been ANALYZEd.
		// Logged so the developer knows to run ANALYZE if this appears repeatedly.
		System.out.println("SQLTableIndex: no pg_stats for column \"" + column + "\" in " + schema + "." + tableName
				+ "  -  falling back to COUNT(DISTINCT) scan." + " Run ANALYZE to avoid this.");
		String countSql = "SELECT COUNT(DISTINCT \"" + column + "\") FROM \"" + schema + "\".\"" + tableName + "\"";
		stmt = null;
		rs = null;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(countSql);
			if (rs.next()) {
				long distinct = rs.getLong(1);
				if (distinct <= 0)
					return -1;
				if (distinct == 1)
					return 1.0;

				return 1.0 / distinct;
			}
		} catch (SQLException e) {
			// column may not exist or type is not comparable -- not an error
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

		return -1;
	}

	/**
	 * Logs a warning if the table has a high ratio of updates+deletes to inserts,
	 * indicating that index maintenance overhead may be significant.
	 */
	private static void checkWriteRatio(Connection conn, String schema, String tableName, String tableKey) {

		String sql = "SELECT n_tup_ins, n_tup_upd, n_tup_del " + "FROM pg_stat_user_tables " + "WHERE schemaname = '"
				+ schema + "' AND relname = '" + tableName + "'";
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			if (rs.next()) {
				long ins = rs.getLong("n_tup_ins");
				long upd = rs.getLong("n_tup_upd");
				long del = rs.getLong("n_tup_del");
				if (ins > 0) {
					double writeRatio = (double) (upd + del) / ins;
					if (writeRatio > WRITE_HEAVY_WARN_RATIO)
						System.out.println("SQLTableIndex: WARNING  -  table " + tableKey
								+ " appears write-heavy (upd+del/ins ratio = " + String.format("%.2f", writeRatio)
								+ "). Index maintenance overhead may be significant.");
				}
			}
		} catch (SQLException e) {
			// pg_stat_user_tables may not be accessible -- not critical
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
	}

	// -------------------------------------------------------------------------
	// Index creation
	// -------------------------------------------------------------------------

	/**
	 * Creates the index in the database using {@code CREATE INDEX IF NOT EXISTS}.
	 * The index name is deterministically derived from schema, table, and columns
	 * so it is stable across restarts.
	 */
	private static void ensureIndexInDatabase(Connection conn, String schema, String tableName,
			List<String> filterColumns, String orderAttribute) throws SQLException {

		List<String> indexCols = new ArrayList<String>(filterColumns);
		if (orderAttribute != null && !indexCols.contains(orderAttribute))
			indexCols.add(orderAttribute);

		if (indexCols.isEmpty())
			return;

		String indexName = buildIndexName(schema, tableName, indexCols);
		String colList = buildQuotedColumnList(indexCols, orderAttribute);

		String sql = "CREATE INDEX IF NOT EXISTS \"" + indexName + "\" ON \"" + schema + "\".\"" + tableName + "\" ("
				+ colList + ")";

		Statement stmt = conn.createStatement();
		try {
			stmt.execute(sql);
		} finally {
			stmt.close();
		}
	}

	// -------------------------------------------------------------------------
	// Column extraction from WHERE clause
	// -------------------------------------------------------------------------

	/**
	 * Extracts column names from a SQL WHERE clause.
	 *
	 * <p>
	 * Three patterns are tried in order of specificity:
	 * <ol>
	 * <li><b>Double-quoted</b> {@code "Column Name"} -- handles PostgreSQL
	 * identifiers including spaces and special characters.</li>
	 * <li><b>Backtick-quoted</b> {@code `Column Name`} -- handles MySQL-style
	 * identifiers, including those with spaces like {@code `Parse Date`}. These are
	 * common in legacy code paths that later go through
	 * {@code PostgreSQL.replaceMySQLQuotes()}.</li>
	 * <li><b>Unquoted</b> single-word tokens before a comparison operator -- last
	 * resort for raw strings like {@code ISIN = 'x'}.</li>
	 * </ol>
	 *
	 * <p>
	 * Patterns 1 and 2 are applied together first. The unquoted fallback (Pattern
	 * 3) is only used when neither quoted pattern finds anything, to avoid
	 * extracting false positives from quoted value strings.
	 *
	 * <p>
	 * Known limitation: {@code SIMILAR TO} is not in the operator list because the
	 * column before it is always captured by the quoted patterns in practice.
	 */
	static List<String> extractColumnsFromWhereClause(String whereClause) {

		List<String> columns = new ArrayList<String>();
		if (whereClause == null || whereClause.trim().isEmpty())
			return columns;

		Set<String> seen = new LinkedHashSet<String>();

		// Pattern 1: double-quoted identifiers "Column Name"
		Matcher m1 = P_DOUBLE_QUOTED.matcher(whereClause);
		while (m1.find())
			seen.add(m1.group(1));

		// Pattern 2: backtick-quoted identifiers `Column Name`
		// Covers legacy MySQL-style WHERE clauses including those with spaces.
		Matcher m2 = P_BACKTICK_QUOTED.matcher(whereClause);
		while (m2.find())
			seen.add(m2.group(1));

		// Pattern 3: unquoted identifiers before a comparison operator.
		// Only used as fallback when no quoted identifiers were found, to avoid
		// extracting words from quoted value strings (e.g. 'Parse Date').
		if (seen.isEmpty()) {
			Matcher m3 = P_UNQUOTED.matcher(whereClause);
			while (m3.find()) {
				String col = m3.group(1);
				if (!isSQLKeyword(col))
					seen.add(col);
			}
		}

		columns.addAll(seen);
		return columns;
	}

	// -------------------------------------------------------------------------
	// Internal helpers
	// -------------------------------------------------------------------------

	/**
	 * Builds the column list for the CREATE INDEX statement. The order column (if
	 * present) gets {@code DESC} appended.
	 */
	private static String buildQuotedColumnList(List<String> allCols, String orderAttribute) {

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < allCols.size(); i++) {
			if (i > 0)
				sb.append(", ");
			sb.append("\"").append(allCols.get(i)).append("\"");
			if (allCols.get(i).equals(orderAttribute))
				sb.append(" DESC");
		}
		return sb.toString();
	}

	/**
	 * Produces a deterministic, DB-safe index name. PostgreSQL limits identifier
	 * length to 63 bytes, so the name is truncated with a short hash suffix if
	 * needed.
	 */
	private static String buildIndexName(String schema, String tableName, List<String> cols) {

		StringBuilder joined = new StringBuilder();
		for (String col : cols)
			joined.append("_").append(col);

		String raw = ("idx_" + schema + "_" + tableName + joined.toString()).replaceAll("[^A-Za-z0-9_]", "_");

		if (raw.length() <= 63)
			return raw;

		String hash = Integer.toHexString(raw.hashCode());
		return raw.substring(0, 55) + "_" + hash;
	}

	private static String buildKey(String schema, String tableName, List<String> columns) {
		StringBuilder sb = new StringBuilder(schema).append(".").append(tableName).append("|");
		for (int i = 0; i < columns.size(); i++) {
			if (i > 0)
				sb.append(",");
			sb.append(columns.get(i));
		}
		return sb.toString();
	}

	private static final Set<String> SQL_KEYWORDS = new HashSet<String>(Arrays.asList("SELECT", "FROM", "WHERE", "AND",
			"OR", "NOT", "IN", "IS", "NULL", "LIKE", "BETWEEN", "EXISTS", "CASE", "WHEN", "THEN", "ELSE", "END", "TRUE",
			"FALSE", "AS", "ON", "JOIN", "LEFT", "RIGHT", "INNER", "OUTER"));

	private static boolean isSQLKeyword(String token) {
		return SQL_KEYWORDS.contains(token.toUpperCase());
	}
}