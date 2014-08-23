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

package org.terasology.polyworld.water;

import org.terasology.math.geom.Rect2d;
import org.terasology.math.geom.Vector2d;
import org.terasology.utilities.procedural.BrownianNoise3D;
import org.terasology.utilities.procedural.PerlinNoise;

/**
 * TODO Type description
 * @author Martin Steiger
 */
public class PerlinWaterDistribution implements WaterDistribution {

    private final Rect2d bounds;

    private final BrownianNoise3D noise;

    /**
     *
     */
    public PerlinWaterDistribution(Rect2d bounds) {
        this.bounds = bounds;
        this.noise = new BrownianNoise3D(new PerlinNoise(97829853), 8);
    }

    public boolean isWater(Vector2d p2) {
        Vector2d p = new Vector2d(2 * (p2.getX() / bounds.width() - 0.5), 2 * (p2.getY() / bounds.height() - 0.5));

        double x = (p.getX() + 1) * 128;
        double y = (p.getY() + 1) * 128;
        double val = noise.getScale() + noise.noise(x, y, 0);
        return val < 1.3 + .7 * p.length();
    }
}
