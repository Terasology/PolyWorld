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

package org.terasology.polyworld.moisture;

import com.google.common.collect.Maps;
import org.terasology.engine.world.generation.WorldFacet;
import org.terasology.polyworld.graph.Graph;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * TODO Type description
 */
public class MoistureModelFacet implements WorldFacet {

    private final Map<Graph, MoistureModel> map = Maps.newHashMap();

    /**
     * @param g
     * @param model the moisture model for the graph
     */
    public void add(Graph g, MoistureModel model) {
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
    public MoistureModel get(Graph graph) {
        return map.get(graph);
    }

}
