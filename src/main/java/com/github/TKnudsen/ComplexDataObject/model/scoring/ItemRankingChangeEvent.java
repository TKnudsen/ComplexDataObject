package com.github.TKnudsen.ComplexDataObject.model.scoring;

import javax.swing.event.ChangeEvent;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.data.entry.EntryWithComparableKey;
import com.github.TKnudsen.ComplexDataObject.data.ranking.Ranking;

public class ItemRankingChangeEvent extends ChangeEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3357176953527706541L;

	private final Ranking<EntryWithComparableKey<Double, ComplexDataObject>> ranking;

	public ItemRankingChangeEvent(Object source, Ranking<EntryWithComparableKey<Double, ComplexDataObject>> ranking) {
		super(source);

		this.ranking = ranking;
	}

	public Ranking<EntryWithComparableKey<Double, ComplexDataObject>> getRanking() {
		return ranking;
	}

}
