/*
 * Copyright 2015 MovingBlocks
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

package org.terasology.polyworld.rp;

import java.util.Collection;
import java.util.Collections;

import org.terasology.math.Rect2i;
import org.terasology.math.Region3i;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.facets.base.SparseFacet3D;

import com.google.common.collect.Lists;

/**
 * TODO Type description
 * @author Martin Steiger
 */
public class RegionFacet extends SparseFacet3D {

    private Collection<Rect2i> regions = Lists.newArrayList();

    public RegionFacet(Region3i targetRegion, Border3D border) {
        super(targetRegion, border);
    }

    public void addRegion(Rect2i region) {
        regions.add(region);
    }

    public Collection<Rect2i> getRegions() {
        return Collections.unmodifiableCollection(regions);
    }
}
