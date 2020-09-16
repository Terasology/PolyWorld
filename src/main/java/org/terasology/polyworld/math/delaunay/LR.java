// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.polyworld.math.delaunay;

public enum LR {

    LEFT,
    RIGHT;

    public LR other() {
        return this == LEFT ? RIGHT : LEFT;
    }
}
