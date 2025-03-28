package io.github.some_example_name.voxel;

import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public class Chunk implements Disposable {
    public static final int CHUNK_SIZE = 16;
    public static final int CHUNK_HEIGHT = 16;

    private final int chunkX;
    private final int chunkZ;
    private final BlockType[][][] blocks;
    private final Array<ModelInstance> activeBlockModels;

    public Chunk(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.blocks = new BlockType[CHUNK_SIZE][CHUNK_HEIGHT][CHUNK_SIZE];
        this.activeBlockModels = new Array<>();
        generateTerrain();
    }

    private void generateTerrain() {
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                for (int y = 0; y < CHUNK_HEIGHT; y++) {
                    if (y == 0) blocks[x][y][z] = BlockType.STONE;
                    else if (y <= 2) blocks[x][y][z] = BlockType.DIRT;
                    else blocks[x][y][z] = BlockType.AIR;
                }
            }
        }

        buildMesh();
    }

    // Implement Face Culling

    public void buildMesh() {
        activeBlockModels.clear();

        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int y = 0; y < CHUNK_HEIGHT; y++) {
                for (int z = 0; z < CHUNK_SIZE; z++) {
                    // Takes the block saved in the Blocks Array
                    BlockType type = blocks[x][y][z];
                    if (type != BlockType.AIR) {
                        // Only create a block if at least one face is visible
                        if (shouldRenderBlock(x, y, z)) {
                            float worldX = (chunkX * CHUNK_SIZE) + x;
                            float worldZ = (chunkZ * CHUNK_SIZE) + z;
                            // Places Blocks in its spots, saved in the Blocks Array
                            Block block = new Block(type, worldX, (float) y, worldZ, getVisibleFaces(x, y, z));
                            ModelInstance modelInstance = block.getModelInstance();
                            if (modelInstance != null) activeBlockModels.add(modelInstance);
                        }
                    }
                }
            }
        }
    }

    private boolean shouldRenderBlock(int x, int y, int z) {
        return isBlockSolid(x + 1, y, z) || isBlockSolid(x - 1, y, z) ||
            isBlockSolid(x, y + 1, z) || isBlockSolid(x, y - 1, z) ||
            isBlockSolid(x, y, z + 1) || isBlockSolid(x, y, z - 1);
    }

    private boolean isBlockSolid(int x, int y, int z) {
        if (x < 0 || x >= CHUNK_SIZE || y < 0 || y >= CHUNK_HEIGHT || z < 0 || z >= CHUNK_SIZE) return true;
        return blocks[x][y][z] == BlockType.AIR;
    }

    // Determine which faces of the block are visible
    private boolean[] getVisibleFaces(int x, int y, int z) {
        // Order: right, left, top, bottom, front, back
        boolean[] visibleFaces = new boolean[6];
        visibleFaces[Block.RIGHT] = isBlockSolid(x + 1, y, z);
        visibleFaces[Block.LEFT] = isBlockSolid(x - 1, y, z);
        visibleFaces[Block.TOP] = isBlockSolid(x, y + 1, z);
        visibleFaces[Block.BOTTOM] = isBlockSolid(x, y - 1, z);
        visibleFaces[Block.FRONT] = isBlockSolid(x, y, z + 1);
        visibleFaces[Block.BACK] = isBlockSolid(x, y, z - 1);
        return visibleFaces;
    }

    // End Face Culling

    public void render(ModelBatch modelBatch, Environment environment) {
        for (ModelInstance model : activeBlockModels)
            modelBatch.render(model, environment);
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    @Override
    public void dispose() {
        for (ModelInstance model : activeBlockModels)
            if (model != null && model.model != null) model.model.dispose();
        activeBlockModels.clear();
    }
}
