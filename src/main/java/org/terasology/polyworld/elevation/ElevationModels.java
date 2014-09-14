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

import org.terasology.polyworld.voronoi.Corner;
import org.terasology.polyworld.voronoi.Region;

/**
 * Default implementations for {@link ElevationModel}s
 * @author Martin Steiger
 */
public class ElevationModels {

    /**
     * @param model the elevation model
     * @param r the region
     * @return the
     */
    public static double getElevation(ElevationModel model, Region r) {
        double total = 0;
        for (Corner c : r.getCorners()) {
            total += model.getElevation(c);
        }

        return total / r.getCorners().size();
    }

    /**
     * @param model the elevation model
     * @param c the corner of interest
     * @return the neighbor corner with the lowest elevation
     */
    public static Corner getDownslope(ElevationModel model, Corner c) {
        Corner down = c;

        for (Corner a : c.getAdjacent()) {
            if (model.getElevation(a) <= model.getElevation(down)) {
                down = a;
            }
        }

        return down;
    }

}
