// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.math.delaunay;

import org.terasology.math.geom.Vector2f;

import java.util.ArrayList;
import java.util.List;

final class HalfedgePriorityQueue {
    private List<Halfedge> hash;
    private int count;
    private int minBucket;
    private final int hashsize;
    private final float ymin;
    private final float deltay;

    public HalfedgePriorityQueue(float ymin, float deltay, int sqrtNumSites) {
        this.ymin = ymin;
        this.deltay = deltay;
        hashsize = 4 * sqrtNumSites;

        count = 0;
        minBucket = 0;
        hash = new ArrayList<Halfedge>(hashsize);

        // dummy Halfedge at the top of each hash
        for (int i = 0; i < hashsize; ++i) {
            hash.add(Halfedge.createDummy());
            hash.get(i).nextInPriorityQueue = null;
        }
    }

    public void dispose() {
        // get rid of dummies
        for (int i = 0; i < hashsize; ++i) {
            hash.get(i).dispose();
        }
        hash.clear();
        hash = null;
    }


    public void insert(Halfedge halfEdge) {
        Halfedge previous;
        Halfedge next;
        int insertionBucket = bucket(halfEdge);
        if (insertionBucket < minBucket) {
            minBucket = insertionBucket;
        }
        previous = hash.get(insertionBucket);
        next = previous.nextInPriorityQueue;
        while (next != null && (halfEdge.ystar > next.ystar || (halfEdge.ystar == next.ystar && halfEdge.vertex.getX() > next.vertex.getX()))) {
            previous = next;
            next = previous.nextInPriorityQueue;
        }
        halfEdge.nextInPriorityQueue = previous.nextInPriorityQueue;
        previous.nextInPriorityQueue = halfEdge;
        ++count;
    }

    public void remove(Halfedge halfEdge) {
        Halfedge previous;
        int removalBucket = bucket(halfEdge);

        if (halfEdge.vertex != null) {
            previous = hash.get(removalBucket);
            while (previous.nextInPriorityQueue != halfEdge) {
                previous = previous.nextInPriorityQueue;
            }
            previous.nextInPriorityQueue = halfEdge.nextInPriorityQueue;
            count--;
            halfEdge.vertex = null;
            halfEdge.nextInPriorityQueue = null;
            halfEdge.dispose();
        }
    }

    private int bucket(Halfedge halfEdge) {
        int theBucket = (int) ((halfEdge.ystar - ymin) / deltay * hashsize);
        if (theBucket < 0) {
            theBucket = 0;
        }
        if (theBucket >= hashsize) {
            theBucket = hashsize - 1;
        }
        return theBucket;
    }

    private boolean isEmpty(int bucket) {
        return (hash.get(bucket).nextInPriorityQueue == null);
    }

    /**
     * move _minBucket until it contains an actual Halfedge (not just the dummy at the top);
     */
    private void adjustMinBucket() {
        while (minBucket < hashsize - 1 && isEmpty(minBucket)) {
            ++minBucket;
        }
    }

    public boolean empty() {
        return count == 0;
    }

    /**
     * @return coordinates of the Halfedge's vertex in V*, the transformed Voronoi diagram
     */
    public Vector2f min() {
        adjustMinBucket();
        Halfedge answer = hash.get(minBucket).nextInPriorityQueue;
        return new Vector2f(answer.vertex.getX(), answer.ystar);
    }

    /**
     * remove and return the min Halfedge
     *
     * @return
     */
    public Halfedge extractMin() {
        Halfedge answer;

        // get the first real Halfedge in _minBucket
        answer = hash.get(minBucket).nextInPriorityQueue;

        hash.get(minBucket).nextInPriorityQueue = answer.nextInPriorityQueue;
        count--;
        answer.nextInPriorityQueue = null;

        return answer;
    }
}
