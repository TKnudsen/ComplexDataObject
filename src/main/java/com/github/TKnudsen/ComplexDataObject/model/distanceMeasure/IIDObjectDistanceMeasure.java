package com.github.TKnudsen.ComplexDataObject.model.distanceMeasure;

import java.io.Serializable;

/**
 * <p>
 * Title: IIDObjectDistanceMeasure
 * </p>
 * 
 * <p>
 * Description: Generic interface for all data IDObject distance measures.
 * Similarity is rather complex, so defining an abstract concept is quite
 * challenging.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2013-2017
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.02
 * 
 * TODO_GENERICS No longer specific for IDObject
 */
public interface IIDObjectDistanceMeasure<T> extends IDistanceMeasure<T>, Serializable {

	public abstract String toString();
}
