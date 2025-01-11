package com.github.TKnudsen.ComplexDataObject.model.scoring.ranking;

import javax.swing.event.ChangeEvent;

import com.github.TKnudsen.ComplexDataObject.data.entry.EntryWithComparableKey;
import com.github.TKnudsen.ComplexDataObject.data.ranking.Ranking;

public class ItemRankingChangeEvent<T extends Comparable<T>> extends ChangeEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3357176953527706541L;

	private final Ranking<EntryWithComparableKey<T, String>> ranking;

	public ItemRankingChangeEvent(Object source, Ranking<EntryWithComparableKey<T, String>> ranking) {
		super(source);

		this.ranking = ranking;
	}

	public Ranking<EntryWithComparableKey<T, String>> getRanking() {
		return ranking;
	}

}
