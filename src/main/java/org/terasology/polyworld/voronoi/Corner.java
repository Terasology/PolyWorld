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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.terasology.math.geom.Vector2d;

import com.google.common.base.Preconditions;

/**
 * Corner.java
 *
 * @author Connor
 */
public class Corner {

    private List<Region> touches = new ArrayList<>();
    private List<Corner> adjacent = new ArrayList<>();
    private List<Edge> protrudes = new ArrayList<>();
    private Vector2d loc;
    private boolean border;
    private boolean water;
    private boolean ocean;
    private boolean coast;

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

    public void addAdjacent(Corner c) {
        Preconditions.checkArgument(c != null);
        Preconditions.checkArgument(!adjacent.contains(c));

        adjacent.add(c);
    }

    /**
     * @param region the region to add
     */
    public void addTouches(Region region) {
        Preconditions.checkArgument(region != null);
        Preconditions.checkArgument(!getTouches().contains(region));

        touches.add(region);
    }

    /**
     * @return the coast
     */
    public boolean isCoast() {
        return coast;
    }

    /**
     * @param coast the coast to set
     */
    public void setCoast(boolean coast) {
        this.coast = coast;
    }

    /**
     * @return the ocean
     */
    public boolean isOcean() {
        return ocean;
    }

    /**
     * @param ocean the ocean to set
     */
    public void setOcean(boolean ocean) {
        this.ocean = ocean;
    }

    /**
     * @return the water
     */
    public boolean isWater() {
        return water;
    }

    /**
     * @param water the water to set
     */
    public void setWater(boolean water) {
        this.water = water;
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
        return Collections.unmodifiableList(touches);
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
        return Collections.unmodifiableList(protrudes);
    }

    /**
     * @param edge the protrudes to set
     */
    public void addProtrudes(Edge edge) {
        Preconditions.checkArgument(edge != null);
        Preconditions.checkArgument(!adjacent.contains(edge));

        this.protrudes.add(edge);
    }
}
