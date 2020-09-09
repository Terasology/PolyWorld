// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.polyworld.water;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.terasology.engine.world.generation.Facet;
import org.terasology.engine.world.generation.FacetProvider;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Produces;
import org.terasology.engine.world.generation.Requires;
import org.terasology.polyworld.distribution.Distribution;
import org.terasology.polyworld.distribution.PerlinDistribution;
import org.terasology.polyworld.distribution.RadialDistribution;
import org.terasology.polyworld.graph.Graph;
import org.terasology.polyworld.graph.GraphFacet;
import org.terasology.polyworld.rp.RegionType;
import org.terasology.polyworld.rp.WorldRegion;
import org.terasology.polyworld.rp.WorldRegionFacet;

/**
 * TODO Type description
 */
@Produces(WaterModelFacet.class)
@Requires({@Facet(WorldRegionFacet.class), @Facet(GraphFacet.class)})
public class WaterModelFacetProvider implements FacetProvider {

    private final LoadingCache<Graph, WaterModel> waterModelCache;
    private long seed;
    private final CacheLoader<Graph, WaterModel> loader = new CacheLoader<Graph, WaterModel>() {

        @Override
        public WaterModel load(Graph key) throws Exception {
            long graphSeed = seed ^ key.getBounds().hashCode();

            Distribution waterDist = (graphSeed % 2 == 0)  // a very primitive noise function
                    ? new PerlinDistribution(graphSeed)
                    : new RadialDistribution(graphSeed);

            return new DefaultWaterModel(key, waterDist);
        }

    };

    /**
     * @param maxCacheSize maximum number of cached models
     */
    public WaterModelFacetProvider(int maxCacheSize) {
        waterModelCache = CacheBuilder.newBuilder().maximumSize(maxCacheSize).build(loader);
    }

    @Override
    public void setSeed(long seed) {
        if (this.seed != seed) {
            this.seed = seed;
            waterModelCache.invalidateAll();
        }
    }

    @Override
    public void process(GeneratingRegion region) {
        GraphFacet graphFacet = region.getRegionFacet(GraphFacet.class);
        WorldRegionFacet regionFacet = region.getRegionFacet(WorldRegionFacet.class);
        WaterModelFacet waterFacet = new WaterModelFacet();

        for (WorldRegion wr : regionFacet.getRegions()) {
            // TODO: move to loading cache
            Graph g = graphFacet.getGraph(wr);
            if (wr.getType() == RegionType.OCEAN) {
                waterFacet.add(g, new PureOceanWaterModel());
            } else {
                waterFacet.add(g, waterModelCache.getUnchecked(g));
            }
        }

        region.setRegionFacet(WaterModelFacet.class, waterFacet);
    }
}
