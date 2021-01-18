/*
 * Copyright 2015 MovingBlocks
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

package org.terasology.polyworld.sampling;

import com.google.common.base.Preconditions;
import com.google.common.math.DoubleMath;
import org.joml.Rectanglef;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector2i;
import org.terasology.utilities.random.Random;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates a more or less isotropic sampling based on a 2D rectangular grid
 * that contains at most one point per grid cell.
 * A minimum distance of 1/2 cell size to points in neighboring cells in ensured.
 */
public class PoissonDiscSampling implements PointSampling {

    protected Vector2i getGridDimensions(Rectanglef bounds, int numSites) {

        float ratio = (bounds.maxX - bounds.minX) / (bounds.maxY - bounds.minY);
        double perRow = Math.sqrt(numSites / ratio);

        int rows = DoubleMath.roundToInt(perRow, RoundingMode.FLOOR);
        int cols = DoubleMath.roundToInt(perRow * ratio, RoundingMode.FLOOR);

        // clamp to a minimum value of 2 to avoid polygons that touch
        // two opposing borders of the bounding rectangle
        rows = Math.max(rows, 2);
        cols = Math.max(cols, 2);

        return new Vector2i(cols, rows);
    }

    protected float getMinRadius(Rectanglef bounds, int numSites) {
        Vector2i dims = getGridDimensions(bounds, numSites);
        int cols = dims.x();
        int rows = dims.y();

        float cellWidth = (bounds.maxX - bounds.minX) / cols;
        float cellHeight = (bounds.maxY - bounds.minY) / rows;
        float minRad = Math.min(cellHeight, cellWidth) * 0.5f; // they should be identical
        return minRad;
    }

    @Override
    public List<Vector2fc> create(Rectanglef bounds, int numSites, Random rng) {

        Vector2i dims = getGridDimensions(bounds, numSites);
        int cols = dims.x();
        int rows = dims.y();

        float cellWidth = (bounds.maxX - bounds.minX) / cols;
        float cellHeight = (bounds.maxY - bounds.minY) / rows;
        float minRad = getMinRadius(bounds, numSites);


        Preconditions.checkState(minRad < cellWidth);
        Preconditions.checkState(minRad < cellHeight);

        List<Vector2fc> points = new ArrayList<>(numSites);
        List<Vector2fc> cells = new ArrayList<>(numSites);

        // TODO: it should be possible to shorten the list of cells to (cols + 2)
        //       this would also allow for using constant indices
        for (int r = 0; r < rows; r++) {
            float minY = bounds.minY + r * cellHeight;
            for (int c = 0; c < cols; c++) {
                cells.add(null);
                float minX = bounds.minX + c * cellWidth;

                // try three times to place a new point
                for (int t = 0; t < 3; t++) {
                    float px = minX + rng.nextFloat() * cellWidth;
                    float py = minY + rng.nextFloat() * cellHeight;

                    // check distances in the following order:
                    // *) cell above, but ignore first row
                    // *) cell left, but ignore first column
                    // *) cell top-right, but ignore first row and last column
                    // *) cell top-left, but ignore first row and first column
                    if (((r == 0) || checkDistance(px, py, cells.get((r - 1) * cols + c), minRad))
                     && ((c == 0) || checkDistance(px, py, cells.get(r * cols + c - 1), minRad))
                     && ((r == 0 || c == cols - 1) || checkDistance(px, py, cells.get((r - 1) * cols + c + 1),                                    minRad))
                     && ((r == 0 || c == 0) || checkDistance(px, py, cells.get((r - 1) * cols + c - 1), minRad))) {
                        Vector2f pt = new Vector2f(px, py);
                        points.add(pt);
                        cells.set(r * cols + c, pt);
                        break;
                    }
                }
            }
        }
        return points;
    }

    private static boolean checkDistance(float px, float py, Vector2fc pt, float rad) {
        if (pt == null) {
            return true;
        }

        float dx = px - pt.x();
        float dy = py - pt.y();
        return (dx * dx + dy * dy >= rad * rad);
    }
}
