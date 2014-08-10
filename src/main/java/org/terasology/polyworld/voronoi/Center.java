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
import java.util.List;

import org.terasology.math.geom.Vector2d;

/**
 * Center.java
 *
 * @author Connor
 */
public class Center {

    public final List<Corner> corners = new ArrayList<>();
    public final List<Center> neighbors = new ArrayList<>();
    public final List<Edge> borders = new ArrayList<>();
    public boolean border;
    public boolean ocean;
    public boolean water;
    public boolean coast;
    public double elevation;
    public double moisture;

    private Vector2d pos;

    public Center(Vector2d pos) {
        this.pos = pos;
    }

    /**
     * @return the pos
     */
    public Vector2d getPos() {
        return pos;
    }
}
