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

package org.terasology.polyworld.debug;

import com.google.common.base.Function;
import org.joml.Vector2fc;
import org.terasology.polyworld.graph.Corner;
import org.terasology.polyworld.graph.Edge;
import org.terasology.polyworld.graph.Graph;
import org.terasology.polyworld.graph.GraphRegion;
import org.terasology.polyworld.graph.Triangle;
import org.terasology.world.block.BlockAreac;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Draws the generated graph on a AWT graphics instance
 */
public class GraphPainter {

    /**
     * @param graph
     */
    public GraphPainter() {
    }

    public void drawDelaunay(Graphics2D g, Graph graph) {
        g.setStroke(new BasicStroke(1));
        g.setColor(Color.ORANGE);
        for (Edge e : graph.getEdges()) {
            Vector2fc r0c = e.getRegion0().getCenter();
            Vector2fc r1c = e.getRegion1().getCenter();
            g.drawLine((int) r0c.x(), (int) r0c.y(), (int) r1c.x(), (int) r1c.y());
        }
    }

    public void drawEdges(Graphics2D g, Graph graph) {
        g.setStroke(new BasicStroke(0));
        g.setColor(Color.CYAN);
        for (Edge e : graph.getEdges()) {
            Vector2fc r0c = e.getCorner0().getLocation();
            Vector2fc r1c = e.getCorner1().getLocation();
            g.drawLine((int) r0c.x(), (int) r0c.y(), (int) r1c.x(), (int) r1c.y());
        }
    }

    public void drawPolys(Graphics2D g, Graph graph, Function<GraphRegion, Color> colorFunc) {
        List<GraphRegion> regions = graph.getRegions();

        for (final GraphRegion reg : regions) {

            Color col = colorFunc.apply(reg);
            Collection<Corner> pts = reg.getCorners();

            int[] xPoints = new int[pts.size()];
            int[] yPoints = new int[pts.size()];

            int i = 0;
            for (Corner corner : pts) {
                xPoints[i] = (int) corner.getLocation().x();
                yPoints[i] = (int) corner.getLocation().y();
                i++;
            }

            g.setColor(col);
            g.fillPolygon(xPoints, yPoints, pts.size());
        }
    }

    public void drawTriangles(Graphics2D g, Graph graph) {
        List<GraphRegion> regions = graph.getRegions();

        Random r = new Random(12332434);

        for (final GraphRegion reg : regions) {
            for (Triangle tri : reg.computeTriangles()) {
                g.setColor(new Color(r.nextInt(0xFFFFFF)));
                Vector2fc p0 = tri.getRegion().getCenter();
                Vector2fc p1 = tri.getCorner1().getLocation();
                Vector2fc p2 = tri.getCorner2().getLocation();

                Path2D path = new Path2D.Double();
                path.moveTo(p0.x(), p0.y());
                path.lineTo(p1.x(), p1.y());
                path.lineTo(p2.x(), p2.y());

                g.fill(path);
            }
        }
    }

    public void drawSites(Graphics2D g, Graph graph) {
        List<GraphRegion> centers = graph.getRegions();

        g.setColor(Color.BLACK);
        for (GraphRegion s : centers) {
            g.fillOval((int) (s.getCenter().x() - 2), (int) (s.getCenter().y() - 2), 4, 4);
        }
    }

    public void drawCorners(Graphics2D g, Graph graph) {
        g.setColor(Color.WHITE);
        for (Corner c : graph.getCorners()) {
            g.fillOval((int) (c.getLocation().x() - 2), (int) (c.getLocation().y() - 2), 4, 4);
        }
    }

    public void drawBounds(Graphics2D g, Graph graph) {
        BlockAreac bounds = graph.getBounds();
        g.setColor(Color.BLACK);
        g.drawRect(bounds.minX(), bounds.minY(), bounds.getSizeX(), bounds.getSizeY());
    }

    public void fillBounds(Graphics2D g, Graph graph) {
        BlockAreac bounds = graph.getBounds();
        g.setColor(Color.MAGENTA);
        g.fillRect(bounds.minX() + 1, bounds.minY() + 1, bounds.getSizeX() - 1, bounds.getSizeY() - 1);
    }

}
