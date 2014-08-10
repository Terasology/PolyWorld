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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.math.geom.BaseVector2d;
import org.terasology.math.geom.Circle;
import org.terasology.math.geom.LineSegment;
import org.terasology.math.geom.Vector2d;
import org.terasology.math.geom.Rect2d;

public final class Voronoi {

    private static final Logger logger = LoggerFactory.getLogger(Voronoi.class);
    
    private SiteList sites;
    private Map<Vector2d, Site> sitesIndexedByLocation;

    private final List<Edge> edges = new ArrayList<Edge>();
    // TODO generalize this so it doesn't have to be a rectangle;
    // then we can make the fractal voronois-within-voronois
    private Rect2d plotBounds;

    public Voronoi(List<Vector2d> points, Rect2d plotBounds) {
        init(points, plotBounds);
        fortunesAlgorithm();
    }

    public Voronoi(List<Vector2d> points) {
        double maxWidth = 0;
        double maxHeight = 0;
        for (Vector2d p : points) {
            maxWidth = Math.max(maxWidth, p.getX());
            maxHeight = Math.max(maxHeight, p.getY());
        }
        logger.debug(maxWidth + "," + maxHeight);
        
        init(points, Rect2d.createFromMinAndSize(0, 0, maxWidth, maxHeight));
        fortunesAlgorithm();
    }

    public Voronoi(int numSites, double maxWidth, double maxHeight, Random r) {
        List<Vector2d> points = new ArrayList<Vector2d>();
        for (int i = 0; i < numSites; i++) {
            points.add(new Vector2d(r.nextDouble() * maxWidth, r.nextDouble() * maxHeight));
        }
        init(points, Rect2d.createFromMinAndSize(0, 0, maxWidth, maxHeight));
        fortunesAlgorithm();
    }

    public Rect2d getPlotBounds() {
        return plotBounds;
    }
    
    private void init(List<Vector2d> points, Rect2d bounds) {
        sites = new SiteList();
        sitesIndexedByLocation = new HashMap<Vector2d, Site>();
        addSites(points);
        this.plotBounds = bounds;
    }

    private void addSites(List<Vector2d> points) {
        int length = points.size();
        for (int i = 0; i < length; ++i) {
            addSite(points.get(i), i);
        }
    }

    private void addSite(Vector2d p, int index) {
        Site site = new Site(p, index);
        sites.push(site);
        sitesIndexedByLocation.put(p, site);
    }

    public List<Edge> edges() {
        return edges;
    }

    public List<Vector2d> region(Vector2d p) {
        Site site = sitesIndexedByLocation.get(p);
        if (site == null) {
            return Collections.emptyList();
        }
        return site.region(plotBounds);
    }

    // TODO: bug: if you call this before you call region(), something goes wrong :(
    public List<Vector2d> neighborSitesForSite(Vector2d coord) {
        List<Vector2d> points = new ArrayList<Vector2d>();
        Site site = sitesIndexedByLocation.get(coord);
        if (site == null) {
            return points;
        }
        List<Site> sts = site.neighborSites();
        for (Site neighbor : sts) {
            points.add(neighbor.getCoord());
        }
        return points;
    }

    public List<Circle> circles() {
        return sites.circles();
    }

    private List<Edge> selectEdgesForSitePoint(Vector2d coord, List<Edge> edgesToTest) {
        List<Edge> filtered = new ArrayList<Edge>();

        for (Edge e : edgesToTest) {
            if (((e.getLeftSite() != null && e.getLeftSite().getCoord() == coord)
                    || (e.getRightSite() != null && e.getRightSite().getCoord() == coord))) {
                filtered.add(e);
            }
        }
        return filtered;
    }

    private List<LineSegment> visibleLineSegments(List<Edge> edgs) {
        List<LineSegment> segments = new ArrayList<LineSegment>();

        for (Edge edge : edgs) {
            if (edge.isVisible()) {
                Vector2d p1 = edge.getClippedEnds().get(LR.LEFT);
                Vector2d p2 = edge.getClippedEnds().get(LR.RIGHT);
                segments.add(new LineSegment(p1, p2));
            }
        }

        return segments;
    }

    private List<LineSegment> delaunayLinesForEdges(List<Edge> edgs) {
        List<LineSegment> segments = new ArrayList<LineSegment>();

        for (Edge edge : edgs) {
            segments.add(edge.delaunayLine());
        }

        return segments;
    }

    public List<LineSegment> voronoiBoundaryForSite(Vector2d coord) {
        return visibleLineSegments(selectEdgesForSitePoint(coord, edges));
    }

    public List<LineSegment> delaunayLinesForSite(Vector2d coord) {
        return delaunayLinesForEdges(selectEdgesForSitePoint(coord, edges));
    }

    public List<LineSegment> voronoiDiagram() {
        return visibleLineSegments(edges);
    }

    public List<LineSegment> hull() {
        return delaunayLinesForEdges(hullEdges());
    }

    private List<Edge> hullEdges() {
        List<Edge> filtered = new ArrayList<Edge>();

        for (Edge e : edges) {
            if (e.isPartOfConvexHull()) {
                filtered.add(e);
            }
        }



        return filtered;

        /*function myTest(edge:Edge, index:int, vector:Vector.<Edge>):Boolean
         {
         return (edge.isPartOfConvexHull());
         }*/
    }

    public List<Vector2d> hullPointsInOrder() {
        List<Edge> hullEdges = hullEdges();

        List<Vector2d> points = new ArrayList<Vector2d>();
        if (hullEdges.isEmpty()) {
            return points;
        }

        EdgeReorderer reorderer = new EdgeReorderer(hullEdges, Site.class);
        hullEdges = reorderer.getEdges();
        List<LR> orientations = reorderer.getEdgeOrientations();
        reorderer.dispose();

        LR orientation;

        int n = hullEdges.size();
        for (int i = 0; i < n; ++i) {
            Edge edge = hullEdges.get(i);
            orientation = orientations.get(i);
            points.add(edge.getSite(orientation).getCoord());
        }
        return points;
    }

    public List<List<Vector2d>> regions() {
        return sites.regions(plotBounds);
    }

    public List<Vector2d> siteCoords() {
        return sites.siteCoords();
    }

    private void fortunesAlgorithm() {
        Site newSite;
        Site bottomSite;
        Site topSite;
        Site tempSite;
        Vertex v;
        Vertex vertex;
        Vector2d newintstar = null;
        LR leftRight;
        Halfedge lbnd;
        Halfedge rbnd;
        Halfedge llbnd;
        Halfedge rrbnd;
        Halfedge bisector;
        Edge edge;

        Rect2d dataBounds = sites.getSitesBounds();

        int sqrtNumSites = (int) Math.sqrt(sites.getLength() + 4);
        HalfedgePriorityQueue heap = new HalfedgePriorityQueue(dataBounds.minY(), dataBounds.height(), sqrtNumSites);
        EdgeList edgeList = new EdgeList(dataBounds.minX(), dataBounds.width(), sqrtNumSites);
        List<Halfedge> halfEdges = new ArrayList<Halfedge>();
        List<Vertex> vertices = new ArrayList<Vertex>();

        Site bottomMostSite = sites.next();
        newSite = sites.next();

        for (;;) {
            if (!heap.empty()) {
                newintstar = heap.min();
            }

            if (newSite != null
                    && (heap.empty() || compareByYThenX(newSite, newintstar) < 0)) {
                /* new site is smallest */
                //trace("smallest: new site " + newSite);

                // Step 8:
                lbnd = edgeList.edgeListLeftNeighbor(newSite.getCoord());    // the Halfedge just to the left of newSite
                //trace("lbnd: " + lbnd);
                rbnd = lbnd.edgeListRightNeighbor;    // the Halfedge just to the right
                //trace("rbnd: " + rbnd);
                bottomSite = rightRegion(lbnd, bottomMostSite);   // this is the same as leftRegion(rbnd)
                // this Site determines the region containing the new site
              //trace("new Site is in region of existing site: " + bottomSite);

                // Step 9:
                edge = Edge.createBisectingEdge(bottomSite, newSite);
                //trace("new edge: " + edge);
                edges.add(edge);

                bisector = Halfedge.create(edge, LR.LEFT);
                halfEdges.add(bisector);
                // inserting two Halfedges into edgeList constitutes Step 10:
                // insert bisector to the right of lbnd:
                edgeList.insert(lbnd, bisector);

                // first half of Step 11:
                vertex = Vertex.intersect(lbnd, bisector);
                if (vertex != null) {
                    vertices.add(vertex);
                    heap.remove(lbnd);
                    lbnd.vertex = vertex;
                    lbnd.ystar = vertex.getY() + BaseVector2d.distance(newSite.getCoord(), vertex.getCoord());
                    heap.insert(lbnd);
                }

                lbnd = bisector;
                bisector = Halfedge.create(edge, LR.RIGHT);
                halfEdges.add(bisector);
                // second Halfedge for Step 10:
                // insert bisector to the right of lbnd:
                edgeList.insert(lbnd, bisector);

                // second half of Step 11:
                vertex = Vertex.intersect(bisector, rbnd);
                if (vertex != null) {
                    vertices.add(vertex);
                    bisector.vertex = vertex;
                    bisector.ystar = vertex.getY() + BaseVector2d.distance(newSite.getCoord(), vertex.getCoord());
                    heap.insert(bisector);
                }

                newSite = sites.next();
            } else if (!heap.empty()) {
                /* intersection is smallest */
                lbnd = heap.extractMin();
                llbnd = lbnd.edgeListLeftNeighbor;
                rbnd = lbnd.edgeListRightNeighbor;
                rrbnd = rbnd.edgeListRightNeighbor;
                bottomSite = leftRegion(lbnd, bottomMostSite);
                topSite = rightRegion(rbnd, bottomMostSite);
                // these three sites define a Delaunay triangle
                // (not actually using these for anything...)
                //_triangles.push(new Triangle(bottomSite, topSite, rightRegion(lbnd)));

                v = lbnd.vertex;
                lbnd.edge.setVertex(lbnd.leftRight, v);
                rbnd.edge.setVertex(rbnd.leftRight, v);
                edgeList.remove(lbnd);
                heap.remove(rbnd);
                edgeList.remove(rbnd);
                leftRight = LR.LEFT;
                if (bottomSite.getY() > topSite.getY()) {
                    tempSite = bottomSite;
                    bottomSite = topSite;
                    topSite = tempSite;
                    leftRight = LR.RIGHT;
                }
                edge = Edge.createBisectingEdge(bottomSite, topSite);
                edges.add(edge);
                bisector = Halfedge.create(edge, leftRight);
                halfEdges.add(bisector);
                edgeList.insert(llbnd, bisector);
                edge.setVertex(leftRight.other(), v);
                vertex = Vertex.intersect(llbnd, bisector);
                if (vertex != null) {
                    vertices.add(vertex);
                    heap.remove(llbnd);
                    llbnd.vertex = vertex;
                    llbnd.ystar = vertex.getY() + BaseVector2d.distance(bottomSite.getCoord(), vertex.getCoord());
                    heap.insert(llbnd);
                }
                vertex = Vertex.intersect(bisector, rrbnd);
                if (vertex != null) {
                    vertices.add(vertex);
                    bisector.vertex = vertex;
                    bisector.ystar = vertex.getY() + BaseVector2d.distance(bottomSite.getCoord(), vertex.getCoord());
                    heap.insert(bisector);
                }
            } else {
                break;
            }
        }

        // heap should be empty now
        heap.dispose();

        for (Halfedge halfEdge : halfEdges) {
            halfEdge.reallyDispose();
        }
        halfEdges.clear();

        // we need the vertices to clip the edges
        for (Edge e : edges) {
            e.clipVertices(plotBounds);
        }
        // but we don't actually ever use them again!
        vertices.clear();
    }

    Site leftRegion(Halfedge he, Site bottomMostSite) {
        Edge edge = he.edge;
        if (edge == null) {
            return bottomMostSite;
        }
        return edge.getSite(he.leftRight);
    }

    Site rightRegion(Halfedge he, Site bottomMostSite) {
        Edge edge = he.edge;
        if (edge == null) {
            return bottomMostSite;
        }
        return edge.getSite(he.leftRight.other());
    }

    public static int compareByYThenX(Site s1, Site s2) {
        if (s1.getY() < s2.getY()) {
            return -1;
        }
        if (s1.getY() > s2.getY()) {
            return 1;
        }
        if (s1.getX() < s2.getX()) {
            return -1;
        }
        if (s1.getX() > s2.getX()) {
            return 1;
        }
        return 0;
    }

    public static int compareByYThenX(Site s1, Vector2d s2) {
        if (s1.getY() < s2.getY()) {
            return -1;
        }
        if (s1.getY() > s2.getY()) {
            return 1;
        }
        if (s1.getX() < s2.getX()) {
            return -1;
        }
        if (s1.getX() > s2.getX()) {
            return 1;
        }
        return 0;
    }
}
