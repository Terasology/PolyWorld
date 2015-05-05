/*
 * Copyright 2015 MovingBlocks
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

package org.terasology.polyworld.sampling;

import java.util.List;

import org.terasology.math.geom.Rect2f;
import org.terasology.math.geom.Vector2f;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;

public interface PointSampling {

    /**
     * Computes a set of points that are distributed in a given rectangular area.
     * @param bounds the bounds of the target area
     * @param numSites the number of desired points. Implementations need not return this exact number of points.
     * @return a list of points with a length of about <code>numSites</code>.
     */
    default List<Vector2f> create(Rect2f bounds, int numSites) {
        return create(bounds, numSites, new FastRandom(2343289));
    }

    /**
     * Computes a set of points that are distributed in a given rectangular area.
     * @param bounds the bounds of the target area
     * @param numSites the number of desired points. Implementations need not return this exact number of points.
     * @param rng the random number generator that should be used
     * @return a list of points with a length of about <code>numSites</code>.
     */
    List<Vector2f> create(Rect2f bounds, int numSites, Random rng);
}
