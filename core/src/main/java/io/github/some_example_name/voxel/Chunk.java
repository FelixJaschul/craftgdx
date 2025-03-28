package io.github.some_example_name.voxel;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import io.github.some_example_name.terrain.Generation;
import java.util.HashMap;
import java.util.Map;

public class Chunk implements Disposable {
    public static final int CHUNK_SIZE = 16;
    public static final int CHUNK_HEIGHT = 16;

    private final int chunkX;
    private final int chunkZ;
    private final BlockType[][][] blocks;
    private final Array<ModelInstance> activeBlockModels;
    private static final Generation terrainGenerator = new Generation();

    public Chunk(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.blocks = new BlockType[CHUNK_SIZE][CHUNK_HEIGHT][CHUNK_SIZE];
        this.activeBlockModels = new Array<>();
        generateTerrain();
    }

    private void generateTerrain() {
        int[][] heightMap = terrainGenerator.generateHeightMap(chunkX, chunkZ, CHUNK_SIZE);
        terrainGenerator.fillChunkWithTerrain(blocks, heightMap, CHUNK_HEIGHT);
        buildMesh();
    }

    // Implement Face Culling

    public void buildMesh() {
        Map<BlockType, ModelBuilder> modelBuilders = new HashMap<>();

        // First pass: determine which blocks need to be rendered
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int y = 0; y < CHUNK_HEIGHT; y++) {
                for (int z = 0; z < CHUNK_SIZE; z++) {
                    BlockType type = blocks[x][y][z];
                    if (type != BlockType.AIR && shouldRenderBlock(x, y, z)) {
                        // Get or create model builder for this block type
                        ModelBuilder builder = modelBuilders.computeIfAbsent(type, t -> {
                            ModelBuilder mb = new ModelBuilder();
                            mb.begin();
                            return mb;
                        });
                        addBlockFaces(builder, x, y, z, getVisibleFaces(x, y, z));
                    }
                }
            }
        }

        // Second pass: create model instances from the batched geometry
        activeBlockModels.clear();
        for (Map.Entry<BlockType, ModelBuilder> entry : modelBuilders.entrySet()) {
            Model model = entry.getValue().end();
            ModelInstance instance = new ModelInstance(model);
            instance.transform.setToTranslation(chunkX * CHUNK_SIZE, 0, chunkZ * CHUNK_SIZE);
            activeBlockModels.add(instance);
        }
    }

    private boolean isBlockSolid(int x, int y, int z) {
        if (x < 0 || x >= CHUNK_SIZE || y < 0 || y >= CHUNK_HEIGHT || z < 0 || z >= CHUNK_SIZE) return false;
        return blocks[x][y][z] != BlockType.AIR;  // Solid means NOT air
    }

    private boolean shouldRenderBlock(int x, int y, int z) {
        // Only render if at least one face is adjacent to air or chunk boundary
        return !isBlockSolid(x + 1, y, z) || !isBlockSolid(x - 1, y, z) ||
            !isBlockSolid(x, y + 1, z) || !isBlockSolid(x, y - 1, z) ||
            !isBlockSolid(x, y, z + 1) || !isBlockSolid(x, y, z - 1);
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

    // Part of the ModelBuilder
    private void addBlockFaces(ModelBuilder builder, int x, int y, int z, boolean[] visibleFaces) {
        BlockType blockType = blocks[x][y][z];
        Material material = blockType.getMaterial();
        // Right face (positive X)
        if (!visibleFaces[Block.RIGHT])
            builder.part("right", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates, material)
                .rect(new Vector3(x + 1, y + 1, z), new Vector3(x + 1, y + 1, z + 1), new Vector3(x + 1, y, z + 1), new Vector3(x + 1, y, z), new Vector3(1, 0, 0));
        // Left face (negative X)
        if (!visibleFaces[Block.LEFT])
            builder.part("left", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates, material)
                .rect(new Vector3(x, y + 1, z + 1), new Vector3(x, y + 1, z), new Vector3(x, y, z), new Vector3(x, y, z + 1), new Vector3(-1, 0, 0));
        // Top face (positive Y)
        if (!visibleFaces[Block.TOP])
            builder.part("top", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates, material)
                .rect(new Vector3(x + 1, y + 1, z + 1), new Vector3(x + 1, y + 1, z), new Vector3(x, y + 1, z), new Vector3(x, y + 1, z + 1), new Vector3(0, 1, 0));
        // Bottom face (negative Y)
        if (!visibleFaces[Block.BOTTOM])
            builder.part("bottom", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates, material)
                .rect(new Vector3(x + 1, y, z), new Vector3(x + 1, y, z + 1), new Vector3(x, y, z + 1), new Vector3(x, y, z), new Vector3(0, -1, 0));
        // Front face (positive Z)
        if (!visibleFaces[Block.FRONT])
            builder.part("front", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates, material)
                .rect(new Vector3(x, y, z + 1), new Vector3(x + 1, y, z + 1), new Vector3(x + 1, y + 1, z + 1), new Vector3(x, y + 1, z + 1), new Vector3(0, 0, 1));
        // Back face (negative Z)
        if (!visibleFaces[Block.BACK])
            builder.part("back", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates, material)
                .rect(new Vector3(x, y, z), new Vector3(x, y + 1, z), new Vector3(x + 1, y + 1, z), new Vector3(x + 1, y, z), new Vector3(0, 0, -1));
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
