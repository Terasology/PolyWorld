// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.polyworld.viewer.layers;

import org.terasology.engine.world.viewer.layers.NominalFacetLayer;
import org.terasology.engine.world.viewer.layers.Renders;
import org.terasology.engine.world.viewer.layers.ZOrder;
import org.terasology.polyworld.biome.WhittakerBiome;
import org.terasology.polyworld.biome.WhittakerBiomeFacet;

/**
 * Maps {@link WhittakerBiome} facet to corresponding colors.
 */
@Renders(value = WhittakerBiomeFacet.class, order = ZOrder.BIOME)
public class WhittakerBiomeFacetLayer extends NominalFacetLayer<WhittakerBiome> {

    public WhittakerBiomeFacetLayer() {
        super(WhittakerBiomeFacet.class, new WhittakerBiomeColors());
    }
}
