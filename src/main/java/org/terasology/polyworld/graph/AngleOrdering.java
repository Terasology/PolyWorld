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


import com.google.common.collect.Ordering;
import org.joml.Vector2f;
import org.joml.Vector2fc;

/**
 * Defines an order of points around a central point, based on their angle
 */
final class AngleOrdering extends Ordering<Corner> {

    private final Vector2f center = new Vector2f();

    /**
     * @param center the center point
     */
    AngleOrdering(Vector2fc center) {
        this.center.set(center);
    }

    @Override
    public int compare(Corner o0, Corner o1) {
        Vector2fc p0 = o0.getLocation();
        Vector2fc p1 = o1.getLocation();

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
