/*
 * Copyright 2015 MovingBlocks
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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.terasology.math.geom.Rect2i;
import org.terasology.math.delaunay.Voronoi;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Rect2f;
import org.terasology.math.geom.Vector2f;
import org.terasology.math.geom.Vector2i;
import org.terasology.polyworld.TriangleLookup;
import org.terasology.polyworld.graph.Graph;
import org.terasology.polyworld.graph.GraphRegion;
import org.terasology.polyworld.graph.Triangle;
import org.terasology.polyworld.graph.VoronoiGraph;
import org.terasology.polyworld.sampling.PointSampling;
import org.terasology.polyworld.sampling.PoissonDiscSampling;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * Preview generated world in Swing
 */
final class SwingPreview {

    private SwingPreview() {

    }

    public static void main(String[] args) {
        final int width = 512;
        final int height = 256;
        final long seed = "asdf".hashCode(); //System.nanoTime();

        Rect2i bounds = Rect2i.createFromMinAndSize(0, 0, width, height);
        Rect2f doubleBounds = Rect2f.createFromMinAndSize(bounds.minX(), bounds.minY(), bounds.width(), bounds.height());

//        PointSampling sampling = new PoissonDiscSampling();
//        int numSites = 1000;
//        Random rng = new FastRandom(seed);
//        List<Vector2f> points = sampling.create(doubleBounds, numSites, rng);
        List<Vector2f> points = Arrays.asList(
                new Vector2f(128, 64), new Vector2f(384, 96),
                new Vector2f(224, 72), new Vector2f(256, 192),
                new Vector2f(128, 192), new Vector2f(384, 224));

        Voronoi v = new Voronoi(points, doubleBounds);
        VoronoiGraph graph = new VoronoiGraph(bounds, v);

        final GraphPainter graphPainter = new GraphPainter();
        TriangleLookup lookup = new TriangleLookup(graph);

        JFrame frame = new JFrame();
        JComponent panel = new JComponent() {

            private static final long serialVersionUID = 4178713176841691478L;

            @Override
            public void paint(Graphics g1) {
                Graphics2D g = (Graphics2D) g1;
                g.translate(10, 10);
                g.scale(2, 2);

                graphPainter.fillBounds(g, graph);
//                graphPainter.drawTriangles(g, graph);
                for (BaseVector2i l : bounds.contents()) {
                    Triangle tri = lookup.findTriangleAt(l.getX(), l.getY());
                    Color color;
                    if (tri == null) {
                        color = Color.MAGENTA;
                    } else {
                        color = new Color(0x7F7F7F & tri.hashCode());
                    }
                    g.setColor(color);
                    g.drawLine(l.getX(), l.getY(), l.getX(), l.getY());
                }

//                graphPainter.drawSites(g, graph);
                graphPainter.drawEdges(g, graph);
//                graphPainter.drawCorners(g, graph);
                graphPainter.drawBounds(g, graph);
            }
        };

        frame.add(panel);
        frame.setTitle("Preview - " + seed);
        frame.setVisible(true);
        frame.setSize(2 * width + 40, 2 * height + 60);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}