/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.polyworld;

import org.terasology.engine.SimpleUri;
import org.terasology.polyworld.elevation.ElevationProvider;
import org.terasology.world.generation.BaseFacetedWorldGenerator;
import org.terasology.world.generation.WorldBuilder;
import org.terasology.world.generator.RegisterWorldGenerator;

@RegisterWorldGenerator(id = "island", displayName = "Island")
public class IslandWorldGenerator extends BaseFacetedWorldGenerator {

    public IslandWorldGenerator(SimpleUri uri) {
        super(uri);
    }

    @Override
    protected WorldBuilder createWorld(long seed) {
        return new WorldBuilder(seed)
                .addProvider(new ElevationProvider())
//                .addRasterizer(new FloraRasterizer())
//                .addRasterizer(new TreeRasterizer())
//                .addRasterizer(new SolidRasterizer())
                .addPlugins();
    }
}
