/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.polyworld.map;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * A wrapper of {@link Map} that
 * delegates all calls to a given map.
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 * @author Martin Steiger
 */
public class DelegateMap<K, V> implements Map<K, V>
{
	private final Map<K, V> map;

	/**
	 * @param map the original map
	 */
	public DelegateMap(Map<K, V> map)
	{
		this.map = map;
	}

	@Override
	public int size()
	{
		return map.size();
	}

	@Override
	public boolean isEmpty()
	{
		return map.isEmpty();
	}

	@Override
	public boolean containsKey(Object key)
	{
		return map.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value)
	{
		return map.containsValue(value);
	}

	@Override
	public V get(Object key)
	{
		return map.get(key);
	}

	@Override
	public V put(K key, V value)
	{
		return map.put(key, value);
	}

	@Override
	public V remove(Object key)
	{
		return map.remove(key);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m)
	{
		map.putAll(m);
	}

	@Override
	public void clear()
	{
		map.clear();
	}

	@Override
	public Set<K> keySet()
	{
		return map.keySet();
	}

	@Override
	public Collection<V> values()
	{
		return map.values();
	}

	@Override
	public Set<Entry<K, V>> entrySet()
	{
		return map.entrySet();
	}

	@Override
	public boolean equals(Object o)
	{
		return map.equals(o);
	}

	@Override
	public int hashCode()
	{
		return map.hashCode();
	}

	@Override
	public String toString()
	{
		return map.toString();
	}
}
