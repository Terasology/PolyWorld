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

package org.terasology.polyworld.moisture;

import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.terasology.polyworld.graph.Corner;
import org.terasology.polyworld.graph.Graph;
import org.terasology.polyworld.graph.Region;
import org.terasology.polyworld.rivers.RiverModel;
import org.terasology.polyworld.water.WaterModel;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * TODO Type description
 * @author Martin Steiger
 */
public class DefaultMoistureModel implements MoistureModel {

    private final Graph graph;
    private final Map<Corner, Float> moisture = Maps.newHashMap();
    private final RiverModel riverModel;
    private WaterModel waterModel;

    public DefaultMoistureModel(Graph graph, RiverModel riverModel, WaterModel waterModel) {
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
                moisture.put(c, riverValue > 0 ? Math.min(3.0f, (0.2f * riverValue)) : 1.0f);
                queue.push(c);
            } else {
                moisture.put(c, 0.0f);
            }
        }

        while (!queue.isEmpty()) {
            Corner c = queue.pop();
            for (Corner a : c.getAdjacent()) {
                float newM = .9f * getMoisture(c);
                if (newM > getMoisture(a)) {
                    moisture.put(a, newM);
                    queue.add(a);
                }
            }
        }

        // Salt water
        for (Corner c : graph.getCorners()) {
            if (waterModel.isOcean(c) || waterModel.isCoast(c)) {
                moisture.put(c, 1.0f);
            }
        }
    }

    private void redistributeMoisture() {

        List<Corner> landCorners = Lists.newArrayList();
        for (Corner c : graph.getCorners()) {
            if (!waterModel.isOcean(c) && !waterModel.isCoast(c)) {
                landCorners.add(c);
            }
        }

        Collections.sort(landCorners, new Comparator<Corner>() {
            @Override
            public int compare(Corner o1, Corner o2) {
                float m1 = getMoisture(o1);
                float m2 = getMoisture(o2);

                return Double.compare(m1, m2);
            }
        });
        for (int i = 0; i < landCorners.size(); i++) {
            moisture.put(landCorners.get(i), (float) i / landCorners.size());
        }
    }

    @Override
    public float getMoisture(Region r) {
        float total = 0;
        for (Corner c : r.getCorners()) {
            total += getMoisture(c);
        }
        return total / r.getCorners().size();
    }

    @Override
    public float getMoisture(Corner c) {
        return moisture.get(c);
    }
}
