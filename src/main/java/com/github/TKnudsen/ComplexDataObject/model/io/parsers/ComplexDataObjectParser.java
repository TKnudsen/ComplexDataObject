package com.github.TKnudsen.ComplexDataObject.model.io.parsers;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.data.interfaces.ISelfDescription;

/**
 * <p>
 * Interface for parsers to parse files for ComplexDataObjects.
 * Every line of the file is meant to include a single ComplexDataObject.
 * </p>
 *
 * @version 1.03
 * @since 2015
 */
public interface ComplexDataObjectParser extends IKeyValueObjectParser<ComplexDataObject>, ISelfDescription {

}
