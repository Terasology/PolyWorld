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

import org.terasology.math.geom.Vector2d;

/**
 * Edge.java
 *
 * @author Connor
 */
public class Edge {

    public int index;
    public Center d0;  // Delaunay edge
    public Center d1;  // Delaunay edge
    
    public Corner v0;  // Voronoi edge
    public Corner v1;  // Voronoi edge
    public Vector2d midpoint;  // halfway between v0,v1
    public int river;

    public void setVornoi(Corner nv0, Corner nv1) {
        this.v0 = nv0;
        this.v1 = nv1;
        midpoint = new Vector2d((v0.loc.getX() + v1.loc.getX()) / 2, (v0.loc.getY() + v1.loc.getY()) / 2);
    }
}
