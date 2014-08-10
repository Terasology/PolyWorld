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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.terasology.math.geom.Rect2d;
import org.terasology.polyworld.biome.Biome;
import org.terasology.polyworld.biome.BiomeModel;
import org.terasology.polyworld.biome.DefaultBiomeModel;
import org.terasology.polyworld.voronoi.Center;
import org.terasology.polyworld.voronoi.Corner;
import org.terasology.polyworld.voronoi.Edge;
import org.terasology.polyworld.voronoi.VoronoiGraph;

/**
 * Draws the generated voronoi-based world on a AWT graphics instance
 * @author Martin Steiger
 */
public class IslandPainter {

    private VoronoiGraph graph;
    private Map<Biome, Color> biomeColors;
    private Color riverColor;
    private Color[] defaultColors;
    private BiomeModel biomeModel;

    /**
     * @param graph
     */
    public IslandPainter(VoronoiGraph graph) {
        this.graph = graph;

        Random r = new Random(1254);
        int numSites = graph.getCenters().size();
        defaultColors = new Color[numSites];
        for (int i = 0; i < defaultColors.length; i++) {
            defaultColors[i] = new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255));
        }
        
        biomeModel = new DefaultBiomeModel();
    }

    /**
     * Default drawing mode (biomes and rivers)
     * @param g the graphics instance
     */
    public void paint(Graphics2D g) {
        paint(g, true, true, false, false, false);
    }

    public void paint(Graphics2D g, boolean drawBiomes, boolean drawRivers, boolean drawSites, boolean drawCorners, boolean drawDelaunay) {
        drawRegions(g, drawBiomes);

        if (drawDelaunay) {
            drawDelaunay(g);
        }

        if (drawRivers) {
            drawRivers(g);
        }

        if (drawSites) {
            drawSites(g);
        }

        if (drawCorners) {
            drawCorners(g);
        }

        drawBounds(g);
    }

    public void drawRegions(Graphics2D g, boolean drawBiomes) {
        List<Center> centers = graph.getCenters();

        int index = 0;
        for (final Center c : centers) {
            Color color;
            if (drawBiomes) {
                Biome biome = biomeModel.getBiome(c);
                color =  biomeColors.get(biome);
            } else {
                color = defaultColors[index++];
            }
            
            g.setColor(color);

            List<Corner> pts = new ArrayList<>(c.corners);
            Collections.sort(pts, new AngleSorter(c));

            drawPoly(g, pts);
        }
    }

    public void drawDelaunay(Graphics2D g) {
        for (Edge e : graph.getEdges()) {
            g.setStroke(new BasicStroke(1));
            g.setColor(Color.YELLOW);
            g.drawLine((int) e.d0.getPos().getX(), (int) e.d0.getPos().getY(), (int) e.d1.getPos().getX(), (int) e.d1.getPos().getY());
        }
    }

    public void drawRivers(Graphics2D g) {
        for (Edge e : graph.getEdges()) {
            if (e.getRiverValue() > 0) {
                g.setStroke(new BasicStroke(1 + (int) Math.sqrt(e.getRiverValue() * 2)));
                g.setColor(riverColor);
                g.drawLine((int) e.v0.loc.getX(), (int) e.v0.loc.getY(), (int) e.v1.loc.getX(), (int) e.v1.loc.getY());
            }
        }
    }

    public void drawSites(Graphics2D g) {
        List<Center> centers = graph.getCenters();

        g.setColor(Color.BLACK);
        for (Center s : centers) {
            g.fillOval((int) (s.getPos().getX() - 2), (int) (s.getPos().getY() - 2), 4, 4);
        }
    }

    public void drawCorners(Graphics2D g) {
        g.setColor(Color.WHITE);
        for (Corner c : graph.getCorners()) {
            g.fillOval((int) (c.loc.getX() - 2), (int) (c.loc.getY() - 2), 4, 4);
        }
    }

    public void drawBounds(Graphics2D g) {
        Rect2d bounds = graph.getBounds();
        g.setColor(Color.BLACK);
        g.drawRect((int) bounds.minX(), (int) bounds.minY(), (int) bounds.width(), (int) bounds.height());
    }


    private void drawPoly(Graphics2D g, List<Corner> pts) {
        int[] x = new int[pts.size()];
        int[] y = new int[pts.size()];

        for (int i = 0; i < pts.size(); i++) {
            x[i] = (int) pts.get(i).loc.getX();
            y[i] = (int) pts.get(i).loc.getY();
        }

        g.fillPolygon(x, y, pts.size());
    }

    /**
     * @param map
     */
    public void setBiomeColors(Map<Biome, Color> map) {
        this.biomeColors = map;
    }

    /**
     * @param color
     */
    public void setRiverColor(Color color) {
        this.riverColor = color;
    }
}
