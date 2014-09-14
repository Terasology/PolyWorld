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

import java.util.Collection;

import org.terasology.math.geom.Vector2d;
import org.terasology.utilities.random.Random;

/**
 * TODO Type description
 * @author Martin Steiger
 */
public class GraphEditor {

    /**
     * Moving corners by averaging the nearby centers produces more uniform edge lengths,
     * although it occasionally worsens the polygon sizes. However, moving corners will
     * lose the Voronoi diagram properties.
     * @param corners the collection of corners
     */
    public static void improveCorners(Collection<Corner> corners) {
        Vector2d[] newP = new Vector2d[corners.size()];
        int idx = 0;
        for (Corner c : corners) {
            if (c.isBorder()) {
                newP[idx] = c.getLocation();
            } else {
                double x = 0;
                double y = 0;
                for (Region region : c.getTouches()) {
                    x += region.getCenter().getX();
                    y += region.getCenter().getY();
                }
                newP[idx] = new Vector2d(x / c.getTouches().size(), y / c.getTouches().size());
            }
            idx++;
        }

        idx = 0;
        for (Corner c : corners) {
            c.setLocation(newP[idx++]);
        }
    }

    /**
     * Moves all corners to a random position within a circle with r=maxDist around it
     * @param corners the set of corners
     * @param random the random number gen
     * @param maxDist the maximum moving distance
     */
    public static void jitterCorners(Collection<Corner> corners, Random random, double maxDist) {

        for (Corner c : corners) {
            if (c.isBorder())
                continue;

            Vector2d loc = c.getLocation();
            double ang = random.nextDouble(0, Math.PI * 2.0);
            double len = random.nextDouble(0, maxDist);
            double rx = Math.cos(ang) * len;
            double ry = Math.sin(ang) * len;
            loc.addX(rx);
            loc.addY(ry);
        }
    }
}
