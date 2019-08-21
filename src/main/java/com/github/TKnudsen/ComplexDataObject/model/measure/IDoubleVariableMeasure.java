package com.github.TKnudsen.ComplexDataObject.model.measure;

import java.util.function.IntToDoubleFunction;

public interface IDoubleVariableMeasure {

	double compute(IntToDoubleFunction x, IntToDoubleFunction y, int size);
}
