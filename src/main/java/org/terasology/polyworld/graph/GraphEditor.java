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

import java.util.Collection;
import java.util.List;

import org.terasology.math.delaunay.Voronoi;
import org.terasology.math.geom.ImmutableVector2f;
import org.terasology.math.geom.Vector2f;
import org.terasology.utilities.random.Random;

/**
 * TODO Type description
 * @author Martin Steiger
 */
public final class GraphEditor {

    private GraphEditor() {
        // avoid instantiation
    }

    /**
     * Moving corners by averaging the nearby centers produces more uniform edge lengths,
     * although it occasionally worsens the polygon sizes. However, moving corners will
     * lose the Voronoi diagram properties.
     * @param corners the collection of corners
     */
    public static void improveCorners(Collection<Corner> corners) {
        ImmutableVector2f[] newP = new ImmutableVector2f[corners.size()];
        int idx = 0;
        for (Corner c : corners) {
            if (c.isBorder()) {
                newP[idx] = c.getLocation();
            } else {
                float x = 0;
                float y = 0;
                for (Region region : c.getTouches()) {
                    x += region.getCenter().getX();
                    y += region.getCenter().getY();
                }
                newP[idx] = new ImmutableVector2f(x / c.getTouches().size(), y / c.getTouches().size());
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
    public static void jitterCorners(Collection<Corner> corners, Random random, float maxDist) {

        for (Corner c : corners) {
            if (c.isBorder()) {
                continue;
            }

            ImmutableVector2f loc = c.getLocation();
            float ang = random.nextFloat(0, (float) (Math.PI * 2.0));
            float len = random.nextFloat(0, maxDist);
            float rx = (float) (Math.cos(ang) * len);
            float ry = (float) (Math.sin(ang) * len);
            c.setLocation(loc.add(rx,  ry));
        }
    }

    /**
     * Perform Lloyd's algorithm to achieve well-shaped
     * and uniformly sized convex cells.
     * @param v the Voronoi diagram to relax
     * @return a new Voronoi diagram
     */
    public static Voronoi lloydRelaxation(Voronoi v) {
        List<Vector2f> points = v.siteCoords();
        for (Vector2f p : points) {
            List<Vector2f> region = v.region(p);
            float x = 0;
            float y = 0;
            for (Vector2f c : region) {
                x += c.getX();
                y += c.getY();
            }
            x /= region.size();
            y /= region.size();
            p.setX(x);
            p.setY(y);
        }
        return new Voronoi(points, v.getPlotBounds());
    }
}
