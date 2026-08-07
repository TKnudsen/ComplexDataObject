package com.github.TKnudsen.ComplexDataObject.model.scoring.ranking;

import java.util.EventListener;

/**
 * <p>
 * Listener interface for receiving notifications when an item ranking
 * managed by an ItemRankingModel changes.
 * </p>
 *
 * @deprecated Use
 *             {@code com.github.TKnudsen.scoring.model.scoring.listeners.RankingChangeListener}
 *             instead.
 */
@Deprecated
public interface ItemRankingChangeListener extends EventListener {

	void rankingChanged(ItemRankingChangeEvent event);
}