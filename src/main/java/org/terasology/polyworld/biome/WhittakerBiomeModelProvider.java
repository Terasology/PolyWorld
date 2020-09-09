// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.polyworld.biome;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.world.generation.Facet;
import org.terasology.engine.world.generation.FacetProvider;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Produces;
import org.terasology.engine.world.generation.Requires;
import org.terasology.polyworld.elevation.ElevationModel;
import org.terasology.polyworld.elevation.ElevationModelFacet;
import org.terasology.polyworld.graph.Graph;
import org.terasology.polyworld.graph.GraphFacet;
import org.terasology.polyworld.moisture.MoistureModel;
import org.terasology.polyworld.moisture.MoistureModelFacet;
import org.terasology.polyworld.water.WaterModel;
import org.terasology.polyworld.water.WaterModelFacet;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * TODO Type description
 */
@Produces(WhittakerBiomeModelFacet.class)
@Requires({
        @Facet(GraphFacet.class),
        @Facet(ElevationModelFacet.class),
        @Facet(WaterModelFacet.class),
        @Facet(MoistureModelFacet.class)
})
public class WhittakerBiomeModelProvider implements FacetProvider {

    private static final Logger logger = LoggerFactory.getLogger(WhittakerBiomeModelProvider.class);

    private final Cache<Graph, BiomeModel> modelCache;

    /**
     * @param maxCacheSize maximum number of cached models
     */
    public WhittakerBiomeModelProvider(int maxCacheSize) {
        modelCache = CacheBuilder.newBuilder().maximumSize(maxCacheSize).build();
    }

    @Override
    public void setSeed(long seed) {
        // ignore
    }

    @Override
    public void process(GeneratingRegion region) {
        WhittakerBiomeModelFacet facet = new WhittakerBiomeModelFacet();

        GraphFacet graphFacet = region.getRegionFacet(GraphFacet.class);

        MoistureModelFacet moistureFacet = region.getRegionFacet(MoistureModelFacet.class);
        WaterModelFacet waterFacet = region.getRegionFacet(WaterModelFacet.class);
        ElevationModelFacet elevationFacet = region.getRegionFacet(ElevationModelFacet.class);

        for (Graph graph : graphFacet.getAllGraphs()) {
            WaterModel waterModel = waterFacet.get(graph);
            ElevationModel elevationModel = elevationFacet.get(graph);
            MoistureModel moistureModel = moistureFacet.get(graph);
            BiomeModel model = getOrCreate(graph, elevationModel, waterModel, moistureModel);
            facet.add(graph, model);
        }

        region.setRegionFacet(WhittakerBiomeModelFacet.class, facet);
    }

    private BiomeModel getOrCreate(Graph graph, final ElevationModel elevationModel, final WaterModel waterModel,
                                   final MoistureModel moistureModel) {
        try {
            return modelCache.get(graph, new Callable<BiomeModel>() {

                @Override
                public BiomeModel call() {
                    return new DefaultBiomeModel(elevationModel, waterModel, moistureModel);
                }
            });
        } catch (ExecutionException e) {
            logger.error("Could not create elevation model", e.getCause());
            return null;
        }
    }
}
