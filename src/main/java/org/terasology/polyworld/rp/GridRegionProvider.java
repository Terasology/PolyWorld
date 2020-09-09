// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.polyworld.rp;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.terasology.math.geom.Rect2i;

import java.util.Collection;

/**
 * Subdivides a rectangle into a regular grid.
 */
public class GridRegionProvider implements RegionProvider {

    private int divX;
    private int divY;

    public GridRegionProvider(int divX, int divY) {
        setDivX(divX);
        setDivY(divY);
    }

    public int getDivX() {
        return divX;
    }

    public void setDivX(int divX) {
        Preconditions.checkArgument(divX > 0);
        this.divX = divX;
    }

    public int getDivY() {
        return divY;
    }

    public void setDivY(int divY) {
        Preconditions.checkArgument(divY > 0);
        this.divY = divY;
    }

    @Override
    public Collection<Rect2i> getSectorRegions(Rect2i fullArea) {
        int width = fullArea.width() / divX;
        int height = fullArea.height() / divY;

        Collection<Rect2i> areas = Lists.newArrayListWithCapacity(divX * divY);

        for (int ry = 0; ry < divY; ry++) {
            for (int rx = 0; rx < divX; rx++) {
                int x = fullArea.minX() + rx * width;
                int y = fullArea.minY() + ry * height;
                areas.add(Rect2i.createFromMinAndSize(x, y, width, height));
            }
        }

        return areas;
    }

}
