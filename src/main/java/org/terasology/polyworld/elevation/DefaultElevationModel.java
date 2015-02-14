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
import java.util.Set;

import org.terasology.polyworld.voronoi.Corner;
import org.terasology.polyworld.voronoi.Graph;
import org.terasology.polyworld.voronoi.Region;
import org.terasology.polyworld.water.WaterModel;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * TODO Type description
 * @author Martin Steiger
 */
public class DefaultElevationModel extends AbstractElevationModel {

    private Graph graph;

    private final Map<Corner, Float> elevations = Maps.newHashMap();

    private final WaterModel waterModel;

    /**
     * @param graph the polygon graph
     * @param waterModel the water model that defines ocean regions
     * @param scale a non-linear scale factor to adjust the height distribution in [0..1]
     */
    public DefaultElevationModel(Graph graph, WaterModel waterModel, float scale) {
        this.graph = graph;
        this.waterModel = waterModel;

        List<Corner> landCorners = Lists.newArrayList();
        for (Corner c : graph.getCorners()) {
            if (!waterModel.isOcean(c) && !waterModel.isCoast(c)) {
                landCorners.add(c);
            }
        }

        assignCornerElevations();
        redistributeElevations(landCorners, scale);
        flattenLakes();

        for (Corner c : graph.getCorners()) {
            if (waterModel.isCoast(c)) {
                elevations.put(c, 0.0f);
            }
        }
    }

    private void flattenLakes() {
        Set<Region> isFlat = Sets.newHashSet();
        for (Region r : graph.getRegions()) {
            boolean isLake = waterModel.isWater(r) && !waterModel.isOcean(r);
            if (isLake && !isFlat.contains(r)) {
                flattenLake(r);
                isFlat.add(r);
                isFlat.addAll(r.getNeighbors());
            }
        }
    }

    private void flattenLake(Region r) {
        float avgHeight = getAverageHeight(r);
        for (Region neigh : r.getNeighbors()) {
            avgHeight += getAverageHeight(neigh);
        }
        float targetHeight = avgHeight / (r.getNeighbors().size() + 1);

        // assign target height to all corner
        for (Corner c : r.getCorners()) {
            elevations.put(c, targetHeight);
        }
        for (Region neigh : r.getNeighbors()) {
            for (Corner c : neigh.getCorners()) {
                elevations.put(c, targetHeight);
            }
        }
    }

    private float getAverageHeight(Region r) {
        float sum = 0;
        for (Corner c : r.getCorners()) {
            sum += elevations.get(c).floatValue();
        }
        return sum / r.getCorners().size();
    }

    private void assignCornerElevations() {

        Deque<Corner> queue = new LinkedList<>();
        for (Corner c : graph.getCorners()) {
            if (c.isBorder()) {
                elevations.put(c, -1.0f);
                queue.add(c);
            } else {
                elevations.put(c, Float.MAX_VALUE);
            }
        }

        while (!queue.isEmpty()) {
            Corner c = queue.pop();
            for (Corner a : c.getAdjacent()) {
                float newElevation = elevations.get(c);
                if (!waterModel.isWater(c) && !waterModel.isWater(a)) {
                    newElevation += 1;
                }
                Float prevElevation = elevations.get(a);
                if (newElevation < prevElevation) {
                    elevations.put(a, newElevation);
                    queue.add(a);
                }
            }
        }
    }

    private void redistributeElevations(List<Corner> landCorners, float scale) {

        // sort land corners by elevation
        Collections.sort(landCorners, new Comparator<Corner>() {
            @Override
            public int compare(Corner o1, Corner o2) {
                Float e1 = elevations.get(o1);
                Float e2 = elevations.get(o2);
                return e1.compareTo(e2);
            }
        });

        // reset the elevation x of each to match the inverse of the desired cumulative distribution:
        // y(x) = 1 - (1-x)^2
        // --> solve for x
        // x = 1 - sqrt(1 - y)

        final float scaleFactor = 1.1f;
        for (int i = 0; i < landCorners.size(); i++) {

            // y is the relative position in the sorted list
            float y = (float) i / (landCorners.size() - 1);

            // x is the desired elevation
            float x = scale * (float) (Math.sqrt(scaleFactor) - Math.sqrt(scaleFactor * (1 - y)));

            // clamp to max 1
            x = Math.min(x, 1);

            // this preserves ordering so that elevations always increase from the coast to the mountains.
            elevations.put(landCorners.get(i), x);
        }
    }

    @Override
    public float getElevation(Corner corner) {
        return elevations.get(corner);
    }

}
