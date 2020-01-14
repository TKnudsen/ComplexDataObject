package com.github.TKnudsen.ComplexDataObject.model.scoring;

import java.util.EventListener;

public interface AttributeScoringFunctionChangeListener extends EventListener {

	void attributeScoringFunctionChanged(AttributeScoringFunctionChangeEvent event);
}