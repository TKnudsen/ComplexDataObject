package com.github.TKnudsen.ComplexDataObject.model.tools;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * <p>
 * Utility class providing helper functions for functional-style programming,
 * such as wrapping a Function with a memoizing (result-caching) buffer.
 * </p>
 */
public class Functions {

	/**
	 * useful if computation is expensive and values remain constant. Do not use
	 * this if function values may change over time.
	 * 
	 * @param <T>
	 * @param <R>
	 * @param f
	 * @return
	 */
	public static <T, R> Function<T, R> bufferedFunction(Function<T, R> f) {
		Map<T, R> buffer = new HashMap<>();

		return new Function<T, R>() {

			@Override
			public R apply(T t) {
				if (buffer.containsKey(t))
					return buffer.get(t);

				buffer.put(t, f.apply(t));
				return buffer.get(t);
			}

		};
	}

	private Functions() {

	}
}
