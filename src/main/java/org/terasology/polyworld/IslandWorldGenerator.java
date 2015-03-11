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

import org.terasology.core.world.generator.facetProviders.SeaLevelProvider;
import org.terasology.core.world.generator.rasterizers.FloraRasterizer;
import org.terasology.core.world.generator.rasterizers.TreeRasterizer;
import org.terasology.engine.SimpleUri;
import org.terasology.polyworld.biome.WhittakerBiomeModelProvider;
import org.terasology.polyworld.biome.WhittakerBiomeProvider;
import org.terasology.polyworld.elevation.ElevationModelFacetProvider;
import org.terasology.polyworld.elevation.ElevationProvider;
import org.terasology.polyworld.flora.FloraProvider;
import org.terasology.polyworld.flora.TreeProvider;
import org.terasology.polyworld.moisture.MoistureModelFacetProvider;
import org.terasology.polyworld.raster.RiverRasterizer;
import org.terasology.polyworld.raster.WhittakerRasterizer;
import org.terasology.polyworld.rivers.RiverModelFacetProvider;
import org.terasology.polyworld.rp.WorldRegionFacetProvider;
import org.terasology.polyworld.voronoi.GraphFacetProvider;
import org.terasology.polyworld.water.WaterModelFacetProvider;
import org.terasology.world.generation.BaseFacetedWorldGenerator;
import org.terasology.world.generation.WorldBuilder;
import org.terasology.world.generator.RegisterWorldGenerator;

@RegisterWorldGenerator(id = "island", displayName = "Island")
public class IslandWorldGenerator extends BaseFacetedWorldGenerator {

    public IslandWorldGenerator(SimpleUri uri) {
        super(uri);
    }

    @Override
    protected WorldBuilder createWorld(final long seed) {
        return new WorldBuilder(seed)
                .setSeaLevel(6)
                .addProvider(new SeaLevelProvider(6))
                .addProvider(new WorldRegionFacetProvider())
                .addProvider(new GraphFacetProvider())
                .addProvider(new WaterModelFacetProvider())
                .addProvider(new ElevationModelFacetProvider())
                .addProvider(new ElevationProvider())
                .addProvider(new RiverModelFacetProvider())
                .addProvider(new MoistureModelFacetProvider())
                .addProvider(new WhittakerBiomeModelProvider())
                .addProvider(new WhittakerBiomeProvider())
                .addProvider(new TreeProvider())
                .addProvider(new FloraProvider())
                .addRasterizer(new WhittakerRasterizer())
                .addRasterizer(new RiverRasterizer())
                .addRasterizer(new TreeRasterizer())
                .addRasterizer(new FloraRasterizer())
                .addPlugins();
    }
}
