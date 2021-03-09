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

import com.google.common.collect.Sets;
import org.terasology.engine.world.generation.Facet;
import org.terasology.engine.world.generation.FacetProvider;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Requires;
import org.terasology.engine.world.generation.Updates;
import org.terasology.polyworld.graph.Corner;
import org.terasology.polyworld.graph.Graph;
import org.terasology.polyworld.graph.GraphFacet;
import org.terasology.polyworld.graph.GraphRegion;
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
        Set<GraphRegion> found = Sets.newHashSet();
        Predicate<GraphRegion> isLake = r -> waterModel.isWater(r) && !waterModel.isOcean(r);

        FlatLakeElevationModel flatModel = new FlatLakeElevationModel(elevationModel);
        for (GraphRegion r : graph.getRegions()) {
            if (isLake.test(r) && !found.contains(r)) {
                Collection<GraphRegion> lake = floodFill(r, isLake);
                flattenLake(flatModel, lake);
                found.addAll(lake);
            }
        }

        return flatModel;
    }

    private Collection<GraphRegion> floodFill(GraphRegion start, Predicate<GraphRegion> pred) {
        Collection<GraphRegion> lake = new HashSet<GraphRegion>();
        lake.add(start);

        Deque<GraphRegion> queue = new LinkedList<>();
        queue.add(start);

        while (!queue.isEmpty()) {
            GraphRegion next = queue.pop();
            for (GraphRegion n : next.getNeighbors()) {
                if (pred.test(n) && !lake.contains(n) && !queue.contains(n)) {
                    lake.add(n);
                    queue.add(n);
                }
            }
        }
        return lake;
    }

    private void flattenLake(FlatLakeElevationModel elevationModel, Collection<GraphRegion> lake) {

        float minHeight = Float.POSITIVE_INFINITY;
        for (GraphRegion r : lake) {
            float elevation = elevationModel.getElevation(r);
            if (minHeight >= elevation) {
                minHeight = elevation;
            }
        }

        // assign target height to all corners
        for (GraphRegion r : lake) {
            for (Corner c : r.getCorners()) {
                elevationModel.setElevation(c, minHeight);
            }
        }
    }

}


