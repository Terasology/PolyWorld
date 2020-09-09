// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.polyworld.graph;

import org.terasology.math.geom.Rect2i;

import java.util.List;

/**
 * TODO Type description
 */
public interface Graph {

    /**
     * @return
     */
    List<Region> getRegions();

    /**
     * @return
     */
    List<Edge> getEdges();

    /**
     * @return the corners
     */
    List<Corner> getCorners();

    /**
     * @return the bounds
     */
    Rect2i getBounds();
}
