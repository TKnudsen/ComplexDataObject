package com.github.TKnudsen.ComplexDataObject.model.tools;

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
 * Copyright: Copyright (c) 2017
 * </p>
 *
 * @author Juergen Bernard
 * @version 1.03
 */
public class StringUtils {

	public static void main(String[] args) {
		double value = 66.3363456;
		System.out.println(value);

		String trunc = StringUtils.truncateDouble(value, 3);
		System.out.println(trunc);
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
		if (query == null || target == null)
			return 0.0;
		if (query.length() == 0 || target.length() == 0)
			return 0.0;

		int kernel = Math.max(length, 1);
		kernel = Math.min(length, Math.min(query.length(), target.length()));

		double count = 0;
		double matches = 0;
		for (int i = 0; i <= query.length() - kernel; i++) {
			for (int j = 0; j <= target.length() - kernel; j++)
				if (query.regionMatches(true, i, target, j, kernel)) {
					matches++;
					break;
				}
			count++;
		}

		return count > 0 ? matches / count : 0.0;
	}
}
