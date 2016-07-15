package com.github.TKnudsen.ComplexDataObject.test;

import java.io.IOException;
import java.util.List;

import com.github.TKnudsen.ComplexDataObject.data.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.examples.TitanicParser;

public class TitanicDataSetParserTester {
	public static void main(String[] args) {
		List<ComplexDataObject> titanicData = null;

		TitanicParser p = new TitanicParser("", true);
		try {
			titanicData = p.parse("data/titanic_extended.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(titanicData);
	}
}
