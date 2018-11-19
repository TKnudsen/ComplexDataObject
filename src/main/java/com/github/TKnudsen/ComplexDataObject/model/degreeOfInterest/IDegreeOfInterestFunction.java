package com.github.TKnudsen.ComplexDataObject.model.degreeOfInterest;

import com.github.TKnudsen.ComplexDataObject.data.interfaces.ISelfDescription;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 
 * ComplexDataObject
 *
 * Copyright: Copyright (c) 2017-2018,
 * https://github.com/TKnudsen/ComplexDataObject<br>
 * <br>
 * 
 * A function that computes an "interestingness" for a collection of values.<br>
 * <br>
 * The default value domain is [0...1] as this allows an easy combination of
 * several (weighted) interestingness functions.<br>
 * 
 * @version 1.03
 */

public interface IDegreeOfInterestFunction<FV> extends Function<List<? extends FV>, Map<FV, Double>>, ISelfDescription {

}
