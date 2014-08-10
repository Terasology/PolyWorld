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
import java.util.LinkedHashSet;

import org.terasology.math.geom.Vector2d;

import com.google.common.base.Preconditions;

/**
 * Defines a polygon region (vornoi region)
 * @author Martin Steiger
 */
public class Region {

    private final Collection<Corner> corners = new LinkedHashSet<>();
    private final Collection<Edge> borders = new ArrayList<>();

    private final Collection<Region> neighbors = new LinkedHashSet<>();

    private boolean water;
    private boolean coast;

    private boolean isOcean;
    private boolean isBorder;
    private Vector2d center;

    public Region(Vector2d pos) {
        this.center = pos;
    }

    /**
     * @return the pos
     */
    public Vector2d getCenter() {
        return center;
    }

    public double getElevation() {
        double total = 0;
        for (Corner c : getCorners()) {
            total += c.elevation;
        }
        return total / getCorners().size();
    }

    public double getMoisture() {
        double total = 0;
        for (Corner c : getCorners()) {
            total += c.moisture;
        }
        return total / getCorners().size();
    }

    /**
     * @param border true if it is at the border
     */
    public void setBorder(boolean border) {
        this.isBorder = border;
    }

    /**
     * @return true if it is at the border
     */
    public boolean isBorder() {
        return isBorder;
    }

    /**
     * @return the ocean
     */
    public boolean isOcean() {
        return isOcean;
    }

    /**
     * @param ocean the ocean to set
     */
    public void setOcean(boolean ocean) {
        this.isOcean = ocean;
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

    public Collection<Region> getNeighbors() {
        return Collections.unmodifiableCollection(neighbors);
    }

    /**
     * @param region the region to add
     */
    public void addNeigbor(Region region) {
        Preconditions.checkArgument(region != null);
        Preconditions.checkArgument(!neighbors.contains(region));

        neighbors.add(region);
    }

    /**
     * @param edge
     */
    public void addBorder(Edge edge) {
        borders.add(edge);
    }

    /**
     * @return the corners
     */
    public Collection<Corner> getCorners() {
        return Collections.unmodifiableCollection(corners);
    }

    /**
     * @param c
     */
    public void addCorner(Corner c) {
        Preconditions.checkArgument(c != null);
        Preconditions.checkArgument(!corners.contains(c));

        corners.add(c);
    }
}
