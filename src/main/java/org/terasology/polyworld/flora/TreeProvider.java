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
package org.terasology.polyworld.flora;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import org.joml.Vector3i;
import org.terasology.core.world.generator.facetProviders.PositionFilters;
import org.terasology.core.world.generator.facetProviders.SurfaceObjectProvider;
import org.terasology.core.world.generator.facets.TreeFacet;
import org.terasology.core.world.generator.trees.TreeGenerator;
import org.terasology.core.world.generator.trees.Trees;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.utilities.procedural.Noise;
import org.terasology.engine.utilities.procedural.WhiteNoise;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.ConfigurableFacetProvider;
import org.terasology.engine.world.generation.Facet;
import org.terasology.engine.world.generation.FacetBorder;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Produces;
import org.terasology.engine.world.generation.Requires;
import org.terasology.engine.world.generation.facets.SeaLevelFacet;
import org.terasology.engine.world.generation.facets.SurfacesFacet;
import org.terasology.nui.properties.Range;
import org.terasology.polyworld.biome.WhittakerBiome;
import org.terasology.polyworld.biome.WhittakerBiomeFacet;

import java.util.List;

/**
 * Determines where trees can be placed.  Will put trees one block above the surface.
 */
@Produces(TreeFacet.class)
@Requires({
        @Facet(value = SeaLevelFacet.class, border = @FacetBorder(sides = 12)),
        @Facet(value = SurfacesFacet.class, border = @FacetBorder(sides = 12 + 1, bottom = 1)),
        @Facet(value = WhittakerBiomeFacet.class, border = @FacetBorder(sides = 12))
})
public class TreeProvider extends SurfaceObjectProvider<WhittakerBiome, TreeGenerator> implements ConfigurableFacetProvider {

    private Noise densityNoiseGen;

    private TreeProviderConfiguration configuration = new TreeProviderConfiguration();

    public TreeProvider() {
        register(WhittakerBiome.TEMPERATE_RAIN_FOREST, Trees.oakTree(), 0.05f);
        register(WhittakerBiome.TEMPERATE_RAIN_FOREST, Trees.pineTree(), 0.03f);
        register(WhittakerBiome.TEMPERATE_RAIN_FOREST, Trees.redTree(), 0.05f);

        register(WhittakerBiome.TEMPERATE_DECIDUOUS_FOREST, Trees.pineTree(), 0.08f);
        register(WhittakerBiome.TEMPERATE_DECIDUOUS_FOREST, Trees.redTree(), 0.05f);

        register(WhittakerBiome.TROPICAL_RAIN_FOREST, Trees.pineTree(), 0.07f);
        register(WhittakerBiome.TROPICAL_RAIN_FOREST, Trees.redTree(), 0.08f);

        register(WhittakerBiome.TROPICAL_SEASONAL_FOREST, Trees.oakVariationTree(), 0.15f);

        register(WhittakerBiome.LAKESHORE, Trees.oakTree(), 0.15f);
        register(WhittakerBiome.LAKESHORE, Trees.redTree(), 0.1f);

        register(WhittakerBiome.SHRUBLAND, Trees.birchTree(), 0.02f);

        register(WhittakerBiome.TAIGA, Trees.pineTree(), 0.08f);

        register(WhittakerBiome.TUNDRA, Trees.birchTree(), 0.01f);
        register(WhittakerBiome.TUNDRA, Trees.pineTree(), 0.01f);

        register(WhittakerBiome.GRASSLAND, Trees.oakTree(), 0.03f);

        register(WhittakerBiome.SUBTROPICAL_DESERT, Trees.cactus(), 0.02f);

        register(WhittakerBiome.TEMPERATE_DESERT, Trees.cactus(), 0.04f);
    }

    @Override
    public void setSeed(long seed) {
        super.setSeed(seed);

        densityNoiseGen = new WhiteNoise(seed);
    }

    @Override
    public void process(GeneratingRegion region) {
        SurfacesFacet surface = region.getRegionFacet(SurfacesFacet.class);
        WhittakerBiomeFacet biome = region.getRegionFacet(WhittakerBiomeFacet.class);
        SeaLevelFacet seaLevel = region.getRegionFacet(SeaLevelFacet.class);

        List<Predicate<Vector3i>> filters = Lists.newArrayList();

        filters.add(PositionFilters.minHeight(seaLevel.getSeaLevel()));
        filters.add(PositionFilters.probability(densityNoiseGen, configuration.density * 0.1f));
        filters.add(PositionFilters.flatness(surface, 1, 1));

        int maxRad = 12;
        int maxHeight = 32;
        Border3D borderForTreeFacet = region.getBorderForFacet(TreeFacet.class);
        TreeFacet facet = new TreeFacet(region.getRegion(), borderForTreeFacet.extendBy(0, maxHeight, maxRad));

        populateFacet(facet, surface, biome, filters);

        region.setRegionFacet(TreeFacet.class, facet);
    }

    @Override
    public String getConfigurationName() {
        return "Trees";
    }

    @Override
    public Component getConfiguration() {
        return configuration;
    }

    @Override
    public void setConfiguration(Component configuration) {
        this.configuration = (TreeProviderConfiguration) configuration;
    }

    private static class TreeProviderConfiguration implements Component {
        @Range(min = 0, max = 1.0f, increment = 0.05f, precision = 2, description = "Define the overall tree density")
        private float density = 0.4f;

    }
}
