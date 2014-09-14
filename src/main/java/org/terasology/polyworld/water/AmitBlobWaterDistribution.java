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

/**
 * TODO Type description
 * @author Martin Steiger
 */
public class AmitBlobWaterDistribution implements WaterDistribution {

    private final Rect2d bounds;

    public AmitBlobWaterDistribution(Rect2d bounds) {
        this.bounds = bounds;
    }

    public boolean isWater(Vector2d p2) {
        double nx = (p2.getX() - bounds.minX()) / bounds.width();
        double ny = (p2.getY() - bounds.minY()) / bounds.height();
        Vector2d p = new Vector2d(2 * (nx - 0.5), 2 * (ny - 0.5));

        boolean eye1 = new Vector2d(p.getX() - 0.2, p.getY() / 2 + 0.2).length() < 0.05;
        boolean eye2 = new Vector2d(p.getX() + 0.2, p.getY() / 2 + 0.2).length() < 0.05;
        boolean body = p.length() < 0.8 - 0.18 * Math.sin(5 * Math.atan2(p.getY(), p.getX()));
        return !(body && !eye1 && !eye2);
    }
}
