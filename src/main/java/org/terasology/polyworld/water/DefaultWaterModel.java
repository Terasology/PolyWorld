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
import java.util.List;
import java.util.Map;

import org.terasology.polyworld.voronoi.Corner;
import org.terasology.polyworld.voronoi.Graph;
import org.terasology.polyworld.voronoi.Region;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * TODO Type description
 * @author Martin Steiger
 */
public class DefaultWaterModel implements WaterModel {

    private Map<Corner, Boolean> cornerWater = Maps.newHashMap();
    private Map<Corner, Boolean> cornerOcean = Maps.newHashMap();
    private Map<Corner, Boolean> cornerCoast = Maps.newHashMap();

    private Map<Region, Boolean> regionWater = Maps.newHashMap();
    private Map<Region, Boolean> regionOcean = Maps.newHashMap();
    private Map<Region, Boolean> regionCoast = Maps.newHashMap();

    private Graph graph;
    private List<Corner> landCorners;

    public DefaultWaterModel(Graph graph, WaterDistribution dist) {
        this.graph = graph;

        final double waterThreshold = .3;

        for (Corner c : graph.getCorners()) {
            setWater(c, dist.isWater(c.getLocation()));
        }

        Deque<Region> queue = new LinkedList<>();
        for (final Region region : graph.getRegions()) {
            int numWater = 0;
            Collection<Corner> corners = region.getCorners();
            for (final Corner c : corners) {
                if (c.isBorder()) {
                    region.setBorder(true);
                    setWater(region, true);
                    setOcean(region, true);
                    queue.add(region);
                }
                if (isWater(c)) {
                    numWater++;
                }
            }
            setWater(region, isOcean(region) || ((double) numWater / corners.size() >= waterThreshold));
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

        findLandCorners();

    }

    private void findLandCorners() {
        landCorners = Lists.newArrayList();
        for (Corner c : graph.getCorners()) {
            if (!isOcean(c) && !isCoast(c)) {
                landCorners.add(c);
            }
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
    public boolean isWater(Corner c)
    {
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
    public boolean isWater(Region c)
    {
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

    @Override
    public List<Corner> getLandCorners() {
        return landCorners;
    }

}
