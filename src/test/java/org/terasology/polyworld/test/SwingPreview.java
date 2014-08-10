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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Map;
import java.util.Random;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.terasology.math.delaunay.Voronoi;
import org.terasology.polyworld.biome.Biome;
import org.terasology.polyworld.voronoi.VoronoiGraph;

import com.google.common.collect.Maps;

/**
 * Preview generated world in Swing
 * @author Martin Steiger
 */
final class SwingPreview {
    
    private SwingPreview() {
        
    }

    public static void main(String[] args) {
        final int width = 1024;
        final int height = 1024;
        final int numSites = 5000;
        final long seed = System.nanoTime();

        final Random r = new Random(seed);

        final Voronoi v = new Voronoi(numSites, width, height, r);

        final VoronoiGraph graph = new VoronoiGraph(v, 2, r);
        final IslandPainter painter = new IslandPainter(graph);

        Map<Biome, Color> map = Maps.newHashMap();
        map.put(OCEAN, new Color(0x44447a)); 
        map.put(LAKE, new Color(0x336699)); 
        map.put(BEACH, new Color(0xa09077)); 
        map.put(SNOW, new Color(0xffffff));
        map.put(TUNDRA, new Color(0xbbbbaa)); 
        map.put(BARE, new Color(0x888888)); 
        map.put(SCORCHED, new Color(0x555555)); 
        map.put(TAIGA, new Color(0x99aa77));
        map.put(SHURBLAND, new Color(0x889977)); 
        map.put(TEMPERATE_DESERT, new Color(0xc9d29b));
        map.put(TEMPERATE_RAIN_FOREST, new Color(0x448855)); 
        map.put(TEMPERATE_DECIDUOUS_FOREST, new Color(0x679459));
        map.put(GRASSLAND, new Color(0x88aa55)); 
        map.put(SUBTROPICAL_DESERT, new Color(0xd2b98b)); 
        map.put(SHRUBLAND, new Color(0x889977));
        map.put(ICE, new Color(0x99ffff));
        map.put(MARSH, new Color(0x2f6666));
        map.put(TROPICAL_RAIN_FOREST, new Color(0x337755));
        map.put(TROPICAL_SEASONAL_FOREST, new Color(0x559944));
        map.put(COAST, new Color(0x33335a));
        map.put(LAKESHORE, new Color(0x225588));

        painter.setBiomeColors(map);
        painter.setRiverColor(new Color(0x225588));

        JFrame frame = new JFrame();
        JComponent panel = new JComponent() {

            private static final long serialVersionUID = 4178713176841691478L;

            @Override
            public void paint(Graphics g1) {
                Graphics2D g = (Graphics2D) g1;

                painter.drawRegions(g, true);
//                painter.drawDelaunay(g);

                painter.drawRivers(g);

//                painter.drawSites(g);
//                painter.drawCorners(g);
                painter.drawBounds(g);
            }
        };
        frame.add(panel);
        frame.setTitle("Preview - " + seed);
        frame.setVisible(true);
        frame.setSize(width + 50, height + 50);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}
