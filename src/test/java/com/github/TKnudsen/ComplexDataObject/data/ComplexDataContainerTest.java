package data;

import static org.assertj.core.api.Assertions.*;
import org.junit.Before;
import org.junit.Test;
import tools.ComplexDataObjectFactory;

import java.util.Arrays;
import java.util.List;

public class ComplexDataContainerTest {

	@Test
	public void shouldConstructWithObjects() {
		final List<ComplexDataObject> objects = Arrays.asList(
				ComplexDataObjectFactory.createObject("Att A", new Double(2.0), "Att B", "asdf"),
				ComplexDataObjectFactory.createObject("Att A", new Double(3.0), "Att B", "jkl�")
		);

		final ComplexDataContainer complexDataContainer = new ComplexDataContainer(objects);

		assertThat(complexDataContainer).hasSameElementsAs(objects);
	}
}
