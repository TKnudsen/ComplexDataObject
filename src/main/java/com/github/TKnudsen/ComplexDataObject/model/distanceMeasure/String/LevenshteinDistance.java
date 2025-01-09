package com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.String;

public class LevenshteinDistance extends StringDistanceMeasure {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public double getDistance(String o1, String o2) {
		if (o1 == null || o2 == null)
			return Double.NaN;

		String s1 = o1.toString().toLowerCase();
		String s2 = o2.toString().toLowerCase();

		int[] costs = new int[s2.length() + 1];
		for (int i = 0; i <= s1.length(); i++) {
			int lastValue = i;
			for (int j = 0; j <= s2.length(); j++) {
				if (i == 0)
					costs[j] = j;
				else {
					if (j > 0) {
						int newValue = costs[j - 1];
						if (s1.charAt(i - 1) != s2.charAt(j - 1))
							newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
						costs[j - 1] = lastValue;
						lastValue = newValue;
					}
				}
			}
			if (i > 0)
				costs[s2.length()] = lastValue;
		}
		return costs[s2.length()];
	}

	@Override
	public String getName() {
		return "Levenshtein Distance";
	}

	@Override
	public String getDescription() {
		return "Adds the transposition (swapping) of two adjacent characters as an additional operation";
	}

}
