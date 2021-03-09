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
