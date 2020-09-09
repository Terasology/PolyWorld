// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.polyworld.rp;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.terasology.engine.utilities.random.MersenneRandom;
import org.terasology.engine.utilities.random.Random;
import org.terasology.math.geom.Rect2i;

import java.util.Collection;

/**
 * Subdivides a given rectangle recursively.
 */
public class SubdivRegionProvider implements RegionProvider {

    private final long seed;
    private int minEdgeLen;
    private float baseSplitProb;

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
    public Collection<Rect2i> getSectorRegions(Rect2i fullArea) {

        MersenneRandom random = new MersenneRandom(seed ^ fullArea.hashCode());

        Collection<Rect2i> areas = Lists.newArrayList();

        split(areas, random, baseSplitProb, fullArea);

        return areas;
    }

    private void split(Collection<Rect2i> areas, Random random, float splitProb, Rect2i fullArea) {
        int maxWidth = fullArea.width();
        int maxHeight = fullArea.height();

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

            Rect2i first = Rect2i.createFromMinAndSize(fullArea.minX(), fullArea.minY(), width, height);
            Rect2i second = Rect2i.createFromMinAndMax(x, y, fullArea.maxX(), fullArea.maxY());

            float childSplitProb = splitProb / 2f;
            split(areas, random, childSplitProb, first);
            split(areas, random, childSplitProb, second);
        }
    }

}
