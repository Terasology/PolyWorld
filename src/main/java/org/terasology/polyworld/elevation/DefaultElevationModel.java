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

import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.terasology.polyworld.voronoi.Corner;
import org.terasology.polyworld.voronoi.Graph;
import org.terasology.polyworld.water.WaterModel;

import com.google.common.collect.Maps;

/**
 * TODO Type description
 * @author Martin Steiger
 */
public class DefaultElevationModel extends AbstractElevationModel {

    private Graph graph;

    private final Map<Corner, Double> elevations = Maps.newHashMap();

    private final WaterModel waterModel;

    public DefaultElevationModel(Graph graph, WaterModel waterModel) {
        this.graph = graph;
        this.waterModel = waterModel;

        assignCornerElevations();
        redistributeElevations(waterModel.getLandCorners());
    }

    private void assignCornerElevations() {

        Deque<Corner> queue = new LinkedList<>();
        for (Corner c : graph.getCorners()) {
            if (c.isBorder()) {
                elevations.put(c, 0.0);
                queue.add(c);
            } else {
                elevations.put(c, Double.MAX_VALUE);
            }
        }

        while (!queue.isEmpty()) {
            Corner c = queue.pop();
            for (Corner a : c.getAdjacent()) {
                double newElevation = 0.01 + elevations.get(c);
                if (!waterModel.isWater(c) && !waterModel.isWater(a)) {
                    newElevation += 1;
                }
                if (newElevation < elevations.get(a)) {
                    elevations.put(a, newElevation);
                    queue.add(a);
                }
            }
        }
    }

    private void redistributeElevations(List<Corner> landCorners) {
        Collections.sort(landCorners, new Comparator<Corner>() {
            @Override
            public int compare(Corner o1, Corner o2) {
                if (elevations.get(o1) > elevations.get(o2)) {
                    return 1;
                } else if (elevations.get(o1) < elevations.get(o2)) {
                    return -1;
                }
                return 0;
            }
        });

        final double scaleFactor = 1.1;
        for (int i = 0; i < landCorners.size(); i++) {
            double y = (double) i / landCorners.size();
            double x = Math.sqrt(scaleFactor) - Math.sqrt(scaleFactor * (1 - y));
            x = Math.min(x, 1);
            elevations.put(landCorners.get(i), x);
        }

        for (Corner c : graph.getCorners()) {
            if (waterModel.isOcean(c) || waterModel.isCoast(c)) {
                elevations.put(c, 0.0);
            }
        }
    }

    @Override
    public double getElevation(Corner corner) {
        return elevations.get(corner);
    }

}
