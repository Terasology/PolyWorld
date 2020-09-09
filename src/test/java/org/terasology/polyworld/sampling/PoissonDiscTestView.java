// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.polyworld.sampling;

import com.google.common.math.DoubleMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.math.geom.Rect2f;
import org.terasology.math.geom.Vector2f;
import org.terasology.math.geom.Vector2i;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.math.RoundingMode;
import java.util.List;

public final class PoissonDiscTestView {
    private static final Logger logger = LoggerFactory.getLogger(PoissonDiscTestView.class);
    private static final float graphDensity = 10f;

    private PoissonDiscTestView() {
        // no instances
    }

    public static void main(String[] args) {
//      int rows = DoubleMath.roundToInt(area.height() / cellSize, RoundingMode.HALF_UP);
//      int cols = DoubleMath.roundToInt(area.width() / cellSize, RoundingMode.HALF_UP);

        Rect2f area = Rect2f.createFromMinAndSize(30, 10, 512, 256);
        int numSites = DoubleMath.roundToInt(area.area() * graphDensity / 1000, RoundingMode.HALF_UP);

        logger.info("START GRID");
        PoissonDiscSampling sampling = new PoissonDiscSampling();
        List<Vector2f> points = sampling.create(area, numSites);
        logger.info("END GRID");

        System.out.println("SHOULD BE: " + numSites);
        System.out.println("REALITY  : " + points.size());

        JFrame frame = new JFrame();
        frame.setSize(1200, 600);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new JPanel() {
            private static final long serialVersionUID = 7926239205123946814L;

            @Override
            protected void paintComponent(Graphics g1) {
                super.paintComponent(g1);
                int scale = 2;

                Graphics2D g = (Graphics2D) g1;
                g.setColor(Color.GRAY);
                g.setStroke(new BasicStroke(0f));
                g.setTransform(AffineTransform.getScaleInstance(scale, scale));

                drawDebug(area, g);

                g.setColor(Color.BLACK);
                g.setStroke(new BasicStroke(1f));
                for (Vector2f pt : points) {
                    g.draw(new Line2D.Float(pt.getX(), pt.getY(), pt.getX(), pt.getY()));
                }
            }

            private void drawDebug(Rect2f bounds, Graphics2D g) {
                g.setColor(Color.LIGHT_GRAY);
                Vector2i dims = sampling.getGridDimensions(bounds, numSites);
                int cols = dims.getX();
                int rows = dims.getY();

                float cellWidth = bounds.width() / cols;
                float cellHeight = bounds.height() / rows;

                g.translate(bounds.minX(), bounds.minY());
                for (int i = 0; i <= cols; i++) {
                    g.drawLine((int) (i * cellWidth), 0, (int) (i * cellWidth), (int) bounds.height());
                }
                for (int i = 0; i <= rows; i++) {
                    g.drawLine(0, (int) (i * cellHeight), (int) bounds.width(), (int) (i * cellHeight));
                }
                g.translate(-bounds.minX(), -bounds.minY());
            }
        });
        frame.setVisible(true);
    }
}
