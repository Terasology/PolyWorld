/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.polyworld.util;

import org.terasology.math.Vector2i;
import org.terasology.math.Vector3i;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Skaldarnar on 07.03.2015.
 */
public class BresenhamLine {

    public static enum Overlap {
        MAJOR, // Overlap - first go major then minor direction
        MINOR; // Overlap - first go minor then major direction
    }

    /**
     * Thickness mode
     */
    public static enum ThicknessMode {
        /**
         * Line goes through the center
         */
        MIDDLE,

        /**
         * Line goes along the border (clockwise)
         */
        CLOCKWISE,

        /**
         * Line goes along the border (counter-clockwise)
         */
        COUNTERCLOCKWISE
    }

    public static List<Vector2i> getLine2D(Vector2i start, Vector2i end) {
        return getLine2D(start, end, EnumSet.noneOf(Overlap.class));
    }

    /**
     * Modified Bresenham line drawing algorithm with optional overlap (esp. for drawThickLine())
     * Overlap draws additional pixel when changing minor direction - for standard bresenham overlap = LINE_OVERLAP_NONE (0)
     * <pre>
     *  Sample line:
     *    00+
     *     -0000+
     *         -0000+
     *             -00
     *  0 pixels are drawn for normal line without any overlap
     *  + pixels are drawn if LINE_OVERLAP_MAJOR
     *  - pixels are drawn if LINE_OVERLAP_MINOR
     * </pre>
     */
    public static List<Vector2i> getLine2D(Vector2i start, Vector2i end, Set<Overlap> overlap) {
        int tDeltaX;
        int tDeltaY;
        int tDeltaXTimes2;
        int tDeltaYTimes2;
        int tError;
        int tStepX;
        int tStepY;

        int aXStart = start.getX();
        int aYStart = start.getY();

        int xEnd = end.getX();
        int yEnd = end.getY();

        ArrayList<Vector2i> line = new ArrayList<>();

        if ((aXStart == xEnd) || (aYStart == yEnd)) {
            //horizontal or vertical line -> directly add all points
            int sx = Math.min(aXStart, xEnd);
            int sy = Math.min(aYStart, yEnd);
            int ex = Math.max(aXStart, xEnd);
            int ey = Math.max(aYStart, yEnd);

            for (int y = sy; y <= ey; y++) {
                for (int x = sx; x <= ex; x++) {
                    line.add(new Vector2i(x,y));
                }
            }
        } else {
            //calculate direction
            tDeltaX = xEnd - aXStart;
            tDeltaY = yEnd - aYStart;
            if (tDeltaX < 0) {
                tDeltaX = -tDeltaX;
                tStepX = -1;
            } else {
                tStepX = +1;
            }
            if (tDeltaY < 0) {
                tDeltaY = -tDeltaY;
                tStepY = -1;
            } else {
                tStepY = +1;
            }
            tDeltaXTimes2 = tDeltaX << 1;
            tDeltaYTimes2 = tDeltaY << 1;
            // add start pixel
            line.add(new Vector2i(aXStart, aYStart));
            if (tDeltaX > tDeltaY) {
                // start value represents a half step in Y direction
                tError = tDeltaYTimes2 - tDeltaX;
                while (aXStart != xEnd) {
                    // step in main direction
                    aXStart += tStepX;
                    if (tError >= 0) {
                        if (overlap.contains(Overlap.MAJOR)) {
                            // draw pixel in main direction before changing
                            line.add(new Vector2i(aXStart, aYStart));
                        }
                        // change Y
                        aYStart += tStepY;
                        if (overlap.contains(Overlap.MINOR)) {
                            // draw pixel in minor direction before changing
                            line.add(new Vector2i(aXStart - tStepX, aYStart));
                        }
                        tError -= tDeltaXTimes2;
                    }
                    tError += tDeltaYTimes2;
                    line.add(new Vector2i(aXStart, aYStart));
                }
            } else {
                tError = tDeltaXTimes2 - tDeltaY;
                while (aYStart != yEnd) {
                    aYStart += tStepY;
                    if (tError >= 0) {
                        if (overlap.contains(Overlap.MAJOR)) {
                            // draw pixel in main direction before changing
                            line.add(new Vector2i(aXStart, aYStart));
                        }
                        aXStart += tStepX;
                        if (overlap.contains(Overlap.MINOR)) {
                            // draw pixel in minor direction before changing
                            line.add(new Vector2i(aXStart, aYStart - tStepY));
                        }
                        tError -= tDeltaYTimes2;
                    }
                    tError += tDeltaXTimes2;
                    line.add(new Vector2i(aXStart, aYStart));
                }
            }
        }
        return line;
    }

    public static List<Vector3i> getLine3D(Vector3i start, Vector3i end){

        int x1 = start.x;
        int y1 = start.y;
        int z1 = start.z;

        int x2 = end.x;
        int y2 = end.y;
        int z2 = end.y;

        int i, dx, dy, dz, l, m, n, x_inc, y_inc, z_inc, err_1, err_2, dx2, dy2, dz2;
        int x = x1, y = y1, z = z1;

        ArrayList<Vector3i> line = new ArrayList<>();

        dx = x2 - x1;
        dy = y2 - y1;
        dz = z2 - z1;
        x_inc = (dx < 0) ? -1 : 1;
        l = Math.abs(dx);
        y_inc = (dy < 0) ? -1 : 1;
        m = Math.abs(dy);
        z_inc = (dz < 0) ? -1 : 1;
        n = Math.abs(dz);
        dx2 = l << 1;
        dy2 = m << 1;
        dz2 = n << 1;

        if ((l >= m) && (l >= n)) {
            err_1 = dy2 - l;
            err_2 = dz2 - l;
            for (i = 0; i < l; i++) {
                line.add(new Vector3i(x,y,z));
                if (err_1 > 0) {
                    y += y_inc;
                    err_1 -= dx2;
                }
                if (err_2 > 0) {
                    z += z_inc;
                    err_2 -= dx2;
                }
                err_1 += dy2;
                err_2 += dz2;
                x += x_inc;
            }
        } else if ((m >= l) && (m >= n)) {
            err_1 = dx2 - m;
            err_2 = dz2 - m;
            for (i = 0; i < m; i++) {
                line.add(new Vector3i(x, y, z));
                if (err_1 > 0) {
                    x += x_inc;
                    err_1 -= dy2;
                }
                if (err_2 > 0) {
                    z += z_inc;
                    err_2 -= dy2;
                }
                err_1 += dx2;
                err_2 += dz2;
                y += y_inc;
            }
        } else {
            err_1 = dy2 - n;
            err_2 = dx2 - n;
            for (i = 0; i < n; i++) {
                line.add(new Vector3i(x, y, z));
                if (err_1 > 0) {
                    y += y_inc;
                    err_1 -= dz2;
                }
                if (err_2 > 0) {
                    x += x_inc;
                    err_2 -= dz2;
                }
                err_1 += dy2;
                err_2 += dx2;
                z += z_inc;
            }
        }
        line.add(new Vector3i(x, y, z));
        return line;
    }
}
