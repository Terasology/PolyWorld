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

package org.terasology.polyworld.elevation;

import java.util.Map;

import org.terasology.polyworld.graph.Graph;
import org.terasology.world.generation.WorldFacet;

import com.google.common.collect.Maps;

/**
 * TODO Type description
 * @author Martin Steiger
 */
public class ElevationModelFacet implements WorldFacet {

    private final Map<Graph, ElevationModel> map = Maps.newHashMap();

    /**
     * @param g the graph
     * @param model the elevation model to add
     */
    public void add(Graph g, ElevationModel model) {
        map.put(g, model);
    }

    /**
     * @param graph
     * @return
     */
    public ElevationModel get(Graph graph) {
        return map.get(graph);
    }

}
