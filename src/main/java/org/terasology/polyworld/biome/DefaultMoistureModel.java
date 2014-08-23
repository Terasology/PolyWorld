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

import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.terasology.polyworld.voronoi.Corner;
import org.terasology.polyworld.voronoi.Region;
import org.terasology.polyworld.voronoi.VoronoiGraph;

import com.google.common.collect.Maps;

/**
 * TODO Type description
 * @author Martin Steiger
 */
public class DefaultMoistureModel implements MoistureModel {

    private final VoronoiGraph graph;
    private final Map<Corner, Double> moisture = Maps.newHashMap();
    private final RiverModel riverModel;
    private WaterModel waterModel;

    public DefaultMoistureModel(VoronoiGraph graph, RiverModel riverModel, WaterModel waterModel)
    {
        this.graph = graph;
        this.riverModel = riverModel;
        this.waterModel = waterModel;

        assignCornerMoisture();
        redistributeMoisture();
    }

    private void assignCornerMoisture() {
        Deque<Corner> queue = new LinkedList<>();
        for (Corner c : graph.getCorners()) {
            int riverValue = riverModel.getRiverValue(c);
            if ((waterModel.isWater(c) || riverValue > 0) && !waterModel.isOcean(c)) {
                moisture.put(c, riverValue > 0 ? Math.min(3.0, (0.2 * riverValue)) : 1.0);
                queue.push(c);
            } else {
                moisture.put(c, 0.0);
            }
        }

        while (!queue.isEmpty()) {
            Corner c = queue.pop();
            for (Corner a : c.getAdjacent()) {
                double newM = .9 * getMoisture(c);
                if (newM > getMoisture(a)) {
                    moisture.put(a, newM);
                    queue.add(a);
                }
            }
        }

        // Salt water
        for (Corner c : graph.getCorners()) {
            if (waterModel.isOcean(c) || waterModel.isCoast(c)) {
                moisture.put(c, 1.0);
            }
        }
    }

    private void redistributeMoisture() {

        List<Corner> landCorners = waterModel.getLandCorners();

        Collections.sort(landCorners, new Comparator<Corner>() {
            @Override
            public int compare(Corner o1, Corner o2) {
                double m1 = getMoisture(o1);
                double m2 = getMoisture(o2);

                // TODO: replace with Double.compare()
                if (m1 > m2) {
                    return 1;
                } else if (m1 < m2) {
                    return -1;
                }
                return 0;
            }
        });
        for (int i = 0; i < landCorners.size(); i++) {
            moisture.put(landCorners.get(i), (double) i / landCorners.size());
        }
    }

    @Override
    public double getMoisture(Region r) {
        double total = 0;
        for (Corner c : r.getCorners()) {
            total += getMoisture(c);
        }
        return total / r.getCorners().size();
    }

    @Override
    public double getMoisture(Corner c) {
        return moisture.get(c);
    }
}
