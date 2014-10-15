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

package org.terasology.polyworld.rivers;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.terasology.polyworld.elevation.ElevationModel;
import org.terasology.polyworld.voronoi.Corner;
import org.terasology.polyworld.voronoi.Edge;
import org.terasology.polyworld.voronoi.Graph;
import org.terasology.polyworld.water.WaterModel;

import com.google.common.collect.Maps;

/**
 * TODO Type description
 * @author Martin Steiger
 */
public class DefaultRiverModel implements RiverModel {

    private final Map<Edge, Integer> edgeVals = Maps.newHashMap();
    private final Map<Corner, Integer> cornerVals = Maps.newHashMap();

    public DefaultRiverModel(Graph graph, ElevationModel elevationModel, WaterModel waterModel) {
        List<Corner> corners = graph.getCorners();

        int count = corners.size() / 50;
        Random r = new Random(133353);

        for (int i = 0; i < count; i++) {
            Corner c = corners.get(r.nextInt(corners.size()));
            double elevation = elevationModel.getElevation(c);
            if (waterModel.isOcean(c) || elevation < 0.3 || elevation > 0.9) {
                continue;
            }
            // Bias rivers to go west: if (q.downslope.x > q.x) continue;
            while (!waterModel.isCoast(c)) {
                Corner downslope = elevationModel.getDownslope(c);
                if (c == downslope) {
                    break;
                }
                Edge edge = lookupEdgeFromCorner(c, downslope);
                if (!waterModel.isWater(edge.getCorner0()) || !waterModel.isWater(edge.getCorner1())) {
                    incrementEdgeVal(edge, 1);
                    incrementCornerVal(c, 1);
                    incrementCornerVal(downslope, 1);  // TODO: fix double count
                }
                c = downslope;
            }
        }
    }


    public Edge lookupEdgeFromCorner(Corner c1, Corner c2) {
        for (Edge e : c1.getEdges()) {
            if (e.getCorner0() == c2 || e.getCorner1() == c2) {
                return e;
            }
        }
        return null;
    }

    private void incrementEdgeVal(Edge edge, int inc) {
        int newVal = getRiverValue(edge) + inc;
        edgeVals.put(edge, Integer.valueOf(newVal));
    }

    private void incrementCornerVal(Corner corner, int inc) {
        int newVal = getRiverValue(corner) + inc;
        cornerVals.put(corner, Integer.valueOf(newVal));
    }

    @Override
    public int getRiverValue(Edge edge) {
        Integer boxed = edgeVals.get(edge);
        return (boxed == null) ? 0 : boxed.intValue();
    }

    @Override
    public int getRiverValue(Corner c) {
        Integer boxed = cornerVals.get(c);
        return (boxed == null) ? 0 : boxed.intValue();
    }
}
