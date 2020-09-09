// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

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
