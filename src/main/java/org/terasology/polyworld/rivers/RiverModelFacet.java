// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.polyworld.rivers;

import com.google.common.collect.Maps;
import org.terasology.engine.world.generation.WorldFacet;
import org.terasology.polyworld.graph.Graph;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * TODO Type description
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
