// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.polyworld.debug;

import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Rect2f;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2f;
import org.terasology.polyworld.TriangleLookup;
import org.terasology.polyworld.graph.Triangle;
import org.terasology.polyworld.graph.VoronoiGraph;
import org.terasology.polyworld.math.delaunay.Voronoi;

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

        Rect2i bounds = Rect2i.createFromMinAndSize(0, 0, width, height);
        Rect2f doubleBounds = Rect2f.createFromMinAndSize(bounds.minX(), bounds.minY(), bounds.width(),
                bounds.height());

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