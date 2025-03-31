package io.github.some_example_name.voxel;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import io.github.some_example_name.terrain.Generation;
import io.github.some_example_name.player.Camera;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a chunk of the voxel world.
 * A chunk is a fixed-size 3D grid of blocks that is generated, rendered,
 * and managed as a single unit for performance optimization.
 */
public class Chunk implements Disposable {
    /** Width and depth of a chunk in blocks */
    public static final int CHUNK_SIZE = 16;
    /** Maximum height of a chunk in blocks */
    public static final int CHUNK_HEIGHT = 50;

    /** X-coordinate of this chunk in chunk space */
    private final int chunkX;
    /** Z-coordinate of this chunk in chunk space */
    private final int chunkZ;
    /** 3D array of block types in this chunk */
    private final BlockType[][][] blocks;
    /** Model instance for rendering this chunk */
    private ModelInstance chunkModel;
    /** Bounding box for frustum culling */
    private final BoundingBox boundingBox;
    /** Shared terrain generator for all chunks */
    private static final Generation terrainGenerator = new Generation();
    /** Flag indicating if this chunk has a mesh built */
    private boolean hasMesh = false;

    /**
     * Creates a new chunk at the specified coordinates.
     *
     * @param chunkX The X-coordinate of the chunk in chunk space
     * @param chunkZ The Z-coordinate of the chunk in chunk space
     */
    public Chunk(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.blocks = new BlockType[CHUNK_SIZE][CHUNK_HEIGHT][CHUNK_SIZE];
        this.boundingBox = new BoundingBox();
    }

    /**
     * Creates a new chunk at the specified position.
     *
     * @param position The position of the chunk
     */
    public Chunk(ChunkPosition position) {
        this(position.x, position.z);
    }

    /**
     * Generates terrain for this chunk using the terrain generator.
     * Fills the blocks array with appropriate block types based on the height map.
     */
    public void generateTerrain() {
        int[][] heightMap = terrainGenerator.generateHeightMap(chunkX, chunkZ, CHUNK_SIZE);
        terrainGenerator.fillChunkWithTerrain(blocks, heightMap, CHUNK_HEIGHT);
    }

    /**
     * Builds a 3D mesh for this chunk based on its blocks.
     * Uses face culling to optimize rendering by only including visible faces.
     */
    public void buildMesh() {
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();

        Map<BlockType, MeshPartBuilder> meshBuilders = new HashMap<>();

        // Iterate through all blocks in the chunk
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int y = 0; y < CHUNK_HEIGHT; y++) {
                for (int z = 0; z < CHUNK_SIZE; z++) {
                    BlockType type = blocks[x][y][z];
                    if (type != BlockType.AIR && shouldRenderBlock(x, y, z)) {
                        // Get or create a mesh builder for this block type
                        MeshPartBuilder builder = meshBuilders.get(type);
                        if (builder == null) {
                            String partId = "blocktype_" + type.name();
                            builder = modelBuilder.part(
                                partId,
                                GL20.GL_TRIANGLES,
                                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates,
                                type.getMaterial()
                            );
                            meshBuilders.put(type, builder);
                        }
                        addBlockFaces(builder, x, y, z, getVisibleFaces(x, y, z));
                    }
                }
            }
        }

        // If no blocks to render, exit early
        if (meshBuilders.isEmpty()) {
            hasMesh = false;
            return;
        }

        // Create the model and model instance
        Model model = modelBuilder.end();

        if (chunkModel != null && chunkModel.model != null)
            chunkModel.model.dispose();

        chunkModel = new ModelInstance(model);
        chunkModel.transform.setToTranslation(chunkX * CHUNK_SIZE, 0, chunkZ * CHUNK_SIZE);

        // Calculate bounding box for frustum culling
        chunkModel.calculateBoundingBox(boundingBox);
        boundingBox.mul(chunkModel.transform);

        hasMesh = true;
    }

    /**
     * Checks if a block at the specified coordinates is solid.
     *
     * @param x The x-coordinate within the chunk
     * @param y The y-coordinate within the chunk
     * @param z The z-coordinate within the chunk
     * @return True if the block is solid, false if it's air or outside the chunk
     */
    private boolean isBlockSolid(int x, int y, int z) {
        if (x < 0 || x >= CHUNK_SIZE || y < 0 || y >= CHUNK_HEIGHT || z < 0 || z >= CHUNK_SIZE) return false;
        return blocks[x][y][z] != BlockType.AIR;  // Solid means NOT air
    }

    /**
     * Determines if a block should be rendered based on its neighbors.
     * Only renders blocks that have at least one face adjacent to air or the chunk boundary.
     *
     * @param x The x-coordinate within the chunk
     * @param y The y-coordinate within the chunk
     * @param z The z-coordinate within the chunk
     * @return True if the block should be rendered, false otherwise
     */
    private boolean shouldRenderBlock(int x, int y, int z) {
        // Only render if at least one face is adjacent to air or chunk boundary
        return !isBlockSolid(x + 1, y, z) || !isBlockSolid(x - 1, y, z) ||
            !isBlockSolid(x, y + 1, z) || !isBlockSolid(x, y - 1, z) ||
            !isBlockSolid(x, y, z + 1) || !isBlockSolid(x, y, z - 1);
    }

    /**
     * Determines which faces of a block are visible and should be rendered.
     *
     * @param x The x-coordinate within the chunk
     * @param y The y-coordinate within the chunk
     * @param z The z-coordinate within the chunk
     * @return An array of booleans indicating which faces are visible
     */
    private boolean[] getVisibleFaces(int x, int y, int z) {
        // Determine which faces of the block are visible
        // Order: right, left, top, bottom, front, back
        boolean[] visibleFaces = new boolean[6];
        visibleFaces[Block.RIGHT] = !isBlockSolid(x + 1, y, z);
        visibleFaces[Block.LEFT] = !isBlockSolid(x - 1, y, z);
        visibleFaces[Block.TOP] = !isBlockSolid(x, y + 1, z);
        visibleFaces[Block.BOTTOM] = !isBlockSolid(x, y - 1, z);
        visibleFaces[Block.FRONT] = !isBlockSolid(x, y, z + 1);
        visibleFaces[Block.BACK] = !isBlockSolid(x, y, z - 1);
        return visibleFaces;
    }

    /**
     * Renders this chunk if it has a mesh and is visible to the camera.
     *
     * @param modelBatch The model batch to use for rendering
     * @param environment The lighting environment to use for rendering
     */
    public void render(ModelBatch modelBatch, Environment environment) {
        if (!hasMesh || chunkModel == null) return;

        com.badlogic.gdx.graphics.Camera camera = Camera.getInstance().getCamera();
        if (isVisible(camera)) modelBatch.render(chunkModel, environment);
    }

    /**
     * Checks if this chunk is visible to the camera using frustum culling.
     *
     * @param camera The camera to check visibility against
     * @return True if the chunk is visible, false otherwise
     */
    private boolean isVisible(com.badlogic.gdx.graphics.Camera camera) {
        return boundingBox.isValid() && camera.frustum.boundsInFrustum(boundingBox);
    }

    /**
     * Adds the visible faces of a block to the mesh builder.
     *
     * @param builder The mesh builder to add faces to
     * @param x The x-coordinate of the block within the chunk
     * @param y The y-coordinate of the block within the chunk
     * @param z The z-coordinate of the block within the chunk
     * @param visibleFaces Array indicating which faces are visible
     */
    private void addBlockFaces(MeshPartBuilder builder, int x, int y, int z, boolean[] visibleFaces) {
        // Right face (positive X)
        if (visibleFaces[Block.RIGHT]) {
            builder.rect(
                new Vector3(x + 1, y + 1, z),       // top-back
                new Vector3(x + 1, y + 1, z + 1),   // top-front
                new Vector3(x + 1, y, z + 1),       // bottom-front
                new Vector3(x + 1, y, z),           // bottom-back
                new Vector3(1, 0, 0)                // normal pointing right
            );
        }

        // Left face (negative X)
        if (visibleFaces[Block.LEFT]) {
            builder.rect(
                new Vector3(x, y + 1, z + 1),       // top-front
                new Vector3(x, y + 1, z),           // top-back
                new Vector3(x, y, z),               // bottom-back
                new Vector3(x, y, z + 1),           // bottom-front
                new Vector3(-1, 0, 0)               // normal pointing left
            );
        }

        // Top face (positive Y)
        if (visibleFaces[Block.TOP]) {
            builder.rect(
                new Vector3(x + 1, y + 1, z + 1),   // right-front
                new Vector3(x + 1, y + 1, z),       // right-back
                new Vector3(x, y + 1, z),           // left-back
                new Vector3(x, y + 1, z + 1),       // left-front
                new Vector3(0, 1, 0)                // normal pointing up
            );
        }

        // Bottom face (negative Y)
        if (visibleFaces[Block.BOTTOM]) {
            builder.rect(
                new Vector3(x + 1, y, z),           // right-back
                new Vector3(x + 1, y, z + 1),       // right-front
                new Vector3(x, y, z + 1),           // left-front
                new Vector3(x, y, z),               // left-back
                new Vector3(0, -1, 0)               // normal pointing down
            );
        }

        // Front face (positive Z)
        if (visibleFaces[Block.FRONT]) {
            builder.rect(
                new Vector3(x, y, z + 1),           // bottom-left
                new Vector3(x + 1, y, z + 1),       // bottom-right
                new Vector3(x + 1, y + 1, z + 1),   // top-right
                new Vector3(x, y + 1, z + 1),       // top-left
                new Vector3(0, 0, 1)                // normal pointing front
            );
        }

        // Back face (negative Z)
        if (visibleFaces[Block.BACK]) {
            builder.rect(
                new Vector3(x + 1, y, z),           // bottom-right
                new Vector3(x, y, z),               // bottom-left
                new Vector3(x, y + 1, z),           // top-left
                new Vector3(x + 1, y + 1, z),       // top-right
                new Vector3(0, 0, -1)               // normal pointing back
            );
        }
    }

    /**
     * Gets the X-coordinate of this chunk in chunk space.
     *
     * @return The X-coordinate of this chunk
     */
    public int getChunkX() {
        return chunkX;
    }

    /**
     * Gets the Z-coordinate of this chunk in chunk space.
     *
     * @return The Z-coordinate of this chunk
     */
    public int getChunkZ() {
        return chunkZ;
    }

    /**
     * Checks if this chunk has a mesh built.
     *
     * @return True if the chunk has a mesh, false otherwise
     */
    public boolean hasMesh() {
        return hasMesh;
    }

    /**
     * Disposes of the mesh for this chunk to free memory.
     * The chunk data remains intact, but the rendered mesh is removed.
     */
    public void disposeMesh() {
        if (chunkModel != null && chunkModel.model != null) {
            chunkModel.model.dispose();
            chunkModel = null;
        }
        hasMesh = false;
    }

    /**
     * Disposes of all resources used by this chunk.
     */
    @Override
    public void dispose() {
        disposeMesh();
    }
}
