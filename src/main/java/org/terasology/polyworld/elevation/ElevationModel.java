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

package org.terasology.polyworld.elevation;

import org.terasology.polyworld.graph.Corner;
import org.terasology.polyworld.graph.GraphRegion;

/**
 * Defines elevation
 */
public interface ElevationModel {

    /**
     * @param corner the corner of interest
     * @return the elevation at that corner
     */
    float getElevation(Corner corner);

    /**
     * @param r the region
     * @return the
     */
    float getElevation(GraphRegion r);

    /**
     * @param c the corner of interest
     * @return the neighbor corner with the lowest elevation
     */
    Corner getDownslope(Corner c);
}
