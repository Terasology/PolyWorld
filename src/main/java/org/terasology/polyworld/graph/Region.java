// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.polyworld.graph;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.terasology.math.geom.ImmutableVector2f;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Defines a polygon region (vornoi region)
 */
public class Region {

    private final Collection<Corner> corners;
    private final Collection<Edge> borders;
    private final Collection<Region> neighbors;

    private final ImmutableVector2f center;

    /**
     * @param centerPos the center of the region
     */
    public Region(ImmutableVector2f centerPos) {
        this.center = centerPos;
        this.corners = Sets.newTreeSet(new AngleOrdering(centerPos));
        this.borders = Sets.newLinkedHashSet();
        this.neighbors = Sets.newLinkedHashSet();
    }

    /**
     * @return the pos
     */
    public ImmutableVector2f getCenter() {
        return center;
    }

    /**
     * @return an unmodifiable collection of all neighbors in insertion order
     */
    public Collection<Region> getNeighbors() {
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
    public void addNeigbor(Region region) {
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
    public void addCorner(Corner c) {
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
