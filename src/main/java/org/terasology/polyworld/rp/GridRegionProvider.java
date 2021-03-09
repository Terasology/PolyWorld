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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.terasology.engine.world.block.BlockArea;
import org.terasology.engine.world.block.BlockAreac;

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
    public Collection<BlockAreac> getSectorRegions(BlockAreac fullArea) {
        int width = fullArea.getSizeX() / divX;
        int height = fullArea.getSizeY() / divY;

        Collection<BlockAreac> areas = Lists.newArrayListWithCapacity(divX * divY);

        for (int ry = 0; ry < divY; ry++) {
            for (int rx = 0; rx < divX; rx++) {
                int x = fullArea.minX() + rx * width;
                int y = fullArea.minY() + ry * height;
                areas.add(new BlockArea(x, y).setSize(width, height));
            }
        }

        return areas;
    }

}
