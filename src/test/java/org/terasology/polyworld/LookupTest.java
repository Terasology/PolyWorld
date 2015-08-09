/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License"){ }
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

package org.terasology.polyworld;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.math.delaunay.Voronoi;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Rect2f;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2f;
import org.terasology.polyworld.graph.Triangle;
import org.terasology.polyworld.graph.VoronoiGraph;
import org.terasology.polyworld.sampling.PointSampling;
import org.terasology.polyworld.sampling.PoissonDiscSampling;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.MersenneRandom;
import org.terasology.utilities.random.Random;

import com.google.common.math.DoubleMath;

@RunWith(Parameterized.class)
public class LookupTest {
    private static final Logger logger = LoggerFactory.getLogger(LookupTest.class);
    private MersenneRandom rng;

    public LookupTest(long seed) {
        rng = new MersenneRandom(seed);
    }

    @Parameters(name = "{index}: seed={0}")
    public static Collection<Object[]> data() {
        List<Object[]> params = new ArrayList<>();
        Random seedGen = new FastRandom(12345);
        for (int i = 0; i < 50; i++) {
            params.add(new Object[] {seedGen.nextInt()});
        }
        return params;
    }

    @Test
    public void testCoverage() {

        int x = rng.nextInt(-10000, 10000);
        int y = rng.nextInt(-10000, 10000);
        int width = rng.nextInt(100, 2000);
        int height = rng.nextInt(100, 2000);

        Rect2i intBounds = Rect2i.createFromMinAndSize(x, y, width, height);
        Rect2f realBounds = Rect2f.createFromMinAndSize(intBounds.minX(), intBounds.minY(), intBounds.width(), intBounds.height());

        PointSampling sampling = new PoissonDiscSampling();

        int numSites = DoubleMath.roundToInt(intBounds.area() * rng.nextDouble(0.5, 5) / 1000, RoundingMode.HALF_UP);
        List<Vector2f> points = sampling.create(realBounds, numSites, rng);

        logger.info("Sampled {} with {} points", intBounds, points.size());

        Voronoi v = new Voronoi(points, realBounds);
        VoronoiGraph graph = new VoronoiGraph(intBounds, v);

        TriangleLookup lookup = new TriangleLookup(graph);
        for (BaseVector2i coord : intBounds.contents()) {
            Triangle tri = lookup.findTriangleAt(coord.getX(), coord.getY());
            Assert.assertNotNull(tri);
        }
    }

}
