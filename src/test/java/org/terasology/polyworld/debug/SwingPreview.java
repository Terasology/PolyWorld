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

import org.joml.Rectanglef;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector2ic;
import org.terasology.math.delaunay.Voronoi;
import org.terasology.polyworld.TriangleLookup;
import org.terasology.polyworld.graph.Triangle;
import org.terasology.polyworld.graph.VoronoiGraph;
import org.terasology.world.block.BlockArea;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.List;

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

        BlockArea bounds = new BlockArea(0, 0, width, height);
        Rectanglef doubleBounds = bounds.getBounds(new Rectanglef());

//        PointSampling sampling = new PoissonDiscSampling();
//        int numSites = 1000;
//        Random rng = new FastRandom(seed);
//        List<Vector2f> points = sampling.create(doubleBounds, numSites, rng);
        List<Vector2fc> points = Arrays.asList(
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
                for (Vector2ic l : bounds) {
                    Triangle tri = lookup.findTriangleAt(l.x(), l.y());
                    Color color;
                    if (tri == null) {
                        color = Color.MAGENTA;
                    } else {
                        color = new Color(0x7F7F7F & tri.hashCode());
                    }
                    g.setColor(color);
                    g.drawLine(l.x(), l.y(), l.x(), l.y());
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
