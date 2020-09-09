// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.polyworld.raster;

import org.terasology.commonworld.geom.BresenhamCollectorVisitor;
import org.terasology.commonworld.geom.BresenhamLineIterator;
import org.terasology.engine.math.ChunkMath;
import org.terasology.engine.math.Region3i;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.chunks.ChunkConstants;
import org.terasology.engine.world.chunks.CoreChunk;
import org.terasology.engine.world.generation.Region;
import org.terasology.engine.world.generation.WorldRasterizer;
import org.terasology.engine.world.generation.facets.SeaLevelFacet;
import org.terasology.engine.world.generation.facets.SurfaceHeightFacet;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Vector3i;
import org.terasology.polyworld.graph.Edge;
import org.terasology.polyworld.graph.Graph;
import org.terasology.polyworld.graph.GraphFacet;
import org.terasology.polyworld.rivers.RiverModel;
import org.terasology.polyworld.rivers.RiverModelFacet;

import java.util.Collection;
import java.util.EnumSet;

/**
 * Rasterizer for the river model of PolyWorld.
 * <p>
 * This rasterizer class turns the edges of the PolyWorld graph with a positive river value into actual in-game blocks.
 * The river width is determined by the river value associated with an edge s.t. the width is proportional to this
 * value.
 */
public class RiverRasterizer implements WorldRasterizer {

    private Block water;
    private Block air;

    /**
     * Returns a structuring element for the specified radius in form of a 2-dimensional disk/circle.
     * <p>
     * For instance, the structuring element for a radius of 1 looks like follows: 0|1|0 1|1|1 0|1|0
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

    @Override
    public void initialize() {
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        water = blockManager.getBlock("CoreAssets:Water");
        air = blockManager.getBlock(BlockManager.AIR_ID);
    }

    @Override
    public void generateChunk(CoreChunk chunk, Region chunkRegion) {
        GraphFacet graphFacet = chunkRegion.getFacet(GraphFacet.class);
        SeaLevelFacet seaLevelFacet = chunkRegion.getFacet(SeaLevelFacet.class);
        RiverModelFacet riverModelFacet = chunkRegion.getFacet(RiverModelFacet.class);
        SurfaceHeightFacet surfaceHeightData = chunkRegion.getFacet(SurfaceHeightFacet.class);

        Region3i region = chunkRegion.getRegion();
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
                    BresenhamLineIterator.iterateLine2D(x0, z0, x1, z1, bresenhamCollector,
                            EnumSet.allOf(BresenhamLineIterator.Overlap.class));
                    Collection<Vector2i> line = bresenhamCollector.getLinePoints();

                    for (Vector2i p : line) {
                        if (p.getX() >= region.minX() && p.getX() <= region.maxX() && p.getY() >= region.minZ() && p.getY() <= region.maxZ()) {
                            int x = ChunkMath.calcRelativeBlockPos(p.getX(), ChunkConstants.INNER_CHUNK_POS_FILTER.x);
                            int z = ChunkMath.calcRelativeBlockPos(p.getY(), ChunkConstants.INNER_CHUNK_POS_FILTER.z);
                            int y = TeraMath.floorToInt(surfaceHeightData.get(x, z));
                            Vector3i worldPos = new Vector3i(p.getX(), y, p.getY());

                            placeWaterBody(chunk, region, worldPos, structElem, seaLevel);
                        }
                    }
                }
            }
        }
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
    private void placeWaterBody(CoreChunk chunk, Region3i region, Vector3i worldPos, int[][] structElem, int seaLevel) {
        int radius = (structElem.length - 1) / 2;
        Vector3i pos;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (structElem[dx + radius][dz + radius] != 0) {
                    pos = new Vector3i(worldPos.add(dx, 0, dz));

                    // remove top layer (soil)
                    if (region.encompasses(pos.x, pos.y, pos.z)) {
                        chunk.setBlock(ChunkMath.calcRelativeBlockPos(pos.x, pos.y, pos.z), air);
                    }

                    // don't dig below the sea level
                    if (pos.y > seaLevel) {
                        pos.y -= 1;
                    }
                    if (region.encompasses(pos.x, pos.y, pos.z)) {
                        chunk.setBlock(ChunkMath.calcRelativeBlockPos(pos.x, pos.y, pos.z), water);
                    }
                }
            }
        }
    }
}
