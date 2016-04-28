package com.github.TKnudsen.ComplexDataObject.data.interfaces;

/**
 * <p>
 * Title: ITextDescription
 * </p>
 * 
 * <p>
 * Description: interface for all objects having a name and a (small) textual
 * description. These properties should be descriptive, but do not necessarily
 * need to be unique.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2011-2016
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.04
 */
public interface ITextDescription {

	/**
	 * 
	 * @return the title
	 */
	public String getName();

	/**
	 * 
	 * @return textual description of the class
	 */
	public String getDescription();

	/**
	 * 
	 * @return runtime status
	 */
	public String toString();
}
