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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.terasology.math.delaunay.Voronoi;

/**
 * TestDriver.java
 *
 * @author Connor
 */
final class TestDriver {
    
    private TestDriver() {
        
    }

    public static void main(String[] args) {
        final int width = 1650;
        final int height = 750;
        final int numSites = 3000;
        final long seed = 92070987606126L; // System.nanoTime();

        final Random r = new Random(seed);
        System.out.println("seed: " + seed);

        //make the intial underlying voronoi structure
        final Voronoi v = new Voronoi(numSites, width, height, r);

        //assemble the voronoi strucutre into a usable graph object representing a map
        final TestGraphImpl graph = new TestGraphImpl(v, 2, r);

        final BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setColor(Color.CYAN);
        g2.fillRect(0, 0, width, height);
        
//        g2.scale(5.0, 5);
//        g2.translate(-280, -690);


        graph.paint(g2);

        final JFrame frame = new JFrame() {
            private static final long serialVersionUID = -1290616722309726306L;

            @Override
            public void paint(Graphics g) {
                g.drawImage(img, 10, 40, null);
            }
        };
        frame.setTitle("java fortune");
        frame.setVisible(true);
        frame.setSize(width + 50, height + 50);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}
