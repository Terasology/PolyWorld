/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.polyworld;

import org.terasology.commonworld.Sector;
import org.terasology.core.world.generator.facetProviders.SeaLevelProvider;
import org.terasology.engine.SimpleUri;
import org.terasology.polyworld.biome.WhittakerBiomeProvider;
import org.terasology.polyworld.elevation.ElevationProvider;
import org.terasology.polyworld.elevation.IslandLookup;
import org.terasology.world.generation.BaseFacetedWorldGenerator;
import org.terasology.world.generation.WorldBuilder;
import org.terasology.world.generator.RegisterWorldGenerator;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@RegisterWorldGenerator(id = "island", displayName = "Island")
public class IslandWorldGenerator extends BaseFacetedWorldGenerator {

    public IslandWorldGenerator(SimpleUri uri) {
        super(uri);
    }

    @Override
    protected WorldBuilder createWorld(final long seed) {
        LoadingCache<Sector, IslandLookup> islandCache = CacheBuilder.newBuilder().build(new CacheLoader<Sector, IslandLookup>() {

            @Override
            public IslandLookup load(Sector sector) throws Exception {
                return new IslandLookup(sector, seed);
            }
        });

        return new WorldBuilder(seed)
                .addProvider(new SeaLevelProvider(10))
                .addProvider(new ElevationProvider(islandCache))
                .addProvider(new WhittakerBiomeProvider(islandCache));
//                .addRasterizer(new FloraRasterizer())
//                .addRasterizer(new TreeRasterizer())
//                .addRasterizer(new SolidRasterizer())
//                .addPlugins()
    }
}
