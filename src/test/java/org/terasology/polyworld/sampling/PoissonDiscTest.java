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
import org.junit.jupiter.api.Test;
import org.terasology.joml.geom.Rectanglef;

import java.math.RoundingMode;
import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;

public class PoissonDiscTest {

    @Test
    public void testMinDistance() {

        float graphDensity = 100f;

        Rectanglef area = new Rectanglef(0, 0, 512, 256);
        int numSites = DoubleMath.roundToInt(area.area() * graphDensity / 1000, RoundingMode.HALF_UP);

        PoissonDiscSampling sampling = new PoissonDiscSampling();
        float rad = sampling.getMinRadius(area, numSites);
        List<Vector2fc> sample = sampling.create(area, numSites);

        for (int i = 0; i < sample.size(); i++) {
            Vector2fc p0 = sample.get(i);
            for (int j = 0; j < i; j++) {
                Vector2fc p1 = sample.get(j);
                if (p0.distanceSquared(p1) < rad * rad) {
                    System.err.println("FAIL FOR " + p1);
                    System.err.println("EXISTING " + p0);
                    fail(String.format("Distance for %d/%d == %.2f", i, j, p0.distance(p1)));
                }
            }
        }
    }
}
