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

import static org.terasology.polyworld.biome.Biome.BARE;
import static org.terasology.polyworld.biome.Biome.BEACH;
import static org.terasology.polyworld.biome.Biome.COAST;
import static org.terasology.polyworld.biome.Biome.GRASSLAND;
import static org.terasology.polyworld.biome.Biome.ICE;
import static org.terasology.polyworld.biome.Biome.LAKE;
import static org.terasology.polyworld.biome.Biome.LAKESHORE;
import static org.terasology.polyworld.biome.Biome.MARSH;
import static org.terasology.polyworld.biome.Biome.OCEAN;
import static org.terasology.polyworld.biome.Biome.SCORCHED;
import static org.terasology.polyworld.biome.Biome.SHRUBLAND;
import static org.terasology.polyworld.biome.Biome.SHURBLAND;
import static org.terasology.polyworld.biome.Biome.SNOW;
import static org.terasology.polyworld.biome.Biome.SUBTROPICAL_DESERT;
import static org.terasology.polyworld.biome.Biome.TAIGA;
import static org.terasology.polyworld.biome.Biome.TEMPERATE_DECIDUOUS_FOREST;
import static org.terasology.polyworld.biome.Biome.TEMPERATE_DESERT;
import static org.terasology.polyworld.biome.Biome.TEMPERATE_RAIN_FOREST;
import static org.terasology.polyworld.biome.Biome.TROPICAL_RAIN_FOREST;
import static org.terasology.polyworld.biome.Biome.TROPICAL_SEASONAL_FOREST;
import static org.terasology.polyworld.biome.Biome.TUNDRA;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.terasology.polyworld.biome.Biome;
import org.terasology.polyworld.biome.BiomeModel;
import org.terasology.polyworld.voronoi.Corner;
import org.terasology.polyworld.voronoi.Graph;
import org.terasology.polyworld.voronoi.Region;

import com.google.common.collect.Maps;

/**
 * Draws the generated voronoi-based world on a AWT graphics instance
 * @author Martin Steiger
 */
public class BiomePainter {

    private final Map<Biome, Color> biomeColors = Maps.newHashMap();

    public BiomePainter() {
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

    public void drawBiomes(Graphics2D g, BiomeModel biomeModel, Graph graph) {
        List<Region> centers = graph.getRegions();

        for (final Region c : centers) {
            Color color;
            Biome biome = biomeModel.getBiome(c);
            color =  biomeColors.get(biome);

            g.setColor(color);

            List<Corner> pts = new ArrayList<>(c.getCorners());
            Collections.sort(pts, new AngleSorter(c));

            drawPoly(g, pts);
        }
    }

    private void drawPoly(Graphics2D g, List<Corner> pts) {
        int[] x = new int[pts.size()];
        int[] y = new int[pts.size()];

        for (int i = 0; i < pts.size(); i++) {
            x[i] = (int) pts.get(i).getLocation().getX();
            y[i] = (int) pts.get(i).getLocation().getY();
        }

        g.fillPolygon(x, y, pts.size());
    }

    /**
     * @param map
     */
    public void setBiomeColos(Biome biome, Color color) {
        this.biomeColors.put(biome, color);
    }

}
