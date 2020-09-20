package com.github.TKnudsen.ComplexDataObject.model.io.parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.BooleanParser;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.DateParser;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.DoubleParser;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IntegerParser;

/**
 * <p>
 * Title: ParserTools
 * </p>
 * 
 * <p>
 * Description: Tools class for parsers. It handles date conversions, loads rows
 * from files, etc.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2015
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.0
 */
public abstract class ParserTools implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3729971051948506651L;

	// DateFormats
	private static SimpleDateFormat ISO0 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
	private static SimpleDateFormat ISO1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static SimpleDateFormat IS02 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private static SimpleDateFormat IS03 = new SimpleDateFormat("yyyy-MM-dd HH");
	private static SimpleDateFormat IS04 = new SimpleDateFormat("yyyy-MM-dd");
	private static SimpleDateFormat IS05 = new SimpleDateFormat("yyyy-MM");
	private static SimpleDateFormat IS06 = new SimpleDateFormat("dd.MM.yyyy");
	private static SimpleDateFormat IS06b = new SimpleDateFormat("dd_MM_yyyy");
	private static SimpleDateFormat IS07 = new SimpleDateFormat("dd.MM.yyyy HH:mm");
	private static SimpleDateFormat IS08 = new SimpleDateFormat("MM.dd.yyyy");

	private static BooleanParser booleanParser = new BooleanParser();
	private static IntegerParser integerParser = new IntegerParser();
	private static DoubleParser doubleParser = new DoubleParser();
	private static DateParser dateParser = new DateParser();

	/**
	 * Parses date-oriented tokens. Checks most of the popular date formats.
	 * 
	 * @param date token as String.
	 * @return
	 * @throws ParseException
	 * @Deprecated use DateParser
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

	public static synchronized Double parseDouble(Object token) {
		return doubleParser.apply(token);
	}

	public static synchronized Float parseFloat(Object token) {
		return doubleParser.apply(token).floatValue();
	}

	/**
	 * method for loading data from a file. data is returned row-wise as a List of
	 * Strings
	 * 
	 * @param dataFile
	 * @return
	 * @throws IOException
	 */
	public static List<String> loadRows(String dataFile) throws IOException {
		// DATAFILE ACCESS
		List<String> rows = new ArrayList<String>();
		System.out.println("reading " + dataFile + " ...");
		File file = new File(dataFile);
		BufferedReader reader = null;

		// file input
		try {
			reader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException ex) {
			throw new FileNotFoundException("FileNotFoundException...");
		}
		String line = reader.readLine();
		while (line != null) {
			rows.add(line);
			line = reader.readLine();
		}

		if (reader != null)
			reader.close();

		return rows;
	}

	/**
	 * 
	 * @param file
	 * @param tokenizer
	 * @return
	 * @throws IOException
	 */
	public static List<List<String>> loadTokens(String file, String tokenizer) throws IOException {
		List<String> rows = loadRows(file);

		List<List<String>> dataTokens = new ArrayList<List<String>>();
		int coloumbsCount = 0;
		for (int i = 0; i < rows.size(); i++) {
			String row = rows.get(i);
			List<String> lineTokens = new ArrayList<String>();

			while (true) {
				if (row.contains(tokenizer)) {
					lineTokens.add(row.substring(0, row.indexOf(tokenizer)));
					row = row.substring(row.indexOf(tokenizer) + tokenizer.length(), row.length());
					if (!row.contains(tokenizer))
						lineTokens.add(row.trim());
					continue;
				}

				dataTokens.add(lineTokens);
				if (coloumbsCount < lineTokens.size())
					coloumbsCount = lineTokens.size();
				break;
			}
		}

		return dataTokens;
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

}
