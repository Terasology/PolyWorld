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

package org.terasology.polyworld;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.generation.Region;
import org.terasology.engine.world.generation.World;
import org.terasology.polyworld.biome.BiomeModel;
import org.terasology.polyworld.biome.WhittakerBiome;
import org.terasology.polyworld.biome.WhittakerBiomeModelFacet;
import org.terasology.polyworld.elevation.ElevationModel;
import org.terasology.polyworld.elevation.ElevationModelFacet;
import org.terasology.polyworld.graph.Corner;
import org.terasology.polyworld.graph.Graph;
import org.terasology.polyworld.graph.GraphFacet;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the flatness of lakes
 */
public class FlatLakeTest {

    private static final Logger logger = LoggerFactory.getLogger(FlatLakeTest.class);

    private IslandWorldGenerator worldGen;

    @BeforeEach
    public void setup() {
        TinyEnvironment.setup();

        worldGen = new IslandWorldGenerator(new SimpleUri("polyworld:island"));
        worldGen.setWorldSeed("asdfasdf0");
        worldGen.initialize();
    }

    @Test
    public void testFlatness() {

        Region region = createRegion(0, 0, 512, 512);
        GraphFacet graphFacet = region.getFacet(GraphFacet.class);
        WhittakerBiomeModelFacet biomeModelFacet = region.getFacet(WhittakerBiomeModelFacet.class);
        ElevationModelFacet elevationModelFacet = region.getFacet(ElevationModelFacet.class);

        boolean tested = false;
        for (Graph graph : graphFacet.getAllGraphs()) {
            BiomeModel biomeModel = biomeModelFacet.get(graph);
            for (org.terasology.polyworld.graph.GraphRegion reg : graph.getRegions()) {
                WhittakerBiome biome = biomeModel.getBiome(reg);
                if (biome == WhittakerBiome.LAKE) {
                    testRegion(elevationModelFacet.get(graph), reg);
                    tested = true;
                }
            }
        }

        if (!tested) {
            logger.warn("No lakes found to test! Test skipped");
        }
    }

    private void testRegion(ElevationModel elevationModel, org.terasology.polyworld.graph.GraphRegion reg) {
        logger.info("Testing lake at {}", reg);

        float centerElevation = elevationModel.getElevation(reg);
        float eps = 0.001f;
        for (Corner corner : reg.getCorners()) {
            assertEquals(centerElevation, elevationModel.getElevation(corner), eps);
        }
    }

    private Region createRegion(int minX, int minY, int maxX, int maxY) {
        BlockRegion area3d = new BlockRegion(minX, 0, minY, maxX, 1, maxY);
        World world = worldGen.getWorld();
        return world.getWorldData(area3d);
    }
}
