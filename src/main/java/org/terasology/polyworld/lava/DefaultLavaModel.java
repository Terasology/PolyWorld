/*
 * Copyright 2014 MovingBlocks
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

package org.terasology.polyworld.lava;

import org.terasology.polyworld.elevation.ElevationModel;
import org.terasology.polyworld.moisture.MoistureModel;
import org.terasology.polyworld.rivers.RiverModel;
import org.terasology.polyworld.voronoi.Edge;
import org.terasology.polyworld.voronoi.Region;
import org.terasology.polyworld.water.WaterModel;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;

/**
 * TODO Type description
 * @author Martin Steiger
 */
public class DefaultLavaModel implements LavaModel {

    private static final double FRACTION_LAVA_FISSURES = 0.2;  // 0 to 1, probability of fissure

    private Random random = new FastRandom(345345);

    private ElevationModel elevationModel;
    private WaterModel waterModel;
    private RiverModel riverModel;
    private MoistureModel moistureModel;

    public DefaultLavaModel(ElevationModel elevationModel, WaterModel waterModel, MoistureModel moistureModel, RiverModel riverModel) {
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
