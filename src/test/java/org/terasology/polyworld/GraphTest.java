// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.polyworld;

import org.junit.Assert;
import org.junit.Test;
import org.terasology.math.geom.BaseVector2f;
import org.terasology.math.geom.ImmutableVector2f;
import org.terasology.math.geom.Rect2f;
import org.terasology.math.geom.Rect2i;
import org.terasology.polyworld.graph.Corner;
import org.terasology.polyworld.graph.Edge;
import org.terasology.polyworld.graph.Graph;
import org.terasology.polyworld.graph.Region;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests the correct representation of a {@link Graph}
 */
public abstract class GraphTest {

    protected Graph graph;
    protected Rect2i intBounds;
    protected Rect2f realBounds;

    @Test
    public void testGraphCornerLocations() {

        List<BaseVector2f> corners = new ArrayList<>();
        graph.getCorners().forEach(c -> corners.add(c.getLocation()));

        // check location of all border corners
        Assert.assertTrue(corners.contains(new ImmutableVector2f(realBounds.minX(), realBounds.minY())));
        Assert.assertTrue(corners.contains(new ImmutableVector2f(realBounds.minX(), realBounds.maxY())));
        Assert.assertTrue(corners.contains(new ImmutableVector2f(realBounds.maxX(), realBounds.minY())));
        Assert.assertTrue(corners.contains(new ImmutableVector2f(realBounds.maxX(), realBounds.maxY())));
    }

    @Test
    public void testGraphEdgesExist() {

        for (Edge e : graph.getEdges()) {
            Assert.assertTrue(graph.getRegions().contains(e.getRegion0()));
            Assert.assertTrue(graph.getRegions().contains(e.getRegion1()));

            Assert.assertTrue(graph.getCorners().contains(e.getCorner0()));
            Assert.assertTrue(graph.getCorners().contains(e.getCorner1()));
        }
    }

    @Test
    public void testGraphRelations() {

        for (Edge e : graph.getEdges()) {
            Region r0 = e.getRegion0();
            Region r1 = e.getRegion1();

            Corner c0 = e.getCorner0();
            Corner c1 = e.getCorner1();

            Assert.assertTrue(r0.getCorners().contains(c0));
            Assert.assertTrue(r0.getCorners().contains(c1));

            Assert.assertTrue(r1.getCorners().contains(c0));
            Assert.assertTrue(r1.getCorners().contains(c1));

            Assert.assertTrue(c0.getTouches().contains(r0));
            Assert.assertTrue(c0.getTouches().contains(r1));

            Assert.assertTrue(c1.getTouches().contains(r0));
            Assert.assertTrue(c1.getTouches().contains(r1));

            Assert.assertTrue(c1.getAdjacent().contains(c0));
            Assert.assertTrue(c0.getAdjacent().contains(c1));

            Assert.assertTrue(r0.getNeighbors().contains(r1));
            Assert.assertTrue(r1.getNeighbors().contains(r0));
        }
    }

    @Test
    public void testGraphCornerRegions() {

        for (Region r : graph.getRegions()) {
            Assert.assertTrue(graph.getCorners().containsAll(r.getCorners()));
        }
    }

    @Test
    public void testGraphCornerBorderFlag() {
        float eps = 0.1f;
        for (Corner c : graph.getCorners()) {
            if ((Math.abs(c.getLocation().getX() - realBounds.minX()) < eps)
                    || (Math.abs(c.getLocation().getX() - realBounds.maxX()) < eps)
                    || (Math.abs(c.getLocation().getY() - realBounds.minY()) < eps)
                    || (Math.abs(c.getLocation().getY() - realBounds.maxY()) < eps)) {
                Assert.assertTrue("Corner must have border flag: " + c, c.isBorder());
            } else {
                Assert.assertFalse("Corner must not have border flag: " + c, c.isBorder());
            }
        }
    }

    @Test
    public void testGraphRegionNeighborsSet() {
        for (Region r : graph.getRegions()) {
            Assert.assertFalse(r.getNeighbors().isEmpty());
        }
    }

    @Test
    public void testGraphCornerTouchesSet() {
        for (Corner c : graph.getCorners()) {
            Assert.assertFalse(c.getTouches().isEmpty());
        }
    }

}

