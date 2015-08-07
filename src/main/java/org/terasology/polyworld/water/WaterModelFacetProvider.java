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

import org.terasology.polyworld.distribution.Distribution;
import org.terasology.polyworld.distribution.PerlinDistribution;
import org.terasology.polyworld.distribution.RadialDistribution;
import org.terasology.polyworld.graph.Graph;
import org.terasology.polyworld.graph.GraphFacet;
import org.terasology.polyworld.rp.WorldRegionFacet;
import org.terasology.polyworld.rp.RegionType;
import org.terasology.polyworld.rp.WorldRegion;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.Requires;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * TODO Type description
 */
@Produces(WaterModelFacet.class)
@Requires({@Facet(WorldRegionFacet.class), @Facet(GraphFacet.class)})
public class WaterModelFacetProvider implements FacetProvider {

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

    private final LoadingCache<Graph, WaterModel> waterModelCache;

    private long seed;

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
