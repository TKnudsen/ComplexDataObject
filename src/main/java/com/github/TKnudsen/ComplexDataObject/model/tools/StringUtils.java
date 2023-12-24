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
	 * @param s1
	 * @param s2
	 * @param length length of substring
	 * @return
	 */
	public static double subStringSimilarity(String s1, String s2, int length) {
		if (s1 == null || s2 == null)
			return 0.0;
		if (s1.length() == 0 || s2.length() == 0)
			return 0.0;

		int l = Math.max(length, 1);
		l = Math.min(length, Math.min(s1.length(), s2.length()));

		double count = 0;
		double matches = 0;
		for (int i = 0; i <= s1.length() - l; i++) {
			for (int j = 0; j <= s2.length() - l; j++)
				if (s1.regionMatches(true, i, s2, j, l)) {
					matches++;
					break;
				}
			count++;
		}

		return matches / count;
	}
}
