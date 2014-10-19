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

package org.terasology.polyworld;

import org.terasology.polyworld.biome.BiomeModel;
import org.terasology.polyworld.biome.DefaultBiomeModel;
import org.terasology.polyworld.distribution.RadialDistribution;
import org.terasology.polyworld.elevation.DefaultElevationModel;
import org.terasology.polyworld.elevation.ElevationModel;
import org.terasology.polyworld.lava.DefaultLavaModel;
import org.terasology.polyworld.lava.LavaModel;
import org.terasology.polyworld.moisture.DefaultMoistureModel;
import org.terasology.polyworld.moisture.MoistureModel;
import org.terasology.polyworld.rivers.DefaultRiverModel;
import org.terasology.polyworld.rivers.RiverModel;
import org.terasology.polyworld.voronoi.Graph;
import org.terasology.polyworld.water.DefaultWaterModel;
import org.terasology.polyworld.water.WaterModel;

/**
 * TODO Type description
 * @author Martin Steiger
 */
public class IslandGenerator {

    private BiomeModel biomeModel;
    private RiverModel riverModel;
    private LavaModel lavaModel;
    private MoistureModel moistureModel;
    private ElevationModel elevationModel;
    private WaterModel waterModel;

    public IslandGenerator(Graph graph, long seed) {

        RadialDistribution waterDist = new RadialDistribution(seed);
        waterModel = new DefaultWaterModel(graph, waterDist);
        elevationModel = new DefaultElevationModel(graph, waterModel);

        riverModel = new DefaultRiverModel(graph, elevationModel, waterModel);
        moistureModel = new DefaultMoistureModel(graph, riverModel, waterModel);
        biomeModel = new DefaultBiomeModel(elevationModel, waterModel, moistureModel);
        lavaModel = new DefaultLavaModel(elevationModel, waterModel, moistureModel, riverModel);
    }

    public MoistureModel getMoistureModel() {
        return moistureModel;
    }

    public ElevationModel getElevationModel() {
        return elevationModel;
    }

    public WaterModel getWaterModel() {
        return waterModel;
    }

    /**
     * @return the biomeModel
     */
    public BiomeModel getBiomeModel() {
        return biomeModel;
    }

    /**
     * @return the river model
     */
    public RiverModel getRiverModel() {
        return riverModel;
    }

    /**
     * @return the lava model
     */
    public LavaModel getLavaModel() {
        return lavaModel;
    }
}
