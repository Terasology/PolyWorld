// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.polyworld.graph;

import org.terasology.engine.world.generation.WorldFacet2D;
import org.terasology.math.geom.Vector2i;
import org.terasology.polyworld.rp.WorldRegion;

import java.util.Collection;

/**
 * TODO Type description
 */
public interface GraphFacet extends WorldFacet2D {

    Graph get(int x, int z);

    Graph get(Vector2i pos);

    Graph getWorld(int x, int z);

    Graph getWorld(Vector2i pos);

    Collection<Graph> getAllGraphs();

    Graph getGraph(WorldRegion wr);

    Triangle getWorldTriangle(int x, int z);
}
