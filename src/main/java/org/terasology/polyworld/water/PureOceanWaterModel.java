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

package org.terasology.polyworld.water;

import org.terasology.polyworld.graph.Corner;
import org.terasology.polyworld.graph.Region;

/**
 * Contains only ocean water.
 */
public class PureOceanWaterModel implements WaterModel {

    @Override
    public boolean isWater(Region p) {
        return true;
    }

    @Override
    public boolean isWater(Corner c) {
        return true;
    }

    @Override
    public boolean isOcean(Region p) {
        return true;
    }

    @Override
    public boolean isOcean(Corner c) {
        return true;
    }

    @Override
    public boolean isCoast(Region p) {
        return false;
    }

    @Override
    public boolean isCoast(Corner c) {
        return false;
    }
}
