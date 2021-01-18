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

import org.terasology.math.geom.Rect2i;
import org.terasology.utilities.random.MersenneRandom;
import org.terasology.utilities.random.Random;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.terasology.world.block.BlockArea;
import org.terasology.world.block.BlockAreac;

/**
 * Subdivides a given rectangle recursively.
 */
public class SubdivRegionProvider implements RegionProvider {

    private int minEdgeLen;
    private float baseSplitProb;
    private long seed;

    /**
     * splitProb = 1
     * <br>
     * minEdgeLen = 16
     */
    public SubdivRegionProvider(long seed) {
        this(seed, 4, 1f);
    }

    public SubdivRegionProvider(long seed, int minEdgeLen, float splitProb) {
        this.seed = seed;

        setSplitProb(splitProb);
        setMinEdgeLen(minEdgeLen);
    }

    public int getMinEdgeLen() {
        return minEdgeLen;
    }

    public void setMinEdgeLen(int minEdgeLen) {
        Preconditions.checkArgument(minEdgeLen > 1);
        this.minEdgeLen = minEdgeLen;
    }

    public void setSplitProb(float splitProb) {
        Preconditions.checkArgument(splitProb >= 0);
        Preconditions.checkArgument(splitProb <= 1);
        this.baseSplitProb = splitProb;
    }

    @Override
    public Collection<BlockAreac> getSectorRegions(BlockAreac fullArea) {

        MersenneRandom random = new MersenneRandom(seed ^ fullArea.hashCode());

        Collection<BlockAreac> areas = Lists.newArrayList();

        split(areas, random, baseSplitProb, fullArea);

        return areas;
    }

    private void split(Collection<BlockAreac> areas, Random random, float splitProb, BlockAreac fullArea) {
        int maxWidth = fullArea.getSizeX();
        int maxHeight = fullArea.getSizeY();

        boolean splitX = maxWidth >= maxHeight;
        int range = (splitX ? maxWidth : maxHeight) - 2 * minEdgeLen;

        float ratio = (float) Math.min(maxWidth, maxHeight) / Math.max(maxWidth, maxHeight);
        float badRatioSplitProb = (1 - ratio * ratio);
        float realSplitProb = splitProb + badRatioSplitProb * 0.5f;

        float rnd = random.nextFloat();
        boolean stop = (range <= 0) || (rnd > realSplitProb);

        if (stop) {
            areas.add(fullArea);
        } else {

            int splitPos = minEdgeLen + random.nextInt(range);
            int x;
            int y;
            int width;
            int height;
            if (splitX) {
                width = splitPos;
                height = maxHeight;
                x = fullArea.minX() + width;
                y = fullArea.minY();
            } else {
                width = maxWidth;
                height = splitPos;
                x = fullArea.minX();
                y = fullArea.minY() + height;
            }

            BlockArea first = new BlockArea(fullArea.minX(), fullArea.minY()).setSize(width, height);
            BlockArea second = new BlockArea(x, y, fullArea.maxX(), fullArea.maxY());

            float childSplitProb = splitProb / 2f;
            split(areas, random, childSplitProb, first);
            split(areas, random, childSplitProb, second);
        }
    }

}
