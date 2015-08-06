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

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.terasology.polyworld.graph.Corner;
import org.terasology.polyworld.graph.Graph;
import org.terasology.polyworld.graph.Region;
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
        redistributeElevationsInverse(landCorners, scale);
        flattenLakes();

        for (Corner c : graph.getCorners()) {
            if (waterModel.isCoast(c)) {
                elevations.put(c, 0.0f);
            }
            // some of ocean corners that are part of a bay are elevated
            // this is more of a workaround rather than a required operation
            if (waterModel.isOcean(c)) {
                elevations.put(c, -1f);
            }
        }
    }

    private void flattenLakes() {
        Set<Region> found = Sets.newHashSet();
        Predicate<Region> isLake = r -> waterModel.isWater(r) && !waterModel.isOcean(r);

        for (Region r : graph.getRegions()) {
            if (isLake.test(r) && !found.contains(r)) {
                Collection<Region> lake = floodFill(r, isLake);
                flattenLake(lake);
                found.addAll(lake);
            }
        }
    }

    private Collection<Region> floodFill(Region start, Predicate<Region> pred) {
        Collection<Region> lake = new HashSet<Region>();
        lake.add(start);

        Deque<Region> queue = new LinkedList<>();
        queue.add(start);

        while (!queue.isEmpty()) {
            Region next = queue.pop();
            for (Region n : next.getNeighbors()) {
                if (pred.test(n) && !lake.contains(n) && !queue.contains(n)) {
                    lake.add(n);
                    queue.add(n);
                }
            }
        }
        return lake;
    }

    private void flattenLake(Collection<Region> lake) {
//        float totalHeight = 0;
//        for (Region r : lake) {
//            totalHeight += getElevation(r);
//        }
//        float targetHeight = totalHeight / lake.size();

      float minHeight = Float.POSITIVE_INFINITY;
      for (Region r : lake) {
          float elevation = getElevation(r);
          if (minHeight >= elevation) {
              minHeight = elevation;
          }
      }

        // assign target height to all corners
        for (Region r : lake) {
            for (Corner c : r.getCorners()) {
                elevations.put(c, minHeight);
            }
        }
    }

    private void assignCornerElevations() {

        Deque<Corner> queue = new LinkedList<>();
        for (Corner c : graph.getCorners()) {
            if (c.isBorder()) {
                elevations.put(c, -1.0f);
                queue.add(c);
            } else {
                elevations.put(c, Float.POSITIVE_INFINITY);
            }
        }

        while (!queue.isEmpty()) {
            iterateCorners(queue, queue);
        }
    }

    private void iterateCorners(Deque<Corner> input, Deque<Corner> output) {
        while (!input.isEmpty()) {
            Corner c = input.pop();
            for (Corner a : c.getAdjacent()) {
                // adding the extra 0.01f is necessary to make the steepest
                // descent towards the ocean. I can't really tell why.
                float newElevation = elevations.get(c) + 0.01f;
                if (!waterModel.isWater(c) && !waterModel.isWater(a)) {
                    newElevation += 1;
                }
                Float prevElevation = elevations.get(a);
                if (newElevation < prevElevation) {
                    elevations.put(a, newElevation);
                    output.add(a);
                }
            }
        }
    }

    private void redistributeElevationsLinear(List<Corner> landCorners, float scale) {

        if (landCorners.isEmpty()) {
            return;
        }

        // sort land corners by elevation
        Corner peak = Collections.max(landCorners, new Comparator<Corner>() {
            @Override
            public int compare(Corner o1, Corner o2) {
                Float e1 = elevations.get(o1);
                Float e2 = elevations.get(o2);
                return e1.compareTo(e2);
            }
        });

        float maxHeight = elevations.get(peak);
        for (int i = 0; i < landCorners.size(); i++) {
            Corner corner = landCorners.get(i);
            elevations.put(corner, elevations.get(corner) / maxHeight * scale);
        }
    }

    private void redistributeElevationsInverse(List<Corner> landCorners, float scale) {

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
