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

import org.terasology.commonworld.Sector;
import org.terasology.commonworld.Sectors;
import org.terasology.math.Vector2i;
import org.terasology.polyworld.IslandGenerator;
import org.terasology.polyworld.TriangleLookup;
import org.terasology.polyworld.elevation.IslandLookup;
import org.terasology.polyworld.voronoi.Graph;
import org.terasology.polyworld.voronoi.GraphFacet;
import org.terasology.polyworld.voronoi.Region;
import org.terasology.polyworld.voronoi.Triangle;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.Requires;

import com.google.common.cache.LoadingCache;

/**
 * TODO Type description
 * @author Martin Steiger
 */
@Produces(WhittakerBiomeFacet.class)
@Requires(@Facet(GraphFacet.class))
public class WhittakerBiomeProvider implements FacetProvider {

    private LoadingCache<Sector, IslandLookup> islandCache;

    public WhittakerBiomeProvider(LoadingCache<Sector, IslandLookup> islandCache) {
        this.islandCache = islandCache;
    }

    @Override
    public void setSeed(long seed) {
        // ignore
    }

    @Override
    public void process(GeneratingRegion region) {
        Border3D border = region.getBorderForFacet(WhittakerBiomeFacet.class);
        WhittakerBiomeFacet facet = new WhittakerBiomeFacet(region.getRegion(), border);

        GraphFacet graphFacet = region.getRegionFacet(GraphFacet.class);

        TriangleLookup lookup = null;
        BiomeModel model = null;

        for (Vector2i pos : facet.getWorldRegion()) {
            if (lookup == null || !lookup.getBounds().contains(pos)) {
                Sector sec = Sectors.getSectorForBlock(pos.x, pos.y);
                IslandLookup islandLookup = islandCache.getUnchecked(sec);
                Graph graph = graphFacet.getWorld(pos.x, 0, pos.y);
                IslandGenerator generator = islandLookup.getGenerator(graph);
                model = generator.getBiomeModel();
                lookup = islandLookup.getLookupCache(graph);
            }

            Triangle tri = lookup.findTriangleAt(pos.x, pos.y);
            Region r = tri.getRegion();

            @SuppressWarnings("null")
            WhittakerBiome biome = model.getBiome(r);

            facet.setWorld(pos, biome);
        }

        region.setRegionFacet(WhittakerBiomeFacet.class, facet);
    }
}
