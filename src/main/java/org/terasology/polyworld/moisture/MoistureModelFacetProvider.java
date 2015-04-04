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

package org.terasology.polyworld.moisture;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.polyworld.graph.Graph;
import org.terasology.polyworld.graph.GraphFacet;
import org.terasology.polyworld.rivers.RiverModel;
import org.terasology.polyworld.rivers.RiverModelFacet;
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
@Produces(MoistureModelFacet.class)
@Requires({
        @Facet(GraphFacet.class),
        @Facet(WaterModelFacet.class),
        @Facet(RiverModelFacet.class)
        })
public class MoistureModelFacetProvider implements FacetProvider {

    private static final Logger logger = LoggerFactory.getLogger(MoistureModelFacetProvider.class);

    private final Cache<Graph, MoistureModel> modelCache;

    private long seed;

    /**
     * @param maxCacheSize maximum number of cached models
     */
    public MoistureModelFacetProvider(int maxCacheSize) {
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
        MoistureModelFacet moistureFacet = new MoistureModelFacet();
        WaterModelFacet waterFacet = region.getRegionFacet(WaterModelFacet.class);
        RiverModelFacet riverFacet = region.getRegionFacet(RiverModelFacet.class);

        for (Graph graph : graphFacet.getAllGraphs()) {
            WaterModel waterModel = waterFacet.get(graph);
            RiverModel riverModel = riverFacet.get(graph);
            MoistureModel model = getOrCreate(graph, riverModel, waterModel);
            moistureFacet.add(graph, model);
        }

        region.setRegionFacet(MoistureModelFacet.class, moistureFacet);
    }

    private MoistureModel getOrCreate(final Graph graph, final RiverModel riverModel, final WaterModel waterModel) {
        try {
            return modelCache.get(graph, new Callable<MoistureModel>() {

                @Override
                public MoistureModel call() {
                    return new DefaultMoistureModel(graph, riverModel, waterModel);
                }
            });
        } catch (ExecutionException e) {
            logger.error("Could not create moisture model", e.getCause());
            return null;
        }
    }

}
