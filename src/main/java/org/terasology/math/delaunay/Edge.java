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

import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.terasology.joml.geom.Rectanglef;

import java.util.EnumMap;
import java.util.Map;

/**
 * The line segment connecting the two Sites is part of the Delaunay
 * triangulation; the line segment connecting the two Vertices is part of the
 * Voronoi diagram
 *
 *
 */
public final class Edge {

    public static final Edge DELETED = new Edge();

    // the equation of the edge: ax + by = c
    private float a;
    private float b;
    private float c;

    // the two Voronoi vertices that the edge connects
    // (if one of them is null, the edge extends to infinity)
    private Vertex leftVertex;
    private Vertex rightVertex;

    /**
     * Once clipVertices() is called, this HashMap will hold two Points
     * representing the clipped coordinates of the left and right ends...
     */
    private final Map<LR, Vector2fc> clippedVertices = new EnumMap<LR, Vector2fc>(LR.class);

    /**
     * The two input Sites for which this Edge is a bisector:
     */
    private final Map<LR, Site> sites = new EnumMap<LR, Site>(LR.class);


    private Edge() {
    }

    /**
     * This is the only way to create a new Edge
     *
     * @param site0
     * @param site1
     * @return
     *
     */
    public static Edge createBisectingEdge(Site site0, Site site1) {
        float a;
        float b;
        float c;

        float dx = site1.getX() - site0.getX();
        float dy = site1.getY() - site0.getY();
        float absdx = dx > 0 ? dx : -dx;
        float absdy = dy > 0 ? dy : -dy;
        c = site0.getX() * dx + site0.getY() * dy + (dx * dx + dy * dy) * 0.5f;
        if (absdx > absdy) {
            a = 1.0f;
            b = dy / dx;
            c /= dx;
        } else {
            b = 1.0f;
            a = dx / dy;
            c /= dy;
        }

        Edge edge = new Edge();

        edge.setLeftSite(site0);
        edge.setRightSite(site1);
        site0.addEdge(edge);
        site1.addEdge(edge);

        edge.leftVertex = null;
        edge.rightVertex = null;

        edge.a = a;
        edge.b = b;
        edge.c = c;

        //trace("createBisectingEdge: a ", edge.a, "b", edge.b, "c", edge.c);

        return edge;
    }

    public Line2f delaunayLine() {
        // draw a line connecting the input Sites for which the edge is a bisector:
        return new Line2f(getLeftSite().getCoord(), getRightSite().getCoord());
    }

    public Line2f voronoiEdge() {
        if (!isVisible()) {
            return null;
        }
        return new Line2f(clippedVertices.get(LR.LEFT),
            clippedVertices.get(LR.RIGHT));
    }

    public Vertex getLeftVertex() {
        return leftVertex;
    }

    public Vertex getRightVertex() {
        return rightVertex;
    }

    public void setVertex(LR leftRight, Vertex v) {
        if (leftRight == LR.LEFT) {
            leftVertex = v;
        } else {
            rightVertex = v;
        }
    }

    public boolean isPartOfConvexHull() {
        return (leftVertex == null || rightVertex == null);
    }

    public float sitesDistance() {
        return getLeftSite().getCoord().distance(getRightSite().getCoord());
    }

    public static float compareSitesDistancesMax(Edge edge0, Edge edge1) {
        float length0 = edge0.sitesDistance();
        float length1 = edge1.sitesDistance();
        if (length0 < length1) {
            return 1;
        }
        if (length0 > length1) {
            return -1;
        }
        return 0;
    }

    public static float compareSitesDistances(Edge edge0, Edge edge1) {
        return -compareSitesDistancesMax(edge0, edge1);
    }

    public Map<LR, Vector2fc> getClippedEnds() {
        return clippedVertices;
    }

    /**
     * @return true unless the entire Edge is outside the bounds.
     */
    public boolean isVisible() {
        return !clippedVertices.isEmpty();
    }

    public void setLeftSite(Site s) {
        sites.put(LR.LEFT, s);
    }

    public Site getLeftSite() {
        return sites.get(LR.LEFT);
    }

    public void setRightSite(Site s) {
        sites.put(LR.RIGHT, s);
    }

    public Site getRightSite() {
        return sites.get(LR.RIGHT);
    }

    public Site getSite(LR leftRight) {
        return sites.get(leftRight);
    }

    @Override
    public String toString() {
        return "Edge [sites " + sites.get(LR.LEFT) + ", " + sites.get(LR.RIGHT)
               + "; endVertices " + leftVertex + ", " + rightVertex + "]";
    }

    /**
     * Set _clippedVertices to contain the two ends of the portion of the
     * Voronoi edge that is visible within the bounds. If no part of the Edge
     * falls within the bounds, leave _clippedVertices null.
     *
     * @param bounds
     *
     */
    public void clipVertices(Rectanglef bounds) {
        float xmin = bounds.minX;
        float ymin = bounds.minY;
        float xmax = bounds.maxX;
        float ymax = bounds.maxY;

        Vertex vertex0;
        Vertex vertex1;
        float x0;
        float x1;
        float y0;
        float y1;

        if (getA() == 1.0 && getB() >= 0.0) {
            vertex0 = rightVertex;
            vertex1 = leftVertex;
        } else {
            vertex0 = leftVertex;
            vertex1 = rightVertex;
        }

        if (getA() == 1.0) {
            y0 = ymin;
            if (vertex0 != null && vertex0.y() > ymin) {
                y0 = vertex0.y();
            }
            if (y0 > ymax) {
                return;
            }
            x0 = getC() - getB() * y0;

            y1 = ymax;
            if (vertex1 != null && vertex1.y() < ymax) {
                y1 = vertex1.y();
            }
            if (y1 < ymin) {
                return;
            }
            x1 = getC() - getB() * y1;

            if ((x0 > xmax && x1 > xmax) || (x0 < xmin && x1 < xmin)) {
                return;
            }

            if (x0 > xmax) {
                x0 = xmax;
                y0 = (getC() - x0) / getB();
            } else if (x0 < xmin) {
                x0 = xmin;
                y0 = (getC() - x0) / getB();
            }

            if (x1 > xmax) {
                x1 = xmax;
                y1 = (getC() - x1) / getB();
            } else if (x1 < xmin) {
                x1 = xmin;
                y1 = (getC() - x1) / getB();
            }
        } else {
            x0 = xmin;
            if (vertex0 != null && vertex0.x() > xmin) {
                x0 = vertex0.x();
            }
            if (x0 > xmax) {
                return;
            }
            y0 = getC() - getA() * x0;

            x1 = xmax;
            if (vertex1 != null && vertex1.x() < xmax) {
                x1 = vertex1.x();
            }
            if (x1 < xmin) {
                return;
            }
            y1 = getC() - getA() * x1;

            if ((y0 > ymax && y1 > ymax) || (y0 < ymin && y1 < ymin)) {
                return;
            }

            if (y0 > ymax) {
                y0 = ymax;
                x0 = (getC() - y0) / getA();
            } else if (y0 < ymin) {
                y0 = ymin;
                x0 = (getC() - y0) / getA();
            }

            if (y1 > ymax) {
                y1 = ymax;
                x1 = (getC() - y1) / getA();
            } else if (y1 < ymin) {
                y1 = ymin;
                x1 = (getC() - y1) / getA();
            }
        }

        clippedVertices.clear();
        if (vertex0 == leftVertex) {
            clippedVertices.put(LR.LEFT, new Vector2f(x0, y0));
            clippedVertices.put(LR.RIGHT, new Vector2f(x1, y1));
        } else {
            clippedVertices.put(LR.RIGHT, new Vector2f(x0, y0));
            clippedVertices.put(LR.LEFT, new Vector2f(x1, y1));
        }

        // overwrite previously computed coordinates with exact vertex locations
        // where possible. This avoids rounding errors and ensures that equals() works properly.
        // TODO: check before computing the clipped vertices
        if (leftVertex != null && bounds.containsPoint(leftVertex.x(), leftVertex.y())) {
            clippedVertices.put(LR.LEFT, new Vector2f(leftVertex.getCoord()));
        }

        if (rightVertex != null && bounds.containsPoint(rightVertex.x(), rightVertex.y())) {
            clippedVertices.put(LR.RIGHT, rightVertex.getCoord());
        }
    }

    /**
     * @return the a
     */
    public float getA() {
        return a;
    }

    /**
     * @return the b
     */
    public float getB() {
        return b;
    }

    /**
     * @return the c
     */
    public float getC() {
        return c;
    }
}
