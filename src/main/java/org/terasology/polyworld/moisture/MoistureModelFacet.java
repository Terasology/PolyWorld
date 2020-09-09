// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

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
