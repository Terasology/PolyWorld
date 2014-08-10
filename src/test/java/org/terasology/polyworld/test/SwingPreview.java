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
import java.util.Random;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.terasology.math.delaunay.Voronoi;

/**
 * Preview generated world in Swing
 * @author Martin Steiger
 */
final class SwingPreview {
    
    private SwingPreview() {
        
    }

    public static void main(String[] args) {
        final int width = 1024;
        final int height = 1024;
        final int numSites = 5000;
        final long seed = System.nanoTime();

        final Random r = new Random(seed);

        final Voronoi v = new Voronoi(numSites, width, height, r);

        final VoronoiWorldGen graph = new VoronoiWorldGen(v, 2, r);

        JFrame frame = new JFrame();
        JComponent panel = new JComponent() {

            private static final long serialVersionUID = 4178713176841691478L;

            @Override
            public void paint(Graphics g) {
                graph.paint((Graphics2D) g);
            }
        };
        frame.add(panel);
        frame.setTitle("Preview - " + seed);
        frame.setVisible(true);
        frame.setSize(width + 50, height + 50);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}
