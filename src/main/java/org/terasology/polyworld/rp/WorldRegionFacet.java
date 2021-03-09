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

package org.terasology.polyworld.rp;

import com.google.common.collect.Lists;
import org.terasology.engine.world.block.BlockRegionc;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.facets.base.SparseFacet3D;

import java.util.Collection;
import java.util.Collections;

/**
 * TODO Type description
 */
public class WorldRegionFacet extends SparseFacet3D {

    private final Collection<WorldRegion> regions = Lists.newArrayList();

    public WorldRegionFacet(BlockRegionc targetRegion, Border3D border) {
        super(targetRegion, border);
    }

    public void addRegion(WorldRegion region) {
        regions.add(region);
    }

    public Collection<WorldRegion> getRegions() {
        return Collections.unmodifiableCollection(regions);
    }
}
