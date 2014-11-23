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

package org.terasology.polyworld.voronoi;

import java.util.List;

import org.terasology.math.Region3i;
import org.terasology.math.Vector3i;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.facets.base.SparseFacet3D;

import com.google.common.collect.Lists;

/**
 * TODO Type description
 * @author Martin Steiger
 */
public class GraphFacetImpl extends SparseFacet3D implements GraphFacet {

    private List<Graph> graphs = Lists.newArrayList();

    /**
     * @param targetRegion
     * @param border
     */
    public GraphFacetImpl(Region3i targetRegion, Border3D border) {
        super(targetRegion, border);
    }

    @Override
    public void add(Graph graph) {
        graphs.add(graph);
    }

    @Override
    public Graph getWorld(int x, int y, int z) {
        for (Graph g : graphs) {
            if (g.getBounds().contains(x, z)) {
                return g;
            }
        }

       throw new IllegalArgumentException(String.format("no graph data for {}/{}/{}", x, y, z));
    }

    @Override
    public Graph getWorld(Vector3i pos) {
        return getWorld(pos.x, pos.y, pos.z);
    }

    @Override
    public Graph get(int x, int y, int z) {
        return getWorld(relativeToWorld(x, y, z));
    }

    @Override
    public Graph get(Vector3i pos) {
        return get(pos.x, pos.y, pos.z);
    }

}
