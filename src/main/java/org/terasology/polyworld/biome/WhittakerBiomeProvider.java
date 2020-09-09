// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.polyworld.biome;

import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.Facet;
import org.terasology.engine.world.generation.FacetProvider;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Produces;
import org.terasology.engine.world.generation.Requires;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.polyworld.graph.Graph;
import org.terasology.polyworld.graph.GraphFacet;
import org.terasology.polyworld.graph.Region;
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

        for (BaseVector2i pos : facet.getWorldRegion().contents()) {
            if (graph == null || !graph.getBounds().contains(pos.x(), pos.y())) {
                graph = graphFacet.getWorld(pos.x(), pos.y());
                model = biomeModelFacet.get(graph);
            }

            Triangle tri = graphFacet.getWorldTriangle(pos.x(), pos.y());
            Region r = tri.getRegion();

            WhittakerBiome biome = model.getBiome(r);

            facet.setWorld(pos, biome);
        }

        region.setRegionFacet(WhittakerBiomeFacet.class, facet);
    }
}
