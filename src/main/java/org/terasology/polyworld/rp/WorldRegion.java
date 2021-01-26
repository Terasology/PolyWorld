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

import org.terasology.world.block.BlockArea;
import org.terasology.world.block.BlockAreac;

/**
 * TODO Type description
 */
public class WorldRegion {

    private final BlockArea area = new BlockArea(BlockArea.INVALID);
    private RegionType type = RegionType.OCEAN;
    private float heightScaleFactor = 1.0f;

    /**
     * @param area the area this region covers
     */
    public WorldRegion(BlockAreac area) {
        this.area.set(area);
    }

    public RegionType getType() {
        return type;
    }

    public void setType(RegionType type) {
        this.type = type;
    }

    public float getHeightScaleFactor() {
        return heightScaleFactor;
    }

    public void setHeightScaleFactor(float heightScaleFactor) {
        this.heightScaleFactor = heightScaleFactor;
    }

    public BlockAreac getArea() {
        return area;
    }
}
