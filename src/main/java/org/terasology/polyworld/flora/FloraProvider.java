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
package org.terasology.polyworld.flora;

import java.util.List;
import java.util.Map;

import org.terasology.core.world.generator.facetProviders.PositionFilters;
import org.terasology.core.world.generator.facetProviders.SurfaceObjectProvider;
import org.terasology.core.world.generator.facets.FloraFacet;
import org.terasology.core.world.generator.rasterizers.FloraType;
import org.terasology.entitySystem.Component;
import org.terasology.math.Vector3i;
import org.terasology.polyworld.biome.WhittakerBiome;
import org.terasology.polyworld.biome.WhittakerBiomeFacet;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.utilities.procedural.FastNoise;
import org.terasology.utilities.procedural.Noise3D;
import org.terasology.world.biomes.Biome;
import org.terasology.world.generation.ConfigurableFacetProvider;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.facets.SeaLevelFacet;
import org.terasology.world.generation.facets.SurfaceHeightFacet;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

/**
 * Determines where plants can be placed.  Will put plants one block above the surface if it is in the correct biome.
 */
@Produces(FloraFacet.class)
@Requires({@Facet(SurfaceHeightFacet.class), @Facet(WhittakerBiomeFacet.class)})
public class FloraProvider extends SurfaceObjectProvider<Biome, FloraType> implements ConfigurableFacetProvider {

    private Noise3D densityNoiseGen;

    private DensityConfiguration configuration = new DensityConfiguration();

    private Map<FloraType, Float> typeProbs = ImmutableMap.of(
            FloraType.GRASS, 0.85f,
            FloraType.FLOWER, 0.1f,
            FloraType.MUSHROOM, 0.05f);

    private Map<WhittakerBiome, Float> biomeProbs = ImmutableMap.<WhittakerBiome, Float>builder()
            .put(WhittakerBiome.MARSH, 0.8f)
            .put(WhittakerBiome.SUBTROPICAL_DESERT, 0.01f)
            .put(WhittakerBiome.TROPICAL_RAIN_FOREST, 0.95f)
            .put(WhittakerBiome.LAKESHORE, 0.7f)
            .put(WhittakerBiome.TROPICAL_SEASONAL_FOREST, 0.8f)
            .put(WhittakerBiome.SHRUBLAND, 0.3f)
            .put(WhittakerBiome.TEMPERATE_RAIN_FOREST, 0.7f)
            .put(WhittakerBiome.TEMPERATE_DECIDUOUS_FOREST, 0.6f)
            .put(WhittakerBiome.GRASSLAND, 0.9f)
            .put(WhittakerBiome.TAIGA, 0.05f)
            .put(WhittakerBiome.TEMPERATE_DESERT, 0.15f)
            .put(WhittakerBiome.BEACH, 0.1f).build();

    public FloraProvider() {

        for (WhittakerBiome biome : biomeProbs.keySet()) {
            Float biomeProb = biomeProbs.get(biome);
            for (FloraType type : typeProbs.keySet()) {
                float typeProb = typeProbs.get(type);
                float prob = biomeProb * typeProb;
                register(biome, type, prob);
            }
        }

        register(WhittakerBiome.TAIGA, FloraType.MUSHROOM, 0);
        register(WhittakerBiome.BEACH, FloraType.MUSHROOM, 0);
    }

    @Override
    public void setSeed(long seed) {
        super.setSeed(seed);

        densityNoiseGen = new FastNoise(seed);
    }

    @Override
    public void process(GeneratingRegion region) {
        SurfaceHeightFacet surface = region.getRegionFacet(SurfaceHeightFacet.class);
        WhittakerBiomeFacet biomeFacet = region.getRegionFacet(WhittakerBiomeFacet.class);
        SeaLevelFacet seaLevel = region.getRegionFacet(SeaLevelFacet.class);

        FloraFacet facet = new FloraFacet(region.getRegion(), region.getBorderForFacet(FloraFacet.class));

        List<Predicate<Vector3i>> filters = Lists.newArrayList();

        filters.add(PositionFilters.minHeight(seaLevel.getSeaLevel()));
        filters.add(PositionFilters.probability(densityNoiseGen, configuration.density));

        populateFacet(facet, surface, biomeFacet, filters);

        region.setRegionFacet(FloraFacet.class, facet);
    }

    @Override
    public String getConfigurationName() {
        return "Flora";
    }

    @Override
    public Component getConfiguration() {
        return configuration;
    }

    @Override
    public void setConfiguration(Component configuration) {
        this.configuration = (DensityConfiguration) configuration;
    }

    private static class DensityConfiguration implements Component {
        @Range(min = 0, max = 1.0f, increment = 0.05f, precision = 2, description = "Define the overall flora density")
        private float density = 0.4f;

    }
}
