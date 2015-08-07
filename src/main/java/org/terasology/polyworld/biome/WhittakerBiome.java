/*
 * Copyright 2014 MovingBlocks
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
    OCEAN,
    LAKE,
    BEACH,
    SNOW,

    /**
     * Mostly shrubs, and grasses, mosses, and lichens as well as some scattered trees.
     */
    TUNDRA,
    BARE,
    SCORCHED,

    /**
     * Boreal forest or snowforest, consisting mostly of pines, spruces and larches.
     */
    TAIGA,
    TEMPERATE_DESERT,
    TEMPERATE_RAIN_FOREST,

    /**
     * Trees or shrubs lose their leaves seasonally (most commonly during autumn).
     */
    TEMPERATE_DECIDUOUS_FOREST,

    /**
     * Dominated by grasses, however sedge and rush, shrubs and trees can also be found.
     */
    GRASSLAND,
    SUBTROPICAL_DESERT,
    SHRUBLAND,
    ICE,

    /**
     * Wetland that is dominated by mostly herbaceous plant species.
     */
    MARSH,
    TROPICAL_RAIN_FOREST,
    TROPICAL_SEASONAL_FOREST,
    COAST,
    LAKESHORE;

    @Override
    public String getId() {
        return name();
    }

    @Override
    public String getName() {
        return toString();
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
