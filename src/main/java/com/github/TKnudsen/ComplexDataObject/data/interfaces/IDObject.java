package com.github.TKnudsen.ComplexDataObject.data.interfaces;

/**
 * <p>
 * Title: IDObject
 * </p>
 * 
 * <p>
 * Description: interface for all objects having an identifier, a name and a
 * (small) textual description.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2011-2024
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.07
 */
public interface IDObject {

	/**
	 * I regret that I found it necessary to have an ID field/attribute present
	 * always. In practice, even ID-based usage forms simply define a primary key
	 * attribute and do not make use of the ID attribute.
	 * 
	 * @deprecated Prepare for its deletion and replacement by a standard attribute.
	 */
	public long getID();

	public int hashCode();

	public boolean equals(Object obj);
}
