// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.polyworld.elevation;

import com.google.common.collect.Maps;
import org.terasology.polyworld.graph.Corner;

import java.util.Map;

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
