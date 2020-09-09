// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.polyworld.graph;

import com.google.common.collect.Sets;
import org.terasology.math.geom.ImmutableVector2f;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * Corner.java
 */
public class Corner {

    private final Set<Region> touches = Sets.newLinkedHashSet();
    private final Set<Corner> adjacent = Sets.newLinkedHashSet();
    private final Set<Edge> protrudes = Sets.newLinkedHashSet();
    private ImmutableVector2f loc;
    private boolean border;

    /**
     * @param loc
     */
    public Corner(ImmutableVector2f loc) {
        setLocation(loc);
    }

    /**
     * @return
     */
    public Collection<Corner> getAdjacent() {
        return Collections.unmodifiableCollection(adjacent);
    }

    /**
     * @param c a corner (can be in the set already). <code>null</code> values are ignored
     */
    public void addAdjacent(Corner c) {
        if (c != null) {
            adjacent.add(c);
        }
    }

    /**
     * @param region the touching region to add (can be null or already added)
     */
    public void addTouches(Region region) {
        if (region != null) {
            touches.add(region);
        }
    }

    /**
     * @return the border
     */
    public boolean isBorder() {
        return border;
    }

    /**
     * @param border the border to set
     */
    public void setBorder(boolean border) {
        this.border = border;
    }

    /**
     * @return the touches
     */
    public Collection<Region> getTouches() {
        return Collections.unmodifiableSet(touches);
    }

    /**
     * @return the loc
     */
    public ImmutableVector2f getLocation() {
        return loc;
    }

    /**
     * @param nloc the loc to set
     */
    public void setLocation(ImmutableVector2f nloc) {
        this.loc = nloc;
    }

    /**
     * @return the protrudes
     */
    public Collection<Edge> getEdges() {
        return Collections.unmodifiableSet(protrudes);
    }

    /**
     * @param edge the protruding edge to add (can be null or already added)
     */
    public void addEdge(Edge edge) {
        if (edge != null) {
            protrudes.add(edge);
        }
    }

    @Override
    public String toString() {
        String borderStr = border ? " (border)" : "";
        String format = "Corner [%s%s]";
        return String.format(format, loc, borderStr, touches.size(), protrudes.size(), adjacent.size());
    }
}
