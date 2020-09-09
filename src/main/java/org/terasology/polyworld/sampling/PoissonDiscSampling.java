// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.polyworld.sampling;

import com.google.common.base.Preconditions;
import com.google.common.math.DoubleMath;
import org.terasology.engine.utilities.random.Random;
import org.terasology.math.geom.Rect2f;
import org.terasology.math.geom.Vector2f;
import org.terasology.math.geom.Vector2i;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates a more or less isotropic sampling based on a 2D rectangular grid that contains at most one point per grid
 * cell. A minimum distance of 1/2 cell size to points in neighboring cells in ensured.
 */
public class PoissonDiscSampling implements PointSampling {

    private static boolean checkDistance(float px, float py, Vector2f pt, float rad) {
        if (pt == null) {
            return true;
        }

        float dx = px - pt.getX();
        float dy = py - pt.getY();
        return (dx * dx + dy * dy >= rad * rad);
    }

    protected Vector2i getGridDimensions(Rect2f bounds, int numSites) {

        float ratio = bounds.width() / bounds.height();
        double perRow = Math.sqrt(numSites / ratio);

        int rows = DoubleMath.roundToInt(perRow, RoundingMode.FLOOR);
        int cols = DoubleMath.roundToInt(perRow * ratio, RoundingMode.FLOOR);

        // clamp to a minimum value of 2 to avoid polygons that touch
        // two opposing borders of the bounding rectangle
        rows = Math.max(rows, 2);
        cols = Math.max(cols, 2);

        return new Vector2i(cols, rows);
    }

    protected float getMinRadius(Rect2f bounds, int numSites) {
        Vector2i dims = getGridDimensions(bounds, numSites);
        int cols = dims.getX();
        int rows = dims.getY();

        float cellWidth = bounds.width() / cols;
        float cellHeight = bounds.height() / rows;
        float minRad = Math.min(cellHeight, cellWidth) * 0.5f; // they should be identical
        return minRad;
    }

    @Override
    public List<Vector2f> create(Rect2f bounds, int numSites, Random rng) {

        Vector2i dims = getGridDimensions(bounds, numSites);
        int cols = dims.getX();
        int rows = dims.getY();

        float cellWidth = bounds.width() / cols;
        float cellHeight = bounds.height() / rows;
        float minRad = getMinRadius(bounds, numSites);


        Preconditions.checkState(minRad < cellWidth);
        Preconditions.checkState(minRad < cellHeight);

        List<Vector2f> points = new ArrayList<>(numSites);
        List<Vector2f> cells = new ArrayList<>(numSites);

        // TODO: it should be possible to shorten the list of cells to (cols + 2)
        //       this would also allow for using constant indices
        for (int r = 0; r < rows; r++) {
            float minY = bounds.minY() + r * cellHeight;
            for (int c = 0; c < cols; c++) {
                cells.add(null);
                float minX = bounds.minX() + c * cellWidth;

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
                            && ((r == 0 || c == cols - 1) || checkDistance(px, py, cells.get((r - 1) * cols + c + 1),
                            minRad))
                            && ((r == 0 || c == 0) || checkDistance(px, py, cells.get((r - 1) * cols + c - 1),
                            minRad))) {
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
}
