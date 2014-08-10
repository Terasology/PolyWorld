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

import org.terasology.math.geom.Rect2d;
import org.terasology.math.geom.Vector2d;

import com.google.common.base.Preconditions;

/**
 * Corner.java
 *
 * @author Connor
 */
public class Corner {

    public List<Region> touches = new ArrayList<>();
    public List<Corner> adjacent = new ArrayList<>();
    public List<Edge> protrudes = new ArrayList<>();
    public Vector2d loc;
    public int index;
    public boolean border;
    public double elevation;
    public boolean water;
    public boolean ocean;
    public boolean coast;
    public Corner downslope;
    public int river;
    public double moisture;
    
    public void addTouches(Region r) {
        touches.add(r);
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
}
