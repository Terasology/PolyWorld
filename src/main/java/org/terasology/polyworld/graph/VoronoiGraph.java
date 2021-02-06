// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.polyworld.graph;

import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.terasology.commonworld.geom.Line2f;
import org.terasology.joml.geom.Rectanglef;
import org.terasology.math.delaunay.Voronoi;
import org.terasology.world.block.BlockArea;
import org.terasology.world.block.BlockAreac;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * VoronoiGraph.java
 *
 */
public class VoronoiGraph implements Graph {

    private final List<Edge> edges = new ArrayList<>();
    private final List<Corner> corners = new ArrayList<>();
    private final List<GraphRegion> regions = new ArrayList<>();
    private final Rectanglef realBounds = new Rectanglef();
    private final BlockArea intBounds = new BlockArea(BlockArea.INVALID);

    /**
     * @param bounds bounds of the target area (points from Voronoi will be scaled and translated accordingly)
     * @param v the Voronoi diagram to use
     */
    public VoronoiGraph(BlockAreac bounds, Voronoi v) {

        intBounds.set(bounds);
        bounds.getBounds(realBounds);

        final Map<Vector2fc, GraphRegion> regionMap = new HashMap<>();
        final Map<Vector2fc, Corner> pointCornerMap = new HashMap<>();

        for (Vector2fc vorSite : v.siteCoords()) {
            Vector2f site = transform(v.getPlotBounds(), realBounds, vorSite);
            GraphRegion region = new GraphRegion(new Vector2f(site));
            regions.add(region);
            regionMap.put(new Vector2f(vorSite), region);

            for (Vector2fc pt : v.region(vorSite)) {
                Corner c0 = makeCorner(pointCornerMap, v.getPlotBounds(), pt);

                region.addCorner(c0);
                c0.addTouches(region);
            }
        }

        for (org.terasology.math.delaunay.Edge libedge : v.edges()) {

            if (!libedge.isVisible()) {
                continue;
            }

            final Line2f dEdge = libedge.delaunayLine();
            final Line2f vEdge = libedge.voronoiEdge();

            Corner c0 = makeCorner(pointCornerMap, v.getPlotBounds(), vEdge.getStart());
            Corner c1 = makeCorner(pointCornerMap, v.getPlotBounds(), vEdge.getEnd());

            GraphRegion r0 = regionMap.get(dEdge.getStart());
            GraphRegion r1 = regionMap.get(dEdge.getEnd());

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
    private Corner makeCorner(Map<Vector2fc, Corner> pointCornerMap, Rectanglef srcRc, Vector2fc orgPt) {

        Corner exist = pointCornerMap.get(orgPt);
        if (exist != null) {
            return exist;
        }

        Vector2f p = transform(srcRc, realBounds, orgPt);

        Corner c = new Corner(p);
        corners.add(c);
        pointCornerMap.put(orgPt, c);
        float diff = 0.01f;
        boolean onLeft = closeEnough(p.x(), realBounds.minX, diff);
        boolean onTop = closeEnough(p.y(), realBounds.minY, diff);
        boolean onRight = closeEnough(p.x(), realBounds.maxX, diff);
        boolean onBottom = closeEnough(p.y(), realBounds.maxY, diff);
        if (onLeft || onTop || onRight || onBottom) {
            c.setBorder(true);
        }

        return c;
    }

    /**
     * @return
     */
    @Override
    public List<GraphRegion> getRegions() {
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
    public BlockAreac getBounds() {
        return intBounds;
    }

    /**
     * Transforms the given point from the source rectangle into the destination rectangle.
     * @param srcRc The source rectangle
     * @param dstRc The destination rectangle
     * @param pt The point to transform
     * @return The new, transformed point
     */
    private static Vector2f transform(Rectanglef srcRc, Rectanglef dstRc, Vector2fc pt) {

        // TODO: move this to a better place

        float x = (pt.x() - srcRc.minX) / srcRc.getSizeX();
        float y = (pt.y() - srcRc.minY) / srcRc.getSizeY();

        x = dstRc.minX + x * dstRc.getSizeX();
        y = dstRc.minY + y * dstRc.getSizeY();

        return new Vector2f(x, y);
    }

    private static boolean closeEnough(float d1, float d2, float diff) {
        return Math.abs(d1 - d2) <= diff;
    }
}
