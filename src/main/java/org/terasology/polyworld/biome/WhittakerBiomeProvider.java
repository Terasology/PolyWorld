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

package org.terasology.polyworld.biome;

import org.joml.Vector2ic;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.Facet;
import org.terasology.engine.world.generation.FacetProvider;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Produces;
import org.terasology.engine.world.generation.Requires;
import org.terasology.polyworld.graph.Graph;
import org.terasology.polyworld.graph.GraphFacet;
import org.terasology.polyworld.graph.GraphRegion;
import org.terasology.polyworld.graph.Triangle;

/**
 * TODO Type description
 */
@Produces(WhittakerBiomeFacet.class)
@Requires(@Facet(WhittakerBiomeModelFacet.class))
public class WhittakerBiomeProvider implements FacetProvider {

    @Override
    public void setSeed(long seed) {
        // ignore
    }

    @Override
    public void process(GeneratingRegion region) {
        Border3D border = region.getBorderForFacet(WhittakerBiomeFacet.class);
        WhittakerBiomeFacet facet = new WhittakerBiomeFacet(region.getRegion(), border);
        WhittakerBiomeModelFacet biomeModelFacet = region.getRegionFacet(WhittakerBiomeModelFacet.class);

        GraphFacet graphFacet = region.getRegionFacet(GraphFacet.class);

        Graph graph = null;
        BiomeModel model = null;

        for (Vector2ic pos : facet.getWorldArea()) {
            if (graph == null || !graph.getBounds().contains(pos.x(), pos.y())) {
                graph = graphFacet.getWorld(pos.x(), pos.y());
                model = biomeModelFacet.get(graph);
            }

            Triangle tri = graphFacet.getWorldTriangle(pos.x(), pos.y());
            GraphRegion r = tri.getRegion();

            WhittakerBiome biome = model.getBiome(r);

            facet.setWorld(pos, biome);
        }

        region.setRegionFacet(WhittakerBiomeFacet.class, facet);
    }
}
