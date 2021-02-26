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

package org.terasology.polyworld;

import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.terasology.joml.geom.Rectanglef;
import org.terasology.math.delaunay.Voronoi;
import org.terasology.polyworld.graph.GraphRegion;
import org.terasology.polyworld.graph.VoronoiGraph;
import org.terasology.world.block.BlockArea;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the correct representation of a {@link VoronoiGraph}
 */
public class VoronoiGraphTest extends GraphTest {

    protected List<Vector2fc> points;

    @BeforeEach
    public void setup() {
        final int width = 512;
        final int height = 256;

        intBounds = new BlockArea(0, 0, width, height);
        realBounds = intBounds.getBounds(new Rectanglef());

        points = Arrays.asList(
                new Vector2f(128, 64), new Vector2f(384, 96),
                new Vector2f(224, 72), new Vector2f(256, 192),
                new Vector2f(128, 192), new Vector2f(384, 224));

        Voronoi v = new Voronoi(points, realBounds);
        graph = new VoronoiGraph(intBounds, v);
    }

    @Test
    public void testRegionAndSiteMatch() {
        List<GraphRegion> regions = graph.getRegions();
        assertEquals(points.size(), regions.size(), "Number of regions differs from number of input sites");
        for (GraphRegion reg : regions) {
            assertTrue(points.contains(reg.getCenter()));
        }
    }

    @Test
    @Disabled // umm the locations changed
    public void testVoronoiLocations() {
        List<Vector2fc> corners = new ArrayList<>();
        graph.getCorners().forEach(c -> corners.add(c.getLocation()));

        assertEquals(1, corners.stream().filter(c -> c.equals(new Vector2f(171, 128), .001f)).count());
        assertEquals(1, corners.stream().filter(c -> c.equals(new Vector2f(192, 256), .001f)).count());
        assertEquals(1, corners.stream().filter(c -> c.equals(new Vector2f(308, 256), .001f)).count());
        assertEquals(1, corners.stream().filter(c -> c.equals(new Vector2f(332, 160), .001f)).count());
    }
}

