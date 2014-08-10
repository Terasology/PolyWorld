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

package org.terasology.math.delaunay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class EdgeReorderer {

    private List<Edge> edges;
    private List<LR> orientations;

    public EdgeReorderer(List<Edge> origEdges, Class<?> criterion) {
        if (criterion != Vertex.class && criterion != Site.class) {
            throw new IllegalStateException("Edges: criterion must be Vertex or Site");
        }
        edges = new ArrayList<Edge>();
        orientations = new ArrayList<LR>();
        if (origEdges.size() > 0) {
            edges = reorderEdges(origEdges, criterion);
        }
    }

    public void dispose() {
        edges = null;
        orientations = null;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public List<LR> getEdgeOrientations() {
        return orientations;
    }

    private List<Edge> reorderEdges(List<Edge> origEdges, Class<?> criterion) {
        int n = origEdges.size();
        Edge edge;
        // we're going to reorder the edges in order of traversal
        List<Boolean> done = new ArrayList<Boolean>(n);
        int nDone = 0;
        for (int k = 0; k < n; k++) {
            done.add(false);
        }
        List<Edge> newEdges = new ArrayList<Edge>();

        int i = 0;
        edge = origEdges.get(i);
        newEdges.add(edge);
        orientations.add(LR.LEFT);
        ICoord firstPoint = (criterion == Vertex.class) ? edge.getLeftVertex() : edge.getLeftSite();
        ICoord lastPoint = (criterion == Vertex.class) ? edge.getRightVertex() : edge.getRightSite();

        if (firstPoint == Vertex.VERTEX_AT_INFINITY || lastPoint == Vertex.VERTEX_AT_INFINITY) {
            return Collections.emptyList();
        }

        done.set(i, true);
        ++nDone;

        while (nDone < n) {
            for (i = 1; i < n; ++i) {
                if (done.get(i)) {
                    continue;
                }
                edge = origEdges.get(i);
                ICoord leftPoint = (criterion == Vertex.class) ? edge.getLeftVertex() : edge.getLeftSite();
                ICoord rightPoint = (criterion == Vertex.class) ? edge.getRightVertex() : edge.getRightSite();
                if (leftPoint == Vertex.VERTEX_AT_INFINITY || rightPoint == Vertex.VERTEX_AT_INFINITY) {
                    return Collections.emptyList();
                }
                if (leftPoint == lastPoint) {
                    lastPoint = rightPoint;
                    orientations.add(LR.LEFT);
                    newEdges.add(edge);
                    done.set(i, true);
                } else if (rightPoint == firstPoint) {
                    firstPoint = leftPoint;
                    orientations.add(0, LR.LEFT);
                    newEdges.add(0, edge);
                    done.set(i, true);
                } else if (leftPoint == firstPoint) {
                    firstPoint = rightPoint;
                    orientations.add(0, LR.RIGHT);
                    newEdges.add(0, edge);

                    done.set(i, true);
                } else if (rightPoint == lastPoint) {
                    lastPoint = leftPoint;
                    orientations.add(LR.RIGHT);
                    newEdges.add(edge);
                    done.set(i, true);
                }
                if (done.get(i)) {
                    ++nDone;
                }
            }
        }

        return newEdges;
    }
}
