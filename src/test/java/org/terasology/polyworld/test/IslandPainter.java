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
import org.terasology.math.geom.Vector2d;
import org.terasology.polyworld.biome.Biome;
import org.terasology.polyworld.biome.BiomeModel;
import org.terasology.polyworld.biome.DefaultBiomeModel;
import org.terasology.polyworld.elevation.DefaultElevationModel;
import org.terasology.polyworld.elevation.ElevationModel;
import org.terasology.polyworld.moisture.DefaultMoistureModel;
import org.terasology.polyworld.moisture.MoistureModel;
import org.terasology.polyworld.rivers.DefaultRiverModel;
import org.terasology.polyworld.rivers.RiverModel;
import org.terasology.polyworld.voronoi.Corner;
import org.terasology.polyworld.voronoi.Edge;
import org.terasology.polyworld.voronoi.Graph;
import org.terasology.polyworld.voronoi.Region;
import org.terasology.polyworld.water.DefaultWaterModel;
import org.terasology.polyworld.water.RadialWaterDistribution;
import org.terasology.polyworld.water.WaterModel;

/**
 * Draws the generated voronoi-based world on a AWT graphics instance
 * @author Martin Steiger
 */
public class IslandPainter {

    private final Graph graph;
    private Map<Biome, Color> biomeColors;
    private Color riverColor;
    private Color[] defaultColors;
    private BiomeModel biomeModel;
    private RiverModel riverModel;
    private MoistureModel moistureModel;

    /**
     * @param graph
     */
    public IslandPainter(Graph graph) {
        this.graph = graph;

        Random r = new Random(1254);
        int numSites = graph.getRegions().size();
        defaultColors = new Color[numSites];
        for (int i = 0; i < defaultColors.length; i++) {
            defaultColors[i] = new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255));
        }

        RadialWaterDistribution waterDist = new RadialWaterDistribution(graph.getBounds());
        WaterModel waterModel = new DefaultWaterModel(graph, waterDist);
        ElevationModel elevationModel = new DefaultElevationModel(graph, waterModel);
        riverModel = new DefaultRiverModel(graph, elevationModel, waterModel);
        moistureModel = new DefaultMoistureModel(graph, riverModel, waterModel);
        biomeModel = new DefaultBiomeModel(elevationModel, waterModel, moistureModel);
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
        List<Region> centers = graph.getRegions();

        int index = 0;
        for (final Region c : centers) {
            Color color;
            if (drawBiomes) {
                Biome biome = biomeModel.getBiome(c);
                color =  biomeColors.get(biome);
            } else {
                color = defaultColors[index++];
            }

            g.setColor(color);

            List<Corner> pts = new ArrayList<>(c.getCorners());
            Collections.sort(pts, new AngleSorter(c));

            drawPoly(g, pts);
        }
    }

    public void drawDelaunay(Graphics2D g) {
        g.setStroke(new BasicStroke(1));
        g.setColor(Color.ORANGE);
        for (Edge e : graph.getEdges()) {
            Vector2d r0c = e.getRegion0().getCenter();
            Vector2d r1c = e.getRegion1().getCenter();
            g.drawLine((int) r0c.getX(), (int) r0c.getY(), (int) r1c.getX(), (int) r1c.getY());
        }
    }

    public void drawEdges(Graphics2D g) {
        g.setStroke(new BasicStroke(1));
        g.setColor(Color.CYAN);
        for (Edge e : graph.getEdges()) {
            if (e.getCorner0() != null && e.getCorner1() != null) {
                Vector2d r0c = e.getCorner0().getLocation();
                Vector2d r1c = e.getCorner1().getLocation();
                g.drawLine((int) r0c.getX(), (int) r0c.getY(), (int) r1c.getX(), (int) r1c.getY());
            } else {
                System.out.println(e);
            }
        }
    }

    public void drawRivers(Graphics2D g) {
        for (Edge e : graph.getEdges()) {
            int riverValue = riverModel.getRiverValue(e);
            if (riverValue > 0) {
                g.setStroke(new BasicStroke(1 + (int) Math.sqrt(riverValue * 2)));
                g.setColor(riverColor);
                Vector2d c0p = e.getCorner0().getLocation();
                Vector2d c1p = e.getCorner1().getLocation();
                g.drawLine((int) c0p.getX(), (int) c0p.getY(), (int) c1p.getX(), (int) c1p.getY());
            }
        }
    }

    public void drawSites(Graphics2D g) {
        List<Region> centers = graph.getRegions();

        g.setColor(Color.BLACK);
        for (Region s : centers) {
            g.fillOval((int) (s.getCenter().getX() - 2), (int) (s.getCenter().getY() - 2), 4, 4);
        }
    }

    public void drawCorners(Graphics2D g) {
        g.setColor(Color.WHITE);
        for (Corner c : graph.getCorners()) {
            g.fillOval((int) (c.getLocation().getX() - 2), (int) (c.getLocation().getY() - 2), 4, 4);
        }
    }

    public void drawBounds(Graphics2D g) {
        Rect2d bounds = graph.getBounds();
        g.setColor(Color.BLACK);
        g.drawRect((int) bounds.minX(), (int) bounds.minY(), (int) bounds.width(), (int) bounds.height());
    }

    public void fillBounds(Graphics2D g) {
        Rect2d bounds = graph.getBounds();
        g.setColor(Color.MAGENTA);
        g.fillRect((int) bounds.minX() + 1, (int) bounds.minY() + 1, (int) bounds.width() - 1, (int) bounds.height() - 1);
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
