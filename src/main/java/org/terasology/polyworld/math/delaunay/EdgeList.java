// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.polyworld.math.delaunay;

import org.terasology.math.geom.Vector2f;

import java.util.ArrayList;
import java.util.List;

final class EdgeList {

    private final List<Halfedge> hash;
    private final float deltax;
    private final float xmin;
    private final int hashsize;
    private final Halfedge leftEnd;
    private final Halfedge rightEnd;

    public EdgeList(float xmin, float deltax, int sqrtNumSites) {
        this.xmin = xmin;
        this.deltax = deltax;
        this.hashsize = 2 * sqrtNumSites;

        this.hash = new ArrayList<Halfedge>(hashsize);

        // two dummy Halfedges:
        leftEnd = Halfedge.createDummy();
        rightEnd = Halfedge.createDummy();
        leftEnd.edgeListLeftNeighbor = null;
        leftEnd.edgeListRightNeighbor = rightEnd;
        rightEnd.edgeListLeftNeighbor = leftEnd;
        rightEnd.edgeListRightNeighbor = null;

        for (int i = 0; i < hashsize; i++) {
            hash.add(null);
        }

        hash.set(0, leftEnd);
        hash.set(hashsize - 1, rightEnd);
    }

    /**
     * Insert newHalfedge to the right of lb
     *
     * @param lb
     * @param newHalfedge
     */
    public void insert(Halfedge lb, Halfedge newHalfedge) {
        newHalfedge.edgeListLeftNeighbor = lb;
        newHalfedge.edgeListRightNeighbor = lb.edgeListRightNeighbor;
        lb.edgeListRightNeighbor.edgeListLeftNeighbor = newHalfedge;
        lb.edgeListRightNeighbor = newHalfedge;
    }

    /**
     * This function only removes the Halfedge from the left-right list. We cannot dispose it yet because we are still
     * using it.
     *
     * @param halfEdge
     */
    public void remove(Halfedge halfEdge) {
        halfEdge.edgeListLeftNeighbor.edgeListRightNeighbor = halfEdge.edgeListRightNeighbor;
        halfEdge.edgeListRightNeighbor.edgeListLeftNeighbor = halfEdge.edgeListLeftNeighbor;
        halfEdge.edge = Edge.DELETED;
        halfEdge.edgeListLeftNeighbor = null;
        halfEdge.edgeListRightNeighbor = null;
    }

    /**
     * Find the rightmost Halfedge that is still left of p
     *
     * @param p
     * @return
     */
    public Halfedge edgeListLeftNeighbor(Vector2f p) {
        int i;
        int bucket;
        Halfedge halfEdge;

        /* Use hash table to get close to desired halfedge */
        bucket = (int) ((p.getX() - xmin) / deltax * hashsize);
        if (bucket < 0) {
            bucket = 0;
        }
        if (bucket >= hashsize) {
            bucket = hashsize - 1;
        }
        halfEdge = getHash(bucket);
        if (halfEdge == null) {
            for (i = 1; true; ++i) {
                halfEdge = getHash(bucket - i);
                if (halfEdge != null) {
                    break;
                }
                halfEdge = getHash(bucket + i);
                if (halfEdge != null) {
                    break;
                }
            }
        }
        /* Now search linear list of halfedges for the correct one */
        if (halfEdge == leftEnd || (halfEdge != rightEnd && halfEdge.isLeftOf(p))) {
            do {
                halfEdge = halfEdge.edgeListRightNeighbor;
            } while (halfEdge != rightEnd && halfEdge.isLeftOf(p));
            halfEdge = halfEdge.edgeListLeftNeighbor;
        } else {
            do {
                halfEdge = halfEdge.edgeListLeftNeighbor;
            } while (halfEdge != leftEnd && !halfEdge.isLeftOf(p));
        }

        /* Update hash table and reference counts */
        if (bucket > 0 && bucket < hashsize - 1) {
            hash.set(bucket, halfEdge);
        }
        return halfEdge;
    }

    /**
     * Get entry from hash table, pruning any deleted nodes
     */
    private Halfedge getHash(int b) {
        Halfedge halfEdge;

        if (b < 0 || b >= hashsize) {
            return null;
        }
        halfEdge = hash.get(b);
        if (halfEdge != null && halfEdge.edge == Edge.DELETED) {
            /* Hash table points to deleted halfedge.  Patch as necessary. */
            hash.set(b, null);
            // still can't dispose halfEdge yet!
            return null;
        } else {
            return halfEdge;
        }
    }
}
