package com.github.TKnudsen.ComplexDataObject.model.scoring.functions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;

public class AttributeSortingFunctions {

	public static List<AttributeScoringFunction<?>> sortAlphabetically(
			List<AttributeScoringFunction<?>> scoringFunctions) {
		List<AttributeScoringFunction<?>> functions = new ArrayList<>(scoringFunctions);

		// sort alphabetically
		Collections.sort(functions, new Comparator<Function<? super ComplexDataObject, Double>>() {
			@Override
			public int compare(Function<? super ComplexDataObject, Double> o1,
					Function<? super ComplexDataObject, Double> o2) {
				return o1.toString().compareTo(o2.toString());
			}
		});

		return functions;
	}
}
