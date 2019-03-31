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

import org.terasology.biomesAPI.Biome;

/**
 * Different biome types
 */
public enum WhittakerBiome implements Biome {
    OCEAN("Ocean", 2001),
    LAKE("Lake", 2002),
    BEACH("Beach", 2003),
    SNOW("Snow", 2004),

    /**
     * Mostly shrubs, and grasses, mosses, and lichens as well as some scattered trees.
     */
    TUNDRA("Tundra", 2005),
    BARE("Bare", 2006),
    SCORCHED("Scorched", 2007),

    /**
     * Boreal forest or snowforest, consisting mostly of pines, spruces and larches.
     */
    TAIGA("Taiga", 2008),
    TEMPERATE_DESERT("Temperate desert", 2009),
    TEMPERATE_RAIN_FOREST("Temperate rain forest", 2010),

    /**
     * Trees or shrubs lose their leaves seasonally (most commonly during autumn).
     */
    TEMPERATE_DECIDUOUS_FOREST("Temperate deciduous forest", 2011),

    /**
     * Dominated by grasses, however sedge and rush, shrubs and trees can also be found.
     */
    GRASSLAND("Grassland", 2012),
    SUBTROPICAL_DESERT("Subtropical desert", 2013),
    SHRUBLAND("Shrubland", 2014),
    ICE("Ice", 2015),

    /**
     * Wetland that is dominated by mostly herbaceous plant species.
     */
    MARSH("Marsh", 2016),
    TROPICAL_RAIN_FOREST("Tropical rain forest", 2017),
    TROPICAL_SEASONAL_FOREST("Tropical seasonal forest", 2018),
    COAST("Coast", 2019),
    LAKESHORE("Lakeshore", 2020);

    private String id;
    private String name;
    private short hash;

    WhittakerBiome(final String name, int hash) {
        this.hash = (short) hash;
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
    public short biomeHash() {
        return hash;
    }
}
