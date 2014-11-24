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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.math.Vector2i;
import org.terasology.math.geom.Vector2d;
import org.terasology.polyworld.voronoi.Graph;
import org.terasology.polyworld.voronoi.GraphFacet;
import org.terasology.polyworld.voronoi.Triangle;
import org.terasology.polyworld.water.WaterModel;
import org.terasology.polyworld.water.WaterModelFacet;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.facets.SeaLevelFacet;
import org.terasology.world.generation.facets.SurfaceHeightFacet;

import com.google.common.base.Stopwatch;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * TODO Type description
 * @author Martin Steiger
 */
@Produces(SurfaceHeightFacet.class)
@Requires({
        @Facet(SeaLevelFacet.class),
        @Facet(WaterModelFacet.class),
        @Facet(GraphFacet.class)
        })
public class ElevationProvider implements FacetProvider {

    private static final Logger logger = LoggerFactory.getLogger(ElevationProvider.class);

    private final Cache<Graph, ElevationModel> elevationCache = CacheBuilder.newBuilder().build();

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

        final WaterModelFacet waterFacet = region.getRegionFacet(WaterModelFacet.class);
        SeaLevelFacet seaLevelFacet = region.getRegionFacet(SeaLevelFacet.class);
        float seaLevel = seaLevelFacet.getSeaLevel();
        float seaFloor = 2.0f;
        float maxHeight = 50.0f;

        GraphFacet graphFacet = region.getRegionFacet(GraphFacet.class);

        // make sure that the sea is at least 1 block deep
        if (seaLevel <= seaFloor) {
            seaLevel = seaFloor + 1;
        }

        Stopwatch sw = Stopwatch.createStarted();
        Graph graph = null;
        Triangle prevTri = null;
        double wreg = 0;
        double wc1 = 0;
        double wc2 = 0;

        ElevationModel elevation = null;

        for (Vector2i p : facet.getWorldRegion()) {
            if (graph == null || !graph.getBounds().contains(p)) {
                graph = graphFacet.getWorld(p.x, 0, p.y);
                try {
                    final Graph finalGraph = graph;
                    elevation = elevationCache.get(finalGraph, new Callable<ElevationModel>() {

                        @Override
                        public ElevationModel call() {
                            WaterModel waterModel = waterFacet.get(finalGraph);
                            return new DefaultElevationModel(finalGraph, waterModel);
                        }
                    });
                } catch (ExecutionException e) {
                    logger.error("Could not create elevation model", e);
                    return;
                }
            }

            Triangle tri = graphFacet.getWorldTriangle(p.x, 0, p.y);

            if (tri != prevTri) {
                wreg = elevation.getElevation(tri.getRegion());
                wc1 = elevation.getElevation(tri.getCorner1());
                wc2 = elevation.getElevation(tri.getCorner2());
                prevTri = tri;
            }

            float ele = (float) tri.computeInterpolated(new Vector2d(p.x, p.y), wreg, wc1, wc2);
            float blockHeight = convertModelElevation(seaLevel, seaFloor, maxHeight, ele);

            facet.setWorld(p, blockHeight);
        }

        if (logger.isTraceEnabled()) {
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
