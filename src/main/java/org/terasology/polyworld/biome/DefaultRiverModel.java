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

import java.util.List;
import java.util.Random;

import org.terasology.polyworld.map.DefaultValueMap;
import org.terasology.polyworld.voronoi.Corner;
import org.terasology.polyworld.voronoi.Edge;
import org.terasology.polyworld.voronoi.VoronoiGraph;

/**
 * TODO Type description
 * @author Martin Steiger
 */
public class DefaultRiverModel implements RiverModel {

    private final DefaultValueMap<Edge, Integer> edgeVals = new DefaultValueMap<>(0);
    private final DefaultValueMap<Corner, Integer> cornerVals = new DefaultValueMap<>(0);

    public DefaultRiverModel(VoronoiGraph graph)
    {
        List<Corner> corners = graph.getCorners();

        int count = corners.size() / 50;
        Random r = new Random(133353);

        for (int i = 0; i < count; i++) {
            Corner c = corners.get(r.nextInt(corners.size()));
            if (c.isOcean() || c.getElevation() < 0.3 || c.getElevation() > 0.9) {
                continue;
            }
            // Bias rivers to go west: if (q.downslope.x > q.x) continue;
            while (!c.isCoast()) {
                if (c == c.getDownslope()) {
                    break;
                }
                Edge edge = graph.lookupEdgeFromCorner(c, c.getDownslope());
                if (!edge.getCorner0().isWater() || !edge.getCorner1().isWater()) {
                    edgeVals.put(edge, edgeVals.getOrDefault(edge) + 1);
                    cornerVals.put(c, edgeVals.getOrDefault(c) + 1);
                    cornerVals.put(c.getDownslope(), cornerVals.getOrDefault(c.getDownslope()) + 1);  // TODO: fix double count
                }
                c = c.getDownslope();
            }
        }
    }

    /**
     * @return the river
     */
    public int getRiverValue(Edge edge) {
        return edgeVals.getOrDefault(edge);
    }

    @Override
    public int getRiverValue(Corner c) {
        return cornerVals.getOrDefault(c);
    }
}
