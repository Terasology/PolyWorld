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

package org.terasology.polyworld.voronoi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.terasology.math.delaunay.Voronoi;
import org.terasology.math.geom.LineSegment;
import org.terasology.math.geom.Rect2d;
import org.terasology.math.geom.Vector2d;

/**
 * VoronoiGraph.java
 *
 * @author Connor
 */
public class VoronoiGraph implements Graph {

    private final List<Edge> edges = new ArrayList<>();
    private final List<Corner> corners = new ArrayList<>();
    private final List<Region> regions = new ArrayList<>();
    private final Rect2d bounds;

    public VoronoiGraph(Voronoi ov, int numLloydRelaxations) {
        Voronoi v = ov;

        bounds = v.getPlotBounds();
        for (int i = 0; i < numLloydRelaxations; i++) {
            List<Vector2d> points = v.siteCoords();
            for (Vector2d p : points) {
                List<Vector2d> region = v.region(p);
                double x = 0;
                double y = 0;
                for (Vector2d c : region) {
                    x += c.getX();
                    y += c.getY();
                }
                x /= region.size();
                y /= region.size();
                p.setX(x);
                p.setY(y);
            }
            v = new Voronoi(points, v.getPlotBounds());
        }
        buildGraph(v);
    }


    private static boolean liesOnAxes(Rect2d r, Vector2d p) {
        int diff = 1;
        return closeEnough(p.getX(), r.minX(), diff)
            || closeEnough(p.getY(), r.minY(), diff)
            || closeEnough(p.getX(), r.maxX(), diff)
            || closeEnough(p.getY(), r.maxY(), diff);
    }

    private static boolean closeEnough(double d1, double d2, double diff) {
        return Math.abs(d1 - d2) <= diff;
    }

    private void buildGraph(Voronoi v) {
        final Map<Vector2d, Region> pointCenterMap = new HashMap<>();
        final List<Vector2d> points = v.siteCoords();
        for (Vector2d p : points) {
            Region c = new Region(p);
            regions.add(c);
            pointCenterMap.put(p, c);
        }

        //bug fix
        for (Region c : regions) {
            v.region(c.getCenter());
        }

        final List<org.terasology.math.delaunay.Edge> libedges = v.edges();
        final Map<Integer, Corner> pointCornerMap = new HashMap<>();

        for (org.terasology.math.delaunay.Edge libedge : libedges) {
            final LineSegment vEdge = libedge.voronoiEdge();
            final LineSegment dEdge = libedge.delaunayLine();

            if (vEdge.getP0() == null || vEdge.getP1() == null) {
                continue;
            }

            Corner c0 = makeCorner(pointCornerMap, vEdge.getP0());
            Corner c1 = makeCorner(pointCornerMap, vEdge.getP1());

            Region r0 = pointCenterMap.get(dEdge.getP0());
            Region r1 = pointCenterMap.get(dEdge.getP1());

            final Edge edge = new Edge(c0, c1, r0, r1);

            edges.add(edge);

            // Centers point to edges. Corners point to edges.

            r0.addBorder(edge);
            r1.addBorder(edge);

            c0.addEdge(edge);
            c1.addEdge(edge);

            // Centers point to centers.
            r0.addNeigbor(r1);
            r1.addNeigbor(r0);

            // Corners point to corners
            c0.addAdjacent(c1);
            c1.addAdjacent(c0);

            // Centers point to corners
            r0.addCorner(c0);
            r0.addCorner(c1);
            r1.addCorner(c0);
            r1.addCorner(c1);

            // Corners point to centers
            c0.addTouches(r0);
            c0.addTouches(r1);

            c1.addTouches(r0);
            c1.addTouches(r1);
        }

        // add corners
        for (Region region : regions) {
            boolean onLeft = false;
            boolean onRight = false;
            boolean onTop = false;
            boolean onBottom = false;

            int diff = 1;
            for (Corner corner : region.getCorners()) {
                Vector2d p = corner.getLocation();
                onLeft |= closeEnough(p.getX(), bounds.minX(), diff);
                onTop |= closeEnough(p.getY(), bounds.minY(), diff);
                onRight |= closeEnough(p.getX(), bounds.maxX(), diff);
                onBottom |= closeEnough(p.getY(), bounds.maxY(), diff);
            }

            if (onLeft && onTop) {
                Corner c = new Corner(new Vector2d(bounds.minX(), bounds.minY()));
                c.setBorder(true);
                corners.add(c);
                region.addCorner(c);
            }

            if (onLeft && onBottom) {
                Corner c = new Corner(new Vector2d(bounds.minX(), bounds.maxY()));
                c.setBorder(true);
                corners.add(c);
                region.addCorner(c);
            }

            if (onRight && onTop) {
                Corner c = new Corner(new Vector2d(bounds.maxX(), bounds.minY()));
                c.setBorder(true);
                corners.add(c);
                region.addCorner(c);
            }

            if (onRight && onBottom) {
                Corner c = new Corner(new Vector2d(bounds.maxX(), bounds.maxY()));
                c.setBorder(true);
                corners.add(c);
                region.addCorner(c);
            }

        }
    }

    /**
     * ensures that each corner is represented by only one corner object
     */
    private Corner makeCorner(Map<Integer, Corner> pointCornerMap, Vector2d p) {
        if (p == null) {
            return null;
        }
        int index = (int) ((int) p.getX() + (int) (p.getY()) * bounds.width() * 2);
        Corner c = pointCornerMap.get(index);
        if (c == null) {
            c = new Corner(p);
            c.setBorder(liesOnAxes(bounds, p));
            corners.add(c);
            pointCornerMap.put(index, c);
        }
        return c;
    }

    /**
     * @return
     */
    @Override
    public List<Region> getRegions() {
        return Collections.unmodifiableList(regions);
    }

    /**
     * @return
     */
    @Override
    public List<Edge> getEdges() {
        return Collections.unmodifiableList(edges);
    }

    /**
     * @return the corners
     */
    @Override
    public List<Corner> getCorners() {
        return Collections.unmodifiableList(corners);
    }

    /**
     * @return the bounds
     */
    @Override
    public Rect2d getBounds() {
        return bounds;
    }
}
