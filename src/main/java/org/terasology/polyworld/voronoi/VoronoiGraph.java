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
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.terasology.math.delaunay.Voronoi;
import org.terasology.math.geom.LineSegment;
import org.terasology.math.geom.Rect2d;
import org.terasology.math.geom.Vector2d;

/**
 * VoronoiGraph.java
 *
 * @author Connor
 */
public class VoronoiGraph {

    private static final double ISLAND_FACTOR = 1.07;  // 1.0 means no small islands; 2.0 leads to a lot

    private final List<Edge> edges = new ArrayList<>();
    private final List<Corner> corners = new ArrayList<>();
    private final List<Region> regions = new ArrayList<>();
    private final Rect2d bounds;
    private final Random r;

    private final int bumps;
    private final double startAngle;
    private final double dipAngle;
    private final double dipWidth;
    
    public VoronoiGraph(Voronoi ov, int numLloydRelaxations, Random r) {
        this.r = r;
        bumps = r.nextInt(5) + 1;
        startAngle = r.nextDouble() * 2 * Math.PI;
        dipAngle = r.nextDouble() * 2 * Math.PI;
        dipWidth = r.nextDouble() * .5 + .2;
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
        improveCorners();

        assignCornerElevations();
        assignOceanCoastAndLand();
        redistributeElevations(landCorners());

        calculateDownslopes();
        //calculateWatersheds();
        createRivers();
        assignCornerMoisture();
        redistributeMoisture(landCorners());
    }

    private void improveCorners() {
        Vector2d[] newP = new Vector2d[corners.size()];
        for (Corner c : corners) {
            if (c.border) {
                newP[c.index] = c.loc;
            } else {
                double x = 0;
                double y = 0;
                for (Region region : c.touches) {
                    x += region.getCenter().getX();
                    y += region.getCenter().getY();
                }
                newP[c.index] = new Vector2d(x / c.touches.size(), y / c.touches.size());
            }
        }
        for (Corner c : corners) {
            c.loc = newP[c.index];
        }
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

            Corner c0 = makeCorner(pointCornerMap, vEdge.getP0());
            Corner c1 = makeCorner(pointCornerMap, vEdge.getP1());

            Region r0 = pointCenterMap.get(dEdge.getP0()); 
            Region r1 = pointCenterMap.get(dEdge.getP1()); 

            final Edge edge = new Edge(c0, c1, r0, r1);

            edges.add(edge);

            // Centers point to edges. Corners point to edges.

            if (r0 != null) {
                r0.addBorder(edge);
            }
            if (r1 != null) {
                r1.addBorder(edge);
            }
            if (edge.getCorner0() != null) {
                edge.getCorner0().protrudes.add(edge);
            }
            if (edge.getCorner1() != null) {
                edge.getCorner1().protrudes.add(edge);
            }

            // Centers point to centers.
            if (r0 != null && r1 != null) {
                addToCenterList(r0, r1);
                addToCenterList(r1, r0);
            }

            // Corners point to corners
            if (edge.getCorner0() != null && edge.getCorner1() != null) {
                addToCornerList(edge.getCorner0(), edge.getCorner1());
                addToCornerList(edge.getCorner1(), edge.getCorner0());
            }

            // Centers point to corners
            if (r0 != null) {
                addToCornerList(r0, edge.getCorner0());
                addToCornerList(r0, edge.getCorner1());
            }
            if (r1 != null) {
                addToCornerList(r1, edge.getCorner0());
                addToCornerList(r1, edge.getCorner1());
            }

            // Corners point to centers
            if (edge.getCorner0() != null) {
                addToCenterList(edge.getCorner0(), r0);
                addToCenterList(edge.getCorner0(), r1);
            }
            if (edge.getCorner1() != null) {
                addToCenterList(edge.getCorner1(), r0);
                addToCenterList(edge.getCorner1(), r1);
            }
        }

        // add corners
        for (Region region : regions) {
            boolean onLeft = false;
            boolean onRight = false;
            boolean onTop = false;
            boolean onBottom = false;

            int diff = 1;
            for (Corner corner : region.getCorners()) {
                Vector2d p = corner.loc;
                onLeft |= closeEnough(p.getX(), bounds.minX(), diff);
                onTop |= closeEnough(p.getY(), bounds.minY(), diff); 
                onRight |= closeEnough(p.getX(), bounds.maxX(), diff);
                onBottom |= closeEnough(p.getY(), bounds.maxY(), diff);
            }

            if (onLeft && onTop) {
                Corner c = new Corner();
                c.loc = new Vector2d(bounds.minX(), bounds.minY());
                c.border = true;
                c.index = corners.size();
                corners.add(c);
                region.addCorner(c);
            }

            if (onLeft && onBottom) {
                Corner c = new Corner();
                c.loc = new Vector2d(bounds.minX(), bounds.maxY());
                c.border = true;
                c.index = corners.size();
                corners.add(c);
                region.addCorner(c);
            }

            if (onRight && onTop) {
                Corner c = new Corner();
                c.loc = new Vector2d(bounds.maxX(), bounds.minY());
                c.border = true;
                c.index = corners.size();
                corners.add(c);
                region.addCorner(c);
            }

            if (onRight && onBottom) {
                Corner c = new Corner();
                c.loc = new Vector2d(bounds.maxX(), bounds.maxY());
                c.border = true;
                c.index = corners.size();
                corners.add(c);
                region.addCorner(c);
            }

        }
    }

    private void addToCornerList(Corner corner, Corner c) {
        if (c != null && !corner.getAdjacent().contains(c)) {
            corner.addAdjacent(c);
        }
    }

    private void addToCornerList(Region region, Corner c) {
        if (c != null && !region.getCorners().contains(c)) {
            region.addCorner(c);
        }
    }

    private void addToCenterList(Region region, Region c) {
        if (c != null && !region.getNeighbors().contains(c)) {
            region.addNeigbor(c);
        }
    }

    private void addToCenterList(Corner v, Region c) {
        if (c != null && !v.touches.contains(c)) {
            v.addTouches(c);
        }
    }

    //ensures that each corner is represented by only one corner object
    private Corner makeCorner(Map<Integer, Corner> pointCornerMap, Vector2d p) {
        if (p == null) {
            return null;
        }
        int index = (int) ((int) p.getX() + (int) (p.getY()) * bounds.width() * 2);
        Corner c = pointCornerMap.get(index);
        if (c == null) {
            c = new Corner();
            c.loc = p;
            c.border = liesOnAxes(bounds, p);
            c.index = corners.size();
            corners.add(c);
            pointCornerMap.put(index, c);
        }
        return c;
    }
    
    private void assignCornerElevations() {
        Deque<Corner> queue = new LinkedList<>();
        for (Corner c : corners) {
            c.water = isWater(c.loc);
            if (c.border) {
                c.elevation = 0;
                queue.add(c);
            } else {
                c.elevation = Double.MAX_VALUE;
            }
        }

        while (!queue.isEmpty()) {
            Corner c = queue.pop();
            for (Corner a : c.adjacent) {
                double newElevation = 0.01 + c.elevation;
                if (!c.water && !a.water) {
                    newElevation += 1;
                }
                if (newElevation < a.elevation) {
                    a.elevation = newElevation;
                    queue.add(a);
                }
            }
        }
    }

    //only the radial implementation of amitp's map generation
    //TODO implement more island shapes
    private boolean isWater(Vector2d p2) {
        Vector2d p = new Vector2d(2 * (p2.getX() / bounds.width() - 0.5), 2 * (p2.getY() / bounds.height() - 0.5));

        double angle = Math.atan2(p.getY(), p.getX());
        double length = 0.5 * (Math.max(Math.abs(p.getX()), Math.abs(p.getY())) + p.length());

        double r1 = 0.5 + 0.40 * Math.sin(startAngle + bumps * angle + Math.cos((bumps + 3) * angle));
        double r2 = 0.7 - 0.20 * Math.sin(startAngle + bumps * angle - Math.sin((bumps + 2) * angle));
        if (Math.abs(angle - dipAngle) < dipWidth
                || Math.abs(angle - dipAngle + 2 * Math.PI) < dipWidth
                || Math.abs(angle - dipAngle - 2 * Math.PI) < dipWidth) {
            r1 = 0.2;
            r2 = 0.2;
        }
        return !(length < r1 || (length > r1 * ISLAND_FACTOR && length < r2));

        //return false;

        /*if (noise == null) {
         noise = new Perlin2d(.125, 8, MyRandom.seed).createArray(257, 257);
         }
         int x = (int) ((p.x + 1) * 128);
         int y = (int) ((p.y + 1) * 128);
         return noise[x][y] < .3 + .3 * p.l2();*/

        /*boolean eye1 = new Point(p.x - 0.2, p.y / 2 + 0.2).length() < 0.05;
         boolean eye2 = new Point(p.x + 0.2, p.y / 2 + 0.2).length() < 0.05;
         boolean body = p.length() < 0.8 - 0.18 * Math.sin(5 * Math.atan2(p.y, p.x));
         return !(body && !eye1 && !eye2);*/
    }

    private void assignOceanCoastAndLand() {
        Deque<Region> queue = new LinkedList<>();
        final double waterThreshold = .3;
        for (final Region region : regions) {
            int numWater = 0;
            for (final Corner c : region.getCorners()) {
                if (c.border) {
                    region.setBorder(true);
                    region.setWater(true);
                    region.setOcean(true);
                    queue.add(region);
                }
                if (c.water) {
                    numWater++;
                }
            }
            region.setWater(region.isOcean() || ((double) numWater / region.getCorners().size() >= waterThreshold));
        }
        while (!queue.isEmpty()) {
            final Region region = queue.pop();
            for (final Region n : region.getNeighbors()) {
                if (n.isWater() && !n.isOcean()) {
                    n.setOcean(true);
                    queue.add(n);
                }
            }
        }
        for (Region region : regions) {
            boolean oceanNeighbor = false;
            boolean landNeighbor = false;
            for (Region n : region.getNeighbors()) {
                oceanNeighbor |= n.isOcean();
                landNeighbor |= !n.isWater();
            }
            region.setCoast(oceanNeighbor && landNeighbor);
        }

        for (Corner c : corners) {
            int numOcean = 0;
            int numLand = 0;
            for (Region region : c.touches) {
                numOcean += region.isOcean() ? 1 : 0;
                numLand += !region.isWater() ? 1 : 0;
            }
            c.ocean = numOcean == c.touches.size();
            c.coast = numOcean > 0 && numLand > 0;
            c.water = c.border || ((numLand != c.touches.size()) && !c.coast);
        }
    }

    private List<Corner> landCorners() {
        final List<Corner> list = new ArrayList<>();
        for (Corner c : corners) {
            if (!c.ocean && !c.coast) {
                list.add(c);
            }
        }
        return list;
    }

    private void redistributeElevations(List<Corner> landCorners) {
        Collections.sort(landCorners, new Comparator<Corner>() {
            @Override
            public int compare(Corner o1, Corner o2) {
                if (o1.elevation > o2.elevation) {
                    return 1;
                } else if (o1.elevation < o2.elevation) {
                    return -1;
                }
                return 0;
            }
        });

        final double scaleFactor = 1.1;
        for (int i = 0; i < landCorners.size(); i++) {
            double y = (double) i / landCorners.size();
            double x = Math.sqrt(scaleFactor) - Math.sqrt(scaleFactor * (1 - y));
            x = Math.min(x, 1);
            landCorners.get(i).elevation = x;
        }

        for (Corner c : corners) {
            if (c.ocean || c.coast) {
                c.elevation = 0.0;
            }
        }
    }

    private void calculateDownslopes() {
        for (Corner c : corners) {
            Corner down = c;
            //System.out.println("ME: " + c.elevation);
            for (Corner a : c.adjacent) {
                //System.out.println(a.elevation);
                if (a.elevation <= down.elevation) {
                    down = a;
                }
            }
            c.downslope = down;
        }
    }

    private void createRivers() {
        for (int i = 0; i < bounds.width() / 2; i++) {
            Corner c = corners.get(r.nextInt(corners.size()));
            if (c.ocean || c.elevation < 0.3 || c.elevation > 0.9) {
                continue;
            }
            // Bias rivers to go west: if (q.downslope.x > q.x) continue;
            while (!c.coast) {
                if (c == c.downslope) {
                    break;
                }
                Edge edge = lookupEdgeFromCorner(c, c.downslope);
                if (!edge.getCorner0().water || !edge.getCorner1().water) {
                    edge.setRiverValue(edge.getRiverValue() + 1);
                    c.river++;
                    c.downslope.river++;  // TODO: fix double count
                }
                c = c.downslope;
            }
        }
    }

    private Edge lookupEdgeFromCorner(Corner c, Corner downslope) {
        for (Edge e : c.protrudes) {
            if (e.getCorner0() == downslope || e.getCorner1() == downslope) {
                return e;
            }
        }
        return null;
    }

    private void assignCornerMoisture() {
        Deque<Corner> queue = new LinkedList<>();
        for (Corner c : corners) {
            if ((c.water || c.river > 0) && !c.ocean) {
                c.moisture = c.river > 0 ? Math.min(3.0, (0.2 * c.river)) : 1.0;
                queue.push(c);
            } else {
                c.moisture = 0.0;
            }
        }

        while (!queue.isEmpty()) {
            Corner c = queue.pop();
            for (Corner a : c.adjacent) {
                double newM = .9 * c.moisture;
                if (newM > a.moisture) {
                    a.moisture = newM;
                    queue.add(a);
                }
            }
        }

        // Salt water
        for (Corner c : corners) {
            if (c.ocean || c.coast) {
                c.moisture = 1.0;
            }
        }
    }

    private void redistributeMoisture(List<Corner> landCorners) {
        Collections.sort(landCorners, new Comparator<Corner>() {
            @Override
            public int compare(Corner o1, Corner o2) {
                if (o1.moisture > o2.moisture) {
                    return 1;
                } else if (o1.moisture < o2.moisture) {
                    return -1;
                }
                return 0;
            }
        });
        for (int i = 0; i < landCorners.size(); i++) {
            landCorners.get(i).moisture = (double) i / landCorners.size();
        }
    }

    /**
     * @return
     */
    public List<Region> getRegions() {
        return Collections.unmodifiableList(regions);
    }

    /**
     * @return
     */
    public List<Edge> getEdges() {
        return Collections.unmodifiableList(edges);
    }

    /**
     * @return the corners
     */
    public List<Corner> getCorners() {
        return Collections.unmodifiableList(corners);
    }

    /**
     * @return the bounds
     */
    public Rect2d getBounds() {
        return bounds;
    }
}
