// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.polyworld.raster;

import org.terasology.biomesAPI.BiomeRegistry;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.chunks.CoreChunk;
import org.terasology.engine.world.generation.Region;
import org.terasology.engine.world.generation.WorldRasterizer;
import org.terasology.engine.world.generation.facets.SeaLevelFacet;
import org.terasology.engine.world.generation.facets.SurfaceHeightFacet;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Vector3i;
import org.terasology.polyworld.biome.WhittakerBiome;
import org.terasology.polyworld.biome.WhittakerBiomeFacet;

/**
 *
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
    public void generateChunk(CoreChunk chunk, Region chunkRegion) {
        SurfaceHeightFacet surfaceHeightData = chunkRegion.getFacet(SurfaceHeightFacet.class);
        SeaLevelFacet seaLevelFacet = chunkRegion.getFacet(SeaLevelFacet.class);
        WhittakerBiomeFacet biomeFacet = chunkRegion.getFacet(WhittakerBiomeFacet.class);
        int seaLevel = seaLevelFacet.getSeaLevel();

        Vector3i chunkOffset = chunk.getChunkWorldOffset();
        for (int x = 0; x < chunk.getChunkSizeX(); ++x) {
            for (int z = 0; z < chunk.getChunkSizeZ(); ++z) {

                float surfaceHeight = surfaceHeightData.get(x, z);
                int surfaceHeightInt = TeraMath.floorToInt(surfaceHeight);

                WhittakerBiome biome = biomeFacet.get(x, z);

                for (int y = 0; y < chunk.getChunkSizeY(); ++y) {

                    biomeRegistry.setBiome(biome, chunk, x, y, z);

                    int depth = surfaceHeightInt - y - chunk.getChunkWorldOffsetY();
                    if (depth >= 0) {
                        Block block = getSurfaceBlock(depth, biome);
                        chunk.setBlock(x, y, z, block);
                    } else if (y + chunkOffset.y <= seaLevel) {
                        chunk.setBlock(x, y, z, water);
                    }
                }
            }
        }
    }

    private Block getSurfaceBlock(int depth, WhittakerBiome type) {
        if (depth > 8) {
            return stone;
        }

        if (depth > 1) {
            return dirt;
        }

        switch (type) {
            case TROPICAL_RAIN_FOREST:
            case TROPICAL_SEASONAL_FOREST:
            case GRASSLAND:
            case MARSH:
            case SHRUBLAND:
                return grass;

            case TAIGA:
                return ice;

            case SNOW:
                return snow;

            case TEMPERATE_DECIDUOUS_FOREST:
            case TEMPERATE_RAIN_FOREST:
                return grass;

            case TUNDRA:
            case SCORCHED:
            case BARE:
                return dirt;

            case COAST:
            case BEACH:
            case LAKESHORE:
            case SUBTROPICAL_DESERT:
            case TEMPERATE_DESERT:
                return sand;

            case ICE:
                return sand;

            case LAKE:
            case OCEAN:
                return water;
        }

        return stone;
    }
}
