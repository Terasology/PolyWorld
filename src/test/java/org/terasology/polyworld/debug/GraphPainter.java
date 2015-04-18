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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.terasology.math.Rect2i;
import org.terasology.math.geom.BaseVector2f;
import org.terasology.math.geom.Vector2f;
import org.terasology.polyworld.graph.Corner;
import org.terasology.polyworld.graph.Edge;
import org.terasology.polyworld.graph.Graph;
import org.terasology.polyworld.graph.Region;
import org.terasology.polyworld.graph.Triangle;

import com.google.common.base.Function;
import com.google.common.math.DoubleMath;

/**
 * Draws the generated graph on a AWT graphics instance
 * @author Martin Steiger
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
            Vector2f r0c = e.getRegion0().getCenter();
            Vector2f r1c = e.getRegion1().getCenter();
            g.drawLine((int) r0c.getX(), (int) r0c.getY(), (int) r1c.getX(), (int) r1c.getY());
        }
    }

    public void drawEdges(Graphics2D g, Graph graph) {
        g.setStroke(new BasicStroke(0));
        g.setColor(Color.CYAN);
        for (Edge e : graph.getEdges()) {
            BaseVector2f r0c = e.getCorner0().getLocation();
            BaseVector2f r1c = e.getCorner1().getLocation();
            g.drawLine((int) r0c.getX(), (int) r0c.getY(), (int) r1c.getX(), (int) r1c.getY());
        }
    }

    public void drawPolys(Graphics2D g, Graph graph, Function<Region, Color> colorFunc) {
        List<Region> regions = graph.getRegions();

        for (final Region reg : regions) {

            Color col = colorFunc.apply(reg);
            Collection<Corner> pts = reg.getCorners();

            int[] xPoints = new int[pts.size()];
            int[] yPoints = new int[pts.size()];

            int i = 0;
            for (Corner corner : pts) {
                xPoints[i] = (int) corner.getLocation().getX();
                yPoints[i] = (int) corner.getLocation().getY();
                i++;
            }

            g.setColor(col);
            g.fillPolygon(xPoints, yPoints, pts.size());
        }
    }

    public void drawTriangles(Graphics2D g, Graph graph) {
        List<Region> regions = graph.getRegions();

        Random r = new Random(12332434);

        for (final Region reg : regions) {
            for (Triangle tri : reg.computeTriangles()) {
                g.setColor(new Color(r.nextInt(0xFFFFFF)));
                BaseVector2f p0 = tri.getRegion().getCenter();
                BaseVector2f p1 = tri.getCorner1().getLocation();
                BaseVector2f p2 = tri.getCorner2().getLocation();

                Path2D path = new Path2D.Double();
                path.moveTo(p0.getX(), p0.getY());
                path.lineTo(p1.getX(), p1.getY());
                path.lineTo(p2.getX(), p2.getY());

                g.fill(path);
            }
        }
    }

    public void drawSites(Graphics2D g, Graph graph) {
        List<Region> centers = graph.getRegions();

        g.setColor(Color.BLACK);
        for (Region s : centers) {
            g.fillOval((int) (s.getCenter().getX() - 2), (int) (s.getCenter().getY() - 2), 4, 4);
        }
    }

    public void drawCorners(Graphics2D g, Graph graph) {
        g.setColor(Color.WHITE);
        for (Corner c : graph.getCorners()) {
            g.fillOval((int) (c.getLocation().getX() - 2), (int) (c.getLocation().getY() - 2), 4, 4);
        }
    }

    public void drawBounds(Graphics2D g, Graph graph) {
        Rect2i bounds = graph.getBounds();
        g.setColor(Color.BLACK);
        g.drawRect(bounds.minX(), bounds.minY(), bounds.width(), bounds.height());
    }

    public void fillBounds(Graphics2D g, Graph graph) {
        Rect2i bounds = graph.getBounds();
        g.setColor(Color.MAGENTA);
        g.fillRect(bounds.minX() + 1, bounds.minY() + 1, bounds.width() - 1, bounds.height() - 1);
    }

}
