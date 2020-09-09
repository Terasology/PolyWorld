// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.polyworld.debug;

import org.terasology.math.geom.BaseVector2f;
import org.terasology.polyworld.graph.Edge;
import org.terasology.polyworld.graph.Graph;
import org.terasology.polyworld.rivers.RiverModel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Draws the generated rivers on a AWT graphics instance
 */
public class RiverPainter {

    private Color riverColor = new Color(0x225588);

    /**
     *
     */
    public RiverPainter() {
    }

    public void drawRivers(Graphics2D g, RiverModel riverModel, Graph graph) {
        for (Edge e : graph.getEdges()) {
            int riverValue = riverModel.getRiverValue(e);
            if (riverValue > 0) {
                g.setStroke(new BasicStroke(1 + (int) Math.sqrt(riverValue * 2)));
                g.setColor(riverColor);
                BaseVector2f c0p = e.getCorner0().getLocation();
                BaseVector2f c1p = e.getCorner1().getLocation();
                g.drawLine((int) c0p.getX(), (int) c0p.getY(), (int) c1p.getX(), (int) c1p.getY());
            }
        }
        g.setStroke(new BasicStroke());
    }

    /**
     * @param color
     */
    public void setRiverColor(Color color) {
        this.riverColor = color;
    }
}
