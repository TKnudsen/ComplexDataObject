package com.github.TKnudsen.ComplexDataObject.model.io.sql;

import java.util.Collection;

/**
 * Fluent builder for SQL WHERE clauses compatible with PostgreSQL and MySQL.
 *
 * <p>
 * Eliminates scattered, inconsistent hand-rolled WHERE string construction
 * across the framework. All methods return {@code this} for chaining. Call
 * {@link #build()} to get the final clause <em>without</em> the {@code WHERE}
 * keyword -- consistent with how the rest of the framework passes WHERE
 * conditions to {@code selectFromTableWhere} and similar methods.
 *
 * <h3>Return value contract</h3>
 * <ul>
 * <li>{@link #build()} returns {@code null} when nothing was added (null = "no
 * filter", matching the framework convention).</li>
 * <li>{@link #buildWithKeyword()} returns {@code ""} when nothing was
 * added.</li>
 * </ul>
 *
 * <h3>Safety notes</h3>
 * <ul>
 * <li>Values are single-quoted and internal single quotes are escaped by
 * doubling. This is sufficient for trusted internal inputs. Do NOT pass
 * end-user input through this builder without validation.</li>
 * <li>Column names are validated to contain only letters, digits, underscores,
 * and spaces (to support names like {@code "Parse Date"}). Anything else throws
 * {@link IllegalArgumentException}.</li>
 * <li>{@link #raw(String)} and {@link #andIf(boolean, String)} accept pre-built
 * fragments. Use only with trusted, internally constructed strings.</li>
 * </ul>
 *
 * <h3>Examples</h3>
 *
 * <pre>
 * // Single equality
 * SQLWhereClause.postgre().eq("ISIN", "US67066G1040").build();
 * // -> "ISIN" = 'US67066G1040'
 *
 * // IN list
 * SQLWhereClause.postgre().in("ISIN", isins).build();
 * // -> "ISIN" IN ('US123','DE456')
 *
 * // Compound AND
 * SQLWhereClause.postgre().eq("isin", isin).and().gt("year", "2014").build();
 * // -> "isin" = 'val' AND "year" > '2014'
 *
 * // Null-aware equality
 * SQLWhereClause.postgre().eq("col", null).build();
 * // -> "col" IS NULL
 *
 * // Numeric comparison (no quotes around the value)
 * SQLWhereClause.postgre().gtNumeric("year", 2014).build();
 * // -> "year" > 2014
 *
 * // Optional second condition
 * SQLWhereClause.postgre().gt("Parse Date", "2023-01-01").andIf(otherClause != null, otherClause).build();
 * </pre>
 */
public class SQLWhereClause {

	// -------------------------------------------------------------------------
	// Quote style
	// -------------------------------------------------------------------------

	/** Quote style for column identifiers. */
	public enum QuoteStyle {
		/** PostgreSQL-style double quotes: {@code "column"} */
		POSTGRESQL,
		/** MySQL-style back-ticks: {@code `column`} */
		MYSQL
	}

	// -------------------------------------------------------------------------
	// Factory methods
	// -------------------------------------------------------------------------

	/** Creates a builder using PostgreSQL double-quote identifiers. */
	public static SQLWhereClause postgre() {
		return new SQLWhereClause(QuoteStyle.POSTGRESQL);
	}

	/** Creates a builder using MySQL back-tick identifiers. */
	public static SQLWhereClause mysql() {
		return new SQLWhereClause(QuoteStyle.MYSQL);
	}

	/**
	 * Creates a builder that picks the right quote style based on the connection
	 * type, as returned by {@link PostgreSQL#isPostgreSQLConnection}.
	 *
	 * @param postgreSQL true for PostgreSQL, false for MySQL/generic
	 */
	public static SQLWhereClause forConnection(boolean postgreSQL) {
		return new SQLWhereClause(postgreSQL ? QuoteStyle.POSTGRESQL : QuoteStyle.MYSQL);
	}

	// -------------------------------------------------------------------------
	// State
	// -------------------------------------------------------------------------

	private final QuoteStyle quoteStyle;
	private final StringBuilder sb = new StringBuilder();

	/**
	 * Tracks whether the last thing appended was a connector (AND/OR). Prevents
	 * double-connectors and trailing connectors in build().
	 */
	private boolean lastWasConnector = false;

	/** Tracks open parenthesis groups so endGroup() can be validated. */
	private int openGroups = 0;

	private SQLWhereClause(QuoteStyle quoteStyle) {
		this.quoteStyle = quoteStyle;
	}

	// -------------------------------------------------------------------------
	// String value conditions (null-aware)
	// -------------------------------------------------------------------------

	/**
	 * Appends {@code "column" = 'value'}, or {@code "column" IS NULL} if value is
	 * null.
	 */
	public SQLWhereClause eq(String column, String value) {
		validateColumn(column);
		if (value == null)
			sb.append(quoteCol(column)).append(" IS NULL");
		else
			sb.append(quoteCol(column)).append(" = ").append(quoteVal(value));
		lastWasConnector = false;
		return this;
	}

	/**
	 * Appends {@code "column" != 'value'}, or {@code "column" IS NOT NULL} if value
	 * is null.
	 */
	public SQLWhereClause neq(String column, String value) {
		validateColumn(column);
		if (value == null)
			sb.append(quoteCol(column)).append(" IS NOT NULL");
		else
			sb.append(quoteCol(column)).append(" != ").append(quoteVal(value));
		lastWasConnector = false;
		return this;
	}

	/**
	 * Appends {@code "column" > 'value'}.
	 */
	public SQLWhereClause gt(String column, String value) {
		validateColumn(column);
		sb.append(quoteCol(column)).append(" > ").append(quoteVal(value));
		lastWasConnector = false;
		return this;
	}

	/**
	 * Appends {@code "column" >= 'value'}.
	 */
	public SQLWhereClause gte(String column, String value) {
		validateColumn(column);
		sb.append(quoteCol(column)).append(" >= ").append(quoteVal(value));
		lastWasConnector = false;
		return this;
	}

	/**
	 * Appends {@code "column" < 'value'}.
	 */
	public SQLWhereClause lt(String column, String value) {
		validateColumn(column);
		sb.append(quoteCol(column)).append(" < ").append(quoteVal(value));
		lastWasConnector = false;
		return this;
	}

	/**
	 * Appends {@code "column" <= 'value'}.
	 */
	public SQLWhereClause lte(String column, String value) {
		validateColumn(column);
		sb.append(quoteCol(column)).append(" <= ").append(quoteVal(value));
		lastWasConnector = false;
		return this;
	}

	/**
	 * Appends {@code "column" IN ('v1','v2',...)}.
	 * <p>
	 * If {@code values} is empty, emits {@code 1=0} (always-false condition), which
	 * is valid SQL and semantically correct -- no row can match an empty set.
	 *
	 * @param column column name
	 * @param values collection of string values; empty collection emits 1=0
	 */
	public SQLWhereClause in(String column, Collection<String> values) {
		validateColumn(column);
		if (values == null || values.isEmpty()) {
			sb.append("1=0"); // empty IN () is invalid SQL; 1=0 is the correct substitute
		} else {
			sb.append(quoteCol(column)).append(" IN (");
			boolean first = true;
			for (String v : values) {
				if (!first)
					sb.append(',');
				sb.append(quoteVal(v));
				first = false;
			}
			sb.append(')');
		}
		lastWasConnector = false;
		return this;
	}

	/**
	 * Appends {@code "column" LIKE 'pattern'}.
	 */
	public SQLWhereClause like(String column, String pattern) {
		validateColumn(column);
		sb.append(quoteCol(column)).append(" LIKE ").append(quoteVal(pattern));
		lastWasConnector = false;
		return this;
	}

	/**
	 * Appends {@code "column" SIMILAR TO 'pattern'}.
	 * <p>
	 * <strong>PostgreSQL only.</strong> Throws {@link IllegalStateException} if
	 * this builder was created with {@link QuoteStyle#MYSQL}.
	 */
	public SQLWhereClause similarTo(String column, String pattern) {
		if (quoteStyle == QuoteStyle.MYSQL)
			throw new IllegalStateException(
					"SIMILAR TO is a PostgreSQL-only operator and cannot be used with a MySQL-style builder.");
		validateColumn(column);
		sb.append(quoteCol(column)).append(" SIMILAR TO ").append(quoteVal(pattern));
		lastWasConnector = false;
		return this;
	}

	/**
	 * Appends {@code "column" IS NULL}.
	 */
	public SQLWhereClause isNull(String column) {
		validateColumn(column);
		sb.append(quoteCol(column)).append(" IS NULL");
		lastWasConnector = false;
		return this;
	}

	/**
	 * Appends {@code "column" IS NOT NULL}.
	 */
	public SQLWhereClause isNotNull(String column) {
		validateColumn(column);
		sb.append(quoteCol(column)).append(" IS NOT NULL");
		lastWasConnector = false;
		return this;
	}

	// -------------------------------------------------------------------------
	// Numeric value conditions (no quotes around value)
	// -------------------------------------------------------------------------

	/**
	 * Appends {@code "column" = value} with the value unquoted. Use for numeric
	 * columns to preserve index usage and correct semantics.
	 */
	public SQLWhereClause eqNumeric(String column, long value) {
		validateColumn(column);
		sb.append(quoteCol(column)).append(" = ").append(value);
		lastWasConnector = false;
		return this;
	}

	/** Appends {@code "column" > value} with the value unquoted. */
	public SQLWhereClause gtNumeric(String column, long value) {
		validateColumn(column);
		sb.append(quoteCol(column)).append(" > ").append(value);
		lastWasConnector = false;
		return this;
	}

	/** Appends {@code "column" >= value} with the value unquoted. */
	public SQLWhereClause gteNumeric(String column, long value) {
		validateColumn(column);
		sb.append(quoteCol(column)).append(" >= ").append(value);
		lastWasConnector = false;
		return this;
	}

	/** Appends {@code "column" < value} with the value unquoted. */
	public SQLWhereClause ltNumeric(String column, long value) {
		validateColumn(column);
		sb.append(quoteCol(column)).append(" < ").append(value);
		lastWasConnector = false;
		return this;
	}

	/** Appends {@code "column" <= value} with the value unquoted. */
	public SQLWhereClause lteNumeric(String column, long value) {
		validateColumn(column);
		sb.append(quoteCol(column)).append(" <= ").append(value);
		lastWasConnector = false;
		return this;
	}

	// -------------------------------------------------------------------------
	// Logical connectors
	// -------------------------------------------------------------------------

	/**
	 * Appends {@code AND}.
	 * <p>
	 * Throws {@link IllegalStateException} if the builder is empty or if the last
	 * token was already a connector -- preventing malformed clauses.
	 */
	public SQLWhereClause and() {
		if (sb.length() == 0 || lastWasConnector)
			throw new IllegalStateException(
					"Cannot append AND: builder is empty or last token was already a connector.");
		sb.append(" AND ");
		lastWasConnector = true;
		return this;
	}

	/**
	 * Appends {@code OR}.
	 * <p>
	 * Throws {@link IllegalStateException} if the builder is empty or if the last
	 * token was already a connector.
	 */
	public SQLWhereClause or() {
		if (sb.length() == 0 || lastWasConnector)
			throw new IllegalStateException(
					"Cannot append OR: builder is empty or last token was already a connector.");
		sb.append(" OR ");
		lastWasConnector = true;
		return this;
	}

	/**
	 * Opens a parenthesis group. Must be closed with {@link #endGroup()}.
	 * <p>
	 * Example: {@code .beginGroup().eq("a","1").or().eq("a","2").endGroup()}
	 * produces {@code ("a" = '1' OR "a" = '2')}.
	 */
	public SQLWhereClause beginGroup() {
		sb.append("(");
		openGroups++;
		lastWasConnector = false;
		return this;
	}

	/**
	 * Closes a parenthesis group opened by {@link #beginGroup()}.
	 *
	 * @throws IllegalStateException if no group is currently open
	 */
	public SQLWhereClause endGroup() {
		if (openGroups == 0)
			throw new IllegalStateException("endGroup() called without a matching beginGroup().");
		sb.append(")");
		openGroups--;
		lastWasConnector = false;
		return this;
	}

	/**
	 * Conditionally appends {@code AND rawClause} only if condition is true and
	 * rawClause is non-null/non-empty. If the builder is currently empty, the
	 * rawClause is appended without a leading AND.
	 *
	 * <p>
	 * <strong>Security note:</strong> rawClause is appended as-is. Use only with
	 * trusted, internally constructed strings.
	 *
	 * @param condition if false, nothing is appended
	 * @param rawClause raw WHERE fragment (no WHERE keyword)
	 */
	public SQLWhereClause andIf(boolean condition, String rawClause) {
		if (condition && rawClause != null && !rawClause.trim().isEmpty()) {
			if (sb.length() > 0 && !lastWasConnector)
				sb.append(" AND ");
			sb.append(rawClause);
			lastWasConnector = false;
		}
		return this;
	}

	/**
	 * Appends a raw SQL fragment as-is.
	 *
	 * <p>
	 * <strong>Security note:</strong> use only with trusted, internally constructed
	 * strings -- never with end-user input.
	 *
	 * @param rawClause raw SQL fragment (no WHERE keyword)
	 */
	public SQLWhereClause raw(String rawClause) {
		if (rawClause != null && !rawClause.trim().isEmpty()) {
			sb.append(rawClause);
			lastWasConnector = false;
		}
		return this;
	}

	// -------------------------------------------------------------------------
	// Terminal methods
	// -------------------------------------------------------------------------

	/**
	 * Returns the assembled WHERE clause <em>without</em> the {@code WHERE}
	 * keyword, or {@code null} if nothing was added.
	 *
	 * @throws IllegalStateException if there are unclosed parenthesis groups
	 */
	public String build() {
		if (openGroups > 0)
			throw new IllegalStateException(openGroups + " unclosed group(s). Call endGroup() before build().");
		String result = sb.toString().trim();
		return result.isEmpty() ? null : result;
	}

	/**
	 * Returns the assembled WHERE clause <em>with</em> the {@code WHERE} keyword
	 * prepended, or {@code ""} if nothing was added.
	 * <p>
	 * Note: unlike {@link #build()}, this returns {@code ""} (not null) for an
	 * empty builder.
	 */
	public String buildWithKeyword() {
		String result = build();
		return result == null ? "" : "WHERE " + result;
	}

	// -------------------------------------------------------------------------
	// Internal helpers
	// -------------------------------------------------------------------------

	/**
	 * Validates that a column name contains only letters, digits, underscores, and
	 * spaces (to support names like "Parse Date"). Throws
	 * {@link IllegalArgumentException} for anything else to prevent identifier
	 * injection.
	 */
	private static void validateColumn(String column) {
		if (column == null || column.trim().isEmpty())
			throw new IllegalArgumentException("Column name must not be null or empty.");
		// Allow letters, digits, underscore, hyphen, space, dot (schema.table.col
		// style)
		if (!column.matches("[A-Za-z0-9 _.\\-\\[\\]]+"))
			throw new IllegalArgumentException("Column name contains disallowed characters: '" + column + "'. "
					+ "Only letters, digits, spaces, underscores, hyphens, dots, and brackets are permitted.");
	}

	private String quoteCol(String column) {
		// Escape any internal quote characters before wrapping
		switch (quoteStyle) {
		case MYSQL:
			return "`" + column.replace("`", "``") + "`";
		case POSTGRESQL:
		default:
			return "\"" + column.replace("\"", "\"\"") + "\"";
		}
	}

	private static String quoteVal(String value) {
		if (value == null)
			return "NULL";
		// Standard SQL escaping: double up single quotes
		return "'" + value.replace("'", "''") + "'";
	}
}