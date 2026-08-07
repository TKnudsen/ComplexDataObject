package com.github.TKnudsen.ComplexDataObject.data.complexDataObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.github.TKnudsen.ComplexDataObject.data.DataContainers;
import com.github.TKnudsen.ComplexDataObject.data.DataSchema;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.Parsers;
import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;

/**
 * <p>
 * Little helpers for ComplexDataContainers. Note that
 * ComplexDataContainer is now more powerful: it has a primaryKeyAttribute
 * extension that shall be used if the primary key attribute is to be determined
 * explicitly. Also, new constructors are available, making several methods here
 * obsolete.
 * </p>
 *
 * @version 1.07
 * @since 2017
 */
public class ComplexDataContainers {

	public static List<ComplexDataObject> getObjectList(ComplexDataContainer container) {
		List<ComplexDataObject> list = new ArrayList<>();

		for (ComplexDataObject cdo : container)
			list.add(cdo);

		return list;
	}

	/**
	 * @deprecated create ComplexDataContainer directly, with the new constructor.
	 *             Note that it is preferable to determine the primary key with a
	 *             second constructor parameter
	 * @param complexDataObjects
	 * @return
	 */
	public static ComplexDataContainer createComplexDataContainer(
			Iterable<? extends ComplexDataObject> complexDataObjects) {

		List<ComplexDataObject> list = new ArrayList<>();

		for (ComplexDataObject cdo : complexDataObjects)
			list.add(cdo);

		return new ComplexDataContainer(list);
	}

	/**
	 * @deprecated create ComplexDataContainer directly, with the new constructor.
	 * @param complexDataObjects
	 * @return
	 */
	public static ComplexDataContainer createComplexDataContainer(
			Iterable<? extends ComplexDataObject> complexDataObjects, String primaryKeyAttribute) {
		Objects.requireNonNull(primaryKeyAttribute);

		List<ComplexDataObject> list = new ArrayList<>();

		for (ComplexDataObject cdo : complexDataObjects)
			list.add(cdo);

		return new ComplexDataContainer(list, primaryKeyAttribute);
	}

	/**
	 * @deprecated create ComplexDataContainer directly, with the new constructor.
	 * @param cdo
	 * @return
	 */
	public static ComplexDataContainer createComplexDataContainer(ComplexDataObject cdo) {
		Objects.requireNonNull(cdo);

		return new ComplexDataContainer(Arrays.asList(new ComplexDataObject[] { cdo }));

	}

	/**
	 * @deprecated create ComplexDataContainer directly, with the new constructor.
	 * @param cdo
	 * @return
	 */
	public static ComplexDataContainer createComplexDataContainer(ComplexDataObject cdo, String primaryKeyAttribute) {
		Objects.requireNonNull(cdo);
		Objects.requireNonNull(primaryKeyAttribute);

		return new ComplexDataContainer(Arrays.asList(new ComplexDataObject[] { cdo }), primaryKeyAttribute);

	}

	public static DataSchema dataSchema(ComplexDataContainer container) {
		DataSchema schema = new DataSchema();

		for (String attribute : container.getAttributeNames()) {
			schema.add(attribute, container.getType(attribute), container.getDefaultValue(attribute));
		}

		return schema;
	}

	/**
	 * 
	 * @param containers
	 * @param primaryAttribute the attribute that is supposed to be contained in any
	 *                         container. will be used as a basis for identifying
	 *                         and merging ComplexDataObjects within individual
	 *                         containers.
	 * @return new ComplexDataContainer. Schema information of old containers gets
	 *         lost.
	 */
	public static ComplexDataContainer mergeContainers(Collection<ComplexDataContainer> containers,
			String primaryAttribute) {
		return mergeContainers(containers, primaryAttribute, false);
	}

	/**
	 * 
	 * @param containers
	 * @param primaryAttribute               the attribute that is supposed to be
	 *                                       contained in any container. will be
	 *                                       used as a basis for identifying and
	 *                                       merging ComplexDataObjects within
	 *                                       individual containers.
	 * @param removeSourceContainerListeners option to remove listeners to old
	 *                                       containers. Useful if the plan is to
	 *                                       only continue with the new merger.
	 * @return new ComplexDataContainer. Schema information of old containers gets
	 *         lost.
	 */
	public static ComplexDataContainer mergeContainers(Collection<ComplexDataContainer> containers,
			String primaryAttribute, boolean removeSourceContainerListeners) {

		Objects.requireNonNull(containers);
		Objects.requireNonNull(primaryAttribute);

		Map<String, List<ComplexDataObject>> objects = new HashMap<String, List<ComplexDataObject>>();

		for (ComplexDataContainer container : containers)
			if (container.getAttributes().contains(primaryAttribute))
				for (ComplexDataObject cdo : container) {
					if (cdo.getAttribute(primaryAttribute) != null) {
						String attributeValue = cdo.getAttribute(primaryAttribute).toString();

						if (objects.get(attributeValue) == null)
							objects.put(attributeValue, new ArrayList<>());

						objects.get(attributeValue).add(cdo);
					}
				}

		List<ComplexDataObject> mergedCDOs = new ArrayList<>();
		for (String attributeValue : objects.keySet())
			mergedCDOs.add(ComplexDataObjects.merge(objects.get(attributeValue), !removeSourceContainerListeners));

		//already done with ComplexDataObjects.merge 
//		if (removeSourceContainerListeners)
//			for (ComplexDataObject cdo : mergedCDOs)
//				for (ComplexDataContainer container : containers)
//					cdo.removeComplexDataObjectListener(container);

		return new ComplexDataContainer(mergedCDOs, primaryAttribute);
	}

	/**
	 * 
	 * Removes the ComplexDataContainer as a listener from the ComplexDataObjects it
	 * contains.
	 * 
	 * Very useful if a ComplexDataContainer is at the end of life, wants to go into
	 * the garbage container, but can't, due to the references from the
	 * ComplexDataObjects to their listening container.
	 * 
	 * @param container
	 */
	public static void removeAsListener(ComplexDataContainer container) {
		for (ComplexDataObject cdo : container)
			cdo.removeComplexDataObjectListener(container);
	}

	/**
	 * creates a map with the ID attribute of ComplexDataObjects as key.
	 * 
	 * @deprecated uses the ID attribute of ComplexDataObjects that was replaced by
	 *             a dynamic primary key concept. Those primary keys do not need to
	 *             be of type Long any more.
	 * @param attribute
	 * @return
	 */
	public static Map<Long, Object> getAttributeValues(ComplexDataContainer container, String attribute) {
		return DataContainers.getAttributeValues(container, attribute);
	}

//	/**
//	 * Retrieves the collection of vales for a particular attribute as a string
//	 * representation. Handy for categorical data.
//	 * 
//	 * @param container
//	 * @param attribute
//	 * @param skipNull   do not return null values
//	 * @param skipEmpty  do not return empty values
//	 * @param saveMemory one-time computation without storing the values in the
//	 *                   container
//	 * @return
//	 */
//	public static Collection<String> getAttributeValuesCategorical(ComplexDataContainer container, String attribute,
//			boolean skipNull, boolean skipEmpty, boolean saveMemory) {
//		Objects.requireNonNull(container);
//		if (container.isNumeric(attribute))
//			throw new IllegalArgumentException(
//					"ComplexDataContainers.getAttributeValueCategories does not work for numerical attributes "
//							+ attribute);
//
//		Collection<Object> values = new ArrayList<Object>(container.size());
//		if (saveMemory) {
//			Iterator<ComplexDataObject> iterator = container.iterator();
//			while (iterator.hasNext()) {
//				ComplexDataObject o = iterator.next();
//				values.add(o.getAttribute(attribute));
//			}
//		} else
//			values = container.getAttributeValueCollection(attribute);
//
//		List<String> v = new ArrayList<String>(values.size());
//		for (Object o : values) {
//			if (skipNull && o == null)
//				continue;
//
//			String s = Parsers.parseString(o);
//
//			if (Parsers.isMissingValue(s))
//				continue;
//
//			v.add(s);
//
//		}
//		return v;
//	}

	/**
	 * Retrieves attribute values as strings. Designed for categorical data.
	 *
	 * <p>
	 * Mirrors the contract of {@code collectNumerical}: supports key set filtering,
	 * a single-pass implementation, and consistent null/missing handling.
	 *
	 * @param container    source container, must not be null
	 * @param attribute    attribute name, must not be null
	 * @param keysetOrNull if non-null, only objects whose primary key is in this
	 *                     set are included
	 * @param skipNull     exclude null values (before string conversion)
	 * @param skipEmpty    exclude empty-string and missing-value results
	 * @param saveMemory   iterate directly instead of using the container's cached
	 *                     attribute-value collection
	 * @return list of string values in encounter order
	 */
	public static List<String> getAttributeValuesCategorical(ComplexDataContainer container, String attribute,
			Collection<?> keysetOrNull, boolean skipNull, boolean skipEmpty, boolean saveMemory) {

		Objects.requireNonNull(container, "container must not be null");
		Objects.requireNonNull(attribute, "attribute must not be null");
		if (container.isNumeric(attribute)) {
			throw new IllegalArgumentException(
					"getAttributeValuesCategorical does not work for numerical attributes: " + attribute);
		}

		final Set<?> keyset = normalizeKeyset(keysetOrNull);
		final int capacity = estimateCapacity(container.size(), keyset, saveMemory, skipNull, skipEmpty);
		final List<String> out = new ArrayList<>(Math.max(capacity, 16));

		if (keyset == null && !saveMemory) {
			// Fast path: direct attribute-value scan, no full object iteration.
			for (Object o : container.getAttributeValueCollection(attribute)) {
				appendParsedString(out, o, skipNull, skipEmpty);
			}
		} else {
			final String keyAttr = container.getPrimaryKeyAttribute();
			for (Iterator<ComplexDataObject> it = container.iterator(); it.hasNext();) {
				final ComplexDataObject cdo = it.next();
				if (cdo == null)
					continue;
				if (keyset != null) {
					final Object key = cdo.getAttribute(keyAttr);
					if (key == null || !keyset.contains(key))
						continue;
				}
				appendParsedString(out, cdo.getAttribute(attribute), skipNull, skipEmpty);
			}
		}

		return out;
	}

	// Backwards-compatible overload for callers that don't need key set filtering.
	public static List<String> getAttributeValuesCategorical(ComplexDataContainer container, String attribute,
			boolean skipNull, boolean skipEmpty, boolean saveMemory) {
		return getAttributeValuesCategorical(container, attribute, null, skipNull, skipEmpty, saveMemory);
	}

	// -------------------------------------------------------------------------
	// string append helper
	// -------------------------------------------------------------------------

	private static void appendParsedString(List<String> out, Object o, boolean skipNull, boolean skipEmpty) {

		if (o == null) {
			if (!skipNull)
				out.add(null);
			return;
		}

		final String s = Parsers.parseString(o);

		// isMissingValue covers empty string, placeholder tokens ("NA", "?", etc.)
		// skipEmpty gates both empty and missing; skipNull already handled above.
		if (skipEmpty && Parsers.isMissingValue(s)) {
			return;
		}

		out.add(s);
	}

//	/**
//	 * Retrieves the collection of vales for a particular attribute as a numeric
//	 * representation. Handy for numerical data.
//	 * 
//	 * @param container
//	 * @param attribute
//	 * @param skipNull   do not return null values
//	 * @param skipNaN    do not return NaN values
//	 * @param saveMemory one-time computation without storing the values in the
//	 *                   container
//	 * @return
//	 */
//	public static Collection<Number> getAttributeValuesNumerical(ComplexDataContainer container, String attribute,
//			boolean skipNull, boolean skipNaN, boolean saveMemory) {
//		Objects.requireNonNull(container);
//		if (!container.isNumeric(attribute))
//			throw new IllegalArgumentException(
//					"ComplexDataContainers.getAttributeValueCategories does not work for non-numerical attributes "
//							+ attribute);
//
//		Collection<Object> values = new ArrayList<Object>(container.size());
//		if (saveMemory) {
//			Iterator<ComplexDataObject> iterator = container.iterator();
//			while (iterator.hasNext()) {
//				ComplexDataObject o = iterator.next();
//				values.add(o.getAttribute(attribute));
//			}
//		} else
//			values = container.getAttributeValueCollection(attribute);
//
//		List<Number> v = new ArrayList<Number>(values.size());
//		for (Object o : values) {
//			if (skipNull && o == null)
//				continue;
//
//			Double d = Parsers.parseDouble(o);
//			if (skipNaN && Double.isNaN(d))
//				continue;
//
//			v.add(d);
//		}
//
//		return v;
//	}

	/**
	 * Retrieves the collection of values for a particular attribute as a numeric
	 * representation. Handy for numerical data.
	 *
	 * @param container
	 * @param attribute
	 * @param skipNull   do not return null values
	 * @param skipNaN    do not return NaN values
	 * @param saveMemory one-time computation without storing the values in the
	 *                   container
	 * @return numeric values (Double), filtered as requested
	 */
	public static double[] getAttributeValuesNumerical(ComplexDataContainer container, String attribute,
			boolean skipNull, boolean skipNaN, boolean saveMemory) {

		return collectNumerical(container, attribute, null, skipNull, skipNaN, saveMemory);
	}

	/**
	 * Same as getAttributeValuesNumerical, but only for objects whose key attribute
	 * value is contained in the given key set.
	 *
	 * @param container
	 * @param attribute
	 * @param keyset     external set of keys, interpreted as values of
	 *                   container.getKeyAttribute()
	 * @param skipNull   do not return null values
	 * @param skipNaN    do not return NaN values
	 * @param saveMemory one-time computation without storing the values in the
	 *                   container
	 * @return numeric values (Double) for the selected subset
	 */
	public static double[] getAttributeValuesNumerical(ComplexDataContainer container, String attribute,
			Collection<?> keyset, boolean skipNull, boolean skipNaN, boolean saveMemory) {

		Objects.requireNonNull(keyset, "keyset must not be null");
		return collectNumerical(container, attribute, keyset, skipNull, skipNaN, saveMemory);
	}

	// -------------------------------------------------------------------------
	// Shared implementation
	// -------------------------------------------------------------------------

	static double[] collectNumerical(ComplexDataContainer container, String attribute, Collection<?> keysetOrNull,
			boolean skipNull, boolean skipNaN, boolean saveMemory) {

		Objects.requireNonNull(container, "container must not be null");
		Objects.requireNonNull(attribute, "attribute must not be null");
		if (!container.isNumeric(attribute)) {
			throw new IllegalArgumentException(
					"collectNumerical does not work for non-numerical attributes: " + attribute);
		}

		final Set<?> keyset = normalizeKeyset(keysetOrNull);
		final int capacity = estimateCapacity(container.size(), keyset, saveMemory, skipNull, skipNaN);
		final DoubleBuffer buf = new DoubleBuffer(capacity);

		if (keyset == null && !saveMemory) {
			for (Object o : container.getAttributeValueCollection(attribute)) {
				appendParsed(buf, o, skipNull, skipNaN);
			}
		} else {
			final String keyAttr = container.getPrimaryKeyAttribute();
			for (Iterator<ComplexDataObject> it = container.iterator(); it.hasNext();) {
				final ComplexDataObject cdo = it.next();
				if (cdo == null)
					continue;
				if (keyset != null) {
					final Object key = cdo.getAttribute(keyAttr);
					if (key == null || !keyset.contains(key))
						continue;
				}
				appendParsed(buf, cdo.getAttribute(attribute), skipNull, skipNaN);
			}
		}

		return buf.trimmedArray();
	}

	// -------------------------------------------------------------------------
	// parsing
	// -------------------------------------------------------------------------

	private static void appendParsed(DoubleBuffer buf, Object o, boolean skipNull, boolean skipNaN) {
		if (o == null) {
			if (!skipNull)
				buf.add(Double.NaN); // null sentinel
			return;
		}
		final double v;
		if (o instanceof Number) {
			v = ((Number) o).doubleValue();
		} else {
			final Double d = Parsers.parseDouble(o);
			if (d == null) {
				if (!skipNull)
					buf.add(Double.NaN);
				return;
			}
			v = d;
		}
		if (skipNaN && Double.isNaN(v))
			return;
		buf.add(v);
	}

	// -------------------------------------------------------------------------
	// buffer
	// -------------------------------------------------------------------------

	/**
	 * A minimal, thread-safe-by-design (non-shared) re-sizable double[].
	 * Encapsulates grow logic so callers never deal with buffer/size pairs.
	 */
	private static final class DoubleBuffer {
		double[] data;
		int size;

		DoubleBuffer(int initialCapacity) {
			this.data = new double[Math.max(initialCapacity, 16)];
		}

		void add(double v) {
			if (size == data.length) {
				data = Arrays.copyOf(data, (int) Math.min((long) data.length * 2, Integer.MAX_VALUE - 8));
			}
			data[size++] = v;
		}

		double[] trimmedArray() {
			return (size == data.length) ? data : Arrays.copyOf(data, size);
		}
	}

	// -------------------------------------------------------------------------
	// helpers
	// -------------------------------------------------------------------------

	private static Set<?> normalizeKeyset(Collection<?> keysetOrNull) {
		if (keysetOrNull == null)
			return null;
		if (keysetOrNull instanceof Set<?>)
			return (Set<?>) keysetOrNull;
		if (keysetOrNull.size() > 10_000) {
			System.err.println("ComplexDataContainers.normalizeKeyset: collectNumerical: converting large Collection ("
					+ keysetOrNull.size() + " elements) to HashSet; pass a Set to avoid this copy.");
		}
		return new HashSet<>(keysetOrNull);
	}

	private static int estimateCapacity(int containerSize, Set<?> keyset, boolean saveMemory, boolean skipNull,
			boolean skipNaN) {

		int base = (keyset == null) ? containerSize : Math.min(containerSize, keyset.size());

		// Only cap when saveMemory is requested AND filtering will likely reduce
		// output.
		if (saveMemory && base > 200_000 && (skipNull || skipNaN)) {
			return 200_000;
		}
		return base;
	}

	/**
	 * returns the missing value rate of an attribute in the container.
	 * 
	 * @param container
	 * @param attribute
	 * @param saveMemory
	 * @return output as relative number between 0 and 1. Multiply with 100 to
	 *         achieve a percent notation.
	 */
	public static double getAttributeMissingValueRate(ComplexDataContainer container, String attribute,
			boolean saveMemory) {
		Objects.requireNonNull(container);

		if (!container.containsAttribute(attribute))
			throw new IllegalArgumentException(
					"ComplexDataContainers.getAttributeMissingValueRate: container does not contain attribute "
							+ attribute);

		int size = 0;
		if (container.isNumeric(attribute))
			size = getAttributeValuesNumerical(container, attribute, true, true, saveMemory).length;
		else
			size = getAttributeValuesCategorical(container, attribute, true, true, saveMemory).size();

		return MathFunctions.round((1 - size / (double) container.size()), 3);
	}

	/**
	 * Convenient method to convert from a set of Object to a set of String
	 * 
	 * @param container
	 * @return
	 */
	public static Set<String> keySet(ComplexDataContainer container) {
		Set<String> primaryKeys = new HashSet<>();

		for (Object o : container.primaryKeySet())
			primaryKeys.add(Parsers.parseString(o));

		return primaryKeys;
	}

	/**
	 * Convenient method to convert from a set of Object to a set of Long
	 * 
	 * @param container
	 * @return
	 */
	public static Set<Long> keySetAsLong(ComplexDataContainer container) {
		Set<Long> primaryKeys = new HashSet<>();

		for (Object o : container.primaryKeySet())
			primaryKeys.add(Parsers.parseLong(o));

		return primaryKeys;
	}

	/**
	 * Reduces the objects in a ComplexDataContainer to those whose primary key is
	 * contained in the given set. Objects with a primary key not present in
	 * {@code primaryKeys} are removed from the container in-place.
	 *
	 * @param container   the container to reduce, must not be null
	 * @param primaryKeys the primary keys to retain, must not be null
	 */
	public static void reduceToKeySet(ComplexDataContainer container, Collection<?> primaryKeys) {
		Objects.requireNonNull(container, "container must not be null");
		Objects.requireNonNull(primaryKeys, "primaryKeys must not be null");

		final Set<?> keep = (primaryKeys instanceof Set<?>) ? (Set<?>) primaryKeys : new HashSet<>(primaryKeys);

		final List<Object> keysToRemove = new ArrayList<>();
		for (Object key : container.primaryKeySet())
			if (key == null || !keep.contains(key))
				keysToRemove.add(key);

		for (Object key : keysToRemove) {
			ComplexDataObject cdo = container.get(key);
			if (cdo != null)
				container.remove(cdo);
		}
	}

	/**
	 * Returns a new ComplexDataContainer containing only the objects whose primary
	 * key is present in {@code primaryKeys}. The original container is not
	 * modified.
	 *
	 * @param container   source container, must not be null
	 * @param primaryKeys primary keys to retain, must not be null
	 * @return new ComplexDataContainer with the matching objects, preserving the
	 *         original primaryKeyAttribute
	 */
	public static ComplexDataContainer reducedToKeySet(ComplexDataContainer container, Collection<?> primaryKeys) {
		Objects.requireNonNull(container, "container must not be null");
		Objects.requireNonNull(primaryKeys, "primaryKeys must not be null");

		final Set<?> keep = (primaryKeys instanceof Set<?>) ? (Set<?>) primaryKeys : new HashSet<>(primaryKeys);

		final List<ComplexDataObject> retained = new ArrayList<>();
		for (Object key : container.primaryKeySet()) {
			if (key == null || !keep.contains(key))
				continue;
			ComplexDataObject cdo = container.get(key);
			if (cdo != null)
				retained.add(cdo);
		}

		return new ComplexDataContainer(retained, container.getPrimaryKeyAttribute());
	}

}
