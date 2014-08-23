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

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.terasology.polyworld.voronoi.Corner;
import org.terasology.polyworld.voronoi.Region;
import org.terasology.polyworld.voronoi.VoronoiGraph;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * TODO Type description
 * @author Martin Steiger
 */
public class DefaultWaterModel implements WaterModel {

    private Map<Corner, Boolean> isWater = Maps.newHashMap();
    private Map<Corner, Boolean> isOcean = Maps.newHashMap();
    private Map<Corner, Boolean> isCoast = Maps.newHashMap();

    private VoronoiGraph graph;
    private List<Corner> landCorners;

    public DefaultWaterModel(VoronoiGraph graph, WaterDistribution dist) {
        this.graph = graph;

        final double waterThreshold = .3;

        for (Corner c : graph.getCorners()) {
            setWater(c, dist.isWater(c.getLocation()));
        }

        Deque<Region> queue = new LinkedList<>();
        for (final Region region : graph.getRegions()) {
            int numWater = 0;
            for (final Corner c : region.getCorners()) {
                if (c.isBorder()) {
                    region.setBorder(true);
                    region.setWater(true);
                    region.setOcean(true);
                    queue.add(region);
                }
                if (isWater(c)) {
                    numWater++;
                }
            }
            region.setWater(region.isOcean() || ((double) numWater / region.getCorners().size() >= waterThreshold));
        }
        while (!queue.isEmpty()) {
            final Region region = queue.pop();
            for (final Region n : region.getNeighbors()) {
                if (n.isWater() && !n.isOcean()) {
                    n.setOcean(true);
                    queue.add(n);
                }
            }
        }
        for (Region region : graph.getRegions()) {
            boolean oceanNeighbor = false;
            boolean landNeighbor = false;
            for (Region n : region.getNeighbors()) {
                oceanNeighbor |= n.isOcean();
                landNeighbor |= !n.isWater();
            }
            region.setCoast(oceanNeighbor && landNeighbor);
        }

        for (Corner c : graph.getCorners()) {
            int numOcean = 0;
            int numLand = 0;
            for (Region region : c.getTouches()) {
                numOcean += region.isOcean() ? 1 : 0;
                numLand += !region.isWater() ? 1 : 0;
            }
            setOcean(c, numOcean == c.getTouches().size());
            setCoast(c, numOcean > 0 && numLand > 0);
            setWater(c, c.isBorder() || ((numLand != c.getTouches().size()) && !isCoast(c)));
        }

        findLandCorners();

    }

    /**
     *
     */
    private void findLandCorners() {
        landCorners = Lists.newArrayList();
        for (Corner c : graph.getCorners()) {
            if (!isOcean(c) && !isCoast(c)) {
                landCorners.add(c);
            }
        }
    }

    private void setCoast(Corner c, boolean coast) {
         isCoast.put(c, Boolean.valueOf(coast));
    }

    private void setOcean(Corner c, boolean ocean) {
        isOcean.put(c, Boolean.valueOf(ocean));
    }

    private void setWater(Corner c, boolean water) {
        isWater.put(c, Boolean.valueOf(water));
    }

    @Override
    public boolean isWater(Corner c)
    {
        return isWater.get(c);
    }

    @Override
    public boolean isCoast(Corner c) {
        return isCoast.get(c);
    }

    @Override
    public boolean isOcean(Corner c) {
        return isOcean.get(c);
    }

    @Override
    public List<Corner> getLandCorners() {
        return landCorners;
    }

}
