// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.polyworld.graph;

import com.google.common.base.Preconditions;
import org.terasology.math.geom.BaseVector2f;
import org.terasology.math.geom.Vector2f;
import org.terasology.math.geom.Vector3f;

/**
 * Defines a triangle in the region-based {@link Graph} structure.
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

    public static boolean barycoordInsideTriangle(Vector3f bary) {
        return bary.getX() >= 0 && bary.getY() >= 0 && bary.getX() + bary.getY() <= 1;
    }

    private static Vector3f computeBarycentricCoordinates(BaseVector2f a, BaseVector2f b, BaseVector2f c,
                                                          BaseVector2f p) {

        Vector2f v0 = new Vector2f(b).sub(a);
        Vector2f v1 = new Vector2f(c).sub(a);
        Vector2f v2 = new Vector2f(p).sub(a);

        float d00 = v0.dot(v0);
        float d01 = v0.dot(v1);
        float d11 = v1.dot(v1);
        float d20 = v2.dot(v0);
        float d21 = v2.dot(v1);
        float denom = d00 * d11 - d01 * d01;
        float u = (d11 * d20 - d01 * d21) / denom;
        float v = (d00 * d21 - d01 * d20) / denom;
        float w = 1.0f - u - v;

        // note that w is the first parameter
        return new Vector3f(w, u, v);
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

    public float computeInterpolated(Vector2f p, float wreg, float wc1, float wc2) {
        Vector3f bary = computeBarycentricCoordinates(p);
        return wreg * bary.getX() + wc1 * bary.getY() + wc2 * bary.getZ();
    }

    public Vector3f computeBarycentricCoordinates(Vector2f p) {
        return computeBarycentricCoordinates(region.getCenter(), c1.getLocation(), c2.getLocation(), p);
    }

    @Override
    public String toString() {
        return String.format("Triangle [region=%s, c1=%s, c2=%s]", region, c1, c2);
    }
}
