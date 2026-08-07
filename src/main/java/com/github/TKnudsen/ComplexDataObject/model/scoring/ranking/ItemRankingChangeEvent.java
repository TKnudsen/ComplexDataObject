package com.github.TKnudsen.ComplexDataObject.model.scoring.ranking;

import javax.swing.event.ChangeEvent;

import com.github.TKnudsen.ComplexDataObject.data.entry.EntryWithComparableKey;
import com.github.TKnudsen.ComplexDataObject.data.ranking.Ranking;

/**
 * <p>
 * Change event fired when an item ranking is (re-)calculated. Carries the
 * resulting Ranking of entries with comparable keys produced by an
 * ItemRankingModel.
 * </p>
 *
 * @deprecated Use
 *             {@code com.github.TKnudsen.scoring.model.scoring.listeners.RankingChangedEvent}
 *             instead, which no longer extends this class and additionally
 *             carries score/rank/uncertainty maps.
 */
@Deprecated
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
