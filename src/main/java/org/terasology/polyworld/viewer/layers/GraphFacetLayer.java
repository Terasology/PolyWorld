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

import org.joml.Rectanglef;
import org.joml.Vector2fc;
import org.terasology.math.JomlUtil;
import org.terasology.math.geom.BaseVector2f;
import org.terasology.math.geom.ImmutableVector2f;
import org.terasology.math.geom.Rect2i;
import org.terasology.nui.properties.Checkbox;
import org.terasology.polyworld.graph.Corner;
import org.terasology.polyworld.graph.Edge;
import org.terasology.polyworld.graph.Graph;
import org.terasology.polyworld.graph.GraphFacet;
import org.terasology.polyworld.graph.GraphRegion;
import org.terasology.polyworld.graph.Triangle;
import org.terasology.world.block.BlockAreac;
import org.terasology.world.viewer.layers.AbstractFacetLayer;
import org.terasology.world.viewer.layers.FacetLayerConfig;
import org.terasology.world.viewer.layers.Renders;
import org.terasology.world.viewer.layers.ZOrder;
import org.terasology.world.viewer.picker.CirclePickerClosest;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * Draws the generated graph on a AWT graphics instance
 */
@Renders(value = GraphFacet.class, order = ZOrder.BIOME + 1)
public class GraphFacetLayer extends AbstractFacetLayer {

    private Config config = new Config();

    public GraphFacetLayer() {
        setVisible(false);
        // use default settings
    }

    /**
     * This can be called only through reflection since Config is private
     * @param config the layer configuration info
     */
    public GraphFacetLayer(Config config) {
        this.config = config;
    }

    @Override
    public void render(BufferedImage img, org.terasology.world.generation.Region region) {
        GraphFacet graphFacet = region.getFacet(GraphFacet.class);

        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int dx = region.getRegion().minX();
        int dy = region.getRegion().minZ();
        g.translate(-dx, -dy);
        for (Graph graph : graphFacet.getAllGraphs()) {
            if (config.showEdges) {
                drawEdges(g, graph);
            }

            if (config.showTris) {
                drawTriangles(g, graph);
            }

            if (config.showCorners) {
                drawCorners(g, graph);
            }

            if (config.showSites) {
                drawSites(g, graph);
            }

            if (config.showBounds) {
                drawBounds(g, graph);
            }
        }

        if (config.showLookUp) {
            drawTriangleLookup(g, graphFacet);
        }

        g.dispose();
    }

    private void drawTriangleLookup(Graphics2D g, GraphFacet graphFacet) {
        BlockAreac worldReg = graphFacet.getWorldArea();
        for (int z = worldReg.minY(); z < worldReg.maxY(); z++) {
            for (int x = worldReg.minX(); x < worldReg.maxX(); x++) {
                Triangle tri = graphFacet.getWorldTriangle(x, z);
                if (tri == null) {
                    g.setStroke(new BasicStroke(3f));
                    g.setColor(Color.MAGENTA);
                    g.drawLine(x, z, x, z);
                }
            }
        }
    }

    @Override
    public String getWorldText(org.terasology.world.generation.Region region, int wx, int wy) {
        GraphFacet graphFacet = region.getFacet(GraphFacet.class);
        CirclePickerClosest<Corner> cornerPicker = new CirclePickerClosest<>(new org.joml.Vector2f(wx, wy), c -> 3);
        CirclePickerClosest<GraphRegion> sitePicker = new CirclePickerClosest<>(new org.joml.Vector2f(wx, wy), r -> 3);
        for (Graph graph : graphFacet.getAllGraphs()) {
            if (graph.getBounds().contains(wx, wy)) {
                Triangle tri = graphFacet.getWorldTriangle(wx, wy);
                cornerPicker.offer(tri.getCorner1().getLocation(), tri.getCorner1());
                cornerPicker.offer(tri.getCorner2().getLocation(), tri.getCorner2());
                sitePicker.offer(tri.getRegion().getCenter(), tri.getRegion());
                if (cornerPicker.getClosest() != null) {
                    return cornerPicker.getClosest().toString();
                }
                if (sitePicker.getClosest() != null) {
                    return sitePicker.getClosest().toString();
                }
            }
        }
        return null;
    }

    public boolean isShowEdges() {
        return config.showEdges;
    }

    public void setShowEdges(boolean showEdges) {
        if (config.showEdges != showEdges) {
            config.showEdges = showEdges;
            notifyObservers();
        }
    }

    public boolean isShowBounds() {
        return config.showBounds;
    }

    public void setShowBounds(boolean showBounds) {
        if (config.showBounds != showBounds) {
            config.showBounds = showBounds;
            notifyObservers();
        }
    }

    public boolean isShowCorners() {
        return config.showCorners;
    }

    public void setShowCorners(boolean showCorners) {
        if (config.showCorners != showCorners) {
            config.showCorners = showCorners;
            notifyObservers();
        }
    }

    public boolean isShowSites() {
        return config.showSites;
    }

    public void setShowSites(boolean showSites) {
        if (config.showSites != showSites) {
            config.showSites = showSites;
            notifyObservers();
        }
    }

    public boolean isShowTris() {
        return config.showTris;
    }

    public void setShowTris(boolean showTris) {
        if (config.showTris != showTris) {
            config.showTris = showTris;
            notifyObservers();
        }
    }

    public boolean isShowLookup() {
        return config.showLookUp;
    }

    public void setShowLookup(boolean showLookUp) {
        if (config.showLookUp != showLookUp) {
            config.showLookUp = showLookUp;
            notifyObservers();
        }
    }

    public static void drawEdges(Graphics2D g, Graph graph) {
        g.setStroke(new BasicStroke(1));
        g.setColor(new Color(192, 192, 192, 160));
        for (Edge e : graph.getEdges()) {
            Vector2fc r0c = e.getCorner0().getLocation();
            Vector2fc r1c = e.getCorner1().getLocation();
            g.drawLine((int) r0c.x(), (int) r0c.y(), (int) r1c.x(), (int) r1c.y());
        }
    }

    public static void drawPolys(Graphics2D g, Graph graph, Function<GraphRegion, Color> colorFunc) {
        List<GraphRegion> regions = graph.getRegions();

        for (final GraphRegion reg : regions) {

            Color col = colorFunc.apply(reg);
            Collection<Corner> pts = reg.getCorners();

            int[] xPoints = new int[pts.size()];
            int[] yPoints = new int[pts.size()];

            int i = 0;
            for (Corner corner : pts) {
                xPoints[i] = (int) corner.getLocation().x();
                yPoints[i] = (int) corner.getLocation().y();
                i++;
            }

            g.setColor(col);
            g.fillPolygon(xPoints, yPoints, pts.size());
        }
    }

    public static void drawTriangles(Graphics2D g, Graph graph) {
        List<GraphRegion> regions = graph.getRegions();

        g.setColor(new Color(64, 64, 255, 224));

        for (final GraphRegion reg : regions) {
            Vector2fc p0 = reg.getCenter();
            for (Corner c : reg.getCorners()) {
                Vector2fc p1 = c.getLocation();

                g.draw(new Line2D.Double(p0.x(), p0.y(), p1.x(), p1.y()));
            }
        }
    }

    public static void drawSites(Graphics2D g, Graph graph) {
        List<GraphRegion> centers = graph.getRegions();

        g.setColor(Color.ORANGE);
        for (GraphRegion regs : centers) {
            Vector2fc c = regs.getCenter();
            g.fill(new Rectangle2D.Double(c.x() - 1, c.y() - 1, 2, 2));
        }
    }

    public static void drawCorners(Graphics2D g, Graph graph) {
        g.setColor(Color.WHITE);
        for (Corner c : graph.getCorners()) {
            Vector2fc loc = c.getLocation();
            g.fill(new Rectangle2D.Double(loc.x() - 1, loc.y() - 1, 2, 2));
        }
    }

    public static void drawBounds(Graphics2D g, Graph graph) {
        BlockAreac bounds = graph.getBounds();
        g.setColor(Color.PINK);
        g.drawRect(bounds.minX(), bounds.minY(), bounds.getSizeX(), bounds.getSizeY());
    }

    public static void fillBounds(Graphics2D g, Graph graph) {
        BlockAreac bounds = graph.getBounds();
        g.setColor(Color.MAGENTA);
        g.fillRect(bounds.minX() + 1, bounds.minY() + 1, bounds.getSizeX() - 1, bounds.getSizeY() - 1);
    }

    @Override
    public FacetLayerConfig getConfig() {
        return config;
    }

    /**
     * Persistent data
     */
    private static class Config implements FacetLayerConfig {
        @Checkbox private boolean showEdges = true;
        @Checkbox private boolean showBounds = true;
        @Checkbox private boolean showCorners = true;
        @Checkbox private boolean showSites = true;
        @Checkbox private boolean showLookUp;
        @Checkbox private boolean showTris;
    }
}
