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

import com.google.common.collect.Lists;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.terasology.engine.utilities.random.Random;
import org.terasology.joml.geom.Rectanglef;

import java.util.List;


/**
 * Creates a random sampling based white noise. No minimum distance
 * between points is guaranteed.
 */
public class NaivePointSampling implements PointSampling {

    @Override
    public List<Vector2fc> create(Rectanglef bounds, int numSites, Random rng) {
        List<Vector2fc> points = Lists.newArrayListWithCapacity(numSites);
        for (int i = 0; i < numSites; i++) {
            float px = bounds.minX + rng.nextFloat() * bounds.getSizeX();
            float py = bounds.minY + rng.nextFloat() * bounds.getSizeY();
            points.add(new Vector2f(px, py));
        }
        return points;
    }
}

