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

package org.terasology.polyworld.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.terasology.math.Rect2i;
import org.terasology.math.delaunay.Voronoi;
import org.terasology.math.geom.BaseVector2f;
import org.terasology.math.geom.ImmutableVector2f;
import org.terasology.math.geom.LineSegment;
import org.terasology.math.geom.Rect2f;
import org.terasology.math.geom.Vector2f;

/**
 * VoronoiGraph.java
 *
 * @author Connor
 */
public class VoronoiGraph implements Graph {

    private final List<Edge> edges = new ArrayList<>();
    private final List<Corner> corners = new ArrayList<>();
    private final List<Region> regions = new ArrayList<>();
    private final Rect2f realBounds;
    private final Rect2i intBounds;

    /**
     * @param bounds bounds of the target area (points from Voronoi will be scaled and translated accordingly)
     * @param v the Voronoi diagram to use
     */
    public VoronoiGraph(Rect2i bounds, Voronoi v) {

        intBounds = bounds;
        realBounds = Rect2f.createFromMinAndSize(bounds.minX(), bounds.minY(), bounds.width(), bounds.height());

        final Map<Vector2f, Region> regionMap = new HashMap<>();
        final Map<BaseVector2f, Corner> pointCornerMap = new HashMap<>();

        for (Vector2f vorSite : v.siteCoords()) {
            Vector2f site = transform(v.getPlotBounds(), realBounds, vorSite);
            Region region = new Region(new ImmutableVector2f(site));
            regions.add(region);
            regionMap.put(vorSite, region);

            for (Vector2f pt : v.region(vorSite)) {
                Corner c0 = makeCorner(pointCornerMap, v.getPlotBounds(), pt);

                region.addCorner(c0);
                c0.addTouches(region);
            }
        }

        for (org.terasology.math.delaunay.Edge libedge : v.edges()) {

            if (!libedge.isVisible()) {
                continue;
            }

            final LineSegment dEdge = libedge.delaunayLine();
            final LineSegment vEdge = libedge.voronoiEdge();

            Corner c0 = makeCorner(pointCornerMap, v.getPlotBounds(), vEdge.getStart());
            Corner c1 = makeCorner(pointCornerMap, v.getPlotBounds(), vEdge.getEnd());

            Region r0 = regionMap.get(dEdge.getStart());
            Region r1 = regionMap.get(dEdge.getEnd());

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
        }
    }

    /**
     * ensures that each corner is represented by only one corner object
     */
    private Corner makeCorner(Map<BaseVector2f, Corner> pointCornerMap, Rect2f srcRc, BaseVector2f orgPt) {

        Corner exist = pointCornerMap.get(orgPt);
        if (exist != null) {
            return exist;
        }

        Vector2f p = transform(srcRc, realBounds, orgPt);

        Corner c = new Corner(new ImmutableVector2f(p));
        corners.add(c);
        pointCornerMap.put(orgPt, c);
        float diff = 0.01f;
        boolean onLeft = closeEnough(p.getX(), realBounds.minX(), diff);
        boolean onTop = closeEnough(p.getY(), realBounds.minY(), diff);
        boolean onRight = closeEnough(p.getX(), realBounds.maxX(), diff);
        boolean onBottom = closeEnough(p.getY(), realBounds.maxY(), diff);
        if (onLeft || onTop || onRight || onBottom) {
            c.setBorder(true);
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
    public Rect2i getBounds() {
        return intBounds;
    }

    /**
     * Transforms the given point from the source rectangle into the destination rectangle.
     * @param srcRc The source rectangle
     * @param dstRc The destination rectangle
     * @param pt The point to transform
     * @return The new, transformed point
     */
    private static Vector2f transform(Rect2f srcRc, Rect2f dstRc, BaseVector2f pt) {

        // TODO: move this to a better place

        float x = (pt.getX() - srcRc.minX()) / srcRc.width();
        float y = (pt.getY() - srcRc.minY()) / srcRc.height();

        x = dstRc.minX() + x * dstRc.width();
        y = dstRc.minY() + y * dstRc.height();

        return new Vector2f(x, y);
    }

    private static boolean closeEnough(float d1, float d2, float diff) {
        return Math.abs(d1 - d2) <= diff;
    }
}
