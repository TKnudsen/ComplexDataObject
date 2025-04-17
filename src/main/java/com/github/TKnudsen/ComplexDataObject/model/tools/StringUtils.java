package com.github.TKnudsen.ComplexDataObject.model.tools;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Title: StringUtils
 * </p>
 *
 * <p>
 * Description: little helpers when working with String.
 * </p>
 *
 * <p>
 * Copyright: Copyright (c) 2017-2025
 * </p>
 *
 * @author Juergen Bernard
 * @version 1.05
 */
public class StringUtils {

	public static void main(String[] args) {
		int count = 10000000;
		int length = 4;
		String s1 = "Subset AutomSystemsotive Components";
		String s2 = "SubsetSub Components and Systems";

		long l = System.currentTimeMillis();

		for (int i = 0; i < count; i++)
			subStringSimilarity(s1, s2, length);
		System.out.println("subStringSimilarity took " + (System.currentTimeMillis() - l) + " ms");

		System.out.println();
	}

	public static String truncateDouble(double value, int decimals) {
		if (decimals < 0)
			return null;

		String result = (Double.valueOf(value)).toString();
		int dot = result.lastIndexOf(".");
		int length = result.length();
		result = result.substring(0, (int) (Math.min(dot + 1 + decimals, length)));

		return result;
	}

	/**
	 * simple similarity measure based on the ratio of matching substrings
	 * 
	 * @param query
	 * @param target
	 * @param length length of substring that is used as sliding window kernel
	 * @return
	 */
	public static double subStringSimilarity(String query, String target, int length) {
		if (query == null || target == null || query.length() == 0 || target.length() == 0)
			return 0.0;

		int kernel = Math.min(length, Math.min(query.length(), target.length()));
		int totalWindows = query.length() - kernel + 1;

		int matches = 0;
		for (int i = 0; i <= query.length() - kernel; i++) {
			String windowQuery = query.substring(i, i + kernel);
			int index = target.indexOf(windowQuery);
			if (index != -1 && index + kernel <= target.length())
				matches++;

		}

		return totalWindows > 0 ? (double) matches / totalWindows : 0.0;

		// old version, ten times slower
		// if (query == null || target == null)
		// return 0.0;
		// if (query.length() == 0 || target.length() == 0)
		// return 0.0;
		//
		// int kernel = Math.max(length, 1);
		// kernel = Math.min(length, Math.min(query.length(), target.length()));
		//
		// double count = 0;
		// double matches = 0;
		// for (int i = 0; i <= query.length() - kernel; i++) {
		// for (int j = 0; j <= target.length() - kernel; j++)
		// if (query.regionMatches(true, i, target, j, kernel)) {
		// matches++;
		// break;
		// }
		// count++;
		// }
		//
		// return count > 0 ? matches / count : 0.0;
	}
	
	/**
	 * 
	 * @param row
	 * @param separator
	 * @return
	 */
	public static List<String> tokenize(String row, String separator) {
		List<String> lineTokens = new ArrayList<String>();
		while (true) {
			if (row.contains(separator)) {
				lineTokens.add(row.substring(0, row.indexOf(separator)));
				row = row.substring(row.indexOf(separator) + separator.length(), row.length());
				if (!row.contains(separator))
					lineTokens.add(row.trim());
				continue;
			}
			break;
		}

		return lineTokens;
	}
}
