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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
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
import org.terasology.math.geom.Vector2d;
import org.terasology.math.geom.Rect2d;

/**
 * VoronoiGraph.java
 *
 * @author Connor
 */
public abstract class VoronoiGraph {

    private static final double ISLAND_FACTOR = 1.07;  // 1.0 means no small islands; 2.0 leads to a lot

    private final List<Edge> edges = new ArrayList<>();
    private final List<Corner> corners = new ArrayList<>();
    private final List<Center> centers = new ArrayList<>();
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
        assignPolygonElevations();

        calculateDownslopes();
        //calculateWatersheds();
        createRivers();
        assignCornerMoisture();
        redistributeMoisture(landCorners());
        assignPolygonMoisture();
        assignBiomes();
    }

    protected abstract Biome getBiome(Center p);

    protected abstract Color getColor(Biome biome);

    private void improveCorners() {
        Vector2d[] newP = new Vector2d[corners.size()];
        for (Corner c : corners) {
            if (c.border) {
                newP[c.index] = c.loc;
            } else {
                double x = 0;
                double y = 0;
                for (Center center : c.touches) {
                    x += center.loc.getX();
                    y += center.loc.getY();
                }
                newP[c.index] = new Vector2d(x / c.touches.size(), y / c.touches.size());
            }
        }
        for (Corner c : corners) {
            c.loc = newP[c.index];
        }
        for (Edge e : edges) {
            if (e.v0 != null && e.v1 != null) {
                e.setVornoi(e.v0, e.v1);
            }
        }
    }

    private Edge edgeWithCenters(Center c1, Center c2) {
        for (Edge e : c1.borders) {
            if (e.d0 == c2 || e.d1 == c2) {
                return e;
            }
        }
        return null;
    }

    private void drawPoly(Graphics2D g, List<Corner> pts) {
        int[] x = new int[pts.size()];
        int[] y = new int[pts.size()];
        
        for (int i = 0; i < pts.size(); i++) {
            x[i] = (int) pts.get(i).loc.getX();
            y[i] = (int) pts.get(i).loc.getY();
        }
        
        g.fillPolygon(x, y, pts.size());
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

    public void paint(Graphics2D g) {
        paint(g, true, true, false, false, false);
    }

    //also records the area of each voronoi cell
    public void paint(Graphics2D g, boolean drawBiomes, boolean drawRivers, boolean drawSites, boolean drawCorners, boolean drawDelaunay) {
        final int numSites = centers.size();

        Color[] defaultColors = null;
        if (!drawBiomes) {
            defaultColors = new Color[numSites];
            for (int i = 0; i < defaultColors.length; i++) {
                defaultColors[i] = new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255));
            }
        }

        //draw via triangles
        for (final Center c : centers) {
            g.setColor(drawBiomes ? getColor(c.biome) : defaultColors[c.index]);

            List<Corner> pts = new ArrayList<>(c.corners);
            Collections.sort(pts, new Comparator<Corner>() {

                @Override
                public int compare(Corner o0, Corner o1) {
                    Vector2d a = new Vector2d(o0.loc).sub(c.loc).normalize();
                    Vector2d b = new Vector2d(o1.loc).sub(c.loc).normalize();

                    if (a.y() > 0) { //a between 0 and 180
                        if (b.y() < 0)  //b between 180 and 360
                            return -1;
                        return a.x() < b.x() ? 1 : -1; 
                    } else { // a between 180 and 360
                        if (b.y() > 0) //b between 0 and 180
                            return 1;
                        return a.x() > b.x() ? 1 : -1;
                    }
                }
            });
            
            drawPoly(g, pts);
        }

        for (Edge e : edges) {
            if (drawDelaunay) {
                g.setStroke(new BasicStroke(1));
                g.setColor(Color.YELLOW);
                g.drawLine((int) e.d0.loc.getX(), (int) e.d0.loc.getY(), (int) e.d1.loc.getX(), (int) e.d1.loc.getY());
            }
            if (drawRivers && e.river > 0) {
                g.setStroke(new BasicStroke(1 + (int) Math.sqrt(e.river * 2)));
                g.setColor(getRiverColor());
                g.drawLine((int) e.v0.loc.getX(), (int) e.v0.loc.getY(), (int) e.v1.loc.getX(), (int) e.v1.loc.getY());
            }
        }

        if (drawSites) {
            g.setColor(Color.BLACK);
            for (Center s : centers) {
                g.fillOval((int) (s.loc.getX() - 2), (int) (s.loc.getY() - 2), 4, 4);
            }
        }

        if (drawCorners) {
            g.setColor(Color.WHITE);
            for (Corner c : corners) {
                g.fillOval((int) (c.loc.getX() - 2), (int) (c.loc.getY() - 2), 4, 4);
            }
        }
        g.setColor(Color.WHITE);
        g.drawRect((int) bounds.minX(), (int) bounds.minY(), (int) bounds.width(), (int) bounds.height());
    }

    private void buildGraph(Voronoi v) {
        final Map<Vector2d, Center> pointCenterMap = new HashMap<>();
        final List<Vector2d> points = v.siteCoords();
        for (Vector2d p : points) {
            Center c = new Center();
            c.loc = p;
            c.index = centers.size();
            centers.add(c);
            pointCenterMap.put(p, c);
        }

        //bug fix
        for (Center c : centers) {
            v.region(c.loc);
        }

        final List<org.terasology.math.delaunay.Edge> libedges = v.edges();
        final Map<Integer, Corner> pointCornerMap = new HashMap<>();

        for (org.terasology.math.delaunay.Edge libedge : libedges) {
            final LineSegment vEdge = libedge.voronoiEdge();
            final LineSegment dEdge = libedge.delaunayLine();

            final Edge edge = new Edge();
            edge.index = edges.size();
            edges.add(edge);

            edge.v0 = makeCorner(pointCornerMap, vEdge.getP0());
            edge.v1 = makeCorner(pointCornerMap, vEdge.getP1());
            edge.d0 = pointCenterMap.get(dEdge.getP0());
            edge.d1 = pointCenterMap.get(dEdge.getP1());

            // Centers point to edges. Corners point to edges.
            if (edge.d0 != null) {
                edge.d0.borders.add(edge);
            }
            if (edge.d1 != null) {
                edge.d1.borders.add(edge);
            }
            if (edge.v0 != null) {
                edge.v0.protrudes.add(edge);
            }
            if (edge.v1 != null) {
                edge.v1.protrudes.add(edge);
            }

            // Centers point to centers.
            if (edge.d0 != null && edge.d1 != null) {
                addToCenterList(edge.d0.neighbors, edge.d1);
                addToCenterList(edge.d1.neighbors, edge.d0);
            }

            // Corners point to corners
            if (edge.v0 != null && edge.v1 != null) {
                addToCornerList(edge.v0.adjacent, edge.v1);
                addToCornerList(edge.v1.adjacent, edge.v0);
            }

            // Centers point to corners
            if (edge.d0 != null) {
                addToCornerList(edge.d0.corners, edge.v0);
                addToCornerList(edge.d0.corners, edge.v1);
            }
            if (edge.d1 != null) {
                addToCornerList(edge.d1.corners, edge.v0);
                addToCornerList(edge.d1.corners, edge.v1);
            }

            // Corners point to centers
            if (edge.v0 != null) {
                addToCenterList(edge.v0.touches, edge.d0);
                addToCenterList(edge.v0.touches, edge.d1);
            }
            if (edge.v1 != null) {
                addToCenterList(edge.v1.touches, edge.d0);
                addToCenterList(edge.v1.touches, edge.d1);
            }
        }
        
        // add corners
        for (Center center : centers) {
            boolean onLeft = false;
            boolean onRight = false;
            boolean onTop = false;
            boolean onBottom = false;
            
            int diff = 1;
            for (Corner corner : center.corners) {
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
                center.corners.add(c);
            }

            if (onLeft && onBottom) {
                Corner c = new Corner();
                c.loc = new Vector2d(bounds.minX(), bounds.maxY());
                c.border = true;
                c.index = corners.size();
                corners.add(c);
                center.corners.add(c);
            }

            if (onRight && onTop) {
                Corner c = new Corner();
                c.loc = new Vector2d(bounds.maxX(), bounds.minY());
                c.border = true;
                c.index = corners.size();
                corners.add(c);
                center.corners.add(c);
            }

            if (onRight && onBottom) {
                Corner c = new Corner();
                c.loc = new Vector2d(bounds.maxX(), bounds.maxY());
                c.border = true;
                c.index = corners.size();
                corners.add(c);
                center.corners.add(c);
            }

        }
    }

    // Helper functions for the following for loop; ideally these
    // would be inlined
    private void addToCornerList(List<Corner> list, Corner c) {
        if (c != null && !list.contains(c)) {
            list.add(c);
        }
    }

    private void addToCenterList(List<Center> list, Center c) {
        if (c != null && !list.contains(c)) {
            list.add(c);
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
        Deque<Center> queue = new LinkedList<>();
        final double waterThreshold = .3;
        for (final Center center : centers) {
            int numWater = 0;
            for (final Corner c : center.corners) {
                if (c.border) {
                    center.border = true;
                    center.water = true;
                    center.ocean = true;
                    queue.add(center);
                }
                if (c.water) {
                    numWater++;
                }
            }
            center.water = center.ocean || ((double) numWater / center.corners.size() >= waterThreshold);
        }
        while (!queue.isEmpty()) {
            final Center center = queue.pop();
            for (final Center n : center.neighbors) {
                if (n.water && !n.ocean) {
                    n.ocean = true;
                    queue.add(n);
                }
            }
        }
        for (Center center : centers) {
            boolean oceanNeighbor = false;
            boolean landNeighbor = false;
            for (Center n : center.neighbors) {
                oceanNeighbor |= n.ocean;
                landNeighbor |= !n.water;
            }
            center.coast = oceanNeighbor && landNeighbor;
        }

        for (Corner c : corners) {
            int numOcean = 0;
            int numLand = 0;
            for (Center center : c.touches) {
                numOcean += center.ocean ? 1 : 0;
                numLand += !center.water ? 1 : 0;
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

    private void assignPolygonElevations() {
        for (Center center : centers) {
            double total = 0;
            for (Corner c : center.corners) {
                total += c.elevation;
            }
            center.elevation = total / center.corners.size();
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
                if (!edge.v0.water || !edge.v1.water) {
                    edge.river++;
                    c.river++;
                    c.downslope.river++;  // TODO: fix double count
                }
                c = c.downslope;
            }
        }
    }

    private Edge lookupEdgeFromCorner(Corner c, Corner downslope) {
        for (Edge e : c.protrudes) {
            if (e.v0 == downslope || e.v1 == downslope) {
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

    private void assignPolygonMoisture() {
        for (Center center : centers) {
            double total = 0;
            for (Corner c : center.corners) {
                total += c.moisture;
            }
            center.moisture = total / center.corners.size();
        }
    }

    private void assignBiomes() {
        for (Center center : centers) {
            center.biome = getBiome(center);
        }
    }

    /**
     * @return
     */
    protected Color getRiverColor() {
        // TODO Auto-generated method stub
        return null;
    }
}
