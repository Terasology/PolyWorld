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
import org.junit.jupiter.api.Test;
import org.terasology.engine.world.block.BlockArea;
import org.terasology.joml.geom.Rectanglef;
import org.terasology.polyworld.graph.Corner;
import org.terasology.polyworld.graph.Edge;
import org.terasology.polyworld.graph.Graph;
import org.terasology.polyworld.graph.GraphRegion;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
/**
 * Tests the correct representation of a {@link Graph}
 */
public abstract class GraphTest {

    protected Graph graph;
    protected BlockArea intBounds;
    protected Rectanglef realBounds;

    @Test
    public void testGraphCornerLocations() {

        List<Vector2fc> corners = new ArrayList<>();
        graph.getCorners().forEach(c -> corners.add(c.getLocation()));

        // check location of all border corners

        assertEquals(1, corners.stream().filter(c -> c.equals(new Vector2f(realBounds.minX, realBounds.minY), .001f)).count());
        assertEquals(1, corners.stream().filter(c -> c.equals(new Vector2f(realBounds.minX, realBounds.maxY), .001f)).count());
        assertEquals(1, corners.stream().filter(c -> c.equals(new Vector2f(realBounds.maxX, realBounds.minY), .001f)).count());
        assertEquals(1, corners.stream().filter(c -> c.equals(new Vector2f(realBounds.maxX, realBounds.maxY), .001f)).count());
    }

    @Test
    public void testGraphEdgesExist() {

        for (Edge e : graph.getEdges()) {
            assertTrue(graph.getRegions().contains(e.getRegion0()));
            assertTrue(graph.getRegions().contains(e.getRegion1()));

            assertTrue(graph.getCorners().contains(e.getCorner0()));
            assertTrue(graph.getCorners().contains(e.getCorner1()));
        }
    }

    @Test
    public void testGraphRelations() {

        for (Edge e : graph.getEdges()) {
            GraphRegion r0 = e.getRegion0();
            GraphRegion r1 = e.getRegion1();

            Corner c0 = e.getCorner0();
            Corner c1 = e.getCorner1();

            assertTrue(r0.getCorners().contains(c0));
            assertTrue(r0.getCorners().contains(c1));

            assertTrue(r1.getCorners().contains(c0));
            assertTrue(r1.getCorners().contains(c1));

            assertTrue(c0.getTouches().contains(r0));
            assertTrue(c0.getTouches().contains(r1));

            assertTrue(c1.getTouches().contains(r0));
            assertTrue(c1.getTouches().contains(r1));

            assertTrue(c1.getAdjacent().contains(c0));
            assertTrue(c0.getAdjacent().contains(c1));

            assertTrue(r0.getNeighbors().contains(r1));
            assertTrue(r1.getNeighbors().contains(r0));
        }
    }

    @Test
    public void testGraphCornerRegions() {

        for (GraphRegion r : graph.getRegions()) {
            assertTrue(graph.getCorners().containsAll(r.getCorners()));
        }
    }

    @Test
    public void testGraphCornerBorderFlag() {
        float eps = 0.1f;
        for (Corner c : graph.getCorners()) {
            if ((Math.abs(c.getLocation().x() - realBounds.minX) < eps)
                    || (Math.abs(c.getLocation().x() - realBounds.maxX) < eps)
                    || (Math.abs(c.getLocation().y() - realBounds.minY) < eps)
                    || (Math.abs(c.getLocation().y() - realBounds.maxY) < eps)) {
                assertTrue(c.isBorder(), "Corner must have border flag: " + c);
            } else {
                assertFalse(c.isBorder(), "Corner must not have border flag: " + c);
            }
        }
    }

    @Test
    public void testGraphRegionNeighborsSet() {
        for (GraphRegion r : graph.getRegions()) {
            assertFalse(r.getNeighbors().isEmpty());
        }
    }

    @Test
    public void testGraphCornerTouchesSet() {
        for (Corner c : graph.getCorners()) {
            assertFalse(c.getTouches().isEmpty());
        }
    }

}

