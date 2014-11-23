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

import org.terasology.math.Vector3i;
import org.terasology.world.generation.WorldFacet3D;

/**
 * TODO Type description
 * @author Martin Steiger
 */
public interface GraphFacet extends WorldFacet3D {

    Graph get(int x, int y, int z);

    Graph get(Vector3i pos);

    Graph getWorld(int x, int y, int z);

    Graph getWorld(Vector3i pos);

    Triangle getWorldTriangle(int x, int y, int z);
}
