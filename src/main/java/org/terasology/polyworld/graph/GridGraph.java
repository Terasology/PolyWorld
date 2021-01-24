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

package org.terasology.polyworld.graph;

import com.google.common.collect.Lists;
import org.joml.Vector2f;
import org.terasology.world.block.BlockArea;
import org.terasology.world.block.BlockAreac;

import java.util.Collections;
import java.util.List;

/**
 * A graph that is based on a rectangular grid.
 */
public class GridGraph implements Graph {

    private final int rows;
    private final int cols;

    private final List<Corner> corners = Lists.newArrayList();
    private final List<GraphRegion> regions = Lists.newArrayList();
    private final List<Edge> edges = Lists.newArrayList();

    private final BlockArea bounds = new BlockArea(BlockArea.INVALID);

    /**
     * @param bounds the bounding box
     * @param rows the number of rows
     * @param cols the number of columns
     */
    public GridGraph(BlockAreac bounds, int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.bounds.set(bounds);

        float dx = (float) bounds.getSizeX() / cols;
        float dy = (float) bounds.getSizeY() / rows;

        for (int r = 0; r <= rows; r++) {
            for (int c = 0; c <= cols; c++) {
                float x = bounds.minX() + c * dx;
                float y = bounds.minY() + r * dy;
                Corner corner = new Corner(new Vector2f(x, y));
                corner.setBorder(r == 0 || c == 0 || r == rows || c == cols);
                corners.add(corner);
            }
        }

        for (int r = 0; r <= rows; r++) {
            for (int c = 0; c <= cols; c++) {
                Corner corner = getCorner(r, c);
                corner.addAdjacent(getCorner(r - 1, c));
                corner.addAdjacent(getCorner(r + 1, c));
                corner.addAdjacent(getCorner(r, c - 1));
                corner.addAdjacent(getCorner(r, c + 1));
            }
        }

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                float x = bounds.minX() + (c + 0.5f) * dx;
                float y = bounds.minY() + (r + 0.5f) * dy;
                Vector2f pos = new Vector2f(x, y);
                GraphRegion reg = new GraphRegion(pos);
                Corner tl = getCorner(r, c);
                Corner tr = getCorner(r, c + 1);
                Corner br = getCorner(r + 1, c + 1);
                Corner bl = getCorner(r + 1, c);
                reg.addCorner(tl);
                reg.addCorner(tr);
                reg.addCorner(br);
                reg.addCorner(bl);
                tl.addTouches(reg);
                tr.addTouches(reg);
                bl.addTouches(reg);
                br.addTouches(reg);
                regions.add(reg);
            }
        }

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                GraphRegion reg = getRegion(r, c);
                reg.addNeigbor(getRegion(r - 1, c - 1));
                reg.addNeigbor(getRegion(r - 1, c));
                reg.addNeigbor(getRegion(r - 1, c + 1));
                reg.addNeigbor(getRegion(r, c + 1));
                reg.addNeigbor(getRegion(r, c - 1));
                reg.addNeigbor(getRegion(r + 1, c - 1));
                reg.addNeigbor(getRegion(r + 1, c));
                reg.addNeigbor(getRegion(r + 1, c + 1));
            }
        }

        for (int r = 1; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Corner left = getCorner(r, c);
                Corner right = getCorner(r, c + 1);
                GraphRegion regTop = getRegion(r - 1, c);
                GraphRegion regBot = getRegion(r, c);

                Edge edge = new Edge(left, right, regTop, regBot);
                left.addEdge(edge);
                right.addEdge(edge);
                regTop.addBorder(edge);
                regBot.addBorder(edge);
                edges.add(edge);
            }
        }

        for (int r = 0; r < rows; r++) {
            for (int c = 1; c < cols; c++) {
                Corner top = getCorner(r, c);
                Corner bot = getCorner(r + 1, c);
                GraphRegion regLeft = getRegion(r, c - 1);
                GraphRegion regRight = getRegion(r, c);

                Edge edge = new Edge(top, bot, regLeft, regRight);
                top.addEdge(edge);
                bot.addEdge(edge);
                regLeft.addBorder(edge);
                regRight.addBorder(edge);
                edges.add(edge);
            }
        }

    }

    private GraphRegion getRegion(int r, int c) {
        if (r < 0 || r >= rows) {
            return null;
        }

        if (c < 0 || c >= cols) {
            return null;
        }

        int idx = r * cols + c;
        return regions.get(idx);
    }

    private Corner getCorner(int r, int c) {
        if (r < 0 || r > rows) {
            return null;
        }

        if (c < 0 || c > cols) {
            return null;
        }

        int idx = r * (cols + 1) + c;
        return corners.get(idx);
    }

    @Override
    public List<GraphRegion> getRegions() {
        return Collections.unmodifiableList(regions);
    }

    @Override
    public List<Edge> getEdges() {
        return Collections.unmodifiableList(edges);
    }

    @Override
    public List<Corner> getCorners() {
        return Collections.unmodifiableList(corners);
    }

    @Override
    public BlockAreac getBounds() {
        return bounds;
    }

    /**
     * @return the number of rows
     */
    public int getRows() {
        return rows;
    }

    /**
     * @return the number of columns
     */
    public int getCols() {
        return cols;
    }


}
