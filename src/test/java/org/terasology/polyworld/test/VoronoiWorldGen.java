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

package org.terasology.polyworld.test;

import java.awt.Color;
import java.util.Random;

import org.terasology.math.delaunay.Voronoi;
import org.terasology.polyworld.voronoi.Biome;
import org.terasology.polyworld.voronoi.Center;
import org.terasology.polyworld.voronoi.VoronoiGraph;

/**
 * TestGraphImpl.java
 *
 * Supplies information for Voronoi graphing logic:
 *
 * 1) biome mapping information based on a Site's elevation and moisture
 *
 * 2) color mapping information based on biome, and for bodies of water
 *
 * @author Connor
 */
public class VoronoiWorldGen extends VoronoiGraph {

    private static final Color RIVER_COLOR = new Color(0x225588);

    public static enum ColorData implements Biome {

        OCEAN(0x44447a), 
        LAKE(0x336699), 
        BEACH(0xa09077), 
        SNOW(0xffffff),
        TUNDRA(0xbbbbaa), 
        BARE(0x888888), 
        SCORCHED(0x555555), 
        TAIGA(0x99aa77),
        SHURBLAND(0x889977), 
        TEMPERATE_DESERT(0xc9d29b),
        TEMPERATE_RAIN_FOREST(0x448855), 
        TEMPERATE_DECIDUOUS_FOREST(0x679459),
        GRASSLAND(0x88aa55), 
        SUBTROPICAL_DESERT(0xd2b98b), 
        SHRUBLAND(0x889977),
        ICE(0x99ffff), MARSH(0x2f6666),
        TROPICAL_RAIN_FOREST(0x337755),
        TROPICAL_SEASONAL_FOREST(0x559944),
        COAST(0x33335a),
        LAKESHORE(0x225588);
        
        private Color color;

        ColorData(int color) {
            this.color = new Color(color);
        }

        @Override
        public Color getColor() {
            return color;
        }
    }

    public VoronoiWorldGen(Voronoi v, int numLloydRelaxations, Random r) {
        super(v, numLloydRelaxations, r);
    }

    @Override
    protected Color getColor(Biome biome) {
        return biome.getColor();
    }
    
    @Override
    protected Color getRiverColor() {
        return RIVER_COLOR;
    }

}
