package com.github.TKnudsen.ComplexDataObject.model.io.parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.BooleanParser;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.DateParser;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.DoubleParser;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IntegerParser;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.LongParser;

/**
 * <p>
 * Tools class for parsers. It handles date conversions, loads rows
 * from files, etc.
 * </p>
 *
 * @version 1.0
 * @since 2015
 */
public abstract class ParserTools implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3729971051948506651L;

//	// DateFormats
//	private static SimpleDateFormat ISO0 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
//	private static SimpleDateFormat ISO1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//	private static SimpleDateFormat IS02 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
//	private static SimpleDateFormat IS03 = new SimpleDateFormat("yyyy-MM-dd HH");
//	private static SimpleDateFormat IS04 = new SimpleDateFormat("yyyy-MM-dd");
//	private static SimpleDateFormat IS05 = new SimpleDateFormat("yyyy-MM");
//	private static SimpleDateFormat IS06 = new SimpleDateFormat("dd.MM.yyyy");
//	private static SimpleDateFormat IS06b = new SimpleDateFormat("dd_MM_yyyy");
//	private static SimpleDateFormat IS07 = new SimpleDateFormat("dd.MM.yyyy HH:mm");
//	private static SimpleDateFormat IS08 = new SimpleDateFormat("MM.dd.yyyy");

	private static BooleanParser booleanParser = new BooleanParser();
	private static IntegerParser integerParser = new IntegerParser();
	private static LongParser longParser = new LongParser();
	private static DoubleParser doubleParser = new DoubleParser();
	private static DateParser dateParser = new DateParser();

	/**
	 * Parses date-oriented tokens. Checks most of the popular date formats.
	 * 
	 * @param token token as String.
	 * @return
	 */
	public static synchronized Date parseDate(Object token) {
		return dateParser.apply(token);
	}

	public static synchronized Boolean parseBoolean(Object token) {
		return booleanParser.apply(token);
	}

	public static synchronized Integer parseInteger(Object token) {
		return integerParser.apply(token);
	}

	public static synchronized Long parseLong(Object token) {
		return longParser.apply(token);
	}

	public static synchronized Double parseDouble(Object token) {
		return doubleParser.apply(token);
	}

	public static synchronized Float parseFloat(Object token) {
		return doubleParser.apply(token).floatValue();
	}

//	/**
//	 * method for loading data from a file. data is returned row-wise as a List of
//	 * Strings
//	 * 
//	 * @param dataFile
//	 * @return
//	 * @throws IOException
//	 */
//	public static List<String> loadRows(String dataFile) throws IOException {
//		// DATAFILE ACCESS
//		List<String> rows = new ArrayList<String>();
//		System.out.println("reading " + dataFile + " ...");
//		File file = new File(dataFile);
//		BufferedReader reader = null;
//
//		// file input
//		try {
//			reader = new BufferedReader(new FileReader(file));
//		} catch (FileNotFoundException ex) {
//			throw new FileNotFoundException("FileNotFoundException...");
//		}
//		String line = reader.readLine();
//		while (line != null) {
//			rows.add(line);
//			line = reader.readLine();
//		}
//
//		if (reader != null)
//			reader.close();
//
//		return rows;
//	}
	
	public static List<String> loadRows(String dataFile) throws IOException {
	    return loadRows(dataFile, StandardCharsets.UTF_8);
	}

	public static List<String> loadRows(String dataFile, Charset charset) throws IOException {
	    List<String> rows = new ArrayList<>();
	    System.out.println("reading " + dataFile + " ...");

	    File file = new File(dataFile);
	    BufferedReader reader = null;
	    try {
	        reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));
	        String line = reader.readLine();
	        while (line != null) {
	            rows.add(line);
	            line = reader.readLine();
	        }
	    } catch (FileNotFoundException ex) {
	        throw new FileNotFoundException("FileNotFoundException...");
	    } finally {
	        if (reader != null)
	            reader.close();
	    }

	    return rows;
	}

//	/**
//	 * 
//	 * @param file
//	 * @param tokenizerSeparator
//	 * @return
//	 * @throws IOException
//	 */
//	public static List<List<String>> loadTokens(String file, String tokenizerSeparator) throws IOException {
//		List<String> rows = loadRows(file);
//
//		List<List<String>> tokens = new ArrayList<List<String>>();
//		int coloumbsCount = 0;
//		for (int i = 0; i < rows.size(); i++) {
//			String row = rows.get(i);
//			List<String> lineTokens = new ArrayList<String>();
//
//			while (true) {
//				if (row.contains(tokenizerSeparator)) {
//					lineTokens.add(row.substring(0, row.indexOf(tokenizerSeparator)));
//					row = row.substring(row.indexOf(tokenizerSeparator) + tokenizerSeparator.length(), row.length());
//					if (!row.contains(tokenizerSeparator))
//						lineTokens.add(row.trim());
//					continue;
//				}
//
//				tokens.add(lineTokens);
//				if (coloumbsCount < lineTokens.size())
//					coloumbsCount = lineTokens.size();
//				break;
//			}
//		}
//
//		return tokens;
//	}

	/**
	 * @param file
	 * @param tokenizerSeparator
	 * @return
	 * @throws IOException
	 */
	public static List<List<String>> loadTokens(String file, String tokenizerSeparator) throws IOException {
		List<String> rows = loadRows(file);

		List<List<String>> tokens = new ArrayList<>();
		int columnsCount = 0;

		for (String row : rows) {
			List<String> lineTokens = tokenizeRow(row, tokenizerSeparator);
			tokens.add(lineTokens);
			if (columnsCount < lineTokens.size())
				columnsCount = lineTokens.size();
		}

		return tokens;
	}

	/**
	 * Splits a single row by {@code separator}, treating content enclosed in double
	 * quotes as a single token. The enclosing quotes are stripped from the returned
	 * token value.
	 *
	 * @param row       the raw line to split
	 * @param separator the field delimiter (e.g. ",")
	 * @return ordered list of token values for this row
	 */
	private static List<String> tokenizeRow(String row, String separator) {
		List<String> lineTokens = new ArrayList<>();
		StringBuilder current = new StringBuilder();
		boolean inQuotes = false;
		int sepLen = separator.length();

		for (int i = 0; i < row.length(); i++) {
			char c = row.charAt(i);

			if (c == '"') {
				inQuotes = !inQuotes;
				continue; // strip the quote character itself
			}

			// Check whether the separator starts at position i (outside quotes)
			if (!inQuotes && row.startsWith(separator, i)) {
				lineTokens.add(current.toString().trim());
				current.setLength(0);
				i += sepLen - 1; // -1 because the for-loop will increment
				continue;
			}

			current.append(c);
		}

		// Add the final token (covers rows with no separator and the last field)
		lineTokens.add(current.toString().trim());

		return lineTokens;
	}

	/**
	 * assigns an identifier and an object to an entry.
	 * 
	 * @param attribute
	 * @param value
	 * @param missingValueIndicator
	 * @return an entry with the value parsed to the attribute type
	 */
	public static Entry<String, ?> parseValue(String attribute, Class<?> classType, Object value,
			String missingValueIndicator) {

		if (attribute == null || value == null)
			return null;

		Entry<String, ?> entry = null;

		// Integer
		if (classType.equals(Integer.class))
			entry = new SimpleEntry<String, Integer>(attribute, parseInteger(value));

		// Double
		else if (classType.equals(Double.class))
			if (String.valueOf(value).equals("") || String.valueOf(value).equals(missingValueIndicator))
				entry = new SimpleEntry<String, Double>(attribute, Double.NaN);
			else {
				entry = new SimpleEntry<String, Double>(attribute, parseDouble(value));
			}

		// Float
		else if (classType.equals(Float.class))
			if (String.valueOf(value).equals("") || String.valueOf(value).equals(missingValueIndicator))
				entry = new SimpleEntry<String, Float>(attribute, Float.NaN);
			else {
				entry = new SimpleEntry<String, Float>(attribute, parseFloat(value));
			}

		// Date (real date)
		else if (classType.equals(Date.class))
			if (String.valueOf(value).equals(""))
				entry = new SimpleEntry<String, Date>(attribute, null);
			else
				entry = new SimpleEntry<String, Date>(attribute, parseDate(value));

		// String
		else if (classType.equals(String.class))
			entry = new SimpleEntry<String, String>(attribute, new String(String.valueOf(value)));

		// Boolean
		else if (classType.equals(Boolean.class)) {
			entry = new SimpleEntry<String, Boolean>(attribute, parseBoolean(value));
		}

		return entry;
	}

	/**
	 * Filters out rows with token counts unequal the most frequent token count
	 * (mode), in case there is a dominating count.
	 *
	 * @param tokens List of rows, where each row is a list of token strings.
	 * @param report Print out error log for rows filtered out.
	 * @return a new list containing only rows with the dominating token count.
	 */
	public static List<List<String>> validateTokenCount(List<List<String>> tokens, boolean report) {
		if (tokens == null) {
			if (report)
				System.out.println("ParserTools.validateTokenCount: Input tokens is null. Nothing to do.");
			return Collections.emptyList();
		}
		if (tokens.isEmpty()) {
			if (report)
				System.out.println("ParserTools.validateTokenCount: Input tokens is empty. Nothing to do.");
			return tokens;
		}

		// Build histogram: tokenCount -> frequency
		Map<Integer, Long> hist = tokens.stream().map(row -> row == null ? 0 : row.size())
				.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

		// Determine the mode (dominating size)
		long maxFreq = hist.values().stream().mapToLong(Long::longValue).max().orElse(0L);

		List<Integer> candidates = hist.entrySet().stream().filter(e -> e.getValue() == maxFreq).map(Map.Entry::getKey)
				.sorted().collect(Collectors.toList());

		if (report)
			System.out.println("ParserTools.validateTokenCount: Token-count distribution (count -> rows): "
					+ toSortedString(hist));

		if (candidates.size() != 1) {
			// No single dominating size; refusing to silently drop possibly-good rows
			System.out
					.println("ParserTools.validateTokenCount: ERROR: No unique dominating token count. Top candidates: "
							+ candidates + " with frequency=" + maxFreq);
			throw new IllegalStateException("No unique dominating token count (mode). Candidates=" + candidates);
		}

		int dominatingSize = candidates.get(0);

		int totalRows = tokens.size();
		List<List<String>> kept = new ArrayList<>(totalRows);
		List<Integer> removedSizes = new ArrayList<>();
		int nullRows = 0;

		for (List<String> row : tokens) {
			if (row == null) {
				nullRows++;
				removedSizes.add(0);
				continue;
			}
			int sz = row.size();
			if (sz == dominatingSize) {
				kept.add(row);
			} else {
				removedSizes.add(sz);
			}
		}

		int removed = totalRows - kept.size();

		// Additional reporting: how many removed per token size
		Map<Integer, Long> removedHist = removedSizes.stream()
				.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

		if (report)
			System.out.println("ParserTools.validateTokenCount: Dominating token count (mode): " + dominatingSize
					+ " (rows=" + maxFreq + "/" + totalRows + ")");

		if (report)
			System.out.println("ParserTools.validateTokenCount: Kept rows: " + kept.size() + "/" + totalRows);
		if (report)
			System.out.println("ParserTools.validateTokenCount: Removed rows: " + removed + "/" + totalRows
					+ (nullRows > 0 ? " (includes null rows=" + nullRows + ")" : ""));

		if (removed > 0) {
			if (report)
				System.err.println("ParserTools.validateTokenCount: Removed token-count distribution (count -> rows): "
						+ toSortedString(removedHist));
		} else {
			if (report)
				System.out.println("ParserTools.validateTokenCount: No rows removed.");
		}

		return kept;
	}

	private static String toSortedString(Map<Integer, Long> map) {
		return map.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(e -> e.getKey() + " -> " + e.getValue())
				.collect(Collectors.joining(", ", "{", "}"));
	}

}
