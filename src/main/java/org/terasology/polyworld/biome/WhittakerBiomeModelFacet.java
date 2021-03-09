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

package org.terasology.polyworld.biome;

import com.google.common.collect.Maps;
import org.terasology.engine.world.generation.WorldFacet;
import org.terasology.polyworld.graph.Graph;

import java.util.Map;

/**
 * TODO Type description
 */
public class WhittakerBiomeModelFacet implements WorldFacet {

    private final Map<Graph, BiomeModel> map = Maps.newHashMap();

    /**
     * @param g
     * @param model the biome model to add
     */
    public void add(Graph g, BiomeModel model) {
        map.put(g, model);
    }

    /**
     * @param graph
     * @return
     */
    public BiomeModel get(Graph graph) {
        return map.get(graph);
    }

}
