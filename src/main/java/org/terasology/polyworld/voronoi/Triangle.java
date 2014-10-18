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

package org.terasology.polyworld.voronoi;

import org.terasology.math.geom.Vector2d;
import org.terasology.math.geom.Vector3d;

/**
 * TODO Type description
 * @author Martin Steiger
 */
public class Triangle {

    private final Region region;
    private final Corner c1;
    private final Corner c2;

    /**
     * @param region
     * @param c1
     * @param c2
     */
    public Triangle(Region region, Corner c1, Corner c2) {
        this.region = region;
        this.c1 = c1;
        this.c2 = c2;
    }

    public Region getRegion() {
        return region;
    }

    public Corner getCorner1() {
        return c1;
    }

    public Corner getCorner2() {
        return c2;
    }

    public Vector3d computeBarycentricCoordinates(Vector2d p) {
        return computeBarycentricCoordinates(region.getCenter(), c1.getLocation(), c2.getLocation(), p);
    }

    private static Vector3d computeBarycentricCoordinates(Vector2d p0, Vector2d p1, Vector2d p2, Vector2d p) {
        double dx1 = p1.getX() - p0.getX();
        double dy1 = p1.getY() - p0.getY();

        double dx2 = p2.getX() - p0.getX();
        double dy2 = p2.getY() - p0.getY();

        double dx = p.getX() - p0.getX();
        double dy = p.getY() - p0.getY();

        double x = (dy * dx2 - dx * dy2) / (dy1 * dx2 - dx1 * dy2);
        double y = (dy * dx1 - dx * dy1) / (dy2 * dx1 - dx2 * dy1);

        return new Vector3d((float) x, (float) y, (float) (1 - x - y));
    }
}
