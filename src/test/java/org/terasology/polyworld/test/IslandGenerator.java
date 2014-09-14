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

package org.terasology.polyworld.test;

import java.math.RoundingMode;
import java.util.List;
import java.util.Random;

import org.terasology.math.delaunay.Voronoi;
import org.terasology.math.geom.Rect2d;
import org.terasology.math.geom.Vector2d;
import org.terasology.polyworld.biome.BiomeModel;
import org.terasology.polyworld.biome.DefaultBiomeModel;
import org.terasology.polyworld.distribution.RadialDistribution;
import org.terasology.polyworld.elevation.DefaultElevationModel;
import org.terasology.polyworld.elevation.ElevationModel;
import org.terasology.polyworld.moisture.DefaultMoistureModel;
import org.terasology.polyworld.moisture.MoistureModel;
import org.terasology.polyworld.rivers.DefaultRiverModel;
import org.terasology.polyworld.rivers.RiverModel;
import org.terasology.polyworld.voronoi.Graph;
import org.terasology.polyworld.voronoi.GraphEditor;
import org.terasology.polyworld.voronoi.GridGraph;
import org.terasology.polyworld.voronoi.VoronoiGraph;
import org.terasology.polyworld.water.DefaultWaterModel;
import org.terasology.polyworld.water.WaterModel;

import com.google.common.collect.Lists;
import com.google.common.math.DoubleMath;

/**
 * TODO Type description
 * @author Martin Steiger
 */
public class IslandGenerator {

    private Graph graph;
    private BiomeModel biomeModel;
    private RiverModel riverModel;

    IslandGenerator(Rect2d bounds, long seed) {

        graph = createVoronoiGraph(bounds, seed);

        RadialDistribution waterDist = new RadialDistribution(seed);
        WaterModel waterModel = new DefaultWaterModel(graph, waterDist);
        ElevationModel elevationModel = new DefaultElevationModel(graph, waterModel);
        riverModel = new DefaultRiverModel(graph, elevationModel, waterModel);
        MoistureModel moistureModel = new DefaultMoistureModel(graph, riverModel, waterModel);
        biomeModel = new DefaultBiomeModel(elevationModel, waterModel, moistureModel);
    }

    private static Graph createVoronoiGraph(Rect2d bounds, long seed) {
        double density = 256;
        int numSites = DoubleMath.roundToInt(bounds.area() / density, RoundingMode.HALF_UP);
        final Random r = new Random(seed);

        List<Vector2d> points = Lists.newArrayListWithCapacity(numSites);
        for (int i = 0; i < numSites; i++) {
            double px = bounds.minX() + r.nextDouble() * bounds.width();
            double py = bounds.minY() + r.nextDouble() * bounds.height();
            points.add(new Vector2d(px, py));
        }

        final Voronoi v = new Voronoi(points, bounds);
        final Graph graph = new VoronoiGraph(v, 2, r);
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
        GraphEditor.jitterCorners(graph.getCorners(), maxJitter);

        return graph;
    }

    /**
     * @return
     */
    public Graph getGraph() {
        return graph;
    }

    /**
     * @return the biomeModel
     */
    public BiomeModel getBiomeModel() {
        return biomeModel;
    }

    /**
     * @return
     */
    public RiverModel getRiverModel() {
        return riverModel;
    }
}
