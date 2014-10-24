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

import static org.terasology.polyworld.biome.WhittakerBiome.BARE;
import static org.terasology.polyworld.biome.WhittakerBiome.BEACH;
import static org.terasology.polyworld.biome.WhittakerBiome.COAST;
import static org.terasology.polyworld.biome.WhittakerBiome.GRASSLAND;
import static org.terasology.polyworld.biome.WhittakerBiome.ICE;
import static org.terasology.polyworld.biome.WhittakerBiome.LAKE;
import static org.terasology.polyworld.biome.WhittakerBiome.LAKESHORE;
import static org.terasology.polyworld.biome.WhittakerBiome.MARSH;
import static org.terasology.polyworld.biome.WhittakerBiome.OCEAN;
import static org.terasology.polyworld.biome.WhittakerBiome.SCORCHED;
import static org.terasology.polyworld.biome.WhittakerBiome.SHRUBLAND;
import static org.terasology.polyworld.biome.WhittakerBiome.SHURBLAND;
import static org.terasology.polyworld.biome.WhittakerBiome.SNOW;
import static org.terasology.polyworld.biome.WhittakerBiome.SUBTROPICAL_DESERT;
import static org.terasology.polyworld.biome.WhittakerBiome.TAIGA;
import static org.terasology.polyworld.biome.WhittakerBiome.TEMPERATE_DECIDUOUS_FOREST;
import static org.terasology.polyworld.biome.WhittakerBiome.TEMPERATE_DESERT;
import static org.terasology.polyworld.biome.WhittakerBiome.TEMPERATE_RAIN_FOREST;
import static org.terasology.polyworld.biome.WhittakerBiome.TROPICAL_RAIN_FOREST;
import static org.terasology.polyworld.biome.WhittakerBiome.TROPICAL_SEASONAL_FOREST;
import static org.terasology.polyworld.biome.WhittakerBiome.TUNDRA;

import java.awt.Color;
import java.util.Map;

import org.terasology.world.biomes.Biome;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

/**
 * Used to map biomes to colors
 * @author Martin Steiger
 */
public class WhittakerBiomeColors implements Function<Biome, Color> {

    private final Map<Biome, Color> biomeColors = Maps.newHashMap();

    public WhittakerBiomeColors() {
        biomeColors.put(OCEAN, new Color(0x44447a));
        biomeColors.put(LAKE, new Color(0x336699));
        biomeColors.put(BEACH, new Color(0xa09077));
        biomeColors.put(SNOW, new Color(0xffffff));
        biomeColors.put(TUNDRA, new Color(0xbbbbaa));
        biomeColors.put(BARE, new Color(0x888888));
        biomeColors.put(SCORCHED, new Color(0x555555));
        biomeColors.put(TAIGA, new Color(0x99aa77));
        biomeColors.put(SHURBLAND, new Color(0x889977));
        biomeColors.put(TEMPERATE_DESERT, new Color(0xc9d29b));
        biomeColors.put(TEMPERATE_RAIN_FOREST, new Color(0x448855));
        biomeColors.put(TEMPERATE_DECIDUOUS_FOREST, new Color(0x679459));
        biomeColors.put(GRASSLAND, new Color(0x88aa55));
        biomeColors.put(SUBTROPICAL_DESERT, new Color(0xd2b98b));
        biomeColors.put(SHRUBLAND, new Color(0x889977));
        biomeColors.put(ICE, new Color(0x99ffff));
        biomeColors.put(MARSH, new Color(0x2f6666));
        biomeColors.put(TROPICAL_RAIN_FOREST, new Color(0x337755));
        biomeColors.put(TROPICAL_SEASONAL_FOREST, new Color(0x559944));
        biomeColors.put(COAST, new Color(0x33335a));
        biomeColors.put(LAKESHORE, new Color(0x225588));
    }

    @Override
    public Color apply(Biome biome) {
        Color color = biomeColors.get(biome);
        return color;
    }

    /**
     * @param map
     */
    public void setBiomeColor(Biome biome, Color color) {
        this.biomeColors.put(biome, color);
    }

}
