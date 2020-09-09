// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.polyworld.graph;

import com.google.common.collect.Ordering;
import org.terasology.math.geom.BaseVector2f;
import org.terasology.math.geom.ImmutableVector2f;
import org.terasology.math.geom.Vector2f;

/**
 * Defines an order of points around a central point, based on their angle
 */
final class AngleOrdering extends Ordering<Corner> {

    private final ImmutableVector2f center;

    /**
     * @param center the center point
     */
    AngleOrdering(ImmutableVector2f center) {
        this.center = center;
    }

    @Override
    public int compare(Corner o0, Corner o1) {
        BaseVector2f p0 = o0.getLocation();
        BaseVector2f p1 = o1.getLocation();

        if (p0.equals(p1)) {
            return 0;
        }

        Vector2f a = new Vector2f(p0).sub(center).normalize();
        Vector2f b = new Vector2f(p1).sub(center).normalize();

        if (a.y() > 0) { //a between 0 and 180
            if (b.y() < 0) {  //b between 180 and 360
                return -1;
            }
            return a.x() < b.x() ? 1 : -1;
        } else { // a between 180 and 360
            if (b.y() > 0) { //b between 0 and 180
                return 1;
            }
            return a.x() > b.x() ? 1 : -1;
        }
    }
}
