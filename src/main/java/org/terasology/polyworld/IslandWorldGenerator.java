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

import org.joml.RoundingMode;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.terasology.core.world.generator.facetProviders.SeaLevelProvider;
import org.terasology.core.world.generator.facetProviders.SurfaceToDensityProvider;
import org.terasology.core.world.generator.rasterizers.FloraRasterizer;
import org.terasology.core.world.generator.rasterizers.TreeRasterizer;
import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.logic.spawner.FixedSpawner;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.generation.BaseFacetedWorldGenerator;
import org.terasology.engine.world.generation.Region;
import org.terasology.engine.world.generation.WorldBuilder;
import org.terasology.engine.world.generator.RegisterWorldGenerator;
import org.terasology.engine.world.generator.plugin.WorldGeneratorPluginLibrary;
import org.terasology.engine.world.viewer.picker.CirclePickerClosest;
import org.terasology.polyworld.biome.BiomeModel;
import org.terasology.polyworld.biome.WhittakerBiome;
import org.terasology.polyworld.biome.WhittakerBiomeModelFacet;
import org.terasology.polyworld.biome.WhittakerBiomeModelProvider;
import org.terasology.polyworld.biome.WhittakerBiomeProvider;
import org.terasology.polyworld.elevation.ElevationModelFacetProvider;
import org.terasology.polyworld.elevation.ElevationProvider;
import org.terasology.polyworld.elevation.FlatLakeProvider;
import org.terasology.polyworld.flora.FloraProvider;
import org.terasology.polyworld.flora.TreeProvider;
import org.terasology.polyworld.graph.Graph;
import org.terasology.polyworld.graph.GraphFacet;
import org.terasology.polyworld.graph.GraphFacetProvider;
import org.terasology.polyworld.moisture.MoistureModelFacetProvider;
import org.terasology.polyworld.raster.RiverRasterizer;
import org.terasology.polyworld.raster.WhittakerRasterizer;
import org.terasology.polyworld.rivers.RiverModelFacetProvider;
import org.terasology.polyworld.rp.WorldRegionFacetProvider;
import org.terasology.polyworld.water.WaterModelFacetProvider;

@RegisterWorldGenerator(id = "island", displayName = "Island World")
public class IslandWorldGenerator extends BaseFacetedWorldGenerator {

    public IslandWorldGenerator(SimpleUri uri) {
        super(uri);
    }

    @Override
    protected WorldBuilder createWorld() {
        int maxCacheSize = 20;
        return new WorldBuilder(CoreRegistry.get(WorldGeneratorPluginLibrary.class))
                .setSeaLevel(6)
                .addProvider(new SeaLevelProvider(6))
                .addProvider(new WorldRegionFacetProvider(maxCacheSize))
                .addProvider(new GraphFacetProvider(maxCacheSize))
                .addProvider(new WaterModelFacetProvider(maxCacheSize))
                .addProvider(new ElevationModelFacetProvider(maxCacheSize))
                .addProvider(new ElevationProvider())
                .addProvider(new SurfaceToDensityProvider())
                .addProvider(new RiverModelFacetProvider(maxCacheSize))
                .addProvider(new FlatLakeProvider())
                .addProvider(new MoistureModelFacetProvider(maxCacheSize))
                .addProvider(new WhittakerBiomeModelProvider(maxCacheSize))
                .addProvider(new WhittakerBiomeProvider())
                .addProvider(new TreeProvider())
                .addProvider(new FloraProvider())
                .addRasterizer(new WhittakerRasterizer())
                .addRasterizer(new RiverRasterizer())
                .addRasterizer(new TreeRasterizer())
                .addRasterizer(new FloraRasterizer());
    }

    @Override
    public Vector3fc getSpawnPosition(EntityRef entity) {
        LocationComponent loc = entity.getComponent(LocationComponent.class);
        Vector3f pos = loc.getWorldPosition(new Vector3f());

        int searchRadius = 16;
        Vector3i ext = new Vector3i(searchRadius, 1, searchRadius);
        Vector3i desiredPos = new Vector3i(new Vector3f(pos.x(), 1, pos.z()), RoundingMode.HALF_UP);

        // try and find somewhere in this region a spot to land
        BlockRegion spawnArea = new BlockRegion(desiredPos).expand(ext);
        Region worldRegion = getWorld().getWorldData(spawnArea);

        GraphFacet graphs = worldRegion.getFacet(GraphFacet.class);
        WhittakerBiomeModelFacet model = worldRegion.getFacet(WhittakerBiomeModelFacet.class);
        Vector2f pos2d = new Vector2f(pos.x(), pos.z());
        CirclePickerClosest<org.terasology.polyworld.graph.GraphRegion> picker = new CirclePickerClosest<>(pos2d);

        for (Graph g : graphs.getAllGraphs()) {
            BiomeModel biomeModel = model.get(g);
            for (org.terasology.polyworld.graph.GraphRegion r : g.getRegions()) {
                WhittakerBiome biome = biomeModel.getBiome(r);
                if (!biome.equals(WhittakerBiome.OCEAN) && !biome.equals(WhittakerBiome.LAKE)) {
                    picker.offer(r.getCenter(), r);
                }
            }
        }
        Vector2i target;
        if (picker.getClosest() != null) {
            Vector2fc hit = picker.getClosest().getCenter();
            target = new Vector2i(new Vector2f(hit.x(), hit.y()), RoundingMode.HALF_UP);
        } else {
            target = new Vector2i(desiredPos.x(), desiredPos.z());
        }

        FixedSpawner spawner = new FixedSpawner(target.x(), target.y());
        return spawner.getSpawnPosition(getWorld(), entity);
    }
}
