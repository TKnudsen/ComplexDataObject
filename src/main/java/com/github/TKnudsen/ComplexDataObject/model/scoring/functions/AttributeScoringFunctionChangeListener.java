package com.github.TKnudsen.ComplexDataObject.model.scoring.functions;

import java.util.EventListener;

public interface AttributeScoringFunctionChangeListener extends EventListener {

	void attributeScoringFunctionChanged(AttributeScoringFunctionChangeEvent event);
}