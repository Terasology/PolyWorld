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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.polyworld.graph.Graph;
import org.terasology.polyworld.graph.GraphFacet;
import org.terasology.polyworld.rp.WorldRegion;
import org.terasology.polyworld.rp.WorldRegionFacet;
import org.terasology.polyworld.water.WaterModel;
import org.terasology.polyworld.water.WaterModelFacet;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.Requires;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * TODO Type description
 */
@Produces(ElevationModelFacet.class)
@Requires({
        @Facet(WorldRegionFacet.class),
        @Facet(WaterModelFacet.class),
        @Facet(GraphFacet.class)
        })
public class ElevationModelFacetProvider implements FacetProvider {

    private static final Logger logger = LoggerFactory.getLogger(ElevationModelFacetProvider.class);

    private final Cache<Graph, ElevationModel> elevationCache;

    /**
     * @param maxCacheSize maximum number of cached models
     */
    public ElevationModelFacetProvider(int maxCacheSize) {
        elevationCache = CacheBuilder.newBuilder().maximumSize(maxCacheSize).build();
    }

    @Override
    public void setSeed(long seed) {
        // ignore
    }

    @Override
    public void process(GeneratingRegion region) {
        ElevationModelFacet elevationFacet = new ElevationModelFacet();

        WorldRegionFacet regionFacet = region.getRegionFacet(WorldRegionFacet.class);
        GraphFacet graphFacet = region.getRegionFacet(GraphFacet.class);
        WaterModelFacet waterFacet = region.getRegionFacet(WaterModelFacet.class);

        for (WorldRegion wr : regionFacet.getRegions()) {
            Graph graph = graphFacet.getGraph(wr);
            WaterModel waterModel = waterFacet.get(graph);
            float heightScale = wr.getHeightScaleFactor();
            ElevationModel elevationModel = getOrCreate(graph, waterModel, heightScale);
            elevationFacet.set(graph, elevationModel);
        }

        region.setRegionFacet(ElevationModelFacet.class, elevationFacet);
    }

    private ElevationModel getOrCreate(final Graph graph, final WaterModel waterModel, final float scale) {
        try {
            return elevationCache.get(graph, new Callable<ElevationModel>() {

                @Override
                public ElevationModel call() {
                    return new DefaultElevationModel(graph, waterModel, scale);
                }
            });
        } catch (ExecutionException e) {
            logger.error("Could not create elevation model", e.getCause());
            return null;
        }
    }
}
