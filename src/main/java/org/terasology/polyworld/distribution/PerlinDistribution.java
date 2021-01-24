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

package org.terasology.polyworld.distribution;

import org.joml.Vector2fc;
import org.terasology.math.geom.Vector2f;
import org.terasology.utilities.procedural.BrownianNoise;
import org.terasology.utilities.procedural.PerlinNoise;

/**
 * TODO Type description
 */
public class PerlinDistribution implements Distribution {

    private final BrownianNoise noise;

    /**
     * @param seed a random seed value
     */
    public PerlinDistribution(long seed) {
        this.noise = new BrownianNoise(new PerlinNoise(seed), 7);
    }

    @Override
    public boolean isInside(Vector2fc p2) {
        Vector2f p = new Vector2f(2 * (p2.x() - 0.5f), 2 * (p2.y() - 0.5f));

        float x = (p.x() + 1) * 128;
        float y = (p.y() + 1) * 128;
        float val = (noise.noise(x, y, 0) + 1) * 2f;
        return val < 1.3 + .7 * p.length();
    }
}
