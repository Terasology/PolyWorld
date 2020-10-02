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

package org.terasology.polyworld.graph;

import com.google.common.base.Preconditions;


/**
 * Defines an edge
 */
public class Edge {

    private final GraphRegion r0;  // Delaunay edge
    private final GraphRegion r1;  // Delaunay edge

    private final Corner c0;  // Voronoi edge
    private final Corner c1;  // Voronoi edge

    /**
     * @param c0
     * @param c1
     * @param r1
     * @param r0
     */
    public Edge(Corner c0, Corner c1, GraphRegion r0, GraphRegion r1) {
        Preconditions.checkArgument(c0 != null);
        Preconditions.checkArgument(c1 != null);
        Preconditions.checkArgument(r0 != null);
        Preconditions.checkArgument(r1 != null);

        this.c0 = c0;
        this.c1 = c1;
        this.r0 = r0;
        this.r1 = r1;
    }

    /**
     * @return the v0
     */
    public Corner getCorner0() {
        return c0;
    }

    /**
     * @return the v1
     */
    public Corner getCorner1() {
        return c1;
    }

    /**
     * @return the d1
     */
    public GraphRegion getRegion1() {
        return r1;
    }

    /**
     * @return the d0
     */
    public GraphRegion getRegion0() {
        return r0;
    }

    @Override
    public String toString() {
        return String.format("Edge [%s -> %s]", c0.getLocation(), c1.getLocation());
    }
}
