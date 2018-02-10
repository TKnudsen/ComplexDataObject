package com.github.TKnudsen.ComplexDataObject.view.visualMappings.color;

import java.awt.Color;

import com.github.TKnudsen.ComplexDataObject.view.visualMappings.VisualMappingFunction;

/**
 * <p>
 * Title: VisualColorMappingFunction
 * </p>
 * 
 * <p>
 * Description: provides the basic functionality for mapping abstract data to
 * colors.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2016-2018
 * </p>
 * 
 * @author Juergen Bernard
 */
public abstract class VisualColorMappingFunction<T> extends VisualMappingFunction<T, Color> {

	public VisualColorMappingFunction<T> replace(T special, Color specialColor) {
		VisualColorMappingFunction<T> outer = this;

		return new VisualColorMappingFunction<T>() {

			@Override
			protected Color calculateMapping(T t) {
				return outer.calculateMapping(t);
			}

			@Override
			public final Color apply(T t) {
				return (t.equals(special) ? specialColor : super.apply(t));
			}
		};
	}
}
