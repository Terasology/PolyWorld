/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.polyworld.elevation;

import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.function.Predicate;

import org.terasology.polyworld.graph.Corner;
import org.terasology.polyworld.graph.Graph;
import org.terasology.polyworld.graph.GraphFacet;
import org.terasology.polyworld.graph.Region;
import org.terasology.polyworld.water.WaterModel;
import org.terasology.polyworld.water.WaterModelFacet;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.Updates;

import com.google.common.collect.Sets;

/**
 * Makes sure that all corners of lake polygons have that same height.
 */
@Updates(@Facet(ElevationModelFacet.class))
@Requires({
     @Facet(GraphFacet.class),
     @Facet(WaterModelFacet.class)
})
public class FlatLakeProvider implements FacetProvider {

    @Override
    public void setSeed(long seed) {
        // ignore
    }

    @Override
    public void process(GeneratingRegion region) {
        ElevationModelFacet elevationModelFacet = region.getRegionFacet(ElevationModelFacet.class);

        GraphFacet graphFacet = region.getRegionFacet(GraphFacet.class);
        WaterModelFacet waterModelFacet = region.getRegionFacet(WaterModelFacet.class);
        for (Graph g : graphFacet.getAllGraphs()) {
            ElevationModel elevationModel = elevationModelFacet.get(g);
            WaterModel waterModel = waterModelFacet.get(g);
            flattenLakes(g, elevationModel, waterModel);
        }
    }

    private void flattenLakes(Graph graph, ElevationModel elevationModel, WaterModel waterModel) {
        Set<Region> found = Sets.newHashSet();
        Predicate<Region> isLake = r -> waterModel.isWater(r) && !waterModel.isOcean(r);

        for (Region r : graph.getRegions()) {
            if (isLake.test(r) && !found.contains(r)) {
                Collection<Region> lake = floodFill(r, isLake);
                flattenLake(elevationModel, lake);
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

    private void flattenLake(ElevationModel elevationModel, Collection<Region> lake) {

        float minHeight = Float.POSITIVE_INFINITY;
        for (Region r : lake) {
            float elevation = elevationModel.getElevation(r);
            if (minHeight >= elevation) {
                minHeight = elevation;
            }
        }

        // assign target height to all corners
        for (Region r : lake) {
            for (Corner c : r.getCorners()) {
                elevationModel.setElevation(c, minHeight);
            }
        }
    }

}


