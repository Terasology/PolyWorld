/*
 * Copyright 2018 MovingBlocks
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

package org.terasology.polyworld.biome;

import org.terasology.world.biomes.Biome;

/**
 * Different biome types
 */
public enum WhittakerBiome implements Biome {
    OCEAN("Ocean"),
    LAKE("Lake"),
    BEACH("Beach"),
    SNOW("Snow"),

    /**
     * Mostly shrubs, and grasses, mosses, and lichens as well as some scattered trees.
     */
    TUNDRA("Tundra"),
    BARE("Bare"),
    SCORCHED("Scorched"),

    /**
     * Boreal forest or snowforest, consisting mostly of pines, spruces and larches.
     */
    TAIGA("Taiga"),
    TEMPERATE_DESERT("Temperate desert"),
    TEMPERATE_RAIN_FOREST("Temperate rain forest"),

    /**
     * Trees or shrubs lose their leaves seasonally (most commonly during autumn).
     */
    TEMPERATE_DECIDUOUS_FOREST("Temperate deciduous forest"),

    /**
     * Dominated by grasses, however sedge and rush, shrubs and trees can also be found.
     */
    GRASSLAND("Grassland"),
    SUBTROPICAL_DESERT("Subtropical desert"),
    SHRUBLAND("Shrubland"),
    ICE("Ice"),

    /**
     * Wetland that is dominated by mostly herbaceous plant species.
     */
    MARSH("Marsh"),
    TROPICAL_RAIN_FOREST("Tropical rain forest"),
    TROPICAL_SEASONAL_FOREST("Tropical seasonal forest"),
    COAST("Coast"),
    LAKESHORE("Lakeshore");

    private String id;
    private String name;

    WhittakerBiome(final String name) {
        this.id = "WhittakerBiome:" + name().toLowerCase();
        this.name = name;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public float getFog() {
        // TODO: remove this property from Biome
        return 0;
    }

    @Override
    public float getHumidity() {
        // TODO: remove this property from Biome
        return 0;
    }

    @Override
    public float getTemperature() {
        // TODO: remove this property from Biome
        return 0;
    }
}
