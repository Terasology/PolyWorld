package org.terasology.polyworld.voronoi;

import org.terasology.math.geom.Vector2d;

/**
 * Edge.java
 *
 * @author Connor
 */
public class Edge {

    public int index;
    public Center d0, d1;  // Delaunay edge
    public Corner v0, v1;  // Voronoi edge
    public Vector2d midpoint;  // halfway between v0,v1
    public int river;

    public void setVornoi(Corner v0, Corner v1) {
        this.v0 = v0;
        this.v1 = v1;
        midpoint = new Vector2d((v0.loc.getX() + v1.loc.getX()) / 2, (v0.loc.getY() + v1.loc.getY()) / 2);
    }
}
