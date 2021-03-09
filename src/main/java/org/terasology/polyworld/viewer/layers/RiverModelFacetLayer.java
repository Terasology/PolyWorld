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

package org.terasology.polyworld.viewer.layers;

import org.joml.Vector2fc;
import org.terasology.engine.world.generation.Region;
import org.terasology.engine.world.viewer.layers.AbstractFacetLayer;
import org.terasology.engine.world.viewer.layers.Renders;
import org.terasology.engine.world.viewer.layers.ZOrder;
import org.terasology.polyworld.graph.Edge;
import org.terasology.polyworld.graph.Graph;
import org.terasology.polyworld.rivers.RiverModel;
import org.terasology.polyworld.rivers.RiverModelFacet;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Draws the generated rivers on a AWT graphics instance
 */
@Renders(value = RiverModelFacet.class, order = ZOrder.BIOME + 5)
public class RiverModelFacetLayer extends AbstractFacetLayer {

    private Color riverColor = new Color(0x225588ff);

    public RiverModelFacetLayer() {
        // use default settings
    }

    @Override
    public void render(BufferedImage img, org.terasology.engine.world.generation.Region region) {
        RiverModelFacet riverModelFacet = region.getFacet(RiverModelFacet.class);

        Graphics2D g = img.createGraphics();
        int dx = region.getRegion().minX();
        int dy = region.getRegion().minZ();
        g.translate(-dx, -dy);

        for (Graph graph : riverModelFacet.getKeys()) {
            RiverModel model = riverModelFacet.get(graph);
            drawRivers(g, model, graph);
        }

        g.dispose();
    }

    @Override
    public String getWorldText(Region region, int wx, int wy) {
        RiverModelFacet riverModelFacet = region.getFacet(RiverModelFacet.class);
        for (Graph graph : riverModelFacet.getKeys()) {
            if (graph.getBounds().contains(wx, wy)) {
                RiverModel model = riverModelFacet.get(graph);

//                return String.format("%d regs, %d corners, %d edges",
//                        graph.getRegions().size(), graph.getCorners().size(), graph.getEdges().size());
            }
        }
        return null;
    }

    public void drawRivers(Graphics2D g, RiverModel riverModel, Graph graph) {
        for (Edge e : graph.getEdges()) {
            int riverValue = riverModel.getRiverValue(e);
            if (riverValue > 0) {
                g.setStroke(new BasicStroke(1 + (int) Math.sqrt(riverValue * 2)));
                g.setColor(riverColor);
                Vector2fc c0p = e.getCorner0().getLocation();
                Vector2fc c1p = e.getCorner1().getLocation();
                g.drawLine((int) c0p.x(), (int) c0p.y(), (int) c1p.x(), (int) c1p.y());
            }
        }
        g.setStroke(new BasicStroke());
    }
}
