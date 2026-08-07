package com.github.TKnudsen.ComplexDataObject.data.interfaces;

/**
 * <p>
 * interface for all objects having a master object. Allows
 * modeling object hierarchies.
 * </p>
 *
 * @version 1.02
 * @since 2011
 */
public interface IMasterProvider extends IDObject {
	public IDObject getMaster();
}
