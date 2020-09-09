// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.polyworld.elevation;

import com.google.common.collect.Maps;
import org.terasology.engine.world.generation.WorldFacet;
import org.terasology.polyworld.graph.Graph;

import java.util.Map;

/**
 * Manages {@link ElevationModel} instances per graph.
 */
public class ElevationModelFacet implements WorldFacet {

    private final Map<Graph, ElevationModel> map = Maps.newHashMap();

    /**
     * Existing entries will be overwritten
     *
     * @param g the graph
     * @param model the elevation model to set
     */
    public void set(Graph g, ElevationModel model) {
        map.put(g, model);
    }

    /**
     * @param graph the graph of interest
     * @return the corresponding elevation model or <code>null</code>.
     */
    public ElevationModel get(Graph graph) {
        return map.get(graph);
    }

}
