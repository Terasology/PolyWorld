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
 * Edge.java
 *
 * @author Connor
 */
public class Edge {

    public Region d0;  // Delaunay edge
    public Region d1;  // Delaunay edge
    
    public Corner v0;  // Voronoi edge
    public Corner v1;  // Voronoi edge
    
    private int river;

    public void setVornoi(Corner nv0, Corner nv1) {
        this.v0 = nv0;
        this.v1 = nv1;
    }

    /**
     * @return the river
     */
    public int getRiverValue() {
        return river;
    }

    /**
     * @param river the river to set
     */
    public void setRiverValue(int riverVal) {
        this.river = riverVal;
    }
}
