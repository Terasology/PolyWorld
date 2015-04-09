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

package org.terasology.polyworld.rivers;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.polyworld.elevation.ElevationModel;
import org.terasology.polyworld.elevation.ElevationModelFacet;
import org.terasology.polyworld.voronoi.Graph;
import org.terasology.polyworld.voronoi.GraphFacet;
import org.terasology.polyworld.water.WaterModel;
import org.terasology.polyworld.water.WaterModelFacet;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.Requires;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * TODO Type description
 * @author Martin Steiger
 */
@Produces(RiverModelFacet.class)
@Requires({
        @Facet(GraphFacet.class),
        @Facet(WaterModelFacet.class),
        @Facet(ElevationModelFacet.class)
        })
public class RiverModelFacetProvider implements FacetProvider {

    private static final Logger logger = LoggerFactory.getLogger(RiverModelFacetProvider.class);

    private final Cache<Graph, RiverModel> modelCache;

    private long seed;

    /**
     * @param maxCacheSize maximum number of cached models
     */
    public RiverModelFacetProvider(int maxCacheSize) {
        modelCache = CacheBuilder.newBuilder().maximumSize(maxCacheSize).build();
    }

    @Override
    public void setSeed(long seed) {
        if (this.seed != seed) {
            this.seed = seed;
            modelCache.invalidateAll();
        }
    }

    @Override
    public void process(GeneratingRegion region) {
        GraphFacet graphFacet = region.getRegionFacet(GraphFacet.class);
        RiverModelFacet riverFacet = new RiverModelFacet();
        WaterModelFacet waterFacet = region.getRegionFacet(WaterModelFacet.class);
        ElevationModelFacet elevationModelFacet = region.getRegionFacet(ElevationModelFacet.class);

        for (Graph graph : graphFacet.getAllGraphs()) {
            WaterModel waterModel = waterFacet.get(graph);
            ElevationModel elevationModel = elevationModelFacet.get(graph);
            RiverModel model = getOrCreate(graph, elevationModel, waterModel);
            riverFacet.add(graph, model);
        }

        region.setRegionFacet(RiverModelFacet.class, riverFacet);
    }

    private RiverModel getOrCreate(final Graph graph, final ElevationModel elevationModel, final WaterModel waterModel) {
        try {
            return modelCache.get(graph, new Callable<RiverModel>() {

                @Override
                public RiverModel call() {
                    return new DefaultRiverModel(graph, elevationModel, waterModel);
                }
            });
        } catch (ExecutionException e) {
            logger.error("Could not create elevation model", e.getCause());
            return null;
        }
    }

}
