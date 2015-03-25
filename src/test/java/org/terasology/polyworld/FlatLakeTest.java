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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.SimpleUri;
import org.terasology.math.Region3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.polyworld.biome.BiomeModel;
import org.terasology.polyworld.biome.WhittakerBiome;
import org.terasology.polyworld.biome.WhittakerBiomeModelFacet;
import org.terasology.polyworld.elevation.ElevationModel;
import org.terasology.polyworld.elevation.ElevationModelFacet;
import org.terasology.polyworld.voronoi.Corner;
import org.terasology.polyworld.voronoi.Graph;
import org.terasology.polyworld.voronoi.GraphFacet;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.World;

/**
 * Tests the flatness of lakes
 * @author Martin Steiger
 */
public class FlatLakeTest {

    private static final Logger logger = LoggerFactory.getLogger(FlatLakeTest.class);

    private IslandWorldGenerator worldGen;

    @Before
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
            for (org.terasology.polyworld.voronoi.Region reg : graph.getRegions()) {
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

    private void testRegion(ElevationModel elevationModel, org.terasology.polyworld.voronoi.Region reg) {
        logger.info("Testing lake at {}", reg);

        float centerElevation = elevationModel.getElevation(reg);
        float eps = 0.001f;
        for (Corner corner : reg.getCorners()) {
            Assert.assertEquals(centerElevation, elevationModel.getElevation(corner), eps);
        }
    }

    private Region createRegion(int minX, int minY, int maxX, int maxY) {

        Region3i area3d = Region3i.createFromMinMax(new Vector3i(minX, 0, minY), new Vector3i(maxX, 1, maxY));
        World world = worldGen.getWorld();
        Region region = world.getWorldData(area3d);

        return region;
    }
}
