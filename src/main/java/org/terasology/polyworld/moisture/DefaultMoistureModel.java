// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.polyworld.moisture;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.terasology.engine.utilities.random.FastRandom;
import org.terasology.engine.utilities.random.Random;
import org.terasology.polyworld.graph.Corner;
import org.terasology.polyworld.graph.Graph;
import org.terasology.polyworld.graph.Region;
import org.terasology.polyworld.rivers.RiverModel;
import org.terasology.polyworld.water.WaterModel;

import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Uses rivers and lakes as sources of moisture and distributes it over the entire island.
 */
public class DefaultMoistureModel implements MoistureModel {

    private final Graph graph;
    private final Map<Corner, Float> moisture = Maps.newHashMap();
    private final RiverModel riverModel;
    private final WaterModel waterModel;

    public DefaultMoistureModel(Graph graph, RiverModel riverModel, WaterModel waterModel) {
        this.graph = graph;
        this.riverModel = riverModel;
        this.waterModel = waterModel;

        assignRiverAndLakeMoisture();
        assignRandomSeedMoisture();
        spreadMoisture();
        assignOceanMoisture();

        assignRemaining(0f);

        redistributeMoisture();
    }

    private void assignRiverAndLakeMoisture() {
        for (Corner c : graph.getCorners()) {
            int riverValue = riverModel.getRiverValue(c);
            if ((waterModel.isWater(c) || riverValue > 0) && !waterModel.isOcean(c)) {
                moisture.put(c, riverValue > 0 ? Math.min(3.0f, (0.2f * riverValue)) : 1.0f);
            }
        }
    }

    private void assignRandomSeedMoisture() {
        int cornerCount = graph.getCorners().size();
        int count = cornerCount / 50;
        Random r = new FastRandom(133353);

        for (int i = 0; i < count; i++) {
            Corner c = graph.getCorners().get(r.nextInt(cornerCount));
            // the seed value should be below the normalization threshold in redistributeMoisture
            float seedValue = 0.25f;
            moisture.put(c, seedValue);
        }
    }

    private void spreadMoisture() {
        Deque<Corner> queue = new LinkedList<>(moisture.keySet());

        while (!queue.isEmpty()) {
            Corner c = queue.pop();
            float cm = getMoisture(c);
            for (Corner a : c.getAdjacent()) {
                float newM = .9f * cm;
                if (newM > moisture.getOrDefault(a, 0f)) {
                    moisture.put(a, newM);
                    queue.add(a);
                }
            }
        }
    }

    private void assignOceanMoisture() {
        for (Corner c : graph.getCorners()) {
            if (waterModel.isOcean(c) || waterModel.isCoast(c)) {
                moisture.put(c, 1.0f);
            }
        }
    }

    private void assignRemaining(float value) {
        for (Corner c : graph.getCorners()) {
            if (!moisture.containsKey(c)) {
                moisture.put(c, value);
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

        int size = landCorners.size();
        if (size == 0) {
            return;
        }

        Collections.sort(landCorners, new Comparator<Corner>() {
            @Override
            public int compare(Corner o1, Corner o2) {
                float m1 = getMoisture(o1);
                float m2 = getMoisture(o2);

                return Double.compare(m1, m2);
            }
        });

        // the list is sorted now, so the last entry has the largest number
        float maximum = getMoisture(landCorners.get(size - 1));

        // if there is no real moisture then don't scale up to max, which is around lakes and rivers
        float v = (maximum < 0.3) ? 0.3f : 1f;
        float scale = v / size;

        for (int i = 0; i < size; i++) {
            moisture.put(landCorners.get(i), i * scale);
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
