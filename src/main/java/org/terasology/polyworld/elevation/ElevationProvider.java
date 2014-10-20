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

import java.math.RoundingMode;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.commonworld.Sector;
import org.terasology.commonworld.Sectors;
import org.terasology.math.Rect2i;
import org.terasology.math.Vector2i;
import org.terasology.math.delaunay.Voronoi;
import org.terasology.math.geom.Rect2d;
import org.terasology.math.geom.Vector2d;
import org.terasology.polyworld.IslandGenerator;
import org.terasology.polyworld.TriangleLookup;
import org.terasology.polyworld.voronoi.Graph;
import org.terasology.polyworld.voronoi.GraphEditor;
import org.terasology.polyworld.voronoi.GridGraph;
import org.terasology.polyworld.voronoi.Triangle;
import org.terasology.polyworld.voronoi.VoronoiGraph;
import org.terasology.utilities.random.FastRandom;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.facets.SeaLevelFacet;
import org.terasology.world.generation.facets.SurfaceHeightFacet;

import com.google.common.base.Stopwatch;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.math.DoubleMath;

/**
 * TODO Type description
 * @author Martin Steiger
 */
@Produces(SurfaceHeightFacet.class)
@Requires(@Facet(SeaLevelFacet.class))
public class ElevationProvider implements FacetProvider {

    private static final Logger logger = LoggerFactory.getLogger(ElevationProvider.class);

    private long seed;

    private LoadingCache<Sector, Graph> graphCache = CacheBuilder.newBuilder().build(new CacheLoader<Sector, Graph>() {

        @Override
        public Graph load(Sector key) throws Exception {
            Stopwatch sw = Stopwatch.createStarted();
            Graph graph = createVoronoiGraph(key.getWorldBounds(), seed);
            logger.debug("Created graph for {} in {}ms.", key, sw.elapsed(TimeUnit.MILLISECONDS));

            return graph;
        }
    });

    private LoadingCache<Graph, TriangleLookup> lookupCache = CacheBuilder.newBuilder().build(new CacheLoader<Graph, TriangleLookup>() {

        @Override
        public TriangleLookup load(Graph graph) throws Exception {
            return new TriangleLookup(graph);
        }
    });

    private LoadingCache<Graph, IslandGenerator> modelCache = CacheBuilder.newBuilder().build(new CacheLoader<Graph, IslandGenerator>() {

        @Override
        public IslandGenerator load(Graph key) throws Exception {
            return new IslandGenerator(key, seed);
        }
    });

    @Override
    public void setSeed(long seed) {
        this.seed = seed;
    }

    @Override
    public void process(GeneratingRegion region) {
        Border3D border = region.getBorderForFacet(SurfaceHeightFacet.class);
        SurfaceHeightFacet facet = new SurfaceHeightFacet(region.getRegion(), border);

        SeaLevelFacet seaLevelFacet = region.getRegionFacet(SeaLevelFacet.class);
        float seaLevel = seaLevelFacet.getSeaLevel();
        float seaFloor = 2.0f;
        float maxHeight = 50.0f;

        // make sure that the sea is at least 1 block deep
        if (seaLevel <= seaFloor) {
            seaLevel = seaFloor + 1;
        }

        Stopwatch sw = Stopwatch.createStarted();
        Sector sector = null;
        IslandGenerator model = null;
        TriangleLookup lookup = null;
        Triangle prevTri = null;
        double wreg = 0;
        double wc1 = 0;
        double wc2 = 0;

        for (Vector2i p : facet.getWorldRegion()) {
            if (sector == null || !sector.getWorldBounds().contains(p)) {
                Sector sec = Sectors.getSectorForBlock(p.x, p.y);
                Graph graph = graphCache.getUnchecked(sec);
                model = modelCache.getUnchecked(graph);
                lookup = lookupCache.getUnchecked(graph);
                sector = sec;
            }

            @SuppressWarnings("null")
            Triangle tri = lookup.findTriangleAt(p.x, p.y);

            if (tri != prevTri) {
                @SuppressWarnings("null")
                ElevationModel elevation = model.getElevationModel();
                wreg = elevation.getElevation(tri.getRegion());
                wc1 = elevation.getElevation(tri.getCorner1());
                wc2 = elevation.getElevation(tri.getCorner1());
                prevTri = tri;
            }

            float ele = (float) tri.computeInterpolated(new Vector2d(p.x, p.y), wreg, wc1, wc2);
            float clampedEle = Math.max(ele, -1f);
            float blockHeight = convertModelElevation(seaLevel, seaFloor, maxHeight, clampedEle);

            facet.setWorld(p, blockHeight);
        }

        logger.debug("Created elevation facet for {} in {}ms.", facet.getWorldRegion(), sw.elapsed(TimeUnit.MILLISECONDS));

        region.setRegionFacet(SurfaceHeightFacet.class, facet);
    }

    private float convertModelElevation(float seaLevel, float seaFloor, float maxHeight, float ele) {

        if (ele < 0) {
            return seaLevel + ele * (seaLevel - seaFloor);
        } else {
            return seaLevel + ele * maxHeight;
        }
    }

    private static Graph createVoronoiGraph(Rect2i bounds, long seed) {
//        double density = 256;
        double density = 2500;
        int numSites = DoubleMath.roundToInt(bounds.area() / density, RoundingMode.HALF_UP);
        final Random r = new Random(seed);

        List<Vector2d> points = Lists.newArrayListWithCapacity(numSites);
        for (int i = 0; i < numSites; i++) {
            double px = bounds.minX() + r.nextDouble() * bounds.width();
            double py = bounds.minY() + r.nextDouble() * bounds.height();
            points.add(new Vector2d(px, py));
        }

        Rect2d doubleBounds = Rect2d.createFromMinAndSize(bounds.minX(), bounds.minY(), bounds.width(), bounds.height());
        final Voronoi v = new Voronoi(points, doubleBounds);
        final Graph graph = new VoronoiGraph(v, 2);
        GraphEditor.improveCorners(graph.getCorners());

        return graph;
    }


    private static Graph createGridGraph(Rect2d bounds, long seed) {
        double cellSize = 16;

        int rows = DoubleMath.roundToInt(bounds.height() / cellSize, RoundingMode.HALF_UP);
        int cols = DoubleMath.roundToInt(bounds.width() / cellSize, RoundingMode.HALF_UP);

        final Graph graph = new GridGraph(bounds, rows, cols);
        double maxJitterX = bounds.width() / cols * 0.5;
        double maxJitterY = bounds.height() / rows * 0.5;
        double maxJitter = Math.min(maxJitterX, maxJitterY);
        GraphEditor.jitterCorners(graph.getCorners(), new FastRandom(seed), maxJitter);

        return graph;
    }
}
