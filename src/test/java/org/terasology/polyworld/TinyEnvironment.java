/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License"){ }
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

import java.util.Collections;
import java.util.List;

import org.mockito.Matchers;
import org.mockito.Mockito;
import org.terasology.config.Config;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.BlockUri;
import org.terasology.world.generator.plugin.WorldGeneratorPlugin;
import org.terasology.world.generator.plugin.WorldGeneratorPluginLibrary;

/**
 * Setup a tiny Terasology environment
 * @author Martin Steiger
 */
public final class TinyEnvironment {

    private TinyEnvironment() {
        // empty
    }

    /**
     * Default setup order
     */
    public static void setup() {

        setupConfig();

        setupBlockManager();

        setupWorldGen();
    }

    private static void setupConfig() {
        Config config = new Config();
        CoreRegistry.put(Config.class, config);
    }

    private static void setupBlockManager() {
        BlockManager blockManager = Mockito.mock(BlockManager.class);
        Block air = BlockManager.getAir();
        Mockito.when(blockManager.getBlock(Matchers.<BlockUri>any())).thenReturn(air);
        Mockito.when(blockManager.getBlock(Matchers.<String>any())).thenReturn(air);

        CoreRegistry.put(BlockManager.class, blockManager);
    }

    private static void setupWorldGen() {
        CoreRegistry.putPermanently(WorldGeneratorPluginLibrary.class, new WorldGeneratorPluginLibrary() {

            @Override
            public <U extends WorldGeneratorPlugin> List<U> instantiateAllOfType(Class<U> ofType) {
                return Collections.emptyList();
            }
        });
    }
}
