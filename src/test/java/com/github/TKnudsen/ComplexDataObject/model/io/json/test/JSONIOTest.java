package com.github.TKnudsen.ComplexDataObject.model.io.json.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.data.dataFactory.DataSets;
import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeature;
import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeatureVector;
import com.github.TKnudsen.ComplexDataObject.model.io.json.JSONLoader;
import com.github.TKnudsen.ComplexDataObject.model.io.json.JSONWriter;

/**
 * <p>
 * Tests JSON output-input round trips for {@link ComplexDataObject} and
 * {@link NumericalFeatureVector}, both via string and via file. Files are
 * written to a JUnit-managed temporary directory, not the working directory.
 * </p>
 */
public class JSONIOTest {

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Test
	public void complexDataObject_survivesRoundTrip_viaString() {
		ComplexDataObject original = firstTitanicRecord();

		String json = JSONWriter.writeToString(original);
		ComplexDataObject loaded = JSONLoader.loadFromString(json);

		assertThat(loaded.equalValues(original)).as("round-tripped attributes equal the original").isTrue();
	}

	@Test
	public void complexDataObject_survivesRoundTrip_viaFile() throws IOException {
		ComplexDataObject original = firstTitanicRecord();
		String filePath = tempFolder.newFile("titanic0.json").getAbsolutePath();

		JSONWriter.writeToFile(original, filePath);
		ComplexDataObject loaded = JSONLoader.loadFromFile(filePath, false);

		assertThat(loaded.equalValues(original)).as("round-tripped attributes equal the original").isTrue();
	}

	@Test
	public void numericalFeatureVector_survivesRoundTrip_viaString() {
		NumericalFeatureVector original = createSampleNumericalFeatureVector();

		String json = JSONWriter.writeToString(original);
		NumericalFeatureVector loaded = JSONLoader.loadNumericalFeatureVectorFromString(json);

		assertThat(loaded.getVector()).containsExactly(original.getVector());
		assertThat(loaded.equalValues(original)).as("round-tripped attributes equal the original").isTrue();
	}

	@Test
	public void numericalFeatureVector_survivesRoundTrip_viaFile() throws IOException {
		NumericalFeatureVector original = createSampleNumericalFeatureVector();
		String filePath = tempFolder.newFile("testNFV.json").getAbsolutePath();

		JSONWriter.writeToFile(original, filePath);
		NumericalFeatureVector loaded = JSONLoader.loadNumericalFeatureVectorFromFile(filePath);

		assertThat(loaded.getVector()).containsExactly(original.getVector());
		assertThat(loaded.equalValues(original)).as("round-tripped attributes equal the original").isTrue();
	}

	private static ComplexDataObject firstTitanicRecord() {
		List<ComplexDataObject> titanicData = DataSets.titanicDataSet();
		assertThat(titanicData).as("DataSets.titanicDataSet()").isNotNull().isNotEmpty();
		return titanicData.get(0);
	}

	private static NumericalFeatureVector createSampleNumericalFeatureVector() {
		List<NumericalFeature> features = new ArrayList<>();
		features.add(new NumericalFeature("1", 1.0));
		features.add(new NumericalFeature("2", 2.0));
		features.add(new NumericalFeature("3", 3.0));
		features.add(new NumericalFeature("4", 4.0));

		NumericalFeatureVector nfv = new NumericalFeatureVector(features);

		// Example: additional fields
		nfv.add("testA", "a");
		nfv.add("testB", "b");
		nfv.add("testC", "c");

		return nfv;
	}
}
