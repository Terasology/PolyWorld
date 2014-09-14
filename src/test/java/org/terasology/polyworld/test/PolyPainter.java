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
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.terasology.polyworld.voronoi.Corner;
import org.terasology.polyworld.voronoi.Graph;
import org.terasology.polyworld.voronoi.Region;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

/**
 * Draws the generated graph on a AWT graphics instance
 * @author Martin Steiger
 */
class PolyPainter {

    private final Map<Region, Polygon> regs = Maps.newHashMap();
    private Graph graph;

    /**
     * @param graph the graph that is painted
     */
    public PolyPainter(Graph graph) {
        this.graph = graph;
        for (final Region c : graph.getRegions()) {
            List<Corner> pts = new ArrayList<>(c.getCorners());
            Collections.sort(pts, new AngleSorter(c));
            regs.put(c, createPolygon(pts));
        }
    }

    public void drawRegions(Graphics2D g, Function<Region, Color> colorFunc) {
        List<Region> centers = graph.getRegions();

        for (final Region c : centers) {

            Color col = colorFunc.apply(c);
            g.setColor(col);
            g.fill(regs.get(c));
        }
    }

    private Polygon createPolygon(List<Corner> pts) {
        int[] x = new int[pts.size()];
        int[] y = new int[pts.size()];

        for (int i = 0; i < pts.size(); i++) {
            x[i] = (int) pts.get(i).getLocation().getX();
            y[i] = (int) pts.get(i).getLocation().getY();
        }

        return new Polygon(x, y, pts.size());
    }
}
