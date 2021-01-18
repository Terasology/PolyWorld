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
import java.awt.image.DataBufferInt;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.joml.Vector2fc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.BaseVector2f;
import org.terasology.polyworld.graph.Graph;
import org.terasology.polyworld.graph.GraphRegion;
import org.terasology.polyworld.graph.Triangle;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import org.terasology.world.block.BlockArea;
import org.terasology.world.block.BlockAreac;

/**
 * Creates a image-based lookup table to map individual pixel to triangles
 * of the regions in a {@link Graph}
 */
public class TriangleLookup {

    private static final Logger logger = LoggerFactory.getLogger(TriangleLookup.class);

    private final BufferedImage image;
    private final DataBufferInt dataBuffer;

    // TODO: consider not storing this explicitly -> O(1) -> O(log n)
    //       due to binary search in region-triangle start index list
    private final List<Triangle> triangles;

    private final BlockArea bounds = new BlockArea(BlockArea.INVALID);

    /**
     * Creates a lookup image for the graph's region triangles
     */
    public TriangleLookup(Graph graph) {

        bounds.set(graph.getBounds());

        // TODO: maybe use USHORT_GRAY instead
        image = new BufferedImage(bounds.getSizeX(), bounds.getSizeY(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.translate(-bounds.minX(), -bounds.minY());

        try {
            Stopwatch sw = Stopwatch.createStarted();
            triangles = drawTriangles(g, graph);
            logger.debug("Cached {} triangle lookups in {}ms.", triangles.size(), sw.elapsed(TimeUnit.MILLISECONDS));
        } finally {
            g.dispose();
        }

        dataBuffer = (DataBufferInt) image.getRaster().getDataBuffer();
    }

    /**
     * @param x the x world coord.
     * @param y the y world coord.
     * @return the triangle that contains the point or <code>null</code>
     */
    public Triangle findTriangleAt(int x, int y) {
        int imgX = x - bounds.minX();
        int imgY = y - bounds.minY();

        if (imgX < 0 || imgY < 0 || imgX >= image.getWidth() || imgY >= image.getHeight()) {
            logger.warn("Coordinate {}/{} is out of bounds", x, y);
            return null;
        }

        // index 0 is reserved for missing coverage
        // we need to subtract 1 to get the real list index
        int index1 = dataBuffer.getElem(imgY * image.getWidth() + imgX) & 0xFFFFFF;
        if (index1 < 1 || index1 > triangles.size()) {
            logger.warn("Could not find a triangle for {}/{}", x, y);
            return null;
        }

        return triangles.get(index1 - 1);
    }

    private static List<Triangle> drawTriangles(Graphics2D g, Graph graph) {
        List<GraphRegion> regions = graph.getRegions();
        List<Triangle> triangles = Lists.newArrayList();

        // index 0 is reserved for missing coverage
        int index = 1;
        for (final GraphRegion reg : regions) {
            for (Triangle tri : reg.computeTriangles()) {
                Vector2fc p0 = tri.getRegion().getCenter();
                Vector2fc p1 = tri.getCorner1().getLocation();
                Vector2fc p2 = tri.getCorner2().getLocation();

                Path2D path = new Path2D.Double();
                path.moveTo(p0.x(), p0.y());
                path.lineTo(p1.x(), p1.y());
                path.lineTo(p2.x(), p2.y());

                triangles.add(tri);

                g.setColor(new Color(index++));
                g.fill(path);
            }
        }

        return triangles;
    }

    /**
     * @return
     */
    public BlockAreac getBounds() {
        return bounds;
    }
}
