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

import com.google.common.base.Preconditions;

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
        Preconditions.checkArgument(region != null);
        Preconditions.checkArgument(c1 != null);
        Preconditions.checkArgument(c2 != null);
        Preconditions.checkArgument(!c1.equals(c2), "c1 must be different from c2");

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

    public double computeInterpolated(Vector2d p, double wreg, double wc1, double wc2) {
//        Vector3d bary = computeBarycentricCoordinates(p);
        return wreg; // * bary.getX() + wc1 * bary.getY() + wc2 * bary.getZ();
    }

    public Vector3d computeBarycentricCoordinates(Vector2d p) {
        return computeBarycentricCoordinates(region.getCenter(), c1.getLocation(), c2.getLocation(), p);
    }

    public static boolean barycoordInsideTriangle(Vector3d bary) {
        return bary.getX() >= 0 && bary.getY() >= 0 && bary.getX() + bary.getY() <= 1;
    }

    private static Vector3d computeBarycentricCoordinates(Vector2d a, Vector2d b, Vector2d c, Vector2d p) {

        Vector2d v0 = new Vector2d(b).sub(a);
        Vector2d v1 = new Vector2d(c).sub(a);
        Vector2d v2 = new Vector2d(p).sub(a);

        double d00 = v0.dot(v0);
        double d01 = v0.dot(v1);
        double d11 = v1.dot(v1);
        double d20 = v2.dot(v0);
        double d21 = v2.dot(v1);
        double denom = d00 * d11 - d01 * d01;
        double u = (d11 * d20 - d01 * d21) / denom;
        double v = (d00 * d21 - d01 * d20) / denom;
        double w = 1.0 - u - v;

        return new Vector3d(u, v, w);
    }

    @Override
    public String toString() {
        return String.format("Triangle [region=%s, c1=%s, c2=%s]", region, c1, c2);
    }
}
