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

import java.util.HashMap;
import java.util.Map;

/**
 * A wrapper implementation for a {@link Map}
 * with an additional method {@link #getOrDefault(K)}
 * Only the get() method differs from the original implementation.
 * This class is similar to apache.commons.collections.map.DefaultedMap.
 * In Java8, there is also Map.getOrDefault(), but this requires providing a
 * default value on every getOrDefault call
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 * @author Martin Steiger
 */
public class DefaultValueMap<K, V> extends DelegateMap<K, V>
{
	private final V defValue;

	/**
	 * Uses an empty {@link HashMap} as target
	 * @param defValue the default value
	 */
	public DefaultValueMap(V defValue)
	{
		this(new HashMap<K, V>(), defValue);
	}

	/**
	 * @param map the original map
	 * @param defValue the default value (not <code>null</code>)
	 */
	public DefaultValueMap(Map<K, V> map, V defValue)
	{
		super(map);

		if (defValue == null)
	        throw new IllegalArgumentException("Default value cannot be null - use a regular map instead");

		this.defValue = defValue;
	}

	public V getOrDefault(Object key)
	{
		V value = super.get(key);
		if (value != null || containsKey(key)) {
		    return value;
		}

	    return defValue;
	}
}

