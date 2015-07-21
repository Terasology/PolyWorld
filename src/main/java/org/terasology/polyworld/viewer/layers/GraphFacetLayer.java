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

import org.terasology.math.geom.Rect2i;
import org.terasology.math.Region3i;
import org.terasology.math.geom.BaseVector2f;
import org.terasology.math.geom.ImmutableVector2f;
import org.terasology.math.geom.Vector2f;
import org.terasology.polyworld.graph.Corner;
import org.terasology.polyworld.graph.Edge;
import org.terasology.polyworld.graph.Graph;
import org.terasology.polyworld.graph.GraphFacet;
import org.terasology.polyworld.graph.Region;
import org.terasology.polyworld.graph.Triangle;
import org.terasology.rendering.nui.properties.Checkbox;
import org.terasology.world.viewer.layers.AbstractFacetLayer;
import org.terasology.world.viewer.layers.FacetLayerConfig;
import org.terasology.world.viewer.layers.Renders;
import org.terasology.world.viewer.layers.ZOrder;
import org.terasology.world.viewer.picker.CirclePickerClosest;

/**
 * Draws the generated graph on a AWT graphics instance
 * @author Martin Steiger
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
        Region3i worldReg = graphFacet.getWorldRegion();
        for (int z = worldReg.minZ(); z < worldReg.maxZ(); z++) {
            for (int x = worldReg.minX(); x < worldReg.maxX(); x++) {
                Triangle tri = graphFacet.getWorldTriangle(x, 0, z);
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
        CirclePickerClosest<Corner> cornerPicker = new CirclePickerClosest<>(new Vector2f(wx, wy), c -> 3);
        CirclePickerClosest<Region> sitePicker = new CirclePickerClosest<>(new Vector2f(wx, wy), r -> 3);
        for (Graph graph : graphFacet.getAllGraphs()) {
            if (graph.getBounds().contains(wx, wy)) {
                Triangle tri = graphFacet.getWorldTriangle(wx, 0, wy);
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
            BaseVector2f r0c = e.getCorner0().getLocation();
            BaseVector2f r1c = e.getCorner1().getLocation();
            g.drawLine((int) r0c.getX(), (int) r0c.getY(), (int) r1c.getX(), (int) r1c.getY());
        }
    }

    public static void drawPolys(Graphics2D g, Graph graph, Function<Region, Color> colorFunc) {
        List<Region> regions = graph.getRegions();

        for (final Region reg : regions) {

            Color col = colorFunc.apply(reg);
            Collection<Corner> pts = reg.getCorners();

            int[] xPoints = new int[pts.size()];
            int[] yPoints = new int[pts.size()];

            int i = 0;
            for (Corner corner : pts) {
                xPoints[i] = (int) corner.getLocation().getX();
                yPoints[i] = (int) corner.getLocation().getY();
                i++;
            }

            g.setColor(col);
            g.fillPolygon(xPoints, yPoints, pts.size());
        }
    }

    public static void drawTriangles(Graphics2D g, Graph graph) {
        List<Region> regions = graph.getRegions();

        g.setColor(new Color(64, 64, 255, 224));

        for (final Region reg : regions) {
            BaseVector2f p0 = reg.getCenter();
            for (Corner c : reg.getCorners()) {
                BaseVector2f p1 = c.getLocation();

                g.draw(new Line2D.Double(p0.getX(), p0.getY(), p1.getX(), p1.getY()));
            }
        }
    }

    public static void drawSites(Graphics2D g, Graph graph) {
        List<Region> centers = graph.getRegions();

        g.setColor(Color.ORANGE);
        for (Region regs : centers) {
            BaseVector2f c = regs.getCenter();
            g.fill(new Rectangle2D.Double(c.getX() - 1, c.getY() - 1, 2, 2));
        }
    }

    public static void drawCorners(Graphics2D g, Graph graph) {
        g.setColor(Color.WHITE);
        for (Corner c : graph.getCorners()) {
            ImmutableVector2f loc = c.getLocation();
            g.fill(new Rectangle2D.Double(loc.getX() - 1, loc.getY() - 1, 2, 2));
        }
    }

    public static void drawBounds(Graphics2D g, Graph graph) {
        Rect2i bounds = graph.getBounds();
        g.setColor(Color.PINK);
        g.drawRect(bounds.minX(), bounds.minY(), bounds.width(), bounds.height());
    }

    public static void fillBounds(Graphics2D g, Graph graph) {
        Rect2i bounds = graph.getBounds();
        g.setColor(Color.MAGENTA);
        g.fillRect(bounds.minX() + 1, bounds.minY() + 1, bounds.width() - 1, bounds.height() - 1);
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
