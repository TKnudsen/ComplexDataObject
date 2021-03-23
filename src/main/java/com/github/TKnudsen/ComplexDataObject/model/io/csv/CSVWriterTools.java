package com.github.TKnudsen.ComplexDataObject.model.io.csv;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * <p>
 * Description: support functionality to facilitate .csv output
 * 
 * Possible add on: add encapsulation of special chars and comma
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2018-2021
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.02
 */
public class CSVWriterTools {

	private static final char SEPARATOR = ',';

	public static void writeToCSV(Iterable<? extends Iterable<? extends Object>> linesOfObjects, String file)
			throws IOException {
		writeToCSV(linesOfObjects, file, SEPARATOR);
	}

	public static void writeToCSV(Iterable<? extends Iterable<? extends Object>> linesOfObjects, String file,
			char separator) throws IOException {

		Writer writer = createFileWriter(file);

		for (Iterable<? extends Object> objects : linesOfObjects)
			writeLine(writer, objects, separator);

		writer.flush();
		writer.close();
	}

	public static void writeLine(Writer writer, Iterable<? extends Object> values) throws IOException {
		writeLine(writer, values, SEPARATOR);
	}

	public static void writeLine(Writer writer, Iterable<? extends Object> values, char separator) throws IOException {

		boolean first = true;

		if (separator == ' ')
			separator = SEPARATOR;

		StringBuilder sb = new StringBuilder();
		for (Object value : values) {
			if (!first)
				sb.append(separator);

			sb.append(csvFormatSupport(value.toString()));

			first = false;
		}

		sb.append("\n");
		writer.append(sb.toString());
	}

	private static String csvFormatSupport(String value) {
		String output = value;
		return output.replace("\"", "\"\"");
	}

	public static Writer createFileWriter(String csvFile) throws IOException {
		return new FileWriter(csvFile);
	}
}
