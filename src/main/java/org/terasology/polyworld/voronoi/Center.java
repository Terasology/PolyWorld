package org.terasology.polyworld.voronoi;

import java.util.ArrayList;
import java.util.List;

import org.terasology.math.geom.Vector2d;

/**
 * Center.java
 *
 * @author Connor
 */
public class Center {

    public int index;
    public Vector2d loc;
    public final List<Corner> corners = new ArrayList<>();//good
    public final List<Center> neighbors = new ArrayList<>();//good
    public final List<Edge> borders = new ArrayList<>();
    public boolean border, ocean, water, coast;
    public double elevation;
    public double moisture;
    public Enum biome;
    public double area;

    public Center() {
    }

    public Center(Vector2d loc) {
        this.loc = loc;
    }
}
