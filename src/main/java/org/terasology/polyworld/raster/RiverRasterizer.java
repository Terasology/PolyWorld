/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.polyworld.raster;

import org.joml.Vector2ic;
import org.joml.Vector3i;
import org.terasology.commonworld.geom.BresenhamCollectorVisitor;
import org.terasology.commonworld.geom.BresenhamLineIterator;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.Chunks;
import org.terasology.engine.world.generation.Region;
import org.terasology.engine.world.generation.WorldRasterizer;
import org.terasology.engine.world.generation.facets.ElevationFacet;
import org.terasology.engine.world.generation.facets.SeaLevelFacet;
import org.terasology.math.TeraMath;
import org.terasology.polyworld.graph.Edge;
import org.terasology.polyworld.graph.Graph;
import org.terasology.polyworld.graph.GraphFacet;
import org.terasology.polyworld.rivers.RiverModel;
import org.terasology.polyworld.rivers.RiverModelFacet;

import java.util.Collection;
import java.util.EnumSet;

/**
 * Rasterizer for the river model of PolyWorld.
 *
 * This rasterizer class turns the edges of the PolyWorld graph with a positive river value into actual in-game blocks.
 * The river width is determined by the river value associated with an edge s.t. the width is proportional to this value.
 */
public class RiverRasterizer implements WorldRasterizer {

    private Block water;
    private Block air;

    @Override
    public void initialize() {
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        water = blockManager.getBlock("CoreAssets:Water");
        air = blockManager.getBlock(BlockManager.AIR_ID);
    }

    @Override
    public void generateChunk(Chunk chunk, Region chunkRegion) {
        GraphFacet graphFacet = chunkRegion.getFacet(GraphFacet.class);
        SeaLevelFacet seaLevelFacet = chunkRegion.getFacet(SeaLevelFacet.class);
        RiverModelFacet riverModelFacet = chunkRegion.getFacet(RiverModelFacet.class);
        ElevationFacet elevationFacet = chunkRegion.getFacet(ElevationFacet.class);

        BlockRegion region = chunkRegion.getRegion();
        int seaLevel = seaLevelFacet.getSeaLevel();


        for (Graph graph : graphFacet.getAllGraphs()) {
            RiverModel riverModel = riverModelFacet.get(graph);

            for (Edge e : graph.getEdges()) {
                int riverValue = riverModel.getRiverValue(e);
                if (riverValue > 0) {
                    int[][] structElem = getStructuringElement(riverValue);

                    int x0 = TeraMath.floorToInt(e.getCorner0().getLocation().x());
                    int z0 = TeraMath.floorToInt(e.getCorner0().getLocation().y());

                    int x1 = TeraMath.floorToInt(e.getCorner1().getLocation().x());
                    int z1 = TeraMath.floorToInt(e.getCorner1().getLocation().y());

                    BresenhamCollectorVisitor bresenhamCollector = new BresenhamCollectorVisitor();
                    BresenhamLineIterator.iterateLine2D(x0, z0, x1, z1, bresenhamCollector, EnumSet.allOf(BresenhamLineIterator.Overlap.class));
                    Collection<Vector2ic> line = bresenhamCollector.getLinePoints();

                    for (Vector2ic p : line) {
                        if (p.x() >= region.minX() && p.x() <= region.maxX() && p.y() >= region.minZ() && p.y() <= region.maxZ()) {
                            int x = Chunks.toRelative(p.x(), Chunks.INNER_CHUNK_POS_FILTER.x());
                            int z = Chunks.toRelative(p.y(), Chunks.INNER_CHUNK_POS_FILTER.z());
                            int y = TeraMath.floorToInt(elevationFacet.get(x, z));
                            Vector3i worldPos = new Vector3i(p.x(), y, p.y());

                            placeWaterBody(chunk, region, worldPos, structElem, seaLevel);
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns a structuring element for the specified radius in form of a 2-dimensional disk/circle.
     *
     * For instance, the structuring element for a radius of 1 looks like follows:
     *      0|1|0
     *      1|1|1
     *      0|1|0
     *
     * @param radius the radius of the structuring element
     * @return the matrix of the structuring element
     */
    static int[][] getStructuringElement(int radius) {
        int[][] structElem = new int[2 * radius + 1][2 * radius + 1];
        for (int y = -radius; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {
                if (x * x + y * y <= radius * radius) {
                    structElem[x + radius][y + radius] = 1;
                }
            }
        }

        return structElem;
    }

    /**
     * Places a 'disk' of water around the specified position.
     *
     * @param chunk the chunk to be edited
     * @param region chunk region being affected
     * @param worldPos the central position of the water body
     * @param structElem the structuring element for block placement
     * @param seaLevel
     */
    private void placeWaterBody(Chunk chunk, BlockRegion region, Vector3i worldPos, int[][] structElem, int seaLevel) {
        int radius = (structElem.length - 1) / 2;
        Vector3i pos;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (structElem[dx + radius][dz + radius] != 0) {
                    pos = new Vector3i(worldPos.add(dx, 0, dz));

                    // remove top layer (soil)
                    if (region.contains(pos.x, pos.y, pos.z)) {
                        chunk.setBlock(Chunks.toRelative(pos.x, pos.y, pos.z, new Vector3i()), air);
                    }

                    // don't dig below the sea level
                    if (pos.y > seaLevel) {
                        pos.y -= 1;
                    }
                    if (region.contains(pos.x, pos.y, pos.z)) {
                        chunk.setBlock(Chunks.toRelative(pos.x, pos.y, pos.z, new Vector3i()), water);
                    }
                }
            }
        }
    }
}
