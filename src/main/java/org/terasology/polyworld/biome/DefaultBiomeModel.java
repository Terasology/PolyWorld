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

import org.terasology.polyworld.voronoi.Region;

/**
 * TODO Type description
 * @author Martin Steiger
 */
public class DefaultBiomeModel implements BiomeModel {

    public Biome getBiome(Region p) {
        if (p.isOcean()) {
            return Biome.OCEAN;
        } else if (p.isWater()) {
            if (p.getElevation() < 0.1) {
                return Biome.MARSH;
            }
            if (p.getElevation() > 0.8) {
                return Biome.ICE;
            }
            return Biome.LAKE;
        } else if (p.isCoast()) {
            return Biome.BEACH;
        } else if (p.getElevation() > 0.8) {
            if (p.getMoisture() > 0.50) {
                return Biome.SNOW;
            } else if (p.getMoisture() > 0.33) {
                return Biome.TUNDRA;
            } else if (p.getMoisture() > 0.16) {
                return Biome.BARE;
            } else {
                return Biome.SCORCHED;
            }
        } else if (p.getElevation() > 0.6) {
            if (p.getMoisture() > 0.66) {
                return Biome.TAIGA;
            } else if (p.getMoisture() > 0.33) {
                return Biome.SHRUBLAND;
            } else {
                return Biome.TEMPERATE_DESERT;
            }
        } else if (p.getElevation() > 0.3) {
            if (p.getMoisture() > 0.83) {
                return Biome.TEMPERATE_RAIN_FOREST;
            } else if (p.getMoisture() > 0.50) {
                return Biome.TEMPERATE_DECIDUOUS_FOREST;
            } else if (p.getMoisture() > 0.16) {
                return Biome.GRASSLAND;
            } else {
                return Biome.TEMPERATE_DESERT;
            }
        } else {
            if (p.getMoisture() > 0.66) {
                return Biome.TROPICAL_RAIN_FOREST;
            } else if (p.getMoisture() > 0.33) {
                return Biome.TROPICAL_SEASONAL_FOREST;
            } else if (p.getMoisture() > 0.16) {
                return Biome.GRASSLAND;
            } else {
                return Biome.SUBTROPICAL_DESERT;
            }
        }
    }
}
