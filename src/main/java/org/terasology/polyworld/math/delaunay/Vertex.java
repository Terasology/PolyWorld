// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.polyworld.math.delaunay;

import org.terasology.math.geom.Vector2f;

final class Vertex implements ICoord {

    public static final Vertex VERTEX_AT_INFINITY = new Vertex(Float.NaN, Float.NaN);

    private final Vector2f coord;

    private Vertex(float x, float y) {
        coord = new Vector2f(x, y);
    }

    private static Vertex create(float x, float y) {
        if (Double.isNaN(x) || Double.isNaN(y)) {
            return VERTEX_AT_INFINITY;
        } else {
            return new Vertex(x, y);
        }
    }

    /**
     * This is the only way to make a Vertex
     *
     * @param halfedge0
     * @param halfedge1
     * @return
     */
    public static Vertex intersect(Halfedge halfedge0, Halfedge halfedge1) {
        Edge edge0;
        Edge edge1;
        Edge edge;
        Halfedge halfedge;
        float determinant;
        float intersectionX;
        float intersectionY;
        boolean rightOfSite;

        edge0 = halfedge0.edge;
        edge1 = halfedge1.edge;
        if (edge0 == null || edge1 == null) {
            return null;
        }
        if (edge0.getRightSite() == edge1.getRightSite()) {
            return null;
        }

        determinant = edge0.getA() * edge1.getB() - edge0.getB() * edge1.getA();
        if (-1.0e-10 < determinant && determinant < 1.0e-10) {
            // the edges are parallel
            return null;
        }

        intersectionX = (edge0.getC() * edge1.getB() - edge1.getC() * edge0.getB()) / determinant;
        intersectionY = (edge1.getC() * edge0.getA() - edge0.getC() * edge1.getA()) / determinant;

        if (Voronoi.compareByYThenX(edge0.getRightSite(), edge1.getRightSite()) < 0) {
            halfedge = halfedge0;
            edge = edge0;
        } else {
            halfedge = halfedge1;
            edge = edge1;
        }
        rightOfSite = intersectionX >= edge.getRightSite().getX();
        if ((rightOfSite && halfedge.leftRight == LR.LEFT)
                || (!rightOfSite && halfedge.leftRight == LR.RIGHT)) {
            return null;
        }

        return Vertex.create(intersectionX, intersectionY);
    }

    @Override
    public Vector2f getCoord() {
        return coord;
    }

    @Override
    public String toString() {
        return "Vertex (" + coord + ")";
    }

    public float getX() {
        return coord.x();
    }

    public float getY() {
        return coord.y();
    }
}
