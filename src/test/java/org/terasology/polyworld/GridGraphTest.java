// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.polyworld;

import org.junit.Before;
import org.terasology.math.geom.Rect2f;
import org.terasology.math.geom.Rect2i;
import org.terasology.polyworld.graph.GridGraph;

/**
 * TODO Type description
 */
public class GridGraphTest extends GraphTest {

    @Before
    public void setup() {
        final int width = 512;
        final int height = 256;

        intBounds = Rect2i.createFromMinAndSize(0, 0, width, height);
        realBounds = Rect2f.createFromMinAndSize(intBounds.minX(), intBounds.minY(), intBounds.width(),
                intBounds.height());

        graph = new GridGraph(intBounds, 7, 11);
    }
}
