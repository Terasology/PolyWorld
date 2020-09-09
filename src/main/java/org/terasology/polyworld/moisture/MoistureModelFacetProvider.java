// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.polyworld.moisture;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.world.generation.Facet;
import org.terasology.engine.world.generation.FacetProvider;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Produces;
import org.terasology.engine.world.generation.Requires;
import org.terasology.polyworld.graph.Graph;
import org.terasology.polyworld.graph.GraphFacet;
import org.terasology.polyworld.rivers.RiverModel;
import org.terasology.polyworld.rivers.RiverModelFacet;
import org.terasology.polyworld.water.WaterModel;
import org.terasology.polyworld.water.WaterModelFacet;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * TODO Type description
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
