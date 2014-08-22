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

package org.terasology.polyworld.voronoi;


/**
 * Defines an edge
 * @author Martin Steiger
 */
public class Edge {

    private Region d0;  // Delaunay edge
    private Region d1;  // Delaunay edge

    private Corner v0;  // Voronoi edge
    private Corner v1;  // Voronoi edge

    /**
     * @param c0
     * @param c1
     * @param r1
     * @param r0
     */
    public Edge(Corner c0, Corner c1, Region r0, Region r1) {
        this.v0 = c0;
        this.v1 = c1;
        this.d0 = r0;
        this.d1 = r1;
    }

    /**
     * @return the v0
     */
    public Corner getCorner0() {
        return v0;
    }

    /**
     * @return the v1
     */
    public Corner getCorner1() {
        return v1;
    }

    /**
     * @return the d1
     */
    public Region getRegion1() {
        return d1;
    }

    /**
     * @return the d0
     */
    public Region getRegion0() {
        return d0;
    }
}
