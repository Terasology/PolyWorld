// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.polyworld;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.terasology.math.geom.BaseVector2f;
import org.terasology.math.geom.ImmutableVector2f;
import org.terasology.math.geom.Rect2f;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2f;
import org.terasology.polyworld.graph.Region;
import org.terasology.polyworld.graph.VoronoiGraph;
import org.terasology.polyworld.math.delaunay.Voronoi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Tests the correct representation of a {@link VoronoiGraph}
 */
public class VoronoiGraphTest extends GraphTest {

    protected List<Vector2f> points;

    @Before
    public void setup() {
        final int width = 512;
        final int height = 256;

        intBounds = Rect2i.createFromMinAndSize(0, 0, width, height);
        realBounds = Rect2f.createFromMinAndSize(intBounds.minX(), intBounds.minY(), intBounds.width(),
                intBounds.height());

        points = Arrays.asList(
                new Vector2f(128, 64), new Vector2f(384, 96),
                new Vector2f(224, 72), new Vector2f(256, 192),
                new Vector2f(128, 192), new Vector2f(384, 224));

        Voronoi v = new Voronoi(points, realBounds);
        graph = new VoronoiGraph(intBounds, v);
    }

    @Test
    public void testRegionAndSiteMatch() {
        List<Region> regions = graph.getRegions();
        Assert.assertEquals("Number of regions differs from number of input sites", points.size(), regions.size());
        for (Region reg : regions) {
            Assert.assertTrue(points.contains(reg.getCenter()));
        }
    }

    @Test
    public void testVoronoiLocations() {
        List<BaseVector2f> corners = new ArrayList<>();
        graph.getCorners().forEach(c -> corners.add(c.getLocation()));

        // check location of all corners that are inside
        Assert.assertTrue(corners.contains(new ImmutableVector2f(171, 128)));
        Assert.assertTrue(corners.contains(new ImmutableVector2f(192, 256)));
        Assert.assertTrue(corners.contains(new ImmutableVector2f(308, 256)));
        Assert.assertTrue(corners.contains(new ImmutableVector2f(332, 160)));
    }
}

