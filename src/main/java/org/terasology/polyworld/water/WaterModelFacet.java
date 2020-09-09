// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.polyworld.water;

import com.google.common.collect.Maps;
import org.terasology.engine.world.generation.WorldFacet;
import org.terasology.polyworld.graph.Graph;

import java.util.Map;

/**
 * TODO Type description
 */
public class WaterModelFacet implements WorldFacet {

    private final Map<Graph, WaterModel> map = Maps.newHashMap();

    /**
     * @param g
     * @param model the water model for the graph
     */
    public void add(Graph g, WaterModel model) {
        map.put(g, model);
    }

    /**
     * @param graph
     * @return
     */
    public WaterModel get(Graph graph) {
        return map.get(graph);
    }

}
