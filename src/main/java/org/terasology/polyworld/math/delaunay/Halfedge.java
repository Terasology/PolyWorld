// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.polyworld.math.delaunay;

import org.terasology.math.geom.Vector2f;

final class Halfedge {

    public Halfedge edgeListLeftNeighbor;
    public Halfedge edgeListRightNeighbor;
    public Halfedge nextInPriorityQueue;
    public Edge edge;
    public LR leftRight;
    public Vertex vertex;

    // the vertex's y-coordinate in the transformed Voronoi space V*
    public float ystar;

    public Halfedge(Edge edge, LR lr) {
        this.edge = edge;
        leftRight = lr;
        nextInPriorityQueue = null;
        vertex = null;
    }

    public static Halfedge create(Edge edge, LR lr) {
        return new Halfedge(edge, lr);
    }

    public static Halfedge createDummy() {
        return create(null, null);
    }

    @Override
    public String toString() {
        return "Halfedge (leftRight: " + leftRight + "; vertex: " + vertex + ")";
    }

    public void dispose() {
        if (edgeListLeftNeighbor != null || edgeListRightNeighbor != null) {
            // still in EdgeList
            return;
        }
        if (nextInPriorityQueue != null) {
            // still in PriorityQueue
            return;
        }
        edge = null;
        leftRight = null;
        vertex = null;
    }

    public void reallyDispose() {
        edgeListLeftNeighbor = null;
        edgeListRightNeighbor = null;
        nextInPriorityQueue = null;
        edge = null;
        leftRight = null;
        vertex = null;
    }

    public boolean isLeftOf(Vector2f p) {
        Site topSite;
        boolean rightOfSite;
        boolean above;
        boolean fast;
        float dxp;
        float dyp;
        float dxs;
        float t1;
        float t2;
        float t3;
        float yl;

        topSite = edge.getRightSite();
        rightOfSite = p.getX() > topSite.getX();
        if (rightOfSite && this.leftRight == LR.LEFT) {
            return true;
        }
        if (!rightOfSite && this.leftRight == LR.RIGHT) {
            return false;
        }

        if (edge.getA() == 1.0) {
            dyp = p.getY() - topSite.getY();
            dxp = p.getX() - topSite.getX();
            fast = false;
            if ((!rightOfSite && edge.getB() < 0.0) || (rightOfSite && edge.getB() >= 0.0)) {
                above = dyp >= edge.getB() * dxp;
                fast = above;
            } else {
                above = p.getX() + p.getY() * edge.getB() > edge.getC();
                if (edge.getB() < 0.0) {
                    above = !above;
                }
                if (!above) {
                    fast = true;
                }
            }
            if (!fast) {
                dxs = topSite.getX() - edge.getLeftSite().getX();
                above = edge.getB() * (dxp * dxp - dyp * dyp)
                        < dxs * dyp * (1.0 + 2.0 * dxp / dxs + edge.getB() * edge.getB());
                if (edge.getB() < 0.0) {
                    above = !above;
                }
            }
        } else /* edge.b == 1.0 */ {
            yl = edge.getC() - edge.getA() * p.getX();
            t1 = p.getY() - yl;
            t2 = p.getX() - topSite.getX();
            t3 = yl - topSite.getY();
            above = t1 * t1 > t2 * t2 + t3 * t3;
        }
        return (this.leftRight == LR.LEFT) == above;
    }
}
