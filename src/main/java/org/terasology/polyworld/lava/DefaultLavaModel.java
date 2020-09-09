// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.polyworld.lava;

import org.terasology.engine.utilities.random.FastRandom;
import org.terasology.engine.utilities.random.Random;
import org.terasology.polyworld.elevation.ElevationModel;
import org.terasology.polyworld.graph.Edge;
import org.terasology.polyworld.graph.Region;
import org.terasology.polyworld.moisture.MoistureModel;
import org.terasology.polyworld.rivers.RiverModel;
import org.terasology.polyworld.water.WaterModel;

/**
 * TODO Type description
 */
public class DefaultLavaModel implements LavaModel {

    private static final float FRACTION_LAVA_FISSURES = 0.2f;  // 0 to 1, probability of fissure

    private final Random random = new FastRandom(345345);

    private final ElevationModel elevationModel;
    private final WaterModel waterModel;
    private final RiverModel riverModel;
    private final MoistureModel moistureModel;

    public DefaultLavaModel(ElevationModel elevationModel, WaterModel waterModel, MoistureModel moistureModel,
                            RiverModel riverModel) {
        this.elevationModel = elevationModel;
        this.waterModel = waterModel;
        this.moistureModel = moistureModel;
        this.riverModel = riverModel;
    }


    @Override
    public boolean isLava(Edge edge) {
        Region d0 = edge.getRegion0();
        Region d1 = edge.getRegion1();
        return (riverModel.getRiverValue(edge) <= 0 && !waterModel.isWater(d0) && !waterModel.isWater(d1)
                && elevationModel.getElevation(d0) > 0.8
                && elevationModel.getElevation(d1) > 0.8
                && moistureModel.getMoisture(d0) < 0.3
                && moistureModel.getMoisture(d1) < 0.3
                && random.nextDouble() < FRACTION_LAVA_FISSURES);
    }

}
