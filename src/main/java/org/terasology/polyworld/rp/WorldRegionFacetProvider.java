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

import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.terasology.commonworld.Sector;
import org.terasology.commonworld.Sectors;
import org.terasology.entitySystem.Component;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.Region3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.utilities.procedural.Noise;
import org.terasology.utilities.procedural.WhiteNoise;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.ConfigurableFacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;

/**
 * TODO Type description
 */
@Produces(WorldRegionFacet.class)
public class WorldRegionFacetProvider implements ConfigurableFacetProvider {

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(false);

    private RegionProvider regionProvider;
    private Configuration configuration = new Configuration();

    private Noise islandRatioNoise;

    private final CacheLoader<Rect2i, Collection<WorldRegion>> loader = new CacheLoader<Rect2i, Collection<WorldRegion>>() {

        @Override
        public Collection<WorldRegion> load(Rect2i fullArea) throws Exception {
            float maxArea = 0.75f * Sector.SIZE_X * Sector.SIZE_Z;

            List<WorldRegion> result = Lists.newArrayList();
            for (Rect2i area : regionProvider.getSectorRegions(fullArea)) {
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

    private final LoadingCache<Rect2i, Collection<WorldRegion>> cache;

    private long seed;

    /**
     * @param maxCacheSize maximum number of cached regions
     */
    public WorldRegionFacetProvider(int maxCacheSize) {
        cache = CacheBuilder.newBuilder().maximumSize(maxCacheSize).build(loader);
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

        Region3i worldRegion = facet.getWorldRegion();

        Vector3i min = worldRegion.min();
        Vector3i max = worldRegion.max();
        Sector minSec = Sectors.getSectorForBlock(min.x, min.z);
        Sector maxSec = Sectors.getSectorForBlock(max.x, max.z);

        Rect2i target = Rect2i.createFromMinAndMax(min.x, min.z, max.x, max.z);

        for (int sx = minSec.getCoords().getX(); sx <= maxSec.getCoords().getX(); sx++) {
            for (int sz = minSec.getCoords().getY(); sz <= maxSec.getCoords().getY(); sz++) {
                Sector sector = Sectors.getSector(sx, sz);

                Rect2i sb = sector.getWorldBounds();
                Rect2i fullArea = Rect2i.createFromMinAndSize(sb.minX(), sb.minY(), sb.width(), sb.height());

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
                    if (wr.getArea().overlaps(target)) {
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
