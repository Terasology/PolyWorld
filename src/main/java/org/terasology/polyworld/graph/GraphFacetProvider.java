// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.polyworld.graph;

import com.google.common.base.Stopwatch;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.math.DoubleMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.utilities.random.FastRandom;
import org.terasology.engine.utilities.random.Random;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.ConfigurableFacetProvider;
import org.terasology.engine.world.generation.Facet;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Produces;
import org.terasology.engine.world.generation.Requires;
import org.terasology.math.geom.Rect2f;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2f;
import org.terasology.nui.properties.Range;
import org.terasology.polyworld.TriangleLookup;
import org.terasology.polyworld.math.delaunay.Voronoi;
import org.terasology.polyworld.rp.RegionType;
import org.terasology.polyworld.rp.WorldRegion;
import org.terasology.polyworld.rp.WorldRegionFacet;
import org.terasology.polyworld.sampling.PointSampling;
import org.terasology.polyworld.sampling.PoissonDiscSampling;

import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * TODO Type description
 */
@Produces(GraphFacet.class)
@Requires(@Facet(WorldRegionFacet.class))
public class GraphFacetProvider implements ConfigurableFacetProvider {

    private static final Logger logger = LoggerFactory.getLogger(GraphFacetProvider.class);

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(false);
    private final CacheLoader<Graph, TriangleLookup> lookupLoader = new CacheLoader<Graph, TriangleLookup>() {

        @Override
        public TriangleLookup load(Graph graph) throws Exception {
            return new TriangleLookup(graph);
        }
    };
    private final LoadingCache<WorldRegion, Graph> graphCache;
    private final LoadingCache<Graph, TriangleLookup> lookupCache;
    private long seed;
    private int graphUniformity = 1;
    private GraphProviderConfiguration configuration = new GraphProviderConfiguration();
    private final CacheLoader<WorldRegion, Graph> graphLoader = new CacheLoader<WorldRegion, Graph>() {

        @Override
        public Graph load(WorldRegion wr) throws Exception {
            Stopwatch sw = Stopwatch.createStarted();

            Graph graph = createGraph(wr);

            logger.info("Created graph for {} in {}ms.", wr.getArea(), sw.elapsed(TimeUnit.MILLISECONDS));

            return graph;
        }
    };

    /**
     * @param maxCacheSize maximum number of cached graphs
     */
    public GraphFacetProvider(int maxCacheSize) {
        graphCache = CacheBuilder.newBuilder().maximumSize(maxCacheSize).build(graphLoader);
        lookupCache = CacheBuilder.newBuilder().maximumSize(maxCacheSize).build(lookupLoader);
    }

    public GraphFacetProvider(int maxCacheSize, float graphDensity, int graphUniformity) {
        graphCache = CacheBuilder.newBuilder().maximumSize(maxCacheSize).build(graphLoader);
        lookupCache = CacheBuilder.newBuilder().maximumSize(maxCacheSize).build(lookupLoader);
        configuration.graphDensity = graphDensity;
        this.graphUniformity = graphUniformity;
    }

    private static Graph createGridGraph(Rect2i bounds, int rows, int cols) {

        Rect2i doubleBounds = Rect2i.createFromMinAndSize(bounds.minX(), bounds.minY(), bounds.width(),
                bounds.height());
        final Graph graph = new GridGraph(doubleBounds, rows, cols);

        return graph;
    }

    @Override
    public void setSeed(long seed) {
        this.seed = seed;
    }

    @Override
    public void process(GeneratingRegion region) {
        Border3D border = region.getBorderForFacet(GraphFacet.class);
        GraphFacetImpl facet = new GraphFacetImpl(region.getRegion(), border);
        WorldRegionFacet regionFacet = region.getRegionFacet(WorldRegionFacet.class);

        Collection<WorldRegion> areas = regionFacet.getRegions();


        for (WorldRegion wr : areas) {
            Graph graph = graphCache.getIfPresent(wr);
            TriangleLookup lookup = graph == null ? null : lookupCache.getIfPresent(graph);
            if (lookup == null) {
                try {
                    lock.readLock().lock();
                    graph = graphCache.getUnchecked(wr);
                    lookup = lookupCache.getUnchecked(graph);
                } finally {
                    lock.readLock().unlock();
                }
            }
            facet.add(wr, graph, lookup);
        }

        region.setRegionFacet(GraphFacet.class, facet);
    }

    private Graph createGraph(WorldRegion wr) {
        Rect2i area = wr.getArea();
        if (wr.getType() == RegionType.OCEAN) {
//            int rows = DoubleMath.roundToInt(area.height() / cellSize, RoundingMode.HALF_UP);
//            int cols = DoubleMath.roundToInt(area.width() / cellSize, RoundingMode.HALF_UP);
            return createGridGraph(area, 1, 1);
        } else {
            int numSites = DoubleMath.roundToInt(area.area() * configuration.graphDensity / 1000, RoundingMode.HALF_UP);
            return createVoronoiGraph(area, numSites);
        }
    }

    private Graph createVoronoiGraph(Rect2i bounds, int numSites) {

        // use different seeds for different areas.
        // also use the number of target sites since similar numbers could lead to identical
        // distributions otherwise.
        long areaSeed = seed ^ bounds.hashCode() ^ numSites;
        final Random rng = new FastRandom(areaSeed);

        PointSampling sampling = new PoissonDiscSampling();

        Rect2f doubleBounds = Rect2f.createFromMinAndSize(0, 0, bounds.width(), bounds.height());

        // avoid very small triangles at the border by adding a 5 block border
        Rect2f islandBounds = Rect2f.createFromMinAndSize(5, 5, bounds.width() - 10, bounds.height() - 10);
        List<Vector2f> points = sampling.create(islandBounds, numSites, rng);

        Voronoi v = new Voronoi(points, doubleBounds);

        // Lloyd relaxation makes regions more uniform
        for (int i = 0; i < graphUniformity; i++) {
            v = GraphEditor.lloydRelaxation(v);
        }
        final Graph graph = new VoronoiGraph(bounds, v);
        GraphEditor.improveCorners(graph.getCorners());

        return graph;
    }

    @Override
    public String getConfigurationName() {
        return "Voronoi Graphs";
    }

    @Override
    public Component getConfiguration() {
        return configuration;
    }

    @Override
    public void setConfiguration(Component configuration) {
        try {
            lock.writeLock().lock();
            this.configuration = (GraphProviderConfiguration) configuration;
            graphCache.invalidateAll();
            lookupCache.invalidateAll();
        } finally {
            lock.writeLock().unlock();
        }
    }

    private static class GraphProviderConfiguration implements Component {
        @Range(min = 0.1f, max = 10f, increment = 0.1f, precision = 1, description = "Define the density for graph " +
                "cells")
        private float graphDensity = 2f;
    }
}
