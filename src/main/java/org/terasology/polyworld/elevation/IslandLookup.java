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
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.commonworld.Sector;
import org.terasology.math.Rect2i;
import org.terasology.math.Vector2i;
import org.terasology.math.delaunay.Voronoi;
import org.terasology.math.geom.Rect2d;
import org.terasology.math.geom.Vector2d;
import org.terasology.polyworld.voronoi.Graph;
import org.terasology.polyworld.voronoi.GraphEditor;
import org.terasology.polyworld.voronoi.VoronoiGraph;

import com.google.common.base.Stopwatch;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.math.DoubleMath;

/**
 * Caches graphs per sector
 * @author Martin Steiger
 */
public class IslandLookup {

    private static final Logger logger = LoggerFactory.getLogger(IslandLookup.class);

    private LoadingCache<Rect2i, Graph> graphCache = CacheBuilder.newBuilder().build(new CacheLoader<Rect2i, Graph>() {

        @Override
        public Graph load(Rect2i area) throws Exception {
            Stopwatch sw = Stopwatch.createStarted();

            Graph graph = createVoronoiGraph(area, seed ^ area.hashCode());

            logger.debug("Created graph for {} in {}ms.", area, sw.elapsed(TimeUnit.MILLISECONDS));

            return graph;
        }
    });

    private Set<Rect2i> areas = Sets.newLinkedHashSet();

    private long seed;

    /**
     * @param sector
     */
    public IslandLookup(Sector sector, long seed) {
        this.seed = seed;
        Rect2i fullArea = sector.getWorldBounds();
        int width = fullArea.width() / 2;
        int height = fullArea.height() / 2;

        int x = fullArea.minX();
        int y = fullArea.minY();
        areas.add(Rect2i.createFromMinAndSize(x, y, width, height));

        x = fullArea.minX() + width;
        areas.add(Rect2i.createFromMinAndSize(x, y, width, height));

        y = fullArea.minY() + height;
        areas.add(Rect2i.createFromMinAndSize(x, y, width, height));

        x = fullArea.minX();
        areas.add(Rect2i.createFromMinAndSize(x, y, width, height));
    }

    /**
     * @param p
     * @return
     */
    public Graph getGraphAt(Vector2i p) {
        for (Rect2i area : areas) {
            if (area.contains(p)) {
                return graphCache.getUnchecked(area);
            }
        }

        return null;
    }

    private static Graph createVoronoiGraph(Rect2i bounds, long seed) {
        double density = 500;
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


//    private static Graph createGridGraph(Rect2i bounds, long seed) {
//        double cellSize = 16;
//
//        int rows = DoubleMath.roundToInt(bounds.height() / cellSize, RoundingMode.HALF_UP);
//        int cols = DoubleMath.roundToInt(bounds.width() / cellSize, RoundingMode.HALF_UP);
//
//        Rect2d doubleBounds = Rect2d.createFromMinAndSize(bounds.minX(), bounds.minY(), bounds.width(), bounds.height());
//        final Graph graph = new GridGraph(doubleBounds, rows, cols);
//        double maxJitterX = bounds.width() / cols * 0.5;
//        double maxJitterY = bounds.height() / rows * 0.5;
//        double maxJitter = Math.min(maxJitterX, maxJitterY);
//        GraphEditor.jitterCorners(graph.getCorners(), new FastRandom(seed), maxJitter);
//
//        return graph;
//    }
}
