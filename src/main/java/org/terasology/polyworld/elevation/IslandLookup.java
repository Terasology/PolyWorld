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

package org.terasology.polyworld.elevation;

import java.util.Set;

import org.terasology.commonworld.Sector;
import org.terasology.math.Rect2i;
import org.terasology.polyworld.IslandGenerator;
import org.terasology.polyworld.TriangleLookup;
import org.terasology.polyworld.voronoi.Graph;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Sets;

/**
 * Caches graphs per sector
 * @author Martin Steiger
 */
public class IslandLookup {

    // TODO: merge with into the graph cache
    private final LoadingCache<Graph, IslandGenerator> modelCache = CacheBuilder.newBuilder().build(new CacheLoader<Graph, IslandGenerator>() {

        @Override
        public IslandGenerator load(Graph key) throws Exception {
            long islandSeed = seed ^ key.getBounds().hashCode();
            return new IslandGenerator(key, islandSeed);
        }
    });

    private long seed;

    /**
     * @param sector
     */
    public IslandLookup(Sector sector, long seed) {
        this.seed = seed;
    }

    public IslandGenerator getGenerator(Graph graph) {
        return modelCache.getUnchecked(graph);
    }
}
