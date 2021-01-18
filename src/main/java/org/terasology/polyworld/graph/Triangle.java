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

package org.terasology.polyworld.graph;


import com.google.common.base.Preconditions;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * Defines a triangle in the region-based {@link Graph} structure.
 */
public class Triangle {

    private final GraphRegion region;
    private final Corner c1;
    private final Corner c2;

    /**
     * @param region
     * @param c1
     * @param c2
     */
    public Triangle(GraphRegion region, Corner c1, Corner c2) {
        Preconditions.checkArgument(region != null);
        Preconditions.checkArgument(c1 != null);
        Preconditions.checkArgument(c2 != null);
        Preconditions.checkArgument(!c1.equals(c2), "c1 must be different from c2");

        this.region = region;
        this.c1 = c1;
        this.c2 = c2;
    }

    public GraphRegion getRegion() {
        return region;
    }

    public Corner getCorner1() {
        return c1;
    }

    public Corner getCorner2() {
        return c2;
    }

    public float computeInterpolated(Vector2fc p, float wreg, float wc1, float wc2) {
        Vector3f bary = computeBarycentricCoordinates(p);
        return wreg * bary.x() + wc1 * bary.y() + wc2 * bary.z();
    }

    public Vector3f computeBarycentricCoordinates(Vector2fc p) {
        return computeBarycentricCoordinates(region.getCenter(), c1.getLocation(), c2.getLocation(), p);
    }

    public static boolean barycoordInsideTriangle(Vector3fc bary) {
        return bary.x() >= 0 && bary.y() >= 0 && bary.x() + bary.y() <= 1;
    }

    private static Vector3f computeBarycentricCoordinates(Vector2fc a, Vector2fc b, Vector2fc c, Vector2fc p) {

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

    @Override
    public String toString() {
        return String.format("Triangle [region=%s, c1=%s, c2=%s]", region, c1, c2);
    }
}
