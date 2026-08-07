package com.github.TKnudsen.ComplexDataObject.model.scoring.ranking;

import java.util.EventListener;

/**
 * <p>
 * Listener interface for receiving notifications when an item ranking
 * managed by an ItemRankingModel changes.
 * </p>
 */
public interface ItemRankingChangeListener extends EventListener {

	void rankingChanged(ItemRankingChangeEvent event);
}