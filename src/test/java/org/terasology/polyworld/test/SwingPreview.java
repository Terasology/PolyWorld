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
import java.util.Collection;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.terasology.math.geom.Rect2d;
import org.terasology.polyworld.rivers.RiverModel;
import org.terasology.polyworld.voronoi.Graph;

import com.google.common.collect.ImmutableList;

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

        Rect2d bounds1 = Rect2d.createFromMinAndSize(0, 0, width, height);
        Rect2d bounds2 = Rect2d.createFromMinAndSize(width, 0, width, height);

        IslandGenerator sm1 = new IslandGenerator(bounds1, 1234);
        IslandGenerator sm2 = new IslandGenerator(bounds2, 5678);
        final Collection<IslandGenerator> sectors = ImmutableList.of(sm1, sm2);

        final GraphPainter graphPainter = new GraphPainter();
        final RiverPainter riverPainter = new RiverPainter();

        JFrame frame = new JFrame();
        JComponent panel = new JComponent() {

            private static final long serialVersionUID = 4178713176841691478L;

            @Override
            public void paint(Graphics g1) {
                Graphics2D g = (Graphics2D) g1;
                g.translate(10, 10);

                for (IslandGenerator sm : sectors) {
                    Graph graph = sm.getGraph();
                    BiomeColors colorFunc = new BiomeColors(sm.getBiomeModel());

                    graphPainter.fillBounds(g, graph);
                    graphPainter.drawPolys(g, graph, colorFunc);

                    RiverModel riverModel = sm.getRiverModel();
                    riverPainter.drawRivers(g, riverModel, graph);

//                    graphPainter.drawSites(g, graph);
//                    graphPainter.drawCorners(g, graph);
//                    graphPainter.drawBounds(g, graph);
                }
            }
        };
        frame.add(panel);
        frame.setTitle("Preview - " + seed);
        frame.setVisible(true);
        frame.setSize(2 * width + 40, height + 60);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}
