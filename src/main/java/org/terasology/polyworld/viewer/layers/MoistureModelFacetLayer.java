// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.polyworld.viewer.layers;

import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.world.generation.Region;
import org.terasology.engine.world.viewer.layers.AbstractFacetLayer;
import org.terasology.engine.world.viewer.layers.FacetLayerConfig;
import org.terasology.engine.world.viewer.layers.Renders;
import org.terasology.engine.world.viewer.layers.ZOrder;
import org.terasology.engine.world.viewer.picker.CirclePickerClosest;
import org.terasology.math.geom.BaseVector2f;
import org.terasology.math.geom.Vector2f;
import org.terasology.nui.properties.Checkbox;
import org.terasology.polyworld.graph.Corner;
import org.terasology.polyworld.graph.Graph;
import org.terasology.polyworld.moisture.MoistureModel;
import org.terasology.polyworld.moisture.MoistureModelFacet;
import org.terasology.polyworld.water.WaterModel;
import org.terasology.polyworld.water.WaterModelFacet;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * TODO Convert this into a more general class that supports different graph-based value look-ups
 */
@Renders(value = MoistureModelFacet.class, order = ZOrder.BIOME + 10)
public class MoistureModelFacetLayer extends AbstractFacetLayer {

    private static final Logger logger = LoggerFactory.getLogger(MoistureModelFacetLayer.class);

    /**
     * The radius multiplier for the visible circles
     */
    private final float scale = 4f;

    private Config config = new Config();

    public MoistureModelFacetLayer() {
        setVisible(false);
        // use default settings
    }

    /**
     * This can be called only through reflection since Config is private
     *
     * @param config the layer configuration info
     */
    public MoistureModelFacetLayer(Config config) {
        this.config = config;
    }

    @Override
    public void render(BufferedImage img, Region region) {
        MoistureModelFacet facet = region.getFacet(MoistureModelFacet.class);
        WaterModelFacet waterFacet = region.getFacet(WaterModelFacet.class);

        Stopwatch sw = Stopwatch.createStarted();

        Graphics2D g = img.createGraphics();
        int dx = region.getRegion().minX();
        int dy = region.getRegion().minZ();
        g.translate(-dx, -dy);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (Graph graph : facet.getKeys()) {
            MoistureModel moistureModel = facet.get(graph);
            WaterModel waterModel = waterFacet.get(graph);
            draw(g, moistureModel, waterModel, graph);
        }

        g.dispose();

        if (logger.isTraceEnabled()) {
            logger.debug("Rendered regions in {}ms.", sw.elapsed(TimeUnit.MILLISECONDS));
        }
    }

    private void draw(Graphics2D g, MoistureModel model, WaterModel waterModel, Graph graph) {
        g.setColor(new Color(0x4040FF));
        for (Corner c : graph.getCorners()) {
            if (config.showOcean || (!waterModel.isOcean(c) && !waterModel.isCoast(c))) {
                float moisture = model.getMoisture(c);
                float r = scale * moisture;
                BaseVector2f loc = c.getLocation();
                g.fill(new Ellipse2D.Float(loc.getX() - r, loc.getY() - r, 2 * r, 2 * r));
            }
        }
    }

    @Override
    public String getWorldText(Region region, int wx, int wy) {

        MoistureModelFacet moistureModelFacet = region.getFacet(MoistureModelFacet.class);
        Graph graph = findGraph(moistureModelFacet.getKeys(), wx, wy);

        if (graph != null) {
            MoistureModel model = moistureModelFacet.get(graph);

            Vector2f cursor = new Vector2f(wx, wy);

            // Use the value as radius, but clamp it to some minimum value so it
            // remains large enough to be hovered with the mouse cursor
            Function<Corner, Float> radiusFunc = c -> Math.max(2f, model.getMoisture(c) * scale);
            CirclePickerClosest<Corner> picker = new CirclePickerClosest<>(cursor, radiusFunc);

            for (Corner c : graph.getCorners()) {
                picker.offer(c.getLocation(), c);
            }

            if (picker.getClosest() != null) {
                float moisture = model.getMoisture(picker.getClosest());
                return String.format("Moisture: %.2f", moisture);
            }
        }

        return null;
    }

    private Graph findGraph(Collection<Graph> keys, int wx, int wy) {
        for (Graph graph : keys) {
            if (graph.getBounds().contains(wx, wy)) {
                return graph;
            }
        }

        return null;
    }

    @Override
    public FacetLayerConfig getConfig() {
        return config;
    }

    /**
     * Persistent data
     */
    private static class Config implements FacetLayerConfig {
        @Checkbox
        private boolean showOcean;
    }
}
