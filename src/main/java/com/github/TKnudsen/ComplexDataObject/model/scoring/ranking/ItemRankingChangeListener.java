package com.github.TKnudsen.ComplexDataObject.model.scoring.ranking;

import java.util.EventListener;

public interface ItemRankingChangeListener extends EventListener {

	void rankingChanged(ItemRankingChangeEvent event);
}