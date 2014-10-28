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

package org.terasology.polyworld.debug;

import java.awt.Color;
import java.util.Random;

import org.terasology.polyworld.voronoi.Region;

import com.google.common.base.Function;

/**
 * TODO Type description
 * @author Martin Steiger
 */
public class RandomColors implements Function<Region, Color> {
    private Random r;

    public RandomColors() {
        r = new Random(1254);
    }

    @Override
    public Color apply(Region input) {
        return new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255));
    }

}
