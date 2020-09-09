// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.polyworld.distribution;

import org.terasology.math.geom.Vector2f;

import java.util.Random;

/**
 * TODO Type description
 */
public class RadialDistribution implements Distribution {

    private static final float ISLAND_FACTOR = 1.07f;  // 1.0 means no small islands; 2.0 leads to a lot

    private final int bumps;
    private final double startAngle;
    private final double dipAngle;
    private final double dipWidth;

    /**
     * @param seed a random seed value
     */
    public RadialDistribution(long seed) {
        Random r = new Random(seed);

        bumps = r.nextInt(5) + 1;
        startAngle = r.nextDouble() * 2 * Math.PI;
        dipAngle = r.nextDouble() * 2 * Math.PI;
        dipWidth = r.nextDouble() * .5 + .2;
    }

    @Override
    public boolean isInside(Vector2f p2) {
        Vector2f p = new Vector2f(2 * (p2.getX() - 0.5f), 2 * (p2.getY() - 0.5f));

        float angle = (float) Math.atan2(p.getY(), p.getX());
        float length = 0.5f * (Math.max(Math.abs(p.getX()), Math.abs(p.getY())) + p.length());

        float r1 = 0.5f + 0.40f * (float) Math.sin(startAngle + bumps * angle + Math.cos((bumps + 3) * angle));
        float r2 = 0.7f - 0.20f * (float) Math.sin(startAngle + bumps * angle - Math.sin((bumps + 2) * angle));
        if (Math.abs(angle - dipAngle) < dipWidth
                || Math.abs(angle - dipAngle + 2 * Math.PI) < dipWidth
                || Math.abs(angle - dipAngle - 2 * Math.PI) < dipWidth) {
            r1 = 0.2f;
            r2 = 0.2f;
        }
        return !(length < r1 || (length > r1 * ISLAND_FACTOR && length < r2));
    }
}
