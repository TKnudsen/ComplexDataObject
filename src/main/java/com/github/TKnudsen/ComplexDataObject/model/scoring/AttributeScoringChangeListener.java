package com.github.TKnudsen.ComplexDataObject.model.scoring;

import java.util.EventListener;

public interface AttributeScoringChangeListener extends EventListener {

	void attributeScoringFunctionChanged(AttributeScoringChangeEvent event);
}