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

package org.terasology.polyworld;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.math.geom.Vector2d;
import org.terasology.polyworld.voronoi.Graph;
import org.terasology.polyworld.voronoi.Region;
import org.terasology.polyworld.voronoi.Triangle;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.math.DoubleMath;

/**
 * Creates a image-based lookup table to map individual pixel to triangles
 * of the regions in a {@link Graph}
 * @author Martin Steiger
 */
public class TriangleLookup {

    private static final Logger logger = LoggerFactory.getLogger(TriangleLookup.class);

    private final BufferedImage image;
    private final int offsetX;
    private final int offsetY;

    // TODO: consider not storing this explicitly -> O(1) -> O(log n)
    //       due to binary search in region-triangle start index list
    private final List<Triangle> triangles;

    /**
     * Creates a lookup image for the graph's region triangles
     */
    public TriangleLookup(Graph graph) {
        int width = DoubleMath.roundToInt(graph.getBounds().width(), RoundingMode.FLOOR);
        int height = DoubleMath.roundToInt(graph.getBounds().height(), RoundingMode.FLOOR);
        offsetX = DoubleMath.roundToInt(graph.getBounds().minX(), RoundingMode.FLOOR);
        offsetY = DoubleMath.roundToInt(graph.getBounds().minY(), RoundingMode.FLOOR);

        // TODO: maybe use USHORT_GRAY instead
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.translate(-offsetX, -offsetY);

        try {
            Stopwatch sw = Stopwatch.createStarted();
            triangles = drawTriangles(g, graph);
            logger.info("Cached {} triangle lookups in {}ms.", triangles.size(), sw.elapsed(TimeUnit.MILLISECONDS));
        } finally {
            g.dispose();
        }
    }

    /**
     * @param x the x world coord.
     * @param y the y world coord.
     * @return the triangle that contains the point or <code>null</code>
     */
    public Triangle findTriangleAt(int x, int y) {
        int imgX = x - offsetX;
        int imgY = y - offsetY;

        if (imgX < 0 || imgY < 0 || imgX > image.getWidth() || imgY > image.getHeight()) {
            logger.debug("Coordinate {}/{} is out of bounds", x, y);
            return null;
        }

        int index = image.getRGB(imgX, imgY) & 0xFFFFFF;
        if (index < 0 || index >= triangles.size()) {
            logger.debug("Could not find a triangle for {}/{}", x, y);
            return null;
        }

        return triangles.get(index);
    }

    private static List<Triangle> drawTriangles(Graphics2D g, Graph graph) {
        List<Region> regions = graph.getRegions();
        List<Triangle> triangles = Lists.newArrayList();

        int index = 0;
        for (final Region reg : regions) {
            for (Triangle tri : reg.computeTriangles()) {
                Vector2d p0 = tri.getRegion().getCenter();
                Vector2d p1 = tri.getCorner1().getLocation();
                Vector2d p2 = tri.getCorner2().getLocation();

                Path2D path = new Path2D.Double();
                path.moveTo(p0.getX(), p0.getY());
                path.lineTo(p1.getX(), p1.getY());
                path.lineTo(p2.getX(), p2.getY());

                triangles.add(tri);

                g.setColor(new Color(index++));
                g.fill(path);
            }
        }

        return triangles;
    }
}
