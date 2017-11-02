package com.github.TKnudsen.ComplexDataObject.view;

import com.github.TKnudsen.ComplexDataObject.data.interfaces.IDObject;

/**
 * <p>
 * Title: PositionMappingFunction
 * </p>
 * 
 * <p>
 * Description: maps an IDObject into the visual space, i.e. to a position
 * (Double[]).
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2015-2017
 * </p>
 * 
 * @author Juergen Bernard
 */
public abstract class PositionMappingFunction<I extends IDObject> extends VisualMappingFunction<I, Double[]> {

}
