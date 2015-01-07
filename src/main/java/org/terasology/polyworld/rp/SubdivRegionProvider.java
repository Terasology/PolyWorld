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

package org.terasology.polyworld.rp;

import java.util.Collection;

import org.terasology.math.Rect2i;
import org.terasology.utilities.random.MersenneRandom;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Subdivides a given rectangle recursively.
 * @author Martin Steiger
 */
public class SubdivRegionProvider implements RegionProvider {

    private int maxDepth;
    private int minEdgeLen;
    private long seed;

    /**
     * maxDepth = 4
     * <br/>
     * minEdgeLen = 16
     */
    public SubdivRegionProvider(long seed) {
        this(seed, 4, 16);
    }

    public SubdivRegionProvider(long seed, int maxDepth, int minEdgeLen) {
        this.seed = seed;

        setMaxDepth(maxDepth);
        setMinEdgeLen(minEdgeLen);
    }

    public int getMinEdgeLen() {
        return minEdgeLen;
    }

    public void setMinEdgeLen(int minEdgeLen) {
        Preconditions.checkArgument(minEdgeLen > 1);
        this.minEdgeLen = minEdgeLen;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        Preconditions.checkArgument(maxDepth > 0);
        this.maxDepth = maxDepth;
    }

    @Override
    public Collection<Rect2i> getSectorRegions(Rect2i fullArea) {

        MersenneRandom random = new MersenneRandom(seed ^ fullArea.hashCode());

        Collection<Rect2i> areas = Lists.newArrayList();

        split(areas, random, fullArea);

        return areas;
    }

    private void split(Collection<Rect2i> areas, MersenneRandom random, Rect2i fullArea) {
        boolean splitX = fullArea.width() >= fullArea.height();

        int range = (splitX ? fullArea.width() : fullArea.height()) - 2 * minEdgeLen;

        if (range <= 0) {
            areas.add(fullArea);
        } else {

            int splitPos = minEdgeLen + random.nextInt(range);
            int x;
            int y;
            int width;
            int height;
            if (splitX) {
                width = splitPos;
                height = fullArea.height();
                x = fullArea.minX() + width;
                y = fullArea.minY();
            } else {
                width = fullArea.width();
                height = splitPos;
                x = fullArea.minX();
                y = fullArea.minY() + height;
            }

            Rect2i first = Rect2i.createFromMinAndSize(fullArea.minX(), fullArea.minY(), width, height);
            Rect2i second = Rect2i.createFromMinAndMax(x, y, fullArea.maxX(), fullArea.maxY());

            split(areas, random, first);
            split(areas, random, second);
        }
    }

}
