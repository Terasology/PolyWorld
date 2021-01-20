/*
 * Copyright 2015 MovingBlocks
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

package org.terasology.polyworld.sampling;

import com.google.common.math.DoubleMath;
import org.joml.Vector2fc;
import org.joml.Vector2ic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.joml.geom.Rectanglef;

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
    private static float graphDensity = 10f;

    private static final Logger logger = LoggerFactory.getLogger(PoissonDiscTestView.class);

    private PoissonDiscTestView() {
        // no instances
    }

    public static void main(String[] args) {
//      int rows = DoubleMath.roundToInt(area.height() / cellSize, RoundingMode.HALF_UP);
//      int cols = DoubleMath.roundToInt(area.width() / cellSize, RoundingMode.HALF_UP);

        Rectanglef area = new Rectanglef(30, 10, 30 + 512, 10 + 256);
        int numSites = DoubleMath.roundToInt(area.area() * graphDensity / 1000, RoundingMode.HALF_UP);

        logger.info("START GRID");
        PoissonDiscSampling sampling = new PoissonDiscSampling();
        List<Vector2fc> points = sampling.create(area, numSites);
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
                for (Vector2fc pt : points) {
                    g.draw(new Line2D.Float(pt.x(), pt.y(), pt.x(), pt.y()));
                }
            }

            private void drawDebug(Rectanglef bounds, Graphics2D g) {
                g.setColor(Color.LIGHT_GRAY);
                Vector2ic dims = sampling.getGridDimensions(bounds, numSites);
                int cols = dims.x();
                int rows = dims.y();

                float cellWidth = (float) (bounds.maxX - bounds.minX) / cols;
                float cellHeight = (float) (bounds.maxY - bounds.minY) / rows;

                g.translate(bounds.minX, bounds.minY);
                for (int i = 0; i <= cols; i++) {
                    g.drawLine((int) (i * cellWidth), 0, (int) (i * cellWidth), (int) (bounds.maxY - bounds.minY));
                }
                for (int i = 0; i <= rows; i++) {
                    g.drawLine(0, (int) (i * cellHeight), (int) (bounds.maxX - bounds.minX), (int) (i * cellHeight));
                }
                g.translate(-bounds.minX, -bounds.minY);
            }
        });
        frame.setVisible(true);
    }
}
