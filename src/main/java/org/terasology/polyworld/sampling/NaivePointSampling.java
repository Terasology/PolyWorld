// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.polyworld.sampling;

import com.google.common.collect.Lists;
import org.terasology.engine.utilities.random.Random;
import org.terasology.math.geom.Rect2f;
import org.terasology.math.geom.Vector2f;

import java.util.List;


/**
 * Creates a random sampling based white noise. No minimum distance between points is guaranteed.
 */
public class NaivePointSampling implements PointSampling {

    @Override
    public List<Vector2f> create(Rect2f bounds, int numSites, Random rng) {
        List<Vector2f> points = Lists.newArrayListWithCapacity(numSites);
        for (int i = 0; i < numSites; i++) {
            float px = bounds.minX() + rng.nextFloat() * bounds.width();
            float py = bounds.minY() + rng.nextFloat() * bounds.height();
            points.add(new Vector2f(px, py));
        }
        return points;
    }
}

