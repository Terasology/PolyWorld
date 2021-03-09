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

import com.google.common.collect.Maps;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.terasology.engine.world.block.BlockAreac;
import org.terasology.polyworld.distribution.Distribution;
import org.terasology.polyworld.graph.Corner;
import org.terasology.polyworld.graph.Graph;
import org.terasology.polyworld.graph.GraphRegion;

import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;

/**
 * Uses a {@link Distribution} to define how water is distributed in the graph.
 * The result is normalized.
 */
public class DefaultWaterModel implements WaterModel {

    private final Map<Corner, Boolean> cornerWater = Maps.newHashMap();
    private final Map<Corner, Boolean> cornerOcean = Maps.newHashMap();
    private final Map<Corner, Boolean> cornerCoast = Maps.newHashMap();

    private final Map<GraphRegion, Boolean> regionWater = Maps.newHashMap();
    private final Map<GraphRegion, Boolean> regionOcean = Maps.newHashMap();
    private final Map<GraphRegion, Boolean> regionCoast = Maps.newHashMap();

    /**
     * @param graph the graph to use
     * @param dist the distribution of water
     */
    public DefaultWaterModel(Graph graph, Distribution dist) {

        final float waterThreshold = .3f;

        for (Corner c : graph.getCorners()) {
            BlockAreac bounds = graph.getBounds();
            Vector2fc p2 = c.getLocation();
            float nx = (p2.x() - bounds.minX()) / bounds.getSizeX();
            float ny = (p2.y() - bounds.minY()) / bounds.getSizeY();

            setWater(c, dist.isInside(new Vector2f(nx, ny)));
        }

        Deque<GraphRegion> queue = new LinkedList<>();
        for (final GraphRegion region : graph.getRegions()) {
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
            final GraphRegion region = queue.pop();
            for (final GraphRegion n : region.getNeighbors()) {
                if (isWater(n) && !isOcean(n)) {
                    setOcean(n, true);
                    queue.add(n);
                }
            }
        }
        for (GraphRegion region : graph.getRegions()) {
            boolean oceanNeighbor = false;
            boolean landNeighbor = false;
            for (GraphRegion n : region.getNeighbors()) {
                oceanNeighbor |= isOcean(n);
                landNeighbor |= !isWater(n);
            }
            setCoast(region, oceanNeighbor && landNeighbor);
        }

        for (Corner c : graph.getCorners()) {
            int numOcean = 0;
            int numLand = 0;
            for (GraphRegion region : c.getTouches()) {
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

    private void setCoast(GraphRegion c, boolean coast) {
        regionCoast.put(c, Boolean.valueOf(coast));
    }

    private void setOcean(GraphRegion c, boolean ocean) {
        regionOcean.put(c, Boolean.valueOf(ocean));
    }

    private void setWater(GraphRegion c, boolean water) {
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
    public boolean isWater(GraphRegion c) {
        Boolean val = regionWater.get(c);
        return val == null ? false : val.booleanValue();
    }

    @Override
    public boolean isCoast(GraphRegion c) {
        Boolean val = regionCoast.get(c);
        return val == null ? false : val.booleanValue();
    }

    @Override
    public boolean isOcean(GraphRegion c) {
        Boolean val = regionOcean.get(c);
        return val == null ? false : val.booleanValue();
    }
}
