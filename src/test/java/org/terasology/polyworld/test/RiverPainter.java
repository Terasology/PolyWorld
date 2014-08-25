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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import org.terasology.math.geom.Vector2d;
import org.terasology.polyworld.rivers.RiverModel;
import org.terasology.polyworld.voronoi.Edge;
import org.terasology.polyworld.voronoi.Graph;

/**
 * Draws the generated rivers on a AWT graphics instance
 * @author Martin Steiger
 */
public class RiverPainter {

    private Color riverColor = new Color(0x225588);

    /**
     */
    public RiverPainter() {
    }

    public void drawRivers(Graphics2D g, RiverModel riverModel, Graph graph) {
        for (Edge e : graph.getEdges()) {
            int riverValue = riverModel.getRiverValue(e);
            if (riverValue > 0) {
                g.setStroke(new BasicStroke(1 + (int) Math.sqrt(riverValue * 2)));
                g.setColor(riverColor);
                Vector2d c0p = e.getCorner0().getLocation();
                Vector2d c1p = e.getCorner1().getLocation();
                g.drawLine((int) c0p.getX(), (int) c0p.getY(), (int) c1p.getX(), (int) c1p.getY());
            }
        }
    }

    /**
     * @param color
     */
    public void setRiverColor(Color color) {
        this.riverColor = color;
    }
}
