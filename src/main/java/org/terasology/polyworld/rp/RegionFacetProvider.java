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

import java.util.Collection;

import org.terasology.commonworld.Sector;
import org.terasology.commonworld.Sectors;
import org.terasology.entitySystem.Component;
import org.terasology.math.Rect2i;
import org.terasology.math.Region3i;
import org.terasology.math.Vector3i;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.ConfigurableFacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;

/**
 * TODO Type description
 * @author Martin Steiger
 */
@Produces(RegionFacet.class)
public class RegionFacetProvider implements ConfigurableFacetProvider {

    private RegionProvider regionProvider;
    private Configuration configuration = new Configuration();

    @Override
    public void setSeed(long seed) {
        regionProvider = new SubdivRegionProvider(seed, 4, configuration.minSize);
    }

    @Override
    public void process(GeneratingRegion region) {
        Border3D border = region.getBorderForFacet(RegionFacet.class);
        RegionFacet facet = new RegionFacet(region.getRegion(), border);

        Region3i worldRegion = facet.getWorldRegion();

        Vector3i min = worldRegion.min();
        Vector3i max = worldRegion.max();
        Sector minSec = Sectors.getSectorForBlock(min.x, min.z);
        Sector maxSec = Sectors.getSectorForBlock(max.x, max.z);

        Rect2i target = Rect2i.createFromMinAndMax(min.x, min.z, max.x, max.z);

        for (int sx = minSec.getCoords().x; sx <= maxSec.getCoords().x; sx++) {
            for (int sz = minSec.getCoords().y; sz <= maxSec.getCoords().y; sz++) {
                Sector sector = Sectors.getSector(sx, sz);
                Rect2i fullArea = sector.getWorldBounds();
                for (Rect2i area : regionProvider.getSectorRegions(fullArea)) {
                    if (area.overlaps(target)) {
                        facet.addRegion(area);
                    }
                }
            }
        }

        region.setRegionFacet(RegionFacet.class, facet);
    }

    @Override
    public String getConfigurationName() {
        return "Regions";
    }

    @Override
    public Component getConfiguration() {
        return configuration;
    }

    @Override
    public void setConfiguration(Component configuration) {
        this.configuration = (Configuration) configuration;
    }

    private static class Configuration implements Component {

        @Range(min = 50, max = 500f, increment = 10f, precision = 0, description = "Minimum size of a region")
        private int minSize = 200;
    }
}
