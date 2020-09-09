// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.polyworld.debug;

import com.google.common.base.Function;
import org.terasology.polyworld.graph.Region;

import java.awt.Color;
import java.util.Random;

/**
 * TODO Type description
 */
public class RandomColors implements Function<Region, Color> {
    private final Random r;

    public RandomColors() {
        r = new Random(1254);
    }

    @Override
    public Color apply(Region input) {
        return new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255));
    }

}
