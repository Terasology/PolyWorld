// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.polyworld.distribution;

import org.terasology.engine.utilities.procedural.BrownianNoise;
import org.terasology.engine.utilities.procedural.PerlinNoise;
import org.terasology.math.geom.Vector2f;

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
    public boolean isInside(Vector2f p2) {
        Vector2f p = new Vector2f(2 * (p2.getX() - 0.5f), 2 * (p2.getY() - 0.5f));

        float x = (p.getX() + 1) * 128;
        float y = (p.getY() + 1) * 128;
        float val = (noise.noise(x, y, 0) + 1) * 2f;
        return val < 1.3 + .7 * p.length();
    }
}
