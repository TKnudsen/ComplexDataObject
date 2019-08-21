package com.github.TKnudsen.ComplexDataObject.model.measure;

import java.util.function.IntToDoubleFunction;

public interface IDoubleVariablesMeasure {

	double compute(IntToDoubleFunction x, IntToDoubleFunction y, int size);
}
