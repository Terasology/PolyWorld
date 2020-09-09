// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.polyworld.rp;

import org.terasology.math.geom.Rect2i;

import java.util.Collection;

/**
 * Provides a collection of subregions based on a given rectangle.
 */
public interface RegionProvider {

    /**
     * @param fullArea the area that contains all sub-regions
     * @return a collection of regions that lie inside the given area
     */
    Collection<Rect2i> getSectorRegions(Rect2i fullArea);
}
