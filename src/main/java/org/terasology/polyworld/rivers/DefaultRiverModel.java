// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.polyworld.rivers;

import com.google.common.collect.Maps;
import org.terasology.polyworld.elevation.ElevationModel;
import org.terasology.polyworld.graph.Corner;
import org.terasology.polyworld.graph.Edge;
import org.terasology.polyworld.graph.Graph;
import org.terasology.polyworld.water.WaterModel;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * TODO Type description
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
            float elevation = elevationModel.getElevation(c);
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
                    incrementCornerVal(downslope, 1);  // TODO: fix float count
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
