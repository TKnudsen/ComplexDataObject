package com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.Test;

/**
 * <p>
 * Tests {@link DoubleParser} against US and German number formats,
 * fractions, decoration (whitespace/percent/semicolon), and malformed input.
 * </p>
 */
public class DoubleParserTest {

	private static final double EPSILON = 0.0001;

	private final DoubleParser parserUS = new DoubleParser(false);
	private final DoubleParser parserGerman = new DoubleParser(true);

	@Test
	public void parsesBasicValues() {
		assertThat(parserUS.apply(null)).isNaN();
		assertThat(parserUS.apply("42")).isCloseTo(42.0, within(EPSILON));
		assertThat(parserUS.apply("42.5")).isCloseTo(42.5, within(EPSILON));
		assertThat(parserGerman.apply("42,5")).isCloseTo(42.5, within(EPSILON));
	}

	@Test
	public void parsesUSFormat() {
		assertThat(parserUS.apply("1234.56")).isCloseTo(1234.56, within(EPSILON));
		assertThat(parserUS.apply("1,234.56")).isCloseTo(1234.56, within(EPSILON));
		assertThat(parserUS.apply("1,234,567.89")).isCloseTo(1234567.89, within(EPSILON));
	}

	@Test
	public void parsesGermanFormat() {
		assertThat(parserGerman.apply("1234,56")).isCloseTo(1234.56, within(EPSILON));
		assertThat(parserGerman.apply("1.234,56")).isCloseTo(1234.56, within(EPSILON));
		assertThat(parserGerman.apply("1.234.567,89")).isCloseTo(1234567.89, within(EPSILON));
	}

	@Test
	public void autoDetectsFormatFromDotCommaOrder() {
		// parserUS still auto-detects German-style input when the digit grouping
		// makes the format unambiguous (dot before comma implies German)
		assertThat(parserUS.apply("1,234.56")).isCloseTo(1234.56, within(EPSILON));
		assertThat(parserUS.apply("1.234,56")).isCloseTo(1234.56, within(EPSILON));
	}

	@Test
	public void tolerantOfWhitespacePercentAndTrailingSemicolon() {
		assertThat(parserUS.apply("1 234.56")).isCloseTo(1234.56, within(EPSILON));
		assertThat(parserUS.apply("1234.56;")).isCloseTo(1234.56, within(EPSILON));
		assertThat(parserUS.apply("1234.56%")).isCloseTo(1234.56, within(EPSILON));
	}

	@Test
	public void parsesFractions() {
		assertThat(parserUS.apply("1/2")).isCloseTo(0.5, within(EPSILON));
		assertThat(parserUS.apply("3/4")).isCloseTo(0.75, within(EPSILON));
	}

	@Test
	public void handlesEdgeCaseValues() {
		assertThat(parserUS.apply("0")).isCloseTo(0.0, within(EPSILON));
		assertThat(parserUS.apply("-42.5")).isCloseTo(-42.5, within(EPSILON));
		assertThat(parserUS.apply("0.000001")).isCloseTo(0.000001, within(EPSILON));
	}

	@Test
	public void returnsNaNForInvalidInput() {
		assertThat(parserUS.apply("abc")).isNaN();
		assertThat(parserUS.apply("")).isNaN();
		assertThat(parserUS.apply("1/0")).isNaN();
	}
}
