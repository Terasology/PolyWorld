// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.math.delaunay;

import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.terasology.joml.geom.Rectanglef;
import org.terasology.math.geom.BaseRect;

public class Line2f {
    private final Vector2f start = new Vector2f();
    private final Vector2f end = new Vector2f();


    /**
     * @param p0x the first point's x coordinate
     * @param p0y the first point's y coordinate
     * @param p1x the second point's x coordinate
     * @param p1y the second point's y coordinate
     */
    public Line2f(float p0x, float p0y, float p1x, float p1y) {
        this.start.set(p0x, p0y);
        this.end.set(p1x, p1y);
    }

    /**
     * @param p0 the first point
     * @param p1 the second point
     */
    public Line2f(Vector2fc p0, Vector2fc p1) {
        this.start.set(p0.x(), p0.y());
        this.end.set(p1.x(), p1.y());
    }

    /**
     * @return the starting point
     */
    public Vector2fc getStart() {
        return start;
    }

    /**
     * @return the end point
     */
    public Vector2fc getEnd() {
        return end;
    }

    /**
     * @return the direction (not normalized)
     */
    public Vector2f getDir() {
        return end.sub(start, new Vector2f());
    }

    /**
     * Perform a linear interpolation between the segment endpoints.
     * @param val the interpolation factor. A value of zero return start, a value of one return end.
     * @return the interpolated point
     */
    public Vector2f lerp(float val) {
        return start.lerp(end,val, new Vector2f());
    }

    /**
     * Computes the smallest distance to a given point in 2D space
     * @param pointP the point to test
     * @return the smallest distance
     */
    public float distanceToPoint(Vector2fc pointP) {
        return distanceToPoint(getStart(), getEnd(), pointP);
    }

    /**
     * Computes the smallest distance to a given point in 2D space
     * @param pointA the start of the line segment
     * @param pointB the end of the line segment
     * @param pointP the point to test
     * @return the smallest distance
     */
    public static float distanceToPoint(Vector2fc pointA, Vector2fc pointB, Vector2fc pointP) {
        return distanceToPoint(
            pointA.x(), pointA.y(),
            pointB.x(), pointB.y(),
            pointP.x(), pointP.y());
    }
    /**
     * Computes the smallest distance to a given point in 2D space
     * @param pointA the start of the line segment
     * @param pointB the end of the line segment
     * @param pointP the point to test
     * @return the smallest distance
     */
    public static float distanceToPoint(float pointAx, float pointAy, float pointBx, float pointBy,
                                        float pointPx, float pointPy) {

        float ab0 = pointBx - pointAx;
        float ab1 = pointBy - pointAy;

        float ac0 = pointPx - pointAx;
        float ac1 = pointPy - pointAy;

        float bc0 = pointPx - pointBx;
        float bc1 = pointPy - pointBy;

        float dot1 = ab0 * bc0 + ab1 * bc1;

        if (dot1 > 0) {
            return distance(pointBx, pointBy, pointPx, pointPy);
        }

        float dot2 = -ab0 * ac0 - ab1 * ac1;

        if (dot2 > 0) {
            return distance(pointAx, pointAy, pointPx, pointPy);
        }

        float cross = ab0 * ac1 - ab1 * ac0;
        float dist = cross / distance(pointAx, pointAy, pointBx, pointBy);

        return Math.abs(dist);
    }



    private int outcode(Rectanglef rect, float x, float y) {
        int out = 0;
        if ((rect.maxX - rect.minX) <= 0) {
            out |= BaseRect.OUT_LEFT | BaseRect.OUT_RIGHT;
        } else if (x < rect.minX) {
            out |= BaseRect.OUT_LEFT;
        } else if (x >= rect.minX + (rect.maxX - rect.minX)) {
            out |= BaseRect.OUT_RIGHT;
        }
        if ((rect.maxY -  rect.minY)<= 0) {
            out |= BaseRect.OUT_TOP | BaseRect.OUT_BOTTOM;
        } else if (y < rect.minY) {
            out |= BaseRect.OUT_TOP;
        } else if (y >= rect.minY + (rect.maxY - rect.minY)) {
            out |= BaseRect.OUT_BOTTOM;
        }
        return out;
    }

    /**
     * Clips against the given rectangle and returns a new instance.
     * @param rect the clipping rectangle (<code>null</code> not permitted).
     * @param p0 the target vector for the first coordinate (<code>null</code> not permitted).
     * @param p1 the target vector for the second coordinate (<code>null</code> not permitted).
     * @return true if clipped
     */
    public boolean getClipped(Rectanglef rect, Vector2f p0, Vector2f p1) {
        // this method was contributed by David Gilbert, Object Refineries Ltd.
        float x1 = start.x();
        float y1 = start.y();
        float x2 = end.x();
        float y2 = end.y();

        float minX = rect.minX;
        float maxX = Math.nextDown(rect.minX + (rect.maxX - rect.minX));
        float minY = rect.minY;
        float maxY = Math.nextDown(rect.minY + (rect.maxY - rect.minY));

        int f1 = this.outcode(rect, x1, y1);
        int f2 = this.outcode(rect, x2, y2);

        while ((f1 | f2) != 0) {
            if ((f1 & f2) != 0) {
                return false;
            }
            float dx = (x2 - x1);
            float dy = (y2 - y1);

            // update (x1, y1), (x2, y2) and f1 and f2 using intersections then recheck
            if (f1 != 0) {
                // first point is outside, so we update it against one of the
                // four sides then continue
                if ((f1 & BaseRect.OUT_LEFT) == BaseRect.OUT_LEFT && dx != 0.0) {
                    y1 = y1 + (minX - x1) * dy / dx;
                    x1 = minX;
                } else if ((f1 & BaseRect.OUT_RIGHT) == BaseRect.OUT_RIGHT && dx != 0.0) {
                    y1 = y1 + (maxX - x1) * dy / dx;
                    x1 = maxX;
                } else if ((f1 & BaseRect.OUT_BOTTOM) == BaseRect.OUT_BOTTOM && dy != 0.0) {
                    x1 = x1 + (maxY - y1) * dx / dy;
                    y1 = maxY;
                } else if ((f1 & BaseRect.OUT_TOP) == BaseRect.OUT_TOP && dy != 0.0) {
                    x1 = x1 + (minY - y1) * dx / dy;
                    y1 = minY;
                }
                f1 = outcode(rect, x1, y1);
            } else if (f2 != 0) {
                // second point is outside, so we update it against one of the four sides then continue
                if ((f2 & BaseRect.OUT_LEFT) == BaseRect.OUT_LEFT && dx != 0.0) {
                    y2 = y2 + (minX - x2) * dy / dx;
                    x2 = minX;
                } else if ((f2 & BaseRect.OUT_RIGHT) == BaseRect.OUT_RIGHT && dx != 0.0) {
                    y2 = y2 + (maxX - x2) * dy / dx;
                    x2 = maxX;
                } else if ((f2 & BaseRect.OUT_BOTTOM) == BaseRect.OUT_BOTTOM && dy != 0.0) {
                    x2 = x2 + (maxY - y2) * dx / dy;
                    y2 = maxY;
                } else if ((f2 & BaseRect.OUT_TOP) == BaseRect.OUT_TOP && dy != 0.0) {
                    x2 = x2 + (minY - y2) * dx / dy;
                    y2 = minY;
                }
                f2 = outcode(rect, x2, y2);
            }
        }

        // the line is visible - if it wasn't, we'd have returned false from within the while loop above
        p0.set(x1, y1);
        p1.set(x2, y2);
        return true;
    }

    /**
     * Clips against the given rectangle and returns a new instance.
     * @param rect the clipping rectangle (<code>null</code> not permitted).
     * @return the clipped line if visible, <code>null</code> otherwise.
     */
    public Line2f getClipped(Rectanglef rect) {
        Vector2f p0 = new Vector2f();
        Vector2f p1 = new Vector2f();
        if (getClipped(rect, p0, p1)) {
            return new Line2f(p0, p1);
        } else {
            return null;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + start.hashCode();
        result = prime * result + end.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof Line2f) {
            Line2f other = (Line2f) obj;
            return start.equals(other.start) && end.equals(other.end);
        }
        return false;
    }

    @Override
    public String toString() {
        return "LineSegment [" + start + ", " + end + "]";
    }

    private static float distance(float x0, float y0, float x1, float y1) {
        float dx = x1 - x0;
        float dy = y1 - y0;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
}
