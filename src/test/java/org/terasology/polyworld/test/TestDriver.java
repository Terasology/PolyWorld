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
public class TestDriver {

    public static void main(String[] args) {
        final int width = 650;
        final int height = 350;
        final int numSites = 100;
        final long seed = System.nanoTime();
        final Random r = new Random();
        System.out.println("seed: " + seed);

        //make the intial underlying voronoi structure
        final Voronoi v = new Voronoi(numSites, width, height, r);

        //assemble the voronoi strucutre into a usable graph object representing a map
        final TestGraphImpl graph = new TestGraphImpl(v, 2, r);

        final BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setColor(Color.PINK);
        g2.fillRect(0, 0, width, height);

        graph.paint(g2);

        final JFrame frame = new JFrame() {
            private static final long serialVersionUID = -1290616722309726306L;

            @Override
            public void paint(Graphics g) {
                g.drawImage(img, 0, 0, null);
            }
        };
        frame.setTitle("java fortune");
        frame.setVisible(true);
        frame.setSize(width + 50, height + 50);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}
