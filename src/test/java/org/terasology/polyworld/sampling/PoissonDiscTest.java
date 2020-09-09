// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.polyworld.sampling;

import com.google.common.math.DoubleMath;
import org.junit.Assert;
import org.junit.Test;
import org.terasology.math.geom.Rect2f;
import org.terasology.math.geom.Vector2f;

import java.math.RoundingMode;
import java.util.List;

public class PoissonDiscTest {

    @Test
    public void testMinDistance() {

        float graphDensity = 100f;

        Rect2f area = Rect2f.createFromMinAndSize(0, 0, 512, 256);
        int numSites = DoubleMath.roundToInt(area.area() * graphDensity / 1000, RoundingMode.HALF_UP);

        PoissonDiscSampling sampling = new PoissonDiscSampling();
        float rad = sampling.getMinRadius(area, numSites);
        List<Vector2f> sample = sampling.create(area, numSites);

        for (int i = 0; i < sample.size(); i++) {
            Vector2f p0 = sample.get(i);
            for (int j = 0; j < i; j++) {
                Vector2f p1 = sample.get(j);
                if (p0.distanceSquared(p1) < rad * rad) {
                    System.err.println("FAIL FOR " + p1);
                    System.err.println("EXISTING " + p0);
                    Assert.fail(String.format("Distance for %d/%d == %.2f", i, j, p0.distance(p1)));
                }
            }
        }
    }
}
