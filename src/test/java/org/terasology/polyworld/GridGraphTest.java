/*
 * Copyright 2015 MovingBlocks
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

package org.terasology.polyworld;

import org.joml.Rectanglef;
import org.junit.Before;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Rect2f;
import org.terasology.polyworld.graph.GridGraph;
import org.terasology.world.block.BlockArea;

/**
 * TODO Type description
 */
public class GridGraphTest extends GraphTest {

    @Before
    public void setup() {
        final int width = 512;
        final int height = 256;

        intBounds = new BlockArea(0, 0, width, height);
        realBounds = intBounds.getBounds(new Rectanglef());
        
        graph = new GridGraph(intBounds, 7, 11);
    }
}
