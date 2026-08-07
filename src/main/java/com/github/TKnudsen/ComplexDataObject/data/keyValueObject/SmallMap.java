package com.github.TKnudsen.ComplexDataObject.data.keyValueObject;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * <p>
 * A lightweight, memory-efficient Map optimized for small sizes (~ 1--10
 * entries).
 *
 * Backed by a plain {@code ArrayList} scanned linearly -- the right trade-off
 * at this size, and simple enough to keep {@link #entrySet()} (and, via
 * {@link AbstractMap}, {@link #keySet()}/{@link #values()}) genuinely backed by
 * the map rather than a detached snapshot, satisfying the {@link Map} contract
 * (e.g. {@code keySet().remove(k)} actually removes {@code k}).
 * 
 * Does NOT guarantee any ordering of keys or entries.
 * </p>
 */
class SmallMap<K, V> extends AbstractMap<K, V> {

	private final List<Map.Entry<K, V>> entries = new ArrayList<>(4);

	@Override
	public int size() {
		return entries.size();
	}

	@Override
	public boolean isEmpty() {
		return entries.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		for (Map.Entry<K, V> e : entries)
			if (Objects.equals(e.getKey(), key))
				return true;
		return false;
	}

	@Override
	public boolean containsValue(Object value) {
		for (Map.Entry<K, V> e : entries)
			if (Objects.equals(e.getValue(), value))
				return true;
		return false;
	}

	@Override
	public V get(Object key) {
		for (Map.Entry<K, V> e : entries)
			if (Objects.equals(e.getKey(), key))
				return e.getValue();
		return null;
	}

	@Override
	public V put(K key, V value) {
		for (Map.Entry<K, V> e : entries) {
			if (Objects.equals(e.getKey(), key)) {
				V old = e.getValue();
				e.setValue(value);
				return old;
			}
		}
		entries.add(new AbstractMap.SimpleEntry<>(key, value));
		return null;
	}

	@Override
	public V remove(Object key) {
		Iterator<Map.Entry<K, V>> it = entries.iterator();
		while (it.hasNext()) {
			Map.Entry<K, V> e = it.next();
			if (Objects.equals(e.getKey(), key)) {
				it.remove();
				return e.getValue();
			}
		}
		return null;
	}

	@Override
	public void clear() {
		entries.clear();
	}

	/**
	 * Backed by {@link #entries} directly -- no caching, no hashing of mutable
	 * entries. {@code keySet()}/{@code values()} (inherited from
	 * {@link AbstractMap}) are themselves backed by this view, so all three stay
	 * consistent with the map and with each other automatically.
	 */
	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		return new AbstractSet<Map.Entry<K, V>>() {
			@Override
			public Iterator<Map.Entry<K, V>> iterator() {
				return entries.iterator();
			}

			@Override
			public int size() {
				return entries.size();
			}

			@Override
			public boolean contains(Object o) {
				if (!(o instanceof Map.Entry))
					return false;
				Map.Entry<?, ?> other = (Map.Entry<?, ?>) o;
				for (Map.Entry<K, V> e : entries)
					if (Objects.equals(e.getKey(), other.getKey()) && Objects.equals(e.getValue(), other.getValue()))
						return true;
				return false;
			}

			@Override
			public boolean remove(Object o) {
				if (!(o instanceof Map.Entry))
					return false;
				Map.Entry<?, ?> other = (Map.Entry<?, ?>) o;
				Iterator<Map.Entry<K, V>> it = entries.iterator();
				while (it.hasNext()) {
					Map.Entry<K, V> e = it.next();
					if (Objects.equals(e.getKey(), other.getKey()) && Objects.equals(e.getValue(), other.getValue())) {
						it.remove();
						return true;
					}
				}
				return false;
			}

			@Override
			public void clear() {
				entries.clear();
			}
		};
	}

	@Override
	public String toString() {
		return "SmallMap" + entries.toString();
	}
}
