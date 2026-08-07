package com.github.TKnudsen.ComplexDataObject.model.tools;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainers;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.Parsers;

/**
 * <p>
 * Generates structured LaTeX tables for different types of data,
 * including ComplexDataObject, numerical statistics, and categorical data
 * distributions.
 * </p>
 *
 * @version 1.01
 * @since 2025
 */
public class LatexTools {

	// ====================================================================
	// CONSTANTS - LaTeX Commands
	// ====================================================================

	private static final String TABLE_BEGIN = "\\begin{table}[H]\n";
	private static final String TABLE_END = "\\end{table}\n";
	private static final String TABULAR_BEGIN_2COL = "\\begin{tabular}{@{}ll@{}}\n";
	private static final String TABULAR_BEGIN_3COL = "\\begin{tabular}{@{}llr@{}}\n";
	private static final String TABULAR_END = "\\end{tabular}\n";
	private static final String CENTERING = "\\centering\n";
	private static final String SMALL = "\\small\n";
	private static final String TOPRULE = "\\toprule\n";
	private static final String MIDRULE = "\\midrule\n";
	private static final String BOTTOMRULE = "\\bottomrule\n";
	private static final String LINE_BREAK = " \\\\\n";

	// Statistical labels
	private static final String LABEL_SAMPLE_SIZE = "Sample Size (n)";
	private static final String LABEL_MINIMUM = "Minimum";
	private static final String LABEL_MAXIMUM = "Maximum";
	private static final String LABEL_MEAN = "Mean";
	private static final String LABEL_STD_DEV = "Standard Deviation";
	private static final String LABEL_MEDIAN = "Median";
	private static final String LABEL_SKEWNESS = "Skewness";
	private static final String LABEL_KURTOSIS = "Kurtosis";
	private static final String LABEL_MISSING = "Missing Values (\\%)";

	// ====================================================================
	// THREAD-SAFE FORMATTERS
	// ====================================================================

	/**
	 * Thread-local formatter for integer values with thousand separators.
	 */
	private static final ThreadLocal<DecimalFormat> INT_FORMAT = ThreadLocal
			.withInitial(() -> new DecimalFormat("####"));

	/**
	 * Thread-local formatter for floating-point values with 2 decimal places.
	 */
	private static final ThreadLocal<DecimalFormat> FLOAT_FORMAT = ThreadLocal
			.withInitial(() -> new DecimalFormat("###0.00"));

	/**
	 * Thread-local formatter for percentage values with 1 decimal place
	 */
	private static final ThreadLocal<DecimalFormat> PERCENT_FORMAT = ThreadLocal
			.withInitial(() -> new DecimalFormat("0.00"));

	// ====================================================================
	// PUBLIC API METHODS
	// ====================================================================

	/**
	 * Generates a LaTeX table showing an overview of all attributes in a
	 * ComplexDataContainer with their types and missing value rates.
	 * 
	 * @param counts             counts information of the categories
	 * @param categorizationName Name of the categorization for the caption; if null
	 *                           or empty, uses "Unknown Categorization"
	 * @param rawDataCount       to compute the percentages
	 * @param missing            missing value column or, if false, occurrence
	 *                           column. Both in percent.
	 * @return String containing the complete LaTeX table code
	 * @throws IllegalArgumentException if container is null
	 */
	public static String generateCategoryCountsTable(Map<String, Integer> counts, String categorizationName,
			int rawDataCount, boolean missing) {

		if (counts == null)
			throw new IllegalArgumentException("counts cannot be null");

		if (categorizationName == null || categorizationName.trim().isEmpty())
			categorizationName = "Unknown Categorization";

		StringBuilder latex = new StringBuilder(300);

		String caption = "Overview of attributes of the dataset: \\textbf{" + escapeLatex(categorizationName) + "}";
		String label = "tab:attributes_overview";

		// Table header
		appendAttributeTableHeader(latex, caption, label, "Count", missing);

		// Add attributes as rows
		int countOverall = 0;
		for (String key : counts.keySet()) {
			int count = counts.get(key);
			double attributeMissingValueRatePercent = missing
					? MathFunctions.round((1 - count / (double) rawDataCount), 3)
					: MathFunctions.round((count / (double) rawDataCount), 3);
			attributeMissingValueRatePercent *= 100;
			appendAttributeTableRow(latex, escapeLatex(key), INT_FORMAT.get().format(count),
					PERCENT_FORMAT.get().format(attributeMissingValueRatePercent));
			countOverall += count;
		}

		double missingPercent = 0;
		for (String key : counts.keySet())
			if ("null".equals(key) || "".equals(key) || "\"\"".equals(key) || "unknown".equals(key.toLowerCase())
					|| "missing".equals(key.toLowerCase()))
				missingPercent += calculateMissingPercentage(countOverall, counts.get(key));
		appendTableRow(latex, LABEL_MISSING, PERCENT_FORMAT.get().format(missingPercent));

		// Table footer
		appendTableFooter(latex);

		return latex.toString();
	}

	/**
	 * Generates a LaTeX table showing an overview of all attributes in a
	 * ComplexDataContainer with their types and missing value rates.
	 * 
	 * @param container   The complex data container where the data attributes are
	 *                    stored; must not be null
	 * @param datasetName Name of the dataset for the caption; if null or empty,
	 *                    uses "Unknown Dataset"
	 * @return String containing the complete LaTeX table code
	 * @throws IllegalArgumentException if container is null
	 */
	public static String generateDataAttributesTable(ComplexDataContainer container, String datasetName) {

		if (container == null)
			throw new IllegalArgumentException("Container cannot be null");

		if (datasetName == null || datasetName.trim().isEmpty())
			datasetName = "Unknown Dataset";

		StringBuilder latex = new StringBuilder(300);

		String caption = "Overview of attributes of the dataset: \\textbf{" + escapeLatex(datasetName) + "}";
		String label = "tab:attributes_overview";

		// Table header
		appendAttributeTableHeader(latex, caption, label, "Type", true);

		// Add attributes as rows
		for (String attribute : container.getAttributesSorted()) {
			String attributeType = DataConversion.attributeType(container.getType(attribute));
			double attributeMissingValueRatePercent = ComplexDataContainers.getAttributeMissingValueRate(container,
					attribute, true) * 100;
			appendAttributeTableRow(latex, escapeLatex(attribute), attributeType,
					PERCENT_FORMAT.get().format(attributeMissingValueRatePercent));
		}

		// Table footer
		appendTableFooter(latex);

		return latex.toString();
	}

	/**
	 * Generates a LaTeX table showing an overview of all attributes in a
	 * ComplexDataContainer with their types. Uses default dataset name.
	 * 
	 * @param container The complex data container where the data attributes are
	 *                  stored; must not be null
	 * @return String containing the complete LaTeX table code
	 * @throws IllegalArgumentException if container is null
	 */
	public static String generateDataAttributesTable(ComplexDataContainer container) {
		return generateDataAttributesTable(container, null);
	}

	/**
	 * Generates a LaTeX table for numerical attribute statistics using a
	 * StatisticsSupport object.
	 * 
	 * @param attributeName The name of the attribute being analyzed; must not be
	 *                      null or empty
	 * @param statistics    StatisticsSupport object containing computed statistics;
	 *                      must not be null
	 * @param totalRecords  Total number of data instances (including missing
	 *                      values); must be > 0
	 * @return String containing the complete LaTeX table code
	 * @throws IllegalArgumentException if attributeName is null/empty, statistics
	 *                                  is null, or totalRecords <= 0
	 */
	public static String generateNumericalStatsTable(String attributeName, StatisticsSupport statistics,
			int totalRecords) {

		// Input validation
		validateAttributeName(attributeName);
		if (statistics == null) {
			throw new IllegalArgumentException("Statistics object cannot be null");
		}
		if (totalRecords <= 0) {
			throw new IllegalArgumentException("Total records must be greater than 0");
		}

		double missingPercent = calculateMissingPercentage(statistics.getCount(), totalRecords);

		return generateNumericalStatsTable(attributeName, statistics.getCount(), statistics.getMin(),
				statistics.getMax(), statistics.getMean(), statistics.getStandardDeviation(), statistics.getMedian(),
				statistics.getSkewness(), statistics.getKurtosis(), missingPercent);
	}

	/**
	 * Generates a LaTeX table for numerical attribute statistics from raw values.
	 * 
	 * @param attributeName  The name of the attribute being analyzed; must not be
	 *                       null or empty
	 * @param n              Sample size (number of valid values); must be >= 0
	 * @param min            Minimum value
	 * @param max            Maximum value
	 * @param mean           Mean value
	 * @param stdDev         Standard deviation; must be >= 0
	 * @param median         Median value
	 * @param skewness       Skewness coefficient
	 * @param kurtosis       Kurtosis coefficient
	 * @param missingPercent Percentage of missing values; must be in range [0, 100]
	 * @return String containing the complete LaTeX table code
	 * @throws IllegalArgumentException if any parameter is invalid
	 */
	public static String generateNumericalStatsTable(String attributeName, long n, double min, double max, double mean,
			double stdDev, double median, double skewness, double kurtosis, double missingPercent) {

		// Input validation
		validateAttributeName(attributeName);
		if (n < 0) {
			throw new IllegalArgumentException("Sample size cannot be negative");
		}
		if (stdDev < 0) {
			throw new IllegalArgumentException("Standard deviation cannot be negative");
		}
		if (missingPercent < 0 || missingPercent > 100) {
			throw new IllegalArgumentException("Missing percentage must be between 0 and 100");
		}

		StringBuilder latex = new StringBuilder(500); // Pre-allocate reasonable capacity

		String label = "tab:freq_" + attributeName.toLowerCase().replace(" ", "_");
		String caption = "Statistics for numerical attribute: \\textbf{" + escapeLatex(attributeName) + "}";

		// Table header
		appendTableHeader(latex, caption, label, new String[] { "Statistic", "Value" });

		// Statistics rows
		appendTableRow(latex, LABEL_SAMPLE_SIZE, INT_FORMAT.get().format(n));
		appendTableRow(latex, LABEL_MINIMUM, FLOAT_FORMAT.get().format(min));
		appendTableRow(latex, LABEL_MAXIMUM, FLOAT_FORMAT.get().format(max));
		appendTableRow(latex, LABEL_MEAN, FLOAT_FORMAT.get().format(mean));
		appendTableRow(latex, LABEL_STD_DEV, FLOAT_FORMAT.get().format(stdDev));
		appendTableRow(latex, LABEL_MEDIAN, FLOAT_FORMAT.get().format(median));
		appendTableRow(latex, LABEL_SKEWNESS, FLOAT_FORMAT.get().format(skewness));
		appendTableRow(latex, LABEL_KURTOSIS, FLOAT_FORMAT.get().format(kurtosis));
		appendTableRow(latex, LABEL_MISSING, PERCENT_FORMAT.get().format(missingPercent));

		// Table footer
		appendTableFooter(latex);

		return latex.toString();
	}

	/**
	 * Generates a LaTeX table for categorical attribute frequency distribution.
	 * 
	 * <p>
	 * This method creates a formatted table showing each category value with its
	 * occurrence count and percentage of the total. Categories are displayed in the
	 * order provided by the map's iterator (use LinkedHashMap for custom ordering,
	 * or sort the map beforehand for alphabetical/frequency-based ordering).
	 * 
	 * <p>
	 * The table includes:
	 * <ul>
	 * <li>Category values (escaped for LaTeX compatibility)</li>
	 * <li>Absolute frequencies (occurrence counts with thousand separators)</li>
	 * <li>Relative frequencies (percentages with 1 decimal place)</li>
	 * <li>Missing values count and percentage (if applicable)</li>
	 * <li>Total row summarizing all categories</li>
	 * </ul>
	 * 
	 * @param attributeName The name of the categorical attribute being analyzed;
	 *                      must not be null or empty
	 * @param occurrences   Map of category values to their occurrence counts; must
	 *                      not be null. Keys represent category values (use empty
	 *                      string "" for missing/null values), values represent
	 *                      counts. All counts must be >= 0.
	 * @param totalRecords  Total number of data instances (including missing
	 *                      values); must be > 0. Used to calculate missing values
	 *                      if the sum of occurrences is less than totalRecords.
	 * @return String containing the complete LaTeX table code
	 * @throws IllegalArgumentException if attributeName is null/empty, occurrences
	 *                                  is null, totalRecords <= 0, or any
	 *                                  occurrence count is negative
	 * 
	 * @example
	 * 
	 *          <pre>
	 *          Map&lt;String, Integer&gt; genderCounts = new LinkedHashMap&lt;&gt;();
	 *          genderCounts.put("Male", 542);
	 *          genderCounts.put("Female", 486);
	 *          genderCounts.put("Other", 12);
	 *          genderCounts.put("", 5); // Missing values
	 * 
	 *          String latexTable = generateCategoricalFrequencyTable("Gender", genderCounts, 1045);
	 *          </pre>
	 * 
	 * @see #generateNumericalStatsTable(String, StatisticsSupport, int)
	 * @see #generateBooleanFrequencyTable(String, int, int, int)
	 */
	public static String generateCategoricalFrequencyTable(String attributeName, Map<String, Integer> occurrences,
			int totalRecords) {

		// Input validation
		validateAttributeName(attributeName);

		if (occurrences == null) {
			throw new IllegalArgumentException("Occurrences map cannot be null");
		}

		if (totalRecords <= 0) {
			throw new IllegalArgumentException("Total records must be greater than 0");
		}

		// Validate all counts are non-negative
		for (Map.Entry<String, Integer> entry : occurrences.entrySet()) {
			if (entry.getValue() < 0) {
				throw new IllegalArgumentException(
						"Occurrence count for category '" + entry.getKey() + "' cannot be negative");
			}
		}

		StringBuilder latex = new StringBuilder(600);

		String label = "tab:freq_" + attributeName.toLowerCase().replace(" ", "_");
		String caption = "Frequency distribution for categorical attribute: \\textbf{" + escapeLatex(attributeName)
				+ "}";

		// Table header with 3 columns
		appendCategoricalTableHeader(latex, caption, label);

		// Calculate total count from occurrences
		long totalCount = 0;
		int missingCount = 0;

		// Process categories and add rows
		for (Map.Entry<String, Integer> entry : occurrences.entrySet()) {
			String category = entry.getKey();
			int count = entry.getValue();

			// Check if this represents missing values
			if (Parsers.isMissingValue(category)) {
				missingCount = count;
			} else {
				totalCount += count;
				double percentage = (count * 100.0) / totalRecords;

				appendCategoricalRow(latex, escapeLatex(category), INT_FORMAT.get().format(count),
						PERCENT_FORMAT.get().format(percentage));
			}
		}

		// add the gap to the missing count calculation
		missingCount += (Math.max(0, totalRecords - totalCount));

		// Add missing values row if present
		if (missingCount > 0) {
			double missingPercentage = (missingCount * 100.0) / totalRecords;
			latex.append(MIDRULE);
			appendCategoricalRow(latex, "\\textit{(missing/empty)}", INT_FORMAT.get().format(missingCount),
					PERCENT_FORMAT.get().format(missingPercentage));
		}

		// Add total row
		latex.append(MIDRULE);
		long grandTotal = totalCount + missingCount;
		appendCategoricalRow(latex, "\\textbf{Total}", "\\textbf{" + INT_FORMAT.get().format(grandTotal) + "}",
				"\\textbf{100.0}");

		// Table footer
		appendTableFooter(latex);

		return latex.toString();
	}

	/**
	 * Generates a LaTeX table for boolean attribute frequency distribution.
	 * 
	 * <p>
	 * This is a specialized version of the categorical frequency table optimized
	 * for binary (true/false) data. It displays True and False counts with their
	 * percentages, along with missing values if present.
	 * 
	 * @param attributeName The name of the boolean attribute being analyzed; must
	 *                      not be null or empty
	 * @param trueCount     Number of true/yes/1 values; must be >= 0
	 * @param falseCount    Number of false/no/0 values; must be >= 0
	 * @param missingCount  Number of missing/null values; must be >= 0
	 * @return String containing the complete LaTeX table code
	 * @throws IllegalArgumentException if attributeName is null/empty or any count
	 *                                  is negative
	 * 
	 * @example
	 * 
	 *          <pre>
	 *          String latexTable = generateBooleanFrequencyTable("Is_Premium", 342, 658, 5);
	 *          </pre>
	 * 
	 * @see #generateCategoricalFrequencyTable(String, Map, int)
	 */
	public static String generateBooleanFrequencyTable(String attributeName, int trueCount, int falseCount,
			int missingCount) {

		// Input validation
		validateAttributeName(attributeName);

		if (trueCount < 0) {
			throw new IllegalArgumentException("True count cannot be negative");
		}
		if (falseCount < 0) {
			throw new IllegalArgumentException("False count cannot be negative");
		}
		if (missingCount < 0) {
			throw new IllegalArgumentException("Missing count cannot be negative");
		}

		int totalRecords = trueCount + falseCount + missingCount;

		if (totalRecords == 0) {
			throw new IllegalArgumentException("Total count must be greater than 0");
		}

		StringBuilder latex = new StringBuilder(500);

		String label = "tab:freq_" + attributeName.toLowerCase().replace(" ", "_");
		String caption = "Frequency distribution for boolean attribute: \\textbf{" + escapeLatex(attributeName) + "}";

		// Table header
		appendCategoricalTableHeader(latex, caption, label);

		// True row
		double truePercentage = (trueCount * 100.0) / totalRecords;
		appendCategoricalRow(latex, "True", INT_FORMAT.get().format(trueCount),
				PERCENT_FORMAT.get().format(truePercentage));

		// False row
		double falsePercentage = (falseCount * 100.0) / totalRecords;
		appendCategoricalRow(latex, "False", INT_FORMAT.get().format(falseCount),
				PERCENT_FORMAT.get().format(falsePercentage));

		// Missing values row if present
		if (missingCount > 0) {
			double missingPercentage = (missingCount * 100.0) / totalRecords;
			latex.append(MIDRULE);
			appendCategoricalRow(latex, "\\textit{(missing/null)}", INT_FORMAT.get().format(missingCount),
					PERCENT_FORMAT.get().format(missingPercentage));
		}

		// Total row
		latex.append(MIDRULE);
		appendCategoricalRow(latex, "\\textbf{Total}", "\\textbf{" + INT_FORMAT.get().format(totalRecords) + "}",
				"\\textbf{100.0}");

		// Table footer
		appendTableFooter(latex);

		return latex.toString();
	}

	/**
	 * converts a list of text labels into a latex-compatible representation.
	 * 
	 * @param labels
	 * @return
	 */
	public static List<String> convertToLatex(List<String> labels) {
		if (labels == null)
			return null;

		List<String> result = new ArrayList<String>();
		if (labels.isEmpty())
			return result;

		for (String label : labels)
			result.add(escapeLatex(label));

		return result;
	}

	// ====================================================================
	// HELPER METHODS - Table Structure
	// ====================================================================

	/**
	 * Appends the opening LaTeX table structure for attribute overview table with
	 * three columns: Attribute, Type, and Missing Values (%).
	 * 
	 * @param latex   StringBuilder to append to
	 * @param caption Table caption text (already escaped if needed)
	 * @param label   Table label for referencing; can be null
	 * @param missing missing value column or, if false, occurrence column. Both in
	 *                percent.
	 */
	private static void appendAttributeTableHeader(StringBuilder latex, String caption, String label,
			String centerColumnName, boolean missing) {
		latex.append(TABLE_BEGIN);
		latex.append(CENTERING);
		latex.append(SMALL);
		latex.append("\\caption{").append(caption).append("}\n");

		if (label != null && !label.isEmpty()) {
			latex.append("\\label{").append(label).append("}\n");
		}

		latex.append(TABULAR_BEGIN_3COL);
		latex.append(TOPRULE);
		if (missing)
			latex.append("\\textbf{Attribute} & \\textbf{" + centerColumnName + "} & \\textbf{Missing (\\%)}");
		else
			latex.append("\\textbf{Attribute} & \\textbf{" + centerColumnName + "} & \\textbf{Occurrence (\\%)}");
		latex.append(LINE_BREAK);
		latex.append(MIDRULE);
	}

	/**
	 * Appends a single row to the attribute overview table with three columns.
	 * 
	 * @param latex             StringBuilder to append to
	 * @param attribute         Attribute name (already escaped if needed)
	 * @param type              Attribute type
	 * @param missingPercentage Formatted missing value percentage string
	 */
	private static void appendAttributeTableRow(StringBuilder latex, String attribute, String type,
			String missingPercentage) {
		latex.append(attribute).append(" & ").append(type).append(" & ").append(missingPercentage).append(LINE_BREAK);
	}

	/**
	 * Appends the opening LaTeX table structure including caption, label, and
	 * column headers.
	 * 
	 * @param latex         StringBuilder to append to
	 * @param caption       Table caption text (already escaped if needed)
	 * @param label         Table label for referencing; can be null
	 * @param columnHeaders Array of column header names
	 */
	private static void appendTableHeader(StringBuilder latex, String caption, String label, String[] columnHeaders) {
		latex.append(TABLE_BEGIN);
		latex.append(CENTERING);
		latex.append(SMALL);
		latex.append("\\caption{").append(caption).append("}\n");

		if (label != null && !label.isEmpty()) {
			latex.append("\\label{").append(label).append("}\n");
		}

		latex.append(TABULAR_BEGIN_2COL);
		latex.append(TOPRULE);

		// Column headers
		for (int i = 0; i < columnHeaders.length; i++) {
			latex.append("\\textbf{").append(columnHeaders[i]).append("}");
			if (i < columnHeaders.length - 1) {
				latex.append(" & ");
			}
		}
		latex.append(LINE_BREAK);
		latex.append(MIDRULE);
	}

	/**
	 * Appends the closing LaTeX table structure.
	 * 
	 * @param latex StringBuilder to append to
	 */
	private static void appendTableFooter(StringBuilder latex) {
		latex.append(BOTTOMRULE);
		latex.append(TABULAR_END);
		latex.append(TABLE_END);
	}

	/**
	 * Appends a single table row with two columns.
	 * 
	 * @param latex StringBuilder to append to
	 * @param label First column content
	 * @param value Second column content
	 */
	private static void appendTableRow(StringBuilder latex, String label, String value) {
		latex.append(label).append(" & ").append(value).append(LINE_BREAK);
	}

	/**
	 * Appends the header for a categorical frequency table with three columns.
	 * 
	 * @param latex   StringBuilder to append to
	 * @param caption Table caption
	 * @param label   Table label for referencing
	 */
	private static void appendCategoricalTableHeader(StringBuilder latex, String caption, String label) {
		latex.append(TABLE_BEGIN);
		latex.append(CENTERING);
		latex.append(SMALL);
		latex.append("\\caption{").append(caption).append("}\n");

		if (label != null && !label.isEmpty()) {
			latex.append("\\label{").append(label).append("}\n");
		}

		// Three-column table: Category | Count | Percentage
		latex.append("\\begin{tabular}{@{}lrr@{}}\n");
		latex.append(TOPRULE);
		latex.append("\\textbf{Category} & \\textbf{Count} & \\textbf{Percentage (\\%)}");
		latex.append(LINE_BREAK);
		latex.append(MIDRULE);
	}

	/**
	 * Appends a single row to a categorical frequency table.
	 * 
	 * @param latex      StringBuilder to append to
	 * @param category   Category name (already escaped if needed)
	 * @param count      Formatted count string
	 * @param percentage Formatted percentage string
	 */
	private static void appendCategoricalRow(StringBuilder latex, String category, String count, String percentage) {
		latex.append(category).append(" & ").append(count).append(" & ").append(percentage).append(LINE_BREAK);
	}

	// ====================================================================
	// HELPER METHODS - Calculations and Validation
	// ====================================================================

	/**
	 * Calculates the percentage of missing values.
	 * 
	 * @param validCount Number of valid (non-missing) values
	 * @param totalCount Total number of values including missing
	 * @return Percentage of missing values (0.0 to 100.0)
	 */
	private static double calculateMissingPercentage(long validCount, int totalCount) {
		if (totalCount <= 0) {
			return 0.0;
		}

		long missingCount = totalCount - validCount;
		if (missingCount < 0) {
			// Shouldn't happen, but handle gracefully
			return 0.0;
		}

		return (missingCount * 100.0) / (double) totalCount;
	}

	/**
	 * Validates that an attribute name is not null or empty.
	 * 
	 * @param attributeName The attribute name to validate
	 * @throws IllegalArgumentException if attributeName is null or empty
	 */
	private static void validateAttributeName(String attributeName) {
		if (attributeName == null || attributeName.trim().isEmpty()) {
			throw new IllegalArgumentException("Attribute name cannot be null or empty");
		}
	}

	// ====================================================================
	// HELPER METHODS - Text Escaping
	// ====================================================================

	/**
	 * Escapes special LaTeX characters in strings using a single-pass algorithm for
	 * optimal performance.
	 * 
	 * <p>
	 * Escapes the following characters:
	 * <ul>
	 * <li>_ to \_</li>
	 * <li>& to \&</li>
	 * <li>% to \%</li>
	 * <li>$ to \$</li>
	 * <li># to \#</li>
	 * <li>{ to \{</li>
	 * <li>} to \}</li>
	 * <li>~ to \textasciitilde{}</li>
	 * <li>^ to \textasciicircum{}</li>
	 * </ul>
	 * 
	 * @param text The text to escape; can be null
	 * @return Escaped text safe for LaTeX; empty string if input is null
	 */
	public static String escapeLatex(String text) {
		if (text == null || text.isEmpty()) {
			return "";
		}

		// Quick check if any escaping is needed
		if (!needsEscaping(text)) {
			return text;
		}

		// Single-pass escaping with StringBuilder
		StringBuilder result = new StringBuilder(text.length() + 10);

		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			switch (c) {
			case '_':
				result.append("\\_");
				break;
			case '&':
				result.append("\\&");
				break;
			case '%':
				result.append("\\%");
				break;
			case '$':
				result.append("\\$");
				break;
			case '#':
				result.append("\\#");
				break;
			case '{':
				result.append("\\{");
				break;
			case '}':
				result.append("\\}");
				break;
			case '~':
				result.append("\\textasciitilde{}");
				break;
			case '^':
				result.append("\\textasciicircum{}");
				break;
			case '>':
				result.append("$\\xrightarrow{}$");
				break;
			case '<':
				result.append("$\\xleftarrow{}$");
				break;
			default:
				result.append(c);
				break;
			}
		}

		return result.toString();
	}

	/**
	 * Checks if a string contains any characters that need LaTeX escaping.
	 * 
	 * @param text The text to check
	 * @return true if the text contains LaTeX special characters
	 */
	private static boolean needsEscaping(String text) {
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == '_' || c == '&' || c == '%' || c == '$' || c == '#' || c == '{' || c == '}' || c == '~' || c == '^'
					|| c == '<' || c == '>') {
				return true;
			}
		}
		return false;
	}
}