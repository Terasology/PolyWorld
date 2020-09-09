// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.polyworld.biome;

import org.terasology.engine.math.Region3i;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.facets.base.BaseObjectFacet2D;

/**
 * A {@link org.terasology.world.generation.WorldFacet2D} that provides {@link WhittakerBiome}
 */
public class WhittakerBiomeFacet extends BaseObjectFacet2D<WhittakerBiome> {

    public WhittakerBiomeFacet(Region3i targetRegion, Border3D border) {
        super(targetRegion, border, WhittakerBiome.class);
    }
}
