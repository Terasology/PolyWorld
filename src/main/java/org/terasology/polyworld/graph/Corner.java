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

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.terasology.math.geom.ImmutableVector2f;

import com.google.common.collect.Sets;

/**
 * Corner.java
 *
 */
public class Corner {

    private final Set<GraphRegion> touches = Sets.newLinkedHashSet();
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
    public void addTouches(GraphRegion region) {
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
    public Collection<GraphRegion> getTouches() {
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
