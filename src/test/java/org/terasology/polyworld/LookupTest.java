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

import com.google.common.math.DoubleMath;
import org.joml.Vector2fc;
import org.joml.Vector2ic;
import org.junit.Assert;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.joml.geom.Rectanglef;
import org.terasology.math.delaunay.Voronoi;
import org.terasology.polyworld.graph.Triangle;
import org.terasology.polyworld.graph.VoronoiGraph;
import org.terasology.polyworld.sampling.PointSampling;
import org.terasology.polyworld.sampling.PoissonDiscSampling;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.MersenneRandom;
import org.terasology.utilities.random.Random;
import org.terasology.world.block.BlockArea;
import org.terasology.world.block.BlockAreac;

import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class LookupTest {
    private static final Logger logger = LoggerFactory.getLogger(LookupTest.class);

    public static Stream<Arguments> generateSeed() {
        Random seedGen = new FastRandom(12345);
        Arguments[] args = new Arguments[50];
        for (int i = 0; i < 50; i++) {
            args[i] = Arguments.of(seedGen.nextInt());
        }
        return Arrays.stream(args);
    }

    @ParameterizedTest
    @MethodSource("generateSeed")
    public void testCoverage(int seed) {
        MersenneRandom rng = new MersenneRandom(seed);

        int x = rng.nextInt(-10000, 10000);
        int y = rng.nextInt(-10000, 10000);
        int width = rng.nextInt(100, 2000);
        int height = rng.nextInt(100, 2000);

        BlockAreac intBounds = new BlockArea(x, y).setSize(width, height);
        Rectanglef realBounds = intBounds.getBounds(new Rectanglef());

        PointSampling sampling = new PoissonDiscSampling();

        int numSites = DoubleMath.roundToInt(intBounds.area() * rng.nextDouble(0.5, 5) / 1000, RoundingMode.HALF_UP);
        List<Vector2fc> points = sampling.create(realBounds, numSites, rng);

        logger.info("Sampled {} with {} points", intBounds, points.size());

        Voronoi v = new Voronoi(points, realBounds);
        VoronoiGraph graph = new VoronoiGraph(intBounds, v);

        TriangleLookup lookup = new TriangleLookup(graph);
        for (Vector2ic coord : intBounds) {
            Triangle tri = lookup.findTriangleAt(coord.x(), coord.y());
            Assert.assertNotNull(tri);
        }
    }

}
