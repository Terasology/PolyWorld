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

package org.terasology.polyworld.graph;

import org.joml.Vector2ic;
import org.terasology.engine.world.generation.WorldFacet2D;
import org.terasology.polyworld.rp.WorldRegion;

import java.util.Collection;

/**
 * TODO Type description
 */
public interface GraphFacet extends WorldFacet2D {

    Graph get(int x, int z);

    Graph get(Vector2ic pos);

    Graph getWorld(int x, int z);

    Graph getWorld(Vector2ic pos);

    Collection<Graph> getAllGraphs();

    Graph getGraph(WorldRegion wr);

    Triangle getWorldTriangle(int x, int z);
}
