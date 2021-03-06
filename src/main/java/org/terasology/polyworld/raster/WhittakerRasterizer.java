/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.polyworld.raster;

import org.joml.Vector3i;
import org.terasology.biomesAPI.BiomeRegistry;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.generation.Region;
import org.terasology.engine.world.generation.WorldRasterizer;
import org.terasology.engine.world.generation.facets.DensityFacet;
import org.terasology.engine.world.generation.facets.SeaLevelFacet;
import org.terasology.engine.world.generation.facets.SurfacesFacet;
import org.terasology.polyworld.biome.WhittakerBiome;
import org.terasology.polyworld.biome.WhittakerBiomeFacet;

/**
 */
public class WhittakerRasterizer implements WorldRasterizer {

    private Block water;
    private Block ice;
    private Block stone;
    private Block sand;
    private Block grass;
    private Block snow;
    private Block dirt;
    private BiomeRegistry biomeRegistry;

    @Override
    public void initialize() {
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        stone = blockManager.getBlock("CoreAssets:Stone");
        water = blockManager.getBlock("CoreAssets:Water");
        ice = blockManager.getBlock("CoreAssets:Ice");
        sand = blockManager.getBlock("CoreAssets:Sand");
        grass = blockManager.getBlock("CoreAssets:Grass");
        snow = blockManager.getBlock("CoreAssets:Snow");
        dirt = blockManager.getBlock("CoreAssets:Dirt");
        biomeRegistry = CoreRegistry.get(BiomeRegistry.class);
    }

    @Override
    public void generateChunk(Chunk chunk, Region chunkRegion) {
        SurfacesFacet surfacesFacet = chunkRegion.getFacet(SurfacesFacet.class);
        DensityFacet densityFacet = chunkRegion.getFacet(DensityFacet.class);
        SeaLevelFacet seaLevelFacet = chunkRegion.getFacet(SeaLevelFacet.class);
        WhittakerBiomeFacet biomeFacet = chunkRegion.getFacet(WhittakerBiomeFacet.class);
        int seaLevel = seaLevelFacet.getSeaLevel();

        Vector3i chunkOffset = chunk.getChunkWorldOffset(new Vector3i());
        for (int x = 0; x < chunk.getChunkSizeX(); ++x) {
            for (int z = 0; z < chunk.getChunkSizeZ(); ++z) {

                WhittakerBiome biome = biomeFacet.get(x, z);

                for (int y = 0; y < chunk.getChunkSizeY(); ++y) {

                    biomeRegistry.setBiome(biome, chunk, x, y, z);

                    float density = densityFacet.get(x, y, z);

                    if (surfacesFacet.get(x, y, z)) {
                        chunk.setBlock(x, y, z, getSurfaceBlock(biome));
                    } else if (density > 8) {
                        chunk.setBlock(x, y, z, stone);
                    } else if (density > 0) {
                        chunk.setBlock(x, y, z, dirt);
                    } else if (y + chunkOffset.y <= seaLevel) {
                        chunk.setBlock(x, y, z, water);
                    }
                }
            }
        }
    }

    private Block getSurfaceBlock(WhittakerBiome type) {
        switch (type) {
            case TROPICAL_RAIN_FOREST:
            case TROPICAL_SEASONAL_FOREST:
            case GRASSLAND:
            case MARSH:
            case SHRUBLAND:
            case TEMPERATE_DECIDUOUS_FOREST:
            case TEMPERATE_RAIN_FOREST:
                return grass;

            case TAIGA:
                return ice;

            case SNOW:
                return snow;

            case TUNDRA:
            case SCORCHED:
            case BARE:
                return dirt;

            case COAST:
            case BEACH:
            case LAKESHORE:
            case SUBTROPICAL_DESERT:
            case TEMPERATE_DESERT:
            case ICE:
                return sand;

            case LAKE:
            case OCEAN:
                return water;
        }

        return stone;
    }
}
