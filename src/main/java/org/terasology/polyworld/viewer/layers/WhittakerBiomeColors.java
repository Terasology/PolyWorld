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

package org.terasology.polyworld.viewer.layers;

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
import static org.terasology.polyworld.biome.WhittakerBiome.SNOW;
import static org.terasology.polyworld.biome.WhittakerBiome.SUBTROPICAL_DESERT;
import static org.terasology.polyworld.biome.WhittakerBiome.TAIGA;
import static org.terasology.polyworld.biome.WhittakerBiome.TEMPERATE_DECIDUOUS_FOREST;
import static org.terasology.polyworld.biome.WhittakerBiome.TEMPERATE_DESERT;
import static org.terasology.polyworld.biome.WhittakerBiome.TEMPERATE_RAIN_FOREST;
import static org.terasology.polyworld.biome.WhittakerBiome.TROPICAL_RAIN_FOREST;
import static org.terasology.polyworld.biome.WhittakerBiome.TROPICAL_SEASONAL_FOREST;
import static org.terasology.polyworld.biome.WhittakerBiome.TUNDRA;

import java.util.function.Function;

import org.terasology.polyworld.biome.WhittakerBiome;
import org.terasology.rendering.nui.Color;

import com.google.common.collect.ImmutableMap;

/**
 * Maps biome types to color.
 * @author Martin Steiger
 */
public class WhittakerBiomeColors implements Function<WhittakerBiome, Color> {

    public static final WhittakerBiomeColors INSTANCE = new WhittakerBiomeColors();

    private final ImmutableMap<WhittakerBiome, Color> biomeColors;

    public WhittakerBiomeColors() {
        biomeColors = ImmutableMap.<WhittakerBiome, Color>builder()
                .put(OCEAN, new Color(0x44447aff))
                .put(LAKE, new Color(0x336699ff))
                .put(BEACH, new Color(0xa09077ff))
                .put(SNOW, new Color(0xffffffff))
                .put(TUNDRA, new Color(0xa0a099ff))
                .put(BARE, new Color(0x888888ff))
                .put(SCORCHED, new Color(0x555555ff))
                .put(TAIGA, new Color(0x99aa77ff))
                .put(TEMPERATE_DESERT, new Color(0xc9d29bff))
                .put(TEMPERATE_RAIN_FOREST, new Color(0x448855ff))
                .put(TEMPERATE_DECIDUOUS_FOREST, new Color(0x679459ff))
                .put(GRASSLAND, new Color(0x88aa55ff))
                .put(SUBTROPICAL_DESERT, new Color(0xd2b98bff))
                .put(SHRUBLAND, new Color(0x889977ff))
                .put(ICE, new Color(0x99ffffff))
                .put(MARSH, new Color(0x2f6666ff))
                .put(TROPICAL_RAIN_FOREST, new Color(0x337755ff))
                .put(TROPICAL_SEASONAL_FOREST, new Color(0x559944ff))
                .put(COAST, new Color(0x33335aff))
                .put(LAKESHORE, new Color(0x225588ff))
                .build();
    }

    @Override
    public Color apply(WhittakerBiome biome) {
        return biomeColors.get(biome);
    }
}
