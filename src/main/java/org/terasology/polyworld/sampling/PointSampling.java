// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.polyworld.sampling;

import org.terasology.engine.utilities.random.FastRandom;
import org.terasology.engine.utilities.random.Random;
import org.terasology.math.geom.Rect2f;
import org.terasology.math.geom.Vector2f;

import java.util.List;

public interface PointSampling {

    /**
     * Computes a set of points that are distributed in a given rectangular area.
     *
     * @param bounds the bounds of the target area
     * @param numSites the number of desired points. Implementations need not return this exact number of
     *         points.
     * @return a list of points with a length of about <code>numSites</code>.
     */
    default List<Vector2f> create(Rect2f bounds, int numSites) {
        return create(bounds, numSites, new FastRandom(2343289));
    }

    /**
     * Computes a set of points that are distributed in a given rectangular area.
     *
     * @param bounds the bounds of the target area
     * @param numSites the number of desired points. Implementations need not return this exact number of
     *         points.
     * @param rng the random number generator that should be used
     * @return a list of points with a length of about <code>numSites</code>.
     */
    List<Vector2f> create(Rect2f bounds, int numSites, Random rng);
}
