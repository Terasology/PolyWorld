/*
 * Copyright 2015 MovingBlocks
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

package org.terasology.polyworld.rp;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import org.joml.Vector3i;
import org.terasology.entitySystem.Component;
import org.terasology.nui.properties.Range;
import org.terasology.utilities.procedural.Noise;
import org.terasology.utilities.procedural.WhiteNoise;
import org.terasology.world.block.BlockArea;
import org.terasology.world.block.BlockAreac;
import org.terasology.world.block.BlockRegion;
import org.terasology.world.chunks.Chunks;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.ConfigurableFacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * TODO Type description
 */
@Produces(WorldRegionFacet.class)
public class WorldRegionFacetProvider implements ConfigurableFacetProvider {

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(false);

    public static final int SECTOR_SIZE = 1024;
    public static final int SECTOR_POWER = Integer.numberOfTrailingZeros(SECTOR_SIZE);

    private RegionProvider regionProvider;
    private Configuration configuration = new Configuration();

    private Noise islandRatioNoise;

    private final CacheLoader<BlockAreac, Collection<WorldRegion>> loader = new CacheLoader<BlockAreac, Collection<WorldRegion>>() {

        @Override
        public Collection<WorldRegion> load(BlockAreac fullArea) throws Exception {
            float maxArea = 0.75f * SECTOR_SIZE * SECTOR_SIZE;

            List<WorldRegion> result = Lists.newArrayList();
            for (BlockAreac area : regionProvider.getSectorRegions(fullArea)) {
                float rnd = islandRatioNoise.noise(area.minX(), area.minY());
                float scale = area.area() / maxArea;

                WorldRegion wr = new WorldRegion(area);
                wr.setHeightScaleFactor(scale);
                if (rnd < configuration.islandDensity) {
                    wr.setType(RegionType.ISLAND);
                } else {
                    wr.setType(RegionType.OCEAN);
                }
                result.add(wr);
            }
            return result;
        }
    };

    private final LoadingCache<BlockAreac, Collection<WorldRegion>> cache;

    private long seed;

    /**
     * @param maxCacheSize maximum number of cached regions
     */
    public WorldRegionFacetProvider(int maxCacheSize) {
        cache = CacheBuilder.newBuilder().maximumSize(maxCacheSize).build(loader);
    }

    public WorldRegionFacetProvider(int maxCacheSize, float islandDensity) {
        cache = CacheBuilder.newBuilder().maximumSize(maxCacheSize).build(loader);
        configuration.islandDensity = islandDensity;
    }

    @Override
    public void setSeed(long seed) {
        this.seed = seed;
        regionProvider = new SubdivRegionProvider(seed, configuration.minSize, 0.95f);
        islandRatioNoise = new WhiteNoise(seed);
    }

    @Override
    public void process(GeneratingRegion region) {
        Border3D border = region.getBorderForFacet(WorldRegionFacet.class);
        WorldRegionFacet facet = new WorldRegionFacet(region.getRegion(), border);

        BlockRegion worldRegion = facet.getWorldRegion();

        Vector3i min = worldRegion.getMin(new Vector3i());
        Vector3i max = worldRegion.getMax(new Vector3i());

        BlockAreac secArea = new BlockArea(
            Chunks.toChunkPos(min.x, SECTOR_POWER), Chunks.toChunkPos(min.z, SECTOR_POWER),
            Chunks.toChunkPos(max.x, SECTOR_POWER), Chunks.toChunkPos(max.z, SECTOR_POWER));

        BlockAreac target = new BlockArea(min.x, min.z, max.x, max.z);

        for (int sx = secArea.minX(); sx <= secArea.maxX(); sx++) {
            for (int sz = secArea.minY(); sz <= secArea.maxY(); sz++) {
                BlockAreac fullArea = new BlockArea(sx * SECTOR_SIZE, sz * SECTOR_SIZE).setSize(SECTOR_SIZE, SECTOR_SIZE);

                Collection<WorldRegion> collection = cache.getIfPresent(fullArea);
                if (collection == null) {
                    try {
                        lock.readLock().lock();
                        collection = cache.getUnchecked(fullArea);
                    } finally {
                        lock.readLock().unlock();
                    }
                }
                for (WorldRegion wr : collection) {
                    if (wr.getArea().intersectsBlockArea(target)) {
                        facet.addRegion(wr);
                    }
                }
            }
        }

        region.setRegionFacet(WorldRegionFacet.class, facet);
    }

    @Override
    public String getConfigurationName() {
        return "Regions";
    }

    @Override
    public Component getConfiguration() {
        return configuration;
    }

    @Override
    public void setConfiguration(Component configuration) {
        try {
            lock.writeLock().lock();
            this.configuration = (Configuration) configuration;
            setSeed(seed); // trigger updating fields with new configuration
            cache.invalidateAll();
        } finally {
            lock.writeLock().unlock();
        }
    }

    private static class Configuration implements Component {

        @Range(min = 50, max = 500f, increment = 10f, precision = 0, description = "Minimum size of a region")
        private int minSize = 100;

        @Range(min = 0.1f, max = 1.0f, increment = 0.1f, precision = 1, description = "Define the ratio islands/water")
        private float islandDensity = 0.7f;
    }
}
