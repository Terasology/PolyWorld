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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.math.RoundingMode;
import java.util.List;
import java.util.Random;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.terasology.math.delaunay.Voronoi;
import org.terasology.math.geom.Rect2d;
import org.terasology.math.geom.Vector2d;
import org.terasology.polyworld.biome.BiomeModel;
import org.terasology.polyworld.biome.DefaultBiomeModel;
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
import org.terasology.polyworld.water.RadialWaterDistribution;
import org.terasology.polyworld.water.WaterModel;

import com.google.common.collect.Lists;
import com.google.common.math.DoubleMath;

/**
 * Preview generated world in Swing
 * @author Martin Steiger
 */
final class SwingPreview {

    private SwingPreview() {

    }

    public static void main(String[] args) {
        final int width = 512;
        final int height = 512;
        final long seed = 9782985378925l;//System.nanoTime();

        Rect2d bounds = Rect2d.createFromMinAndSize(0, 0, width, height);

//        Graph graph = createGridGraph(bounds, seed);
        final Graph graph = createVoronoiGraph(bounds, seed);

        RadialWaterDistribution waterDist = new RadialWaterDistribution(graph.getBounds());
        WaterModel waterModel = new DefaultWaterModel(graph, waterDist);
        ElevationModel elevationModel = new DefaultElevationModel(graph, waterModel);
        final RiverModel riverModel = new DefaultRiverModel(graph, elevationModel, waterModel);
        MoistureModel moistureModel = new DefaultMoistureModel(graph, riverModel, waterModel);
        final BiomeModel biomeModel = new DefaultBiomeModel(elevationModel, waterModel, moistureModel);

        final GraphPainter graphPainter = new GraphPainter();
        final BiomePainter biomePainter = new BiomePainter();
        final RiverPainter riverPainter = new RiverPainter();

        JFrame frame = new JFrame();
        JComponent panel = new JComponent() {

            private static final long serialVersionUID = 4178713176841691478L;

            @Override
            public void paint(Graphics g1) {
                Graphics2D g = (Graphics2D) g1;
                g.translate(10, 10);
                graphPainter.drawBounds(g, graph);
                graphPainter.fillBounds(g, graph);

                biomePainter.drawBiomes(g, biomeModel, graph);
                graphPainter.drawDelaunay(g, graph);

                riverPainter.drawRivers(g, riverModel, graph);

//                graphPainter.drawSites(g, graph);
//                graphPainter.drawCorners(g, graph);
            }
        };
        frame.add(panel);
        frame.setTitle("Preview - " + seed);
        frame.setVisible(true);
        frame.setSize(width + 40, height + 60);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
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
}
