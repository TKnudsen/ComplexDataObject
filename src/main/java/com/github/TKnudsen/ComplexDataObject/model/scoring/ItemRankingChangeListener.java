package com.github.TKnudsen.ComplexDataObject.model.scoring;

import java.util.EventListener;

public interface ItemRankingChangeListener extends EventListener {

	void rankingChanged(ItemRankingChangeEvent event);
}