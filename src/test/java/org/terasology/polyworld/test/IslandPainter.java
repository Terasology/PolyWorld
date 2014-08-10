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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.terasology.math.geom.Rect2d;
import org.terasology.math.geom.Vector2d;
import org.terasology.polyworld.voronoi.Biome;
import org.terasology.polyworld.voronoi.Center;
import org.terasology.polyworld.voronoi.Corner;
import org.terasology.polyworld.voronoi.Edge;
import org.terasology.polyworld.voronoi.VoronoiGraph;

/**
 * TODO Type description
 * @author Martin Steiger
 */
public class IslandPainter {

    private VoronoiGraph graph;
    private Map<Biome, Color> biomeColors;
    private Color riverColor;
    private Color[] defaultColors;


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
        
    }

    public void paint(Graphics2D g) {
        paint(g, true, true, false, false, false);
    }

    //also records the area of each voronoi cell
    public void paint(Graphics2D g, boolean drawBiomes, boolean drawRivers, boolean drawSites, boolean drawCorners, boolean drawDelaunay) {
        List<Center> centers = graph.getCenters();
        final int numSites = centers.size();

        //draw via triangles
        for (final Center c : centers) {
            g.setColor(drawBiomes ? biomeColors.get(c.biome) : defaultColors[c.index]);

            List<Corner> pts = new ArrayList<>(c.corners);
            Collections.sort(pts, new Comparator<Corner>() {

                @Override
                public int compare(Corner o0, Corner o1) {
                    Vector2d a = new Vector2d(o0.loc).sub(c.loc).normalize();
                    Vector2d b = new Vector2d(o1.loc).sub(c.loc).normalize();

                    if (a.y() > 0) { //a between 0 and 180
                        if (b.y() < 0) {  //b between 180 and 360
                            return -1;
                        }
                        return a.x() < b.x() ? 1 : -1;
                    } else { // a between 180 and 360
                        if (b.y() > 0) { //b between 0 and 180
                            return 1;
                        }
                        return a.x() > b.x() ? 1 : -1;
                    }
                }
            });
            
            drawPoly(g, pts);
        }

        for (Edge e : graph.getEdges()) {
            if (drawDelaunay) {
                g.setStroke(new BasicStroke(1));
                g.setColor(Color.YELLOW);
                g.drawLine((int) e.d0.loc.getX(), (int) e.d0.loc.getY(), (int) e.d1.loc.getX(), (int) e.d1.loc.getY());
            }
            if (drawRivers && e.river > 0) {
                g.setStroke(new BasicStroke(1 + (int) Math.sqrt(e.river * 2)));
                g.setColor(riverColor);
                g.drawLine((int) e.v0.loc.getX(), (int) e.v0.loc.getY(), (int) e.v1.loc.getX(), (int) e.v1.loc.getY());
            }
        }

        if (drawSites) {
            g.setColor(Color.BLACK);
            for (Center s : centers) {
                g.fillOval((int) (s.loc.getX() - 2), (int) (s.loc.getY() - 2), 4, 4);
            }
        }

        if (drawCorners) {
            g.setColor(Color.WHITE);
            for (Corner c : graph.getCorners()) {
                g.fillOval((int) (c.loc.getX() - 2), (int) (c.loc.getY() - 2), 4, 4);
            }
        }
        
        Rect2d bounds = graph.getBounds();
        g.setColor(Color.WHITE);
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
