/*
 * Copyright 2014 MovingBlocks
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

import java.util.Map;

import org.terasology.core.world.generator.facets.PlantFacet;
import org.terasology.math.TeraMath;
import org.terasology.polyworld.biome.WhittakerBiome;
import org.terasology.polyworld.biome.WhittakerBiomeFacet;
import org.terasology.utilities.procedural.NoiseTable;
import org.terasology.world.biomes.Biome;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.facets.SurfaceHeightFacet;

import com.google.common.collect.Maps;

/**
 * Determines where plants can be placed.  Will put plants one block above the surface if it is in the correct biome.
 */
@Produces(PlantFacet.class)
@Requires({@Facet(SurfaceHeightFacet.class), @Facet(WhittakerBiomeFacet.class)})
public class FloraProvider implements FacetProvider {

    private NoiseTable noiseTable;

    private Map<Biome, Integer> probability = Maps.newHashMap();

    public FloraProvider() {
        for (WhittakerBiome biome : WhittakerBiome.values()) {
            probability.put(biome, Integer.valueOf(0));
        }

        probability.put(WhittakerBiome.MARSH, 255);
        probability.put(WhittakerBiome.SUBTROPICAL_DESERT, 150);
        probability.put(WhittakerBiome.TROPICAL_RAIN_FOREST, 200);
        probability.put(WhittakerBiome.LAKESHORE, 200);
        probability.put(WhittakerBiome.TROPICAL_SEASONAL_FOREST, 120);
        probability.put(WhittakerBiome.SHRUBLAND, 50);
        probability.put(WhittakerBiome.TEMPERATE_RAIN_FOREST, 80);
        probability.put(WhittakerBiome.TEMPERATE_DECIDUOUS_FOREST, 40);
        probability.put(WhittakerBiome.GRASSLAND, 30);
        probability.put(WhittakerBiome.TAIGA, 10);
        probability.put(WhittakerBiome.TEMPERATE_DESERT, 5);
        probability.put(WhittakerBiome.SUBTROPICAL_DESERT, 2);
        probability.put(WhittakerBiome.BEACH, 1);
    }

    @Override
    public void setSeed(long seed) {
        noiseTable = new NoiseTable(seed);
    }

    @Override
    public void process(GeneratingRegion region) {
        PlantFacet facet = new PlantFacet(region.getRegion(), region.getBorderForFacet(PlantFacet.class));
        SurfaceHeightFacet surface = region.getRegionFacet(SurfaceHeightFacet.class);
        WhittakerBiomeFacet biomeFacet = region.getRegionFacet(WhittakerBiomeFacet.class);

        int minY = facet.getWorldRegion().minY();
        int maxY = facet.getWorldRegion().maxY();
        for (int z = facet.getRelativeRegion().minZ(); z <= facet.getRelativeRegion().maxZ(); ++z) {
            for (int x = facet.getRelativeRegion().minX(); x <= facet.getRelativeRegion().maxX(); ++x) {
                int height = TeraMath.floorToInt(surface.get(x, z));
                if (height >= minY && height < maxY) {
                    WhittakerBiome biome = biomeFacet.get(x, z);
                    int relHeight = height - minY + facet.getRelativeRegion().minY();

                    if (noiseTable.noise(x, z) < probability.get(biome)) {
                        facet.set(x, relHeight + 1, z, true);
                    }
                }
            }
        }
        region.setRegionFacet(PlantFacet.class, facet);
    }
}
