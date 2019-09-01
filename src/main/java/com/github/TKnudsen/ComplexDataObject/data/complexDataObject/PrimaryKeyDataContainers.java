package com.github.TKnudsen.ComplexDataObject.data.complexDataObject;

import java.util.Arrays;
import java.util.Objects;

public class PrimaryKeyDataContainers {

	public static PrimaryKeyDataContainer createPrimaryKeyDataContainers(ComplexDataObject cdo,
			String primaryKeyAttribute) {
		Objects.requireNonNull(cdo);

		return new PrimaryKeyDataContainer(Arrays.asList(cdo), primaryKeyAttribute);

	}
}
