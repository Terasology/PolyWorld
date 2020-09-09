// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.polyworld.biome;

import org.terasology.biomesAPI.Biome;
import org.terasology.biomesAPI.BiomeRegistry;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.In;

/**
 * Registers all core biomes with the engine.
 */
@RegisterSystem(RegisterMode.ALWAYS)
public class WhittakerBiomes extends BaseComponentSystem {

    @In
    BiomeRegistry biomeRegistry;

    @Override
    public void preBegin() {
        for (Biome biome : WhittakerBiome.values()) {
            biomeRegistry.registerBiome(biome);
        }
    }
}
