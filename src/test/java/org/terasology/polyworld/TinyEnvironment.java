// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.polyworld;

import org.mockito.Matchers;
import org.mockito.Mockito;
import org.terasology.engine.config.Config;
import org.terasology.engine.context.Context;
import org.terasology.engine.context.internal.ContextImpl;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.BlockUri;
import org.terasology.engine.world.generator.plugin.WorldGeneratorPlugin;
import org.terasology.engine.world.generator.plugin.WorldGeneratorPluginLibrary;

import java.util.Collections;
import java.util.List;

/**
 * Setup a tiny Terasology environment
 */
public final class TinyEnvironment {

    private TinyEnvironment() {
        // empty
    }

    /**
     * Default setup order
     *
     * @return a terasology environment: The return context contains a {@link Config}, {@link BlockManager} and {@link
     *         }WorldGeneratorPluginLibrary} mock. mock.
     */
    public static Context setup() {
        Context context = new ContextImpl();
        CoreRegistry.setContext(context);
        setupConfig(context);

        setupBlockManager(context);

        setupWorldGen(context);
        return context;
    }

    private static void setupConfig(Context context) {
        Config config = new Config(context);
        context.put(Config.class, config);
    }

    private static void setupBlockManager(Context context) {
        BlockManager blockManager = Mockito.mock(BlockManager.class);
        Block air = new Block();
        air.setTranslucent(true);
        air.setTargetable(false);
        air.setPenetrable(true);
        air.setReplacementAllowed(true);
        air.setShadowCasting(false);
        air.setAttachmentAllowed(false);
        air.setHardness(0);
        air.setId((short) 0);
        air.setDisplayName("Air");
        air.setUri(BlockManager.AIR_ID);
        Mockito.when(blockManager.getBlock(Matchers.<BlockUri>any())).thenReturn(air);
        Mockito.when(blockManager.getBlock(Matchers.<String>any())).thenReturn(air);

        context.put(BlockManager.class, blockManager);
    }

    private static void setupWorldGen(Context context) {
        context.put(WorldGeneratorPluginLibrary.class, new WorldGeneratorPluginLibrary() {

            @Override
            public <U extends WorldGeneratorPlugin> List<U> instantiateAllOfType(Class<U> ofType) {
                return Collections.emptyList();
            }
        });
    }
}
