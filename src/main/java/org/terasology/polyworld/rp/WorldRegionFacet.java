// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.polyworld.rp;

import com.google.common.collect.Lists;
import org.terasology.engine.math.Region3i;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.facets.base.SparseFacet3D;

import java.util.Collection;
import java.util.Collections;

/**
 * TODO Type description
 */
public class WorldRegionFacet extends SparseFacet3D {

    private final Collection<WorldRegion> regions = Lists.newArrayList();

    public WorldRegionFacet(Region3i targetRegion, Border3D border) {
        super(targetRegion, border);
    }

    public void addRegion(WorldRegion region) {
        regions.add(region);
    }

    public Collection<WorldRegion> getRegions() {
        return Collections.unmodifiableCollection(regions);
    }
}
