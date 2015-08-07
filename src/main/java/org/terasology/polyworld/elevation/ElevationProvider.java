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

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.math.Vector2i;
import org.terasology.math.geom.Vector2f;
import org.terasology.polyworld.graph.Graph;
import org.terasology.polyworld.graph.GraphFacet;
import org.terasology.polyworld.graph.Triangle;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.facets.SeaLevelFacet;
import org.terasology.world.generation.facets.SurfaceHeightFacet;

import com.google.common.base.Stopwatch;

/**
 * Converts graph-based elevation information of the {@link ElevationModelFacet}
 * into a continuous area.
 */
@Produces(SurfaceHeightFacet.class)
@Requires({
        @Facet(SeaLevelFacet.class),
        @Facet(ElevationModelFacet.class),
        @Facet(GraphFacet.class)
        })
public class ElevationProvider implements FacetProvider {

    private static final Logger logger = LoggerFactory.getLogger(ElevationProvider.class);

    /**
     *
     */
    public ElevationProvider() {
    }

    @Override
    public void setSeed(long seed) {
        // ignore
    }

    @Override
    public void process(GeneratingRegion region) {
        Border3D border = region.getBorderForFacet(SurfaceHeightFacet.class);
        SurfaceHeightFacet facet = new SurfaceHeightFacet(region.getRegion(), border);

        ElevationModelFacet elevationModelFacet = region.getRegionFacet(ElevationModelFacet.class);
        SeaLevelFacet seaLevelFacet = region.getRegionFacet(SeaLevelFacet.class);
        float seaLevel = seaLevelFacet.getSeaLevel();
        float seaFloor = 2.0f;
        float maxHeight = 50.0f;

        GraphFacet graphFacet = region.getRegionFacet(GraphFacet.class);

        // make sure that the sea is at least 1 block deep
        if (seaLevel <= seaFloor) {
            seaLevel = seaFloor + 1;
        }

        final boolean traceLog = logger.isTraceEnabled();
        Stopwatch sw = traceLog ? Stopwatch.createStarted() : null;

        Graph graph = null;
        Triangle prevTri = null;
        float wreg = 0;
        float wc1 = 0;
        float wc2 = 0;

        ElevationModel elevation = null;

        for (Vector2i p : facet.getWorldRegion()) {
            if (graph == null || !graph.getBounds().contains(p.x, p.y)) {
                graph = graphFacet.getWorld(p.x, 0, p.y);
                elevation = elevationModelFacet.get(graph);
            }

            Triangle tri = graphFacet.getWorldTriangle(p.x, 0, p.y);

            if (tri != prevTri) {
                wreg = elevation.getElevation(tri.getRegion());
                wc1 = elevation.getElevation(tri.getCorner1());
                wc2 = elevation.getElevation(tri.getCorner2());
                prevTri = tri;
            }

            float ele = tri.computeInterpolated(new Vector2f(p.x, p.y), wreg, wc1, wc2);
            float blockHeight = convertModelElevation(seaLevel, seaFloor, maxHeight, ele);

            facet.setWorld(p, blockHeight);
        }

        if (traceLog) {
            logger.trace("Created elevation facet for {} in {}ms.", facet.getWorldRegion(), sw.elapsed(TimeUnit.MILLISECONDS));
        }

        region.setRegionFacet(SurfaceHeightFacet.class, facet);
    }

    private float convertModelElevation(float seaLevel, float seaFloor, float maxHeight, float ele) {

        if (ele < 0) {
            return seaLevel + ele * (seaLevel - seaFloor);
        } else {
            return seaLevel + ele * maxHeight;
        }
    }
}
