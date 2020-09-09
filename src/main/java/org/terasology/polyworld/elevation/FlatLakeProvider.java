// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.polyworld.elevation;

import com.google.common.collect.Sets;
import org.terasology.engine.world.generation.Facet;
import org.terasology.engine.world.generation.FacetProvider;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Requires;
import org.terasology.engine.world.generation.Updates;
import org.terasology.polyworld.graph.Corner;
import org.terasology.polyworld.graph.Graph;
import org.terasology.polyworld.graph.GraphFacet;
import org.terasology.polyworld.graph.Region;
import org.terasology.polyworld.water.WaterModel;
import org.terasology.polyworld.water.WaterModelFacet;

import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.function.Predicate;

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
            ElevationModel flattenedElevationModel = flattenLakes(g, elevationModel, waterModel);
            elevationModelFacet.set(g, flattenedElevationModel);
        }
    }

    private ElevationModel flattenLakes(Graph graph, ElevationModel elevationModel, WaterModel waterModel) {
        Set<Region> found = Sets.newHashSet();
        Predicate<Region> isLake = r -> waterModel.isWater(r) && !waterModel.isOcean(r);

        FlatLakeElevationModel flatModel = new FlatLakeElevationModel(elevationModel);
        for (Region r : graph.getRegions()) {
            if (isLake.test(r) && !found.contains(r)) {
                Collection<Region> lake = floodFill(r, isLake);
                flattenLake(flatModel, lake);
                found.addAll(lake);
            }
        }

        return flatModel;
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

    private void flattenLake(FlatLakeElevationModel elevationModel, Collection<Region> lake) {

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


