// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.math.delaunay;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.terasology.joml.geom.Rectanglef;
import org.terasology.math.geom.Winding;

import java.util.Collection;
import java.util.List;

public class Poly2f {
    private final ImmutableList<Vector2fc> vertices;
    private Rectanglef bbox;

    private Poly2f(ImmutableList<Vector2fc> vertices) {
        Preconditions.checkArgument(!vertices.isEmpty(), "vertices must not be empty");

        this.vertices = vertices;
    }

    public static Poly2f createCopy(Collection<Vector2fc> vertices) {
        ImmutableList.Builder<Vector2fc> bldr = ImmutableList.builder();
        for (Vector2fc v : vertices) {
            bldr.add(new Vector2f(v));
        }
        return new Poly2f(bldr.build());
    }

    public static Poly2f create(List<Vector2fc> vertices) {
        return new Poly2f(ImmutableList.copyOf(vertices));
    }


    /**
     * @return the area of the polygon
     */
    public float area() {
        return (float) Math.abs(signedArea());
    }

    public Rectanglef getBounds() {
        if (bbox == null) {
            for (Vector2fc v : vertices) {
                bbox.minX = Math.min(bbox.minX, v.x());
                bbox.maxX = Math.max(bbox.maxX, v.x());
                bbox.minY = Math.min(bbox.minY, v.y());
                bbox.maxY = Math.max(bbox.maxY, v.y());
            }
        }
        return bbox;
    }

    /**
     * @return the winding of the polygon
     */
    public Winding winding() {
        double signedArea = signedArea();

        return (signedArea <= 0)
            ? Winding.CLOCKWISE
            : Winding.COUNTERCLOCKWISE;
    }

    private double signedArea() {
        int index;
        int nextIndex;
        int n = vertices.size();
        Vector2fc point;
        Vector2fc next;
        double signedDoubleArea = 0;
        for (index = 0; index < n; ++index) {
            nextIndex = (index + 1) % n;
            point = vertices.get(index);
            next = vertices.get(nextIndex);
            signedDoubleArea += point.x() * next.y() - next.x() * point.y();
        }
        return signedDoubleArea * 0.5;
    }

    /**
     * A point is considered to lie inside a
     * <code>Polygon</code> if and only if:
     * <ul>
     * <li> it lies completely
     * inside the<code>Shape</code> boundary <i>or</i>
     * <li>
     * it lies exactly on the <code>Shape</code> boundary <i>and</i> the
     * space immediately adjacent to the
     * point in the increasing <code>X</code> direction is
     * entirely inside the boundary <i>or</i>
     * <li>
     * it lies exactly on a horizontal boundary segment <b>and</b> the
     * space immediately adjacent to the point in the
     * increasing <code>Y</code> direction is inside the boundary.
     * </ul>
     * @param x the x coord
     * @param y the y coord
     * @return true if the polygon contains the point
     */
    public boolean contains(Vector2fc v) {
        return contains(v.x(), v.y());
    }

    /**
     * A point is considered to lie inside a
     * <code>Polygon</code> if and only if:
     * <ul>
     * <li> it lies completely
     * inside the<code>Shape</code> boundary <i>or</i>
     * <li>
     * it lies exactly on the <code>Shape</code> boundary <i>and</i> the
     * space immediately adjacent to the
     * point in the increasing <code>X</code> direction is
     * entirely inside the boundary <i>or</i>
     * <li>
     * it lies exactly on a horizontal boundary segment <b>and</b> the
     * space immediately adjacent to the point in the
     * increasing <code>Y</code> direction is inside the boundary.
     * </ul>
     * @param x the x coord
     * @param y the y coord
     * @return true if the polygon contains the point
     */
    public boolean contains(float x, float y) {
        int npoints = vertices.size();

        if (npoints <= 2) { // || !getBoundingBox().contains(x, y)) {
            return false;
        }
        int hits = 0;

        Vector2fc last = vertices.get(npoints - 1);

        double lastx = last.x();
        double lasty = last.y();
        double curx;
        double cury;

        // Walk the edges of the polygon
        for (int i = 0; i < npoints; lastx = curx, lasty = cury, i++) {
            Vector2fc cur = vertices.get(i);
            curx = cur.x();
            cury = cur.y();

            if (cury == lasty) {
                continue;
            }

            double leftx;
            if (curx < lastx) {
                if (x >= lastx) {
                    continue;
                }
                leftx = curx;
            } else {
                if (x >= curx) {
                    continue;
                }
                leftx = lastx;
            }

            double test1;
            double test2;
            if (cury < lasty) {
                if (y < cury || y >= lasty) {
                    continue;
                }
                if (x < leftx) {
                    hits++;
                    continue;
                }
                test1 = x - curx;
                test2 = y - cury;
            } else {
                if (y < lasty || y >= cury) {
                    continue;
                }
                if (x < leftx) {
                    hits++;
                    continue;
                }
                test1 = x - lastx;
                test2 = y - lasty;
            }

            if (test1 < (test2 / (lasty - cury) * (lastx - curx))) {
                hits++;
            }
        }

        return ((hits & 1) != 0);
    }
}
