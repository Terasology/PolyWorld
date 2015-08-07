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

package org.terasology.polyworld.elevation;

import java.util.Map;

import org.terasology.polyworld.graph.Corner;
import com.google.common.collect.Maps;

/**
 *
 */
public class FlatLakeElevationModel extends AbstractElevationModel {

    private final Map<Corner, Float> elevations = Maps.newHashMap();

    private final ElevationModel baseModel;

    public FlatLakeElevationModel(ElevationModel baseModel) {
        this.baseModel = baseModel;
    }

    /**
     * @param corner the corner of interest
     * @param elevation the new elevation at that corner
     */
    void setElevation(Corner corner, float elevation) {
        elevations.put(corner, Float.valueOf(elevation));
    }

    @Override
    public float getElevation(Corner corner) {
        Float ele = elevations.get(corner);
        if (ele != null) {
            return ele.floatValue();
        } else {
            return baseModel.getElevation(corner);
        }
    }

    @Override
    public Corner getDownslope(Corner c) {
        // we are not checking this model on purpose, because the flatness of a lake
        // prohibits finding the downlope
        return baseModel.getDownslope(c);
    }

}
