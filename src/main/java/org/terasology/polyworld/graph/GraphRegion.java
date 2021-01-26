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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.joml.Vector2f;
import org.joml.Vector2fc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Defines a polygon region (vornoi region)
 */
public class GraphRegion {

    private final Collection<Corner> corners;
    private final Collection<Edge> borders;
    private final Collection<GraphRegion> neighbors;

    private final Vector2f center = new Vector2f();

    /**
     * @param centerPos the center of the region
     */
    public GraphRegion(Vector2fc centerPos) {
        this.center.set(centerPos);
        this.corners = Sets.newTreeSet(new AngleOrdering(centerPos));
        this.borders = Sets.newLinkedHashSet();
        this.neighbors = Sets.newLinkedHashSet();
    }

    /**
     * @return the pos
     */
    public Vector2fc getCenter() {
        return center;
    }

    /**
     * @return an unmodifiable collection of all neighbors in insertion order
     */
    public Collection<GraphRegion> getNeighbors() {
        return Collections.unmodifiableCollection(neighbors);
    }

    /**
     * @return an unmodifiable collection of all border edges in insertion order
     */
    public Collection<Edge> getBorders() {
        return Collections.unmodifiableCollection(borders);
    }

    /**
     * @param region the region to add (can be null or already added)
     */
    public void addNeigbor(GraphRegion region) {
        if (region != null) {
            neighbors.add(region);
        }
    }

    /**
     * @param edge the border edge to add  (can be null or already added)
     */
    public void addBorder(Edge edge) {
        if (edge != null) {
            borders.add(edge);
        }
    }

    /**
     * @return the corners, <b>sorted by angle</b> around the center point
     */
    public Collection<Corner> getCorners() {
        return Collections.unmodifiableCollection(corners);
    }

    /**
     * @param c the corner to add (can be null or already added)
     */
    public void addCorner(Corner c)  {
        if (c != null) {
            corners.add(c);
        }
    }

    /**
     * @return a new list that contains all triangles of this polygon
     */
    public List<Triangle> computeTriangles() {
        List<Triangle> list = Lists.newArrayList();
        if (corners.isEmpty()) {
            return list;
        }

        Corner first = corners.iterator().next();
        Corner prev = null;

        for (Corner c : corners) {
            if (prev != null) {
                Triangle tri = new Triangle(this, prev, c);
                list.add(tri);
            }
            prev = c;
        }

        list.add(new Triangle(this, prev, first));

        return list;
    }

    @Override
    public String toString() {
        return String.format("Region [%s]", center.toString());
    }


}
