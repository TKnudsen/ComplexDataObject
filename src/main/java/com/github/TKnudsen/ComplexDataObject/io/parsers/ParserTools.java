package main.java.com.github.TKnudsen.ComplexDataObject.io.parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

	/**
	 * Parses date-oriented tokens. Checks most of the popular date formats.
	 * 
	 * @param date
	 *            token as String.
	 * @return
	 * @throws ParseException
	 */
	public static synchronized Date parseDate(String token) {
		Date date = null;
		String t = token.replace("T", " ");

		// speedup:
		if (token.equals("Date/Time"))
			return date;

		// alternative
		if (t.length() == 23)
			try {
				// 2007-01-01 00:00:00:000
				synchronized (ISO0) {
					date = ISO0.parse(t);
				}
			} catch (ParseException pe0) {
			}
		else if (t.length() == 19)
			try {
				// 2007-01-01 00:00:00
				synchronized (ISO1) {
					date = ISO1.parse(t);
				}
			} catch (ParseException pe8) {
			}
		else if (t.length() == 16)
			try {
				// 2007-01-01 00:00
				synchronized (IS02) {
					date = IS02.parse(t);
				}
			} catch (ParseException pe8) {
				try {
					// 2007.01.01 00:00
					date = IS07.parse(t);
				} catch (ParseException pe7) {
				}
			}
		else if (t.length() == 13)
			try {
				// 2007-01-01 00
				synchronized (IS03) {
					date = IS03.parse(t);
				}
			} catch (ParseException pe8) {
			}
		else if (t.length() == 10)
			try {
				// 2007-01-01
				synchronized (IS04) {
					date = IS04.parse(t);
				}
			} catch (ParseException pe8) {
				try {
					// 13.01.1969
					synchronized (IS06) {
						date = IS06.parse(token);
					}
				} catch (ParseException pe) {
					try {
						// 01_13_1969
						synchronized (IS06b) {
							date = IS06b.parse(token);
						}
					} catch (ParseException pe_) {
						try {
							// 01.13.1969
							synchronized (IS08) {
								date = IS08.parse(token);
							}
						} catch (ParseException pe__) {
						}
					}
				}
			}
		else if (t.length() == 7)
			try {
				// 2007-01
				synchronized (IS05) {
					date = IS05.parse(t);
				}
			} catch (ParseException pe8) {
			}
		return date;
	}

	/**
	 * method for loading data from a file. data is returned row-wise as a List
	 * of Strings
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

	// /**
	// * Determines the attribute type of a given instance and attribute.
	// *
	// * @param instances
	// * @param attribute
	// * @return
	// */
	// public static AttributeType getAttributeType(Instances instances,
	// Attribute attribute) {
	// AttributeType type;
	// if (attribute.isNumeric()) {
	// // determine if attribute is numeric or ordinal
	// if
	// (hasFloatingPointValues(instances.attributeToDoubleArray(index(instances,
	// attribute))))
	// type = AttributeType.NUMERIC;
	// else
	// type = AttributeType.ORDINAL;
	// } else {
	// // get list of attribute values
	// List<String> attValues = new ArrayList<>();
	// for (int j = 0; j < attribute.numValues(); j++)
	// attValues.add(attribute.value(j));
	//
	// // determine if attribute is categorical or binary
	// if (attValues.size() == 2) {
	// for (int i = 0; i < attValues.size(); i++)
	// attValues.add(attValues.remove(i).toLowerCase());
	//
	// if (attValues.contains("no") && attValues.contains("yes") ||
	// attValues.contains("false") && attValues.contains("true") ||
	// attValues.contains("0") && attValues.contains("1"))
	// type = AttributeType.BINARY;
	// else
	// type = AttributeType.CATEGORICAL;
	// }
	//
	// else
	// type = AttributeType.CATEGORICAL;
	// }
	// return type;
	// }
	//
	// private static int index(Instances instances, Attribute att) {
	// for (int i = 0; i < instances.numAttributes(); i++)
	// if (instances.attribute(i).equals(att))
	// return i;
	// throw new IndexOutOfBoundsException("Attribut nicht vorhanden! Oder:
	// equals checken ;-)");
	// }

	public static boolean hasFloatingPointValues(double[] values) {
		for (int i = 0; i < values.length; i++) {
			if (values[i] != Math.floor(values[i]))
				return true;
		}
		return false;
	}
}
