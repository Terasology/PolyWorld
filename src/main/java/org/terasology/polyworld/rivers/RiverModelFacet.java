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

package org.terasology.polyworld.rivers;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.terasology.polyworld.graph.Graph;
import org.terasology.world.generation.WorldFacet;

import com.google.common.collect.Maps;

/**
 * TODO Type description
 * @author Martin Steiger
 */
public class RiverModelFacet implements WorldFacet {

    private final Map<Graph, RiverModel> map = Maps.newHashMap();

    /**
     * @param g
     * @param model the river model for the graph
     */
    public void add(Graph g, RiverModel model) {
        map.put(g, model);
    }

    /**
     * @return an unmodifiable set of valid graph entries
     */
    public Set<Graph> getKeys() {
        return Collections.unmodifiableSet(map.keySet());
    }

    /**
     * @param graph
     * @return
     */
    public RiverModel get(Graph graph) {
        return map.get(graph);
    }

}
