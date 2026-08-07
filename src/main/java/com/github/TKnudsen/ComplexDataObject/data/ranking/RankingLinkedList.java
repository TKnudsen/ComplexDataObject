package com.github.TKnudsen.ComplexDataObject.data.ranking;

import java.util.LinkedList;

/**
 * <p>
 * structures objects in sorted manner. Based on a
 * {@link LinkedList}.
 * </p>
 *
 * @version 1.03
 * @since 2011
 */
public class RankingLinkedList<T extends Comparable<T>> extends LinkedList<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3625638957983883603L;

	public boolean add(T t) {
		for (int i = 0; i < this.size(); i++) {
			if (t.compareTo(get(i)) < 0) {
				this.add(i, t);
				return true;
			}
		}
		super.add(t);
		return true;
	}
}
