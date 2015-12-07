package preprocessing;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;

import data.ComplexDataContainer;
import data.ComplexDataObject;

public class BigDecimalConverter implements IPreprocessingRoutine {

	String attribute;
	String targetAttribute;

	DecimalFormat decimalFormat = null;

	public BigDecimalConverter(String attribute, String targetAttribute, Character decimalSeparator, Character thousandsSeparator) {
		this.attribute = attribute;
		this.targetAttribute = targetAttribute;

		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setGroupingSeparator(',');
		symbols.setDecimalSeparator('.');
		String pattern = "#,##0.0#";
		decimalFormat = new DecimalFormat(pattern, symbols);
		decimalFormat.setParseBigDecimal(true);
	}

	@Override
	public void process(ComplexDataContainer container) {
		// add new attribute to schema
		container.add(targetAttribute, BigDecimal.class, BigDecimal.ZERO);

		// parse
		for (ComplexDataObject complexDataObject : container) {
			BigDecimal bd = BigDecimal.ZERO;

			try {
				bd = (BigDecimal) decimalFormat.parse(complexDataObject.get(attribute).toString());
			} catch (ParseException e) {
				e.printStackTrace();
			}

			complexDataObject.add(targetAttribute, bd);
		}
	}

}
