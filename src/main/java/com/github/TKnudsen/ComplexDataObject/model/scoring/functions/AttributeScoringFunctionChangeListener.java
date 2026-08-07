package com.github.TKnudsen.ComplexDataObject.model.scoring.functions;

import java.util.EventListener;

/**
 * <p>
 * Listener interface for receiving notifications when an
 * AttributeScoringFunction's configuration changes.
 * </p>
 *
 * @deprecated Use
 *             {@code com.github.TKnudsen.scoring.model.scoring.listeners.AttributeScoringFunctionChangeListener}
 *             instead.
 */
@Deprecated
public interface AttributeScoringFunctionChangeListener extends EventListener {

	void attributeScoringFunctionChanged(AttributeScoringFunctionChangeEvent event);
}