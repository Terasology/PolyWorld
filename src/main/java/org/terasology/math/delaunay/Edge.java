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

import java.util.EnumMap;
import java.util.Map;

import org.terasology.math.geom.BaseVector2d;
import org.terasology.math.geom.LineSegment;
import org.terasology.math.geom.Vector2d;
import org.terasology.math.geom.Rect2d;

/**
 * The line segment connecting the two Sites is part of the Delaunay
 * triangulation; the line segment connecting the two Vertices is part of the
 * Voronoi diagram
 *
 * @author ashaw
 *
 */
public final class Edge {

    public static final Edge DELETED = new Edge();

    // the equation of the edge: ax + by = c
    private double a;
    private double b;
    private double c;
    
    // the two Voronoi vertices that the edge connects
    // (if one of them is null, the edge extends to infinity)
    private Vertex leftVertex;
    private Vertex rightVertex;

    /**
     * Once clipVertices() is called, this HashMap will hold two Points
     * representing the clipped coordinates of the left and right ends...
     */
    private final Map<LR, Vector2d> clippedVertices = new EnumMap<LR, Vector2d>(LR.class);

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
        double a;
        double b;
        double c;

        double dx = site1.getX() - site0.getX();
        double dy = site1.getY() - site0.getY();
        double absdx = dx > 0 ? dx : -dx;
        double absdy = dy > 0 ? dy : -dy;
        c = site0.getX() * dx + site0.getY() * dy + (dx * dx + dy * dy) * 0.5;
        if (absdx > absdy) {
            a = 1.0;
            b = dy / dx;
            c /= dx;
        } else {
            b = 1.0;
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

        edge.set(a, b, c);
        //trace("createBisectingEdge: a ", edge.a, "b", edge.b, "c", edge.c);

        return edge;
    }

    public LineSegment delaunayLine() {
        // draw a line connecting the input Sites for which the edge is a bisector:
        return new LineSegment(getLeftSite().getCoord(), getRightSite().getCoord());
    }

    public LineSegment voronoiEdge() {
        if (!isVisible()) {
            return new LineSegment(null, null);
        }
        return new LineSegment(clippedVertices.get(LR.LEFT),
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

    public double sitesDistance() {
        return BaseVector2d.distance(getLeftSite().getCoord(), getRightSite().getCoord());
    }

    public static double compareSitesDistancesMax(Edge edge0, Edge edge1) {
        double length0 = edge0.sitesDistance();
        double length1 = edge1.sitesDistance();
        if (length0 < length1) {
            return 1;
        }
        if (length0 > length1) {
            return -1;
        }
        return 0;
    }

    public static double compareSitesDistances(Edge edge0, Edge edge1) {
        return -compareSitesDistancesMax(edge0, edge1);
    }

    public Map<LR, Vector2d> getClippedEnds() {
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
    public void clipVertices(Rect2d bounds) {
        double xmin = bounds.minX();
        double ymin = bounds.minY();
        double xmax = bounds.maxX();
        double ymax = bounds.maxY();

        Vertex vertex0;
        Vertex vertex1;
        double x0;
        double x1;
        double y0;
        double y1;

        if (getA() == 1.0 && getB() >= 0.0) {
            vertex0 = rightVertex;
            vertex1 = leftVertex;
        } else {
            vertex0 = leftVertex;
            vertex1 = rightVertex;
        }

        if (getA() == 1.0) {
            y0 = ymin;
            if (vertex0 != null && vertex0.getY() > ymin) {
                y0 = vertex0.getY();
            }
            if (y0 > ymax) {
                return;
            }
            x0 = getC() - getB() * y0;

            y1 = ymax;
            if (vertex1 != null && vertex1.getY() < ymax) {
                y1 = vertex1.getY();
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
            if (vertex0 != null && vertex0.getX() > xmin) {
                x0 = vertex0.getX();
            }
            if (x0 > xmax) {
                return;
            }
            y0 = getC() - getA() * x0;

            x1 = xmax;
            if (vertex1 != null && vertex1.getX() < xmax) {
                x1 = vertex1.getX();
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
            clippedVertices.put(LR.LEFT, new Vector2d(x0, y0));
            clippedVertices.put(LR.RIGHT, new Vector2d(x1, y1));
        } else {
            clippedVertices.put(LR.RIGHT, new Vector2d(x0, y0));
            clippedVertices.put(LR.LEFT, new Vector2d(x1, y1));
        }
    }
    
    public void set(double na, double nb, double nc) {
        this.a = na;
        this.b = nb;
        this.c = nc;
    }

    /**
     * @return the a
     */
    public double getA() {
        return a;
    }

    /**
     * @return the b
     */
    public double getB() {
        return b;
    }

    /**
     * @return the c
     */
    public double getC() {
        return c;
    }
}
