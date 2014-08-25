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

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.terasology.math.geom.Vector2d;

import com.google.common.collect.Sets;

/**
 * Corner.java
 *
 * @author Connor
 */
public class Corner {

    private final Set<Region> touches = Sets.newLinkedHashSet();
    private final Set<Corner> adjacent = Sets.newLinkedHashSet();
    private final Set<Edge> protrudes = Sets.newLinkedHashSet();
    private Vector2d loc;
    private boolean border;

    /**
     * @param loc
     */
    public Corner(Vector2d loc) {
        this.loc = loc;
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
    public Vector2d getLocation() {
        return loc;
    }

    /**
     * @param loc the loc to set
     */
    public void setLocation(Vector2d loc) {
        this.loc = loc;
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
        String format = "Corner [%s%s, touches %d regions, protrudes %d edges, %d adjacent corners]";
        return String.format(format, loc, borderStr, touches.size(), protrudes.size(), adjacent.size());
    }
}
