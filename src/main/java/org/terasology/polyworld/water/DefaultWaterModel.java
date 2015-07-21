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

package org.terasology.polyworld.water;

import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;

import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.BaseVector2f;
import org.terasology.math.geom.Vector2f;
import org.terasology.polyworld.distribution.Distribution;
import org.terasology.polyworld.graph.Corner;
import org.terasology.polyworld.graph.Graph;
import org.terasology.polyworld.graph.Region;

import com.google.common.collect.Maps;

/**
 * Uses a {@link Distribution} to define how water is distributed in the graph.
 * The result is normalized.
 * @author Martin Steiger
 */
public class DefaultWaterModel implements WaterModel {

    private final Map<Corner, Boolean> cornerWater = Maps.newHashMap();
    private final Map<Corner, Boolean> cornerOcean = Maps.newHashMap();
    private final Map<Corner, Boolean> cornerCoast = Maps.newHashMap();

    private final Map<Region, Boolean> regionWater = Maps.newHashMap();
    private final Map<Region, Boolean> regionOcean = Maps.newHashMap();
    private final Map<Region, Boolean> regionCoast = Maps.newHashMap();

    /**
     * @param graph the graph to use
     * @param dist the distribution of water
     */
    public DefaultWaterModel(Graph graph, Distribution dist) {

        final float waterThreshold = .3f;

        for (Corner c : graph.getCorners()) {
            Rect2i bounds = graph.getBounds();
            BaseVector2f p2 = c.getLocation();
            float nx = (p2.getX() - bounds.minX()) / bounds.width();
            float ny = (p2.getY() - bounds.minY()) / bounds.height();

            setWater(c, dist.isInside(new Vector2f(nx, ny)));
        }

        Deque<Region> queue = new LinkedList<>();
        for (final Region region : graph.getRegions()) {
            int numWater = 0;
            Collection<Corner> corners = region.getCorners();
            for (final Corner c : corners) {
                if (c.isBorder()) {
                    setWater(region, true);
                    setOcean(region, true);
                    queue.add(region);
                }
                if (isWater(c)) {
                    numWater++;
                }
            }
            setWater(region, isOcean(region) || ((float) numWater / corners.size() >= waterThreshold));
        }
        while (!queue.isEmpty()) {
            final Region region = queue.pop();
            for (final Region n : region.getNeighbors()) {
                if (isWater(n) && !isOcean(n)) {
                    setOcean(n, true);
                    queue.add(n);
                }
            }
        }
        for (Region region : graph.getRegions()) {
            boolean oceanNeighbor = false;
            boolean landNeighbor = false;
            for (Region n : region.getNeighbors()) {
                oceanNeighbor |= isOcean(n);
                landNeighbor |= !isWater(n);
            }
            setCoast(region, oceanNeighbor && landNeighbor);
        }

        for (Corner c : graph.getCorners()) {
            int numOcean = 0;
            int numLand = 0;
            for (Region region : c.getTouches()) {
                numOcean += isOcean(region) ? 1 : 0;
                numLand += !isWater(region) ? 1 : 0;
            }
            setOcean(c, numOcean == c.getTouches().size());
            setCoast(c, numOcean > 0 && numLand > 0);
            setWater(c, c.isBorder() || ((numLand != c.getTouches().size()) && !isCoast(c)));
        }
    }

    private void setCoast(Corner c, boolean coast) {
         cornerCoast.put(c, Boolean.valueOf(coast));
    }

    private void setOcean(Corner c, boolean ocean) {
        cornerOcean.put(c, Boolean.valueOf(ocean));
    }

    private void setWater(Corner c, boolean water) {
        cornerWater.put(c, Boolean.valueOf(water));
    }

    private void setCoast(Region c, boolean coast) {
        regionCoast.put(c, Boolean.valueOf(coast));
    }

    private void setOcean(Region c, boolean ocean) {
        regionOcean.put(c, Boolean.valueOf(ocean));
    }

    private void setWater(Region c, boolean water) {
        regionWater.put(c, Boolean.valueOf(water));
    }

    @Override
    public boolean isWater(Corner c) {
        return cornerWater.get(c);
    }

    @Override
    public boolean isCoast(Corner c) {
        return cornerCoast.get(c);
    }

    @Override
    public boolean isOcean(Corner c) {
        return cornerOcean.get(c);
    }

    @Override
    public boolean isWater(Region c) {
        Boolean val = regionWater.get(c);
        return val == null ? false : val.booleanValue();
    }

    @Override
    public boolean isCoast(Region c) {
        Boolean val = regionCoast.get(c);
        return val == null ? false : val.booleanValue();
    }

    @Override
    public boolean isOcean(Region c) {
        Boolean val = regionOcean.get(c);
        return val == null ? false : val.booleanValue();
    }
}
